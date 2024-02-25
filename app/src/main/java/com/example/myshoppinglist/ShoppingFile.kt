@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myshoppinglist

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController


//Data Class Shppingg Items

data class ShoppingItem(
    val id: Int,
    var name: String,
    var quantity: Int,
    var isEditing: Boolean = false,
    var address: String = ""
)

//We going to need it as it needs to require the permission-> locationutils access, viewmodel take care of value,
// navcontroller to move in different screen, context for new screen and finally address.
@Composable
fun shoppingListApp(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context: Context,
    address: String
) {

    //List of items that are going to be added by the user in Text Fields ""
    var sItems by remember { mutableStateOf(listOf<ShoppingItem>()) }
    var showDialog by remember { mutableStateOf(false) }
    var textfield by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }


    val requestPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissions ->
                if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true &&
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                ) {
                    //ACCESS TO THE LOCATION
                    locationUtils.requestLocationUpdates(viewModel = viewModel)
                } else {
                    //ASK FOR PERMISSION
                    val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (rationalRequired) {
                        Toast.makeText(
                            context,
                            "Location Feature is required for this feature to work",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Location Feature is required, Please enable it from settings",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        {
            Text("Add Item")
        }
        //We need to show two things : Edit menu ya phir List Menu
        //It is like what we want to hide or see in the UI: It's an harry potter coat
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(sItems) { item ->
                if (item.isEditing) {
                    //Call lagaya Shopping Editor
                    ShoppingItemEditor(item = item, onEditComplete = {
                        //sitems saare copy kar liye ab find krke name and quantity replaced
                            editedName, editedQuantity ->
                        sItems = sItems.map { it.copy(isEditing = false) }
                        //Dummy variable
                        val editItem = sItems.find { it.id == item.id }
                        editItem?.let {
                            it.name = editedName
                            it.quantity = editedQuantity
                            it.address = address
                        }
                    })
                } else {
                    ShoppingListItem(item = item, onEditClick = {
                        //finding out which item we are editing and changing it "isEditing boolean" to true
                        sItems = sItems.map { it.copy(isEditing = it.id == item.id) }
                    }, onDeleteClick = { sItems = sItems - item })
                }
            }
        }
    }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = {
                            //The value of isEditing is already set to false
                            if (textfield.isNotBlank()) {
                                val newItem = ShoppingItem(
                                    id = sItems.size + 1,
                                    name = textfield, quantity = quantity.toInt(), address = address
                                )
                                sItems = sItems + newItem
                                showDialog = false
                                textfield = ""
                            }
                        }) {
                            Text("Add")
                        }

                        Button(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }

                    }
                }, title = { Text("Add Shopping Item") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = textfield, onValueChange = { textfield = it },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )

                        OutlinedTextField(
                            value = quantity, onValueChange = { quantity = it },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )

                        Button(onClick = {
                            if (locationUtils.hasLocationPermission(context)) {
                                //Permission already Granted
                                locationUtils.requestLocationUpdates(viewModel)
                                navController.navigate("locationscreen") {
                                    //There should not be multicopies of the screen
                                    this.launchSingleTop
                                }
                            } else {
                                //Request location Permission
                                // PRECISE LOCATION AND APPROXIMATE LOCATION
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                )
                            }
                        }) {
                            Text("Address")
                        }
                    }
                }
            )
        }




}

@Composable
fun ShoppingItemEditor(item: ShoppingItem, onEditComplete: (String, Int) -> Unit) {
    var editName by remember { mutableStateOf(item.name) }
    var editQuantity by remember { mutableStateOf(item.quantity.toString()) }
    var isediting by remember { mutableStateOf(item.isEditing) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column {
            BasicTextField(
                value = editName,
                onValueChange = { editName = it },
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )
            BasicTextField(
                value = editQuantity,
                onValueChange = { editQuantity = it },
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            ) {
            }
        }

        Button(onClick = {
            isediting = false
            onEditComplete(editName, editQuantity.toIntOrNull() ?: 1)
        }) {
            Text("Save")
        }
    }
}

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(
                border = BorderStroke(
                    2.dp,
                    Color(0XFF018786)
                ), shape = RoundedCornerShape(20)
            ), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Row {
                Text(text = item.name, modifier = Modifier.padding(8.dp))
                Text(text = "Qty:: ${item.quantity}", modifier = Modifier.padding(8.dp))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Text(text = item.address)
            }
        }



        Row(modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = onEditClick)
            {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }
            IconButton(onClick = onDeleteClick)
            {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        }
    }

}




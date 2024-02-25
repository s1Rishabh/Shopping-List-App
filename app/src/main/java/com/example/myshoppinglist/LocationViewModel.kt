package com.example.myshoppinglist

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LocationViewModel : ViewModel() {

    private val _location = mutableStateOf<LocationData?>(null)
    val location: State<LocationData?> = _location

    private val _address = mutableStateOf(listOf<GeoCodingResults>())
    val address: State<List<GeoCodingResults>> = _address

    fun updateLocation(newLocation: LocationData) {
        _location.value = newLocation
    }

    fun fetchAddress(latlng: String) {
        //We want to call a suspend function so we need try and catch
        try {
            viewModelScope.launch {
                val result = RetrofitClient.create()
                    .getAddressFromCoordinates(latlng, "AIzaSyAWJ-M1sSrYuX6u5CsLD-lagEHCE7iHU5Q")
                _address.value = result.results
            }
        } catch (e: Exception) {
            Log.d("res1", "${e.cause} ${e.message}")
        } catch (e: HttpException) {
            when (e.code()) {
                400 -> {
                    Log.d("res1", "${e.cause} ${e.message}")
                }
            }
        }

    }
}


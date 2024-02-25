package com.example.myshoppinglist

data class LocationData(
    val latitude: Double,
    val longitude: Double
)
data class GeoCodingResponse(
    val results: List<GeoCodingResults>,
    val status: String
)

data class GeoCodingResults(
    val formatted_address: String
)

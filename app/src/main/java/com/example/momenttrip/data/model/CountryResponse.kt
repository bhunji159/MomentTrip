package com.example.momenttrip.data.model

data class CountryResponse(
    val name: Name
) {
    data class Name(
        val common: String = ""
    )
}
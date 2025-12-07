package com.example.myapplication.data.model

data class Prepare(
    val tripId: String = "",
    val prepareList: List<PrepareItem> = emptyList()
)

package com.example.myapplication.data.model

data class User(
    val userId: String = "", //사용자 지정 id
    val phoneNumber: String,
    val email:String = "" //email
)
package com.example.myapplication.data.model

data class Trip(
    val tripId: String = "",
    val title: String = "",
    val creatorId: String = "",
    val createdAt: Long = 0L, // 기본값 0L 추가
    val participants: List<String> = emptyList()
) {
    // Firestore 역직렬화를 위해 기본 생성자 명시적으로 추가
    constructor() : this("", "", "", 0L, emptyList())
}

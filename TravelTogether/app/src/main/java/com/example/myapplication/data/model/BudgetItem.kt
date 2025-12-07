package com.example.myapplication.data.model

data class BudgetItem(
    val id: String = "",    // Firestore 문서 ID
    val name: String = "",  // 항목 이름
    val amount: Long = 0L   // 항목 금액
)


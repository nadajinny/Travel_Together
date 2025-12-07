package com.example.myapplication.data.model

data class Budget(
    val tripId: String = "",
    val budgetitem: List<BudgetItem> = emptyList(), // 항목 이름과 금액 리스트
    val total: Long = 0L
)

package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.myapplication.data.model.BudgetItem

class BudgetAdapter(
    private val context: Context, // Fragment에서 Context를 전달받음
    private val budgetList: List<BudgetItem>
) : BaseAdapter() {

    override fun getCount(): Int = budgetList.size

    override fun getItem(position: Int): Any = budgetList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)

        val budgetItem = budgetList[position]
        val nameTextView = view.findViewById<TextView>(android.R.id.text1)
        val amountTextView = view.findViewById<TextView>(android.R.id.text2)

        nameTextView.text = budgetItem.name
        amountTextView.text = "${budgetItem.amount} 원"

        return view
    }
}

package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Button
import android.widget.BaseAdapter
import com.example.myapplication.R
import com.example.myapplication.data.model.PrepareItem
import com.example.myapplication.ui.Trip.PrepareFragment

class PrepareItemAdapter(
    private val context: Context,
    private val items: MutableList<PrepareItem>,
    private val listener: PrepareFragment.OnItemActionListener? // 콜백 인터페이스 추가
) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_prepare, parent, false)

        val itemNameTextView = view.findViewById<TextView>(R.id.itemNameTextView)
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)

        val item = items[position]

        itemNameTextView.text = item.itemName
        checkBox.isChecked = item.isChecked

        // Set checkbox change listener
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            listener?.updateItemCheckedStatus(item, isChecked) // Fragment 메서드 호출
        }

        // Set delete button listener
        deleteButton.setOnClickListener {
            listener?.deleteItemFromFirestore(item) // Fragment 메서드 호출
        }

        return view
    }
}

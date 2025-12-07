package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.data.model.Plan
import java.text.SimpleDateFormat
import java.util.Locale

// PlanItemAdapter.kt
class PlanItemAdapter(
    private val context: Context,
    private val planItems: List<Plan>
) : BaseAdapter() {

    override fun getCount(): Int {
        return planItems.size
    }

    override fun getItem(position: Int): Any {
        return planItems[position]
    }

    override fun getItemId(position: Int): Long {
        return planItems[position].planId.hashCode().toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_plan_item, parent, false)

        val timeText = view.findViewById<TextView>(R.id.planTime)
        val planNameText = view.findViewById<TextView>(R.id.planName)

        val plan = planItems[position]

        // nullable Long 처리
        val time = plan.time ?: 0L // null이면 기본값 0L을 사용 (안전하게 처리)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeText.text = timeFormat.format(time)
        planNameText.text = plan.planName

        return view
    }
}

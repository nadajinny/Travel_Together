package com.example.myapplication.adapter

import android.app.AlertDialog
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.data.model.Plan
import com.example.myapplication.repository.PlanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlanListAdapter(
    val context: Context,
    val groupedPlans: LinkedHashMap<String, ArrayList<Plan>>,
    private val onPlanChanged: () -> Unit // 콜백 추가
) {
    fun getView(): LinearLayout {
        // 메인 레이아웃 생성
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // 날짜별로 그룹화된 계획 추가
        for (date in groupedPlans.keys) {
            // 날짜별 레이아웃 생성
            val dateLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
            }

            // 날짜 텍스트 추가
            val dateText = TextView(context).apply {
                text = date
                textSize = 18f
                setPadding(0, 16, 0, 8)
            }
            dateLayout.addView(dateText)

            // 각 날짜에 속한 계획 추가
            val plans = groupedPlans[date] ?: emptyList()
            for (plan in plans) {
                val planText = TextView(context).apply {
                    val planTime = plan.time?.let {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
                    } ?: "시간 없음"
                    text = "$planTime ${plan.planName}"
                    textSize = 16f
                    setPadding(0, 8, 0, 8)

                    // 롱 클릭 시 삭제 처리
                    setOnLongClickListener {
                        AlertDialog.Builder(context)
                            .setTitle("일정을 삭제하시겠습니까?")
                            .setMessage("선택한 일정을 삭제하면 복구할 수 없습니다.")
                            .setPositiveButton("삭제") { _, _ ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        PlanRepository().deletePlan(plan.tripId, plan.planId)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "계획이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                            onPlanChanged() // UI 업데이트 콜백 호출
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                            .setNegativeButton("취소", null)
                            .show()
                        true
                    }
                }
                dateLayout.addView(planText) // 계획 텍스트를 날짜 레이아웃에 추가
            }

            layout.addView(dateLayout) // 날짜 레이아웃을 메인 레이아웃에 추가
        }

        return layout // 최종적으로 완성된 레이아웃 반환
    }
}
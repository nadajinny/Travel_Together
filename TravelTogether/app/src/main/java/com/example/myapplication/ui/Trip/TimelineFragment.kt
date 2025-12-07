package com.example.myapplication.ui.Trip

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SimpleAdapter
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.adapter.PlanListAdapter
import com.example.myapplication.data.model.Plan
import com.example.myapplication.repository.PlanRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimelineFragment : Fragment() {
    private lateinit var preferences: SharedPreferences
    private lateinit var tripId: String
    private lateinit var btnAdd: Button

    private var planId: String? = null
    private var initialPlanName: String? = null
    private var initialPlanTime: Long? = null

    private lateinit var etplanName: EditText
    private lateinit var datePicker: DatePicker
    private lateinit var timePicker: TimePicker

    private lateinit var planListLayout: LinearLayout
    private val planRepository = PlanRepository()
    private val groupedPlans = LinkedHashMap<String, ArrayList<Plan>>() // 날짜별로 그룹화된 계획
    private var planListener: ListenerRegistration? = null // Firestore 리스너
    private lateinit var simpleAdapter: SimpleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment 레이아웃 inflate
        val view = inflater.inflate(R.layout.fragment_timeline, container, false)

        // View 초기화
        btnAdd = view.findViewById(R.id.btnadd)
        planListLayout = view.findViewById(R.id.planListLayout)
        val sharedPreferences =
            requireContext().getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
        // Arguments로 전달된 tripId 가져오기
        tripId = arguments?.getString("tripId") ?: ""

        if (tripId.isNotEmpty()) {
            fetchPlans()
        } else {
            Toast.makeText(requireContext(), "여행 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        // 버튼 클릭 이벤트 설정
        btnAdd.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_time, null)

            val etPlanName = dialogView.findViewById<EditText>(R.id.etplanName)
            val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
            val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)

            if (planId != null) {
                // 기존 계획이 있을 때 수정 화면 표시
                etPlanName.setText(initialPlanName)
                initialPlanTime?.let {
                    val calendar = Calendar.getInstance().apply { timeInMillis = it }
                    datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
                    timePicker.minute = calendar.get(Calendar.MINUTE)
                }
            }

            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(if (planId != null) "계획 수정" else "새 타임라인 생성")
                .setView(dialogView)
                .setPositiveButton("확인") { _, _ ->
                    val planName = etPlanName.text.toString()
                    val calendar = Calendar.getInstance().apply {
                        set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                        set(Calendar.HOUR_OF_DAY, timePicker.hour)
                        set(Calendar.MINUTE, timePicker.minute)
                    }
                    val timestampAsLong = calendar.timeInMillis

                    if (planName.isNotEmpty()) {
                        if (planId != null) {
                            // 계획 수정
                            updatePlan(planName, timestampAsLong)
                        } else {
                            // 새 계획 추가
                            addNewPlan(planName, timestampAsLong)
                        }
                    } else {
                        Toast.makeText(requireContext(), "일정을 입력해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .create()
            dialog.show()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        setupRealtimeListener()
    }

    private fun fetchPlans() {
        CoroutineScope(Dispatchers.IO).launch {
            val plans = planRepository.getPlansForTripId(tripId)
            withContext(Dispatchers.Main) {
                if (plans.isNotEmpty()) {
                    // 날짜별로 계획을 그룹화
                    groupPlansByDate(plans)
                    // groupedPlans를 PlanListAdapter에 전달하여 화면에 표시
                    val planListAdapter = PlanListAdapter(
                        context = requireContext(),
                        groupedPlans = groupedPlans,
                        onPlanChanged = { fetchPlans() }
                    )
                    planListLayout.removeAllViews() // 기존의 뷰 제거
                    planListLayout.addView(planListAdapter.getView()) // 새로운 뷰 추가
                } else {
                    Toast.makeText(requireContext(), "등록된 계획이 없습니다. 계획을 추가해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun groupPlansByDate(plans: List<Plan>) {
        groupedPlans.clear() // 초기화

        // 각 계획을 날짜별로 그룹화
        for (plan in plans) {
            val date = formatDate(plan.time) // 날짜 포맷 변경
            if (groupedPlans[date] == null) {
                groupedPlans[date] = ArrayList()
            }
            groupedPlans[date]?.add(plan)
        }
    }

    private fun formatDate(timeInMillis: Long?): String {
        if (timeInMillis != null) {
            val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale("ko", "KR"))
            val date = Date(timeInMillis)
            return sdf.format(date)
        }
        return "날짜 없음"
    }

    private fun setupRealtimeListener() {
        planListener = planRepository.listenToPlansForTrip(tripId) { plans ->
            groupPlansByDate(plans)
            val planListAdapter = PlanListAdapter(
                context = requireContext(),
                groupedPlans = groupedPlans,
                onPlanChanged = { fetchPlans() } // 콜백 전달
            )
            planListLayout.removeAllViews()
            planListLayout.addView(planListAdapter.getView())
        }
    }

    private fun updatePlan(planName: String, time: Long) {
        planId?.let { id ->
            planRepository.updatePlan(tripId, id, planName, time, onSuccess = {
                Toast.makeText(requireContext(), "계획 수정 완료", Toast.LENGTH_SHORT).show()
            }, onFailure = {
                Toast.makeText(requireContext(), "계획 수정 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun addNewPlan(planName: String, time: Long) {
        planRepository.addPlan(tripId, planName, time, onSuccess = {
            Toast.makeText(requireContext(), "계획 추가 완료", Toast.LENGTH_SHORT).show()
        }, onFailure = {
            Toast.makeText(requireContext(), "계획 추가 실패: ${it.message}", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        planListener?.remove()
    }

    companion object {
        // Fragment 인스턴스 생성 함수
        fun newInstance(tripId: String): TimelineFragment {
            val fragment = TimelineFragment()
            val args = Bundle()
            args.putString("tripId", tripId)
            fragment.arguments = args
            return fragment
        }
    }
}

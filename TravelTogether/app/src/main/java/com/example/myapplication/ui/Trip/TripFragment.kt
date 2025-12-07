package com.example.myapplication.ui.Trip

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.adapter.TripListAdapter
import com.example.myapplication.data.model.Trips
import com.example.myapplication.databinding.FragmentMessageBinding
import com.example.myapplication.repository.TripRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TripFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: SharedPreferences
    private lateinit var btnAdd: Button
    private lateinit var listView: ListView
    private val tripRepository = TripRepository()
    private val tripList = ArrayList<Trips>() // Trips 객체를 담을 ArrayList
    private var tripListener: ListenerRegistration? = null // Firestore 리스너(실시간 동기화)
    private lateinit var simpleAdapter: SimpleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_trip, container, false)
        _binding =FragmentMessageBinding.inflate(inflater, container, false)
        preferences = requireContext().getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)  // <-- Initialize preferences
        btnAdd = rootView.findViewById(R.id.btnadd)
        listView = rootView.findViewById(R.id.listView)

        // Get current user email
        val currentUserEmail = preferences.getString("currentUser", null)

        if (currentUserEmail != null) {
            setupRealtimeListener(currentUserEmail)
        } else {
            Toast.makeText(requireContext(), "로그인 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        btnAdd.setOnClickListener {
            // Add trip dialog logic here
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_trip, null)
            val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("새 여행 생성")
                .setView(dialogView)
                .setPositiveButton("생성") { _, _ ->
                    val title = etTitle.text.toString() // trip title

                    if (title.isNotEmpty()) {
                        val userId = preferences.getString("currentUser", "") ?: ""

                        tripRepository.addTrip(
                            title = title,
                            creatorId = userId,
                            createdAt = System.currentTimeMillis(),
                            participants = listOf(userId),
                            onSuccess = {
                                Toast.makeText(requireContext(), "여행 추가 완료", Toast.LENGTH_SHORT).show()
                                if (currentUserEmail != null) {
                                    setupRealtimeListener(currentUserEmail)
                                }
                            },
                            onFailure = {
                                Toast.makeText(requireContext(), "여행 추가 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .create()
            dialog.show()
        }

        return rootView
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setupRealtimeListener(currentUserEmail: String) {
        tripListener = tripRepository.listenToTripsForUser(currentUserEmail) { trips ->
            if (trips.isNotEmpty()) {
                tripList.clear() // 초기화
                tripList.addAll(trips.map { Trips(it.title, it.createdAt, it.tripId) }) // 데이터 추가
                listView.adapter = TripListAdapter(requireContext(), tripList) // 데이터 ListView에 표시
            } else {
                Toast.makeText(requireContext(), "참여 중인 여행이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.myapplication.ui.Trip

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.adapter.PrepareItemAdapter
import com.example.myapplication.data.model.PrepareItem
import com.google.firebase.firestore.FirebaseFirestore

class PrepareFragment : Fragment() {

    private lateinit var itemList: MutableList<PrepareItem> // 준비물 리스트
    private lateinit var itemAdapter: PrepareItemAdapter // 리스트 어댑터
    private lateinit var listView: ListView // 준비물 리스트뷰
    private lateinit var addItemEditText: EditText // 준비물 입력란
    private lateinit var addButton: Button // 준비물 추가 버튼
    private lateinit var db: FirebaseFirestore
    private lateinit var tripId: String

    // 콜백 인터페이스 정의
    interface OnItemActionListener {
        fun updateItemCheckedStatus(item: PrepareItem, isChecked: Boolean)
        fun deleteItemFromFirestore(item: PrepareItem)
    }

    private var listener: OnItemActionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Activity가 OnItemActionListener를 구현한 경우
        if (context is OnItemActionListener) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnItemActionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_prepare, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        tripId = arguments?.getString("tripId") ?: ""

        listView = view.findViewById(R.id.listView)
        addButton = view.findViewById(R.id.addButton)

        itemList = mutableListOf()
        itemAdapter = PrepareItemAdapter(requireContext(), itemList, listener) // 콜백 전달
        listView.adapter = itemAdapter

        // 추가 버튼 클릭 시 다이얼로그 띄우기
        addButton.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_prepare, null)
            val itemNameEditText = dialogView.findViewById<EditText>(R.id.prepare) // 다이얼로그 내 EditText

            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("준비물 추가")
                .setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    val itemName = itemNameEditText.text.toString()
                    if (itemName.isNotBlank()) {
                        val item = PrepareItem(itemName = itemName, isChecked = false)
                        addItemToFirestore(item)
                    } else {
                        Toast.makeText(requireContext(), "준비물 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .create()
            dialog.show()
        }

        setupRealtimeListener()
    }

    private fun fetchItemsFromFirestore() {
        db.collection("trips").document(tripId).collection("prepare")
            .get()
            .addOnSuccessListener { documents ->
                itemList.clear()
                for (document in documents) {
                    val item = document.toObject(PrepareItem::class.java)
                    itemList.add(item)
                }
                itemAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "준비물 목록을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addItemToFirestore(item: PrepareItem) {
        db.collection("trips").document(tripId).collection("prepare")
            .add(item)
            .addOnSuccessListener {
                fetchItemsFromFirestore()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "준비물을 추가하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRealtimeListener() {
        db.collection("trips").document(tripId).collection("prepare")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "실시간 데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    itemList.clear()
                    for (document in snapshots) {
                        val item = document.toObject(PrepareItem::class.java)
                        itemList.add(item)
                    }
                    itemAdapter.notifyDataSetChanged()
                }
            }
    }

    companion object {
        fun newInstance(tripId: String): PrepareFragment {
            val fragment = PrepareFragment()
            val args = Bundle()
            args.putString("tripId", tripId)
            fragment.arguments = args
            return fragment
        }
    }
}

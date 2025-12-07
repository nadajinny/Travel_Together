package com.example.myapplication.ui.Trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.adapter.BudgetAdapter
import com.example.myapplication.data.model.BudgetItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class BudgetFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tripId: String
    private lateinit var budgetListView: ListView
    private lateinit var addBudgetButton: Button
    private lateinit var budgetAdapter: BudgetAdapter
    private lateinit var editItemName: EditText
    private lateinit var editItemAmount: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private var selectedPosition: Int = -1
    private var budgetList: MutableList<BudgetItem> = mutableListOf()
    private var totalAmount: Long = 0L
    private var budgetListener: ListenerRegistration? = null

    companion object {
        // Factory method to create a new instance of the fragment with tripId
        fun newInstance(tripId: String): BudgetFragment {
            val fragment = BudgetFragment()
            val args = Bundle()
            args.putString("tripId", tripId)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        tripId = arguments?.getString("tripId") ?: ""

        budgetListView = view.findViewById(R.id.budgetListView)
        addBudgetButton = view.findViewById(R.id.addBudgetButton)
        editItemName = view.findViewById(R.id.editBudgetItemName)
        editItemAmount = view.findViewById(R.id.editBudgetItemAmount)
        saveButton = view.findViewById(R.id.saveBudgetItemButton)
        deleteButton = view.findViewById(R.id.deleteBudgetItemButton)

        // 예산 리스트 어댑터 초기화
        budgetAdapter = BudgetAdapter(requireContext(), budgetList)
        budgetListView.adapter = budgetAdapter

        // 실시간 데이터 가져오기
        observeBudget()

        // 예산 항목 추가 버튼 클릭
        addBudgetButton.setOnClickListener {
            val name = editItemName.text.toString()
            val amount = editItemAmount.text.toString().toLongOrNull()
            if (name.isNotEmpty() && amount != null) {
                addBudgetItemToFirestore(name, amount)
            } else {
                Toast.makeText(requireContext(), "항목 이름과 금액을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 예산 항목 저장 버튼 클릭
        saveButton.setOnClickListener {
            if (selectedPosition != -1) {
                val item = budgetList[selectedPosition]
                val name = editItemName.text.toString()
                val amount = editItemAmount.text.toString().toLongOrNull()

                if (name.isNotEmpty() && amount != null) {
                    val updatedItem = BudgetItem(id = item.id, name = name, amount = amount)
                    updateBudgetItemInFirestore(item.id, updatedItem)
                } else {
                    Toast.makeText(requireContext(), "항목 이름과 금액을 입력하세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 예산 항목 삭제 버튼 클릭
        deleteButton.setOnClickListener {
            if (selectedPosition != -1) {
                val item = budgetList[selectedPosition]
                deleteBudgetItemFromFirestore(item.id)
            }
        }


        // 예산 항목 리스트 클릭 시 수정 및 삭제 버튼 표시
        budgetListView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = budgetList[position]
            selectedPosition = position

            editItemName.setText(selectedItem.name)
            editItemAmount.setText(selectedItem.amount.toString())

            saveButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        budgetListener?.remove() // Listener 해제
    }

    // 실시간 예산 데이터 관찰
    private fun observeBudget() {
        budgetListener = db.collection("trips").document(tripId).collection("budget")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "예산을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                budgetList.clear()
                totalAmount = 0L

                snapshots?.forEach { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val amount = document.getLong("amount") ?: 0L

                    val budgetItem = BudgetItem(id, name, amount)
                    budgetList.add(budgetItem)
                    totalAmount += amount
                }

                updateTotalAmount()
                budgetAdapter.notifyDataSetChanged()
            }
    }

    // 예산 항목 추가
    private fun addBudgetItemToFirestore(name: String, amount: Long) {
        val newItem = mapOf(
            "name" to name,
            "amount" to amount
        )
        db.collection("trips").document(tripId).collection("budget")
            .add(newItem)
            .addOnSuccessListener {
                editItemName.text.clear()
                editItemAmount.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "예산 항목 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    // 예산 항목 업데이트
    private fun updateBudgetItemInFirestore(itemId: String, updatedItem: BudgetItem) {
        val updatedMap = mapOf(
            "name" to updatedItem.name,
            "amount" to updatedItem.amount
        )

        db.collection("trips").document(tripId).collection("budget").document(itemId)
            .set(updatedMap)
            .addOnSuccessListener {
                saveButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
                selectedPosition = -1
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "예산 항목 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    // 예산 항목 삭제
    private fun deleteBudgetItemFromFirestore(itemId: String) {
        db.collection("trips").document(tripId).collection("budget").document(itemId)
            .delete()
            .addOnSuccessListener {
                saveButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
                selectedPosition = -1
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "예산 항목 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    // 총 금액 갱신
    private fun updateTotalAmount() {
        val totalAmountTextView = view?.findViewById<TextView>(R.id.totalAmountTextView)
        totalAmountTextView?.text = "총 금액: $totalAmount 원"
    }
}

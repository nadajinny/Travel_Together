package com.example.myapplication.ui.Trip

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.model.PrepareItem
import com.example.myapplication.data.model.SocketManager
import com.google.firebase.firestore.FirebaseFirestore

class TripDetailActivity : AppCompatActivity(), PrepareFragment.OnItemActionListener {

    private lateinit var viewPlansButton: Button
    private lateinit var viewPrepareButton: Button
    private lateinit var viewBudgetButton: Button
    private lateinit var backButton: Button
    private lateinit var tripId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_detail)

        viewPlansButton = findViewById(R.id.viewPlansButton)
        viewPrepareButton = findViewById(R.id.viewPrepareButton)
        viewBudgetButton = findViewById(R.id.viewBudgetButton)

        tripId = intent.getStringExtra("tripId") ?: ""
        Log.d("TripDetailActivity", "tripId: $tripId")

        // Initially set the TimelineFragment when the activity is created
        if (savedInstanceState == null) {
            showFragment(TimelineFragment.newInstance(tripId))  // Pass tripId to the fragment
        }

        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { onBackPressed() }

        // Set onClick listeners for the buttons
        viewPlansButton.setOnClickListener {
            showFragment(TimelineFragment.newInstance(tripId))  // Pass tripId to the fragment
        }

        viewPrepareButton.setOnClickListener {
            showFragment(PrepareFragment.newInstance(tripId))  // Pass tripId to the fragment
        }

        viewBudgetButton.setOnClickListener {
            showFragment(BudgetFragment.newInstance(tripId))  // Pass tripId to the fragment
        }


    }

    // Helper function to switch fragments
    private fun showFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.childFragmentContainer, fragment)  // Replace the fragment in the container
        transaction.addToBackStack(null)  // Add to back stack for back navigation
        transaction.commit()
    }

    // OnItemActionListener 인터페이스 구현
    override fun updateItemCheckedStatus(item: PrepareItem, isChecked: Boolean) {
        // Firestore에서 준비물 체크 상태 업데이트
        val db = FirebaseFirestore.getInstance()
        db.collection("trips").document(tripId).collection("prepare")
            .whereEqualTo("itemName", item.itemName)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("isChecked", isChecked)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "준비물 상태를 업데이트하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }



    override fun deleteItemFromFirestore(item: PrepareItem) {
        // Firestore에서 준비물 삭제
        val db = FirebaseFirestore.getInstance()
        db.collection("trips").document(tripId).collection("prepare")
            .whereEqualTo("itemName", item.itemName)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "준비물을 삭제하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}


    // Helper function to switch fragments


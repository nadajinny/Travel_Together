package com.example.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.icu.text.Transliterator.Position
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.data.model.Trips
import com.example.myapplication.repository.TripRepository
import com.example.myapplication.ui.Trip.TripDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TripListAdapter(val context: Context, val TripList: ArrayList<Trips>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_trip, null)
        val Title = view.findViewById<TextView>(R.id.triptitle)

        val trips = TripList[position]
        Title.text = trips.triptitle

        // 짧게 클릭 시 세부 화면으로 이동
        view.setOnClickListener {
            val intent = Intent(context, TripDetailActivity::class.java)
            intent.putExtra("tripId", trips.tripId)
            Log.d("TripListAdapter", "tripId: ${trips.tripId}, tripTitle: ${trips.triptitle}")
            context.startActivity(intent)
        }

        // 길게 클릭 시 다이얼로그 표시
        view.setOnLongClickListener {
            showOptionsDialog(trips)
            true
        }

        return view
    }

    private fun showOptionsDialog(trip: Trips) {
        val options = arrayOf("삭제", "사용자 추가")

        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("${trip.triptitle}에 대해 수행할 작업 선택")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> confirmDeleteTrip(trip) // 삭제
                1 -> addParticipantToTrip(trip) // 사용자 추가
            }
        }
        builder.show()
    }

    private fun confirmDeleteTrip(trip: Trips) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("여행 삭제")
        builder.setMessage("정말 '${trip.triptitle}' 여행을 삭제하시겠습니까?")
        builder.setPositiveButton("삭제") { dialog, _ ->
            deleteTrip(trip.tripId)
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun deleteTrip(tripId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = TripRepository()
                repository.deleteTrip(tripId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "여행이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("TripListAdapter", "Error deleting trip: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "여행 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addParticipantToTrip(trip: Trips) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("사용자 추가")
        builder.setMessage("추가할 사용자의 이메일을 입력하세요.")

        val input = android.widget.EditText(context)
        input.hint = "사용자 이메일"
        builder.setView(input)

        builder.setPositiveButton("추가") { dialog, _ ->
            val email = input.text.toString()
            if (email.isNotEmpty()) {
                addParticipantToFirestore(trip.tripId, email)
            } else {
                Toast.makeText(context, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun addParticipantToFirestore(tripId: String, email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val tripRef = firestore.collection("trips").document(tripId)
                tripRef.update("participants", com.google.firebase.firestore.FieldValue.arrayUnion(email)).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "$email 사용자가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("TripListAdapter", "Error adding participant: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "사용자 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItem(position: Int): Any {
        return TripList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return TripList.size
    }
}
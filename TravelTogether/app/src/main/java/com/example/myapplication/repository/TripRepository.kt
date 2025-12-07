package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.data.model.Trip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class TripRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun addTrip(title: String, creatorId: String, createdAt: Long, participants: List<String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val tripId = firestore.collection("trips").document().id // ID 생성

        // 객체 생성
        val trip = Trip(
            tripId = tripId,
            title = title,
            creatorId = creatorId,
            createdAt = createdAt,
            participants = participants
        )

        // 문서 추가
        firestore.collection("trips")
            .document(tripId)
            .set(trip)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // 실시간 리스너를 설정하여 데이터를 업데이트
    fun listenToTripsForUser(currentUserEmail: String, onTripsChanged: (List<Trip>) -> Unit): ListenerRegistration {
        return firestore.collection("trips")
            .whereArrayContains("participants", currentUserEmail) // email 있는 문서
            .orderBy("createdAt", Query.Direction.DESCENDING) // createdAt으로 정렬
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.e("TripRepository", "Listen failed: ${e.message}")
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val tripsList = querySnapshot.documents.mapNotNull { it.toObject(Trip::class.java) }
                    onTripsChanged(tripsList)
                }
            }
    }

    suspend fun deleteTrip(tripId: String) {
        firestore.collection("trips").document(tripId).delete().await()
    }
}

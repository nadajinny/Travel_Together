package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.data.model.Plan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class PlanRepository{
    private val firestore = FirebaseFirestore.getInstance()

    fun addPlan(tripId: String, planName: String, time: Long?, onSuccess: ()->Unit, onFailure: (Exception)->Unit){
        val planId = firestore.collection("trips").document(tripId).collection("plans").document().id

        val plan = Plan(
            tripId = tripId,
            planId = planId,
            planName = planName,
            time = time
        )

        firestore.collection("trips").document(tripId).collection("plans")
            .document(planId)
            .set(plan)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    suspend fun getPlansForTripId(tripId: String):List<Plan>{
        if(tripId.isEmpty()){
            Log.e("PlanRepository", "tripId is empty")
            return emptyList()
        }
        return try{
            val querySnapshot = firestore.collection("trips").document(tripId).collection("plans")
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.toObject(Plan::class.java) }
        }catch (e:Exception){
            Log.e("PlanRepository", "Error fetching plans: ${e.message}")
            emptyList()
        }
    }

    suspend fun deletePlan(tripId: String, planId: String) {
        firestore.collection("trips").document(tripId).collection("plans").document(planId).delete().await()
    }


    fun listenToPlansForTrip(tripId: String, onPlansChanged: (List<Plan>) -> Unit): ListenerRegistration {
        return firestore.collection("trips")
            .document(tripId)
            .collection("plans")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("PlanRepository", "Listen failed: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val plans = snapshot.documents.mapNotNull { it.toObject(Plan::class.java) }
                    onPlansChanged(plans)
                }
            }
    }

    fun updatePlan(tripId: String, planId: String, planName: String, time: Long, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val plan = mapOf(
            "planName" to planName,
            "time" to time
        )

        firestore.collection("trips").document(tripId).collection("plans")
            .document(planId)
            .update(plan)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

}

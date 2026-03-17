package com.afquintana.weightcontroller.data.weight

import com.afquintana.weightcontroller.data.analytics.AnalyticsHelper
import com.afquintana.weightcontroller.data.crash.CrashReporter
import com.afquintana.weightcontroller.data.model.WeightEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.pow
import kotlin.math.round
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val analyticsHelper: AnalyticsHelper,
    private val crashReporter: CrashReporter
) {
    fun observeWeights(): Flow<List<WeightEntry>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid).collection("weights")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    crashReporter.record(error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map {
                    WeightEntry(
                        id = it.id,
                        weightKg = it.getDouble("weightKg") ?: 0.0,
                        bmi = it.getDouble("bmi") ?: 0.0,
                        createdAt = it.getLong("createdAt") ?: 0L
                    )
                }
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addWeight(weightKg: Double, heightCm: Double) {
        val uid = requireNotNull(auth.currentUser?.uid)
        val bmi = calculateBmi(weightKg, heightCm)
        firestore.collection("users").document(uid).collection("weights").add(
            hashMapOf(
                "weightKg" to weightKg,
                "bmi" to bmi,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        analyticsHelper.logWeightAdded(weightKg, bmi)
        crashReporter.log("weight_added")
    }

    suspend fun deleteWeight(weightId: String) {
        val uid = requireNotNull(auth.currentUser?.uid)
        firestore.collection("users").document(uid).collection("weights").document(weightId).delete().await()
        analyticsHelper.logWeightDeleted()
        crashReporter.log("weight_deleted")
    }

    private fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        val heightMeters = heightCm / 100.0
        val raw = weightKg / heightMeters.pow(2)
        return round(raw * 100) / 100
    }
}

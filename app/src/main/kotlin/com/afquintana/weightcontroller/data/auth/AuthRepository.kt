package com.afquintana.weightcontroller.data.auth

import com.afquintana.weightcontroller.data.analytics.AnalyticsHelper
import com.afquintana.weightcontroller.data.crash.CrashReporter
import com.afquintana.weightcontroller.data.model.AuthUser
import com.afquintana.weightcontroller.data.model.RegisterInput
import com.afquintana.weightcontroller.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val analyticsHelper: AnalyticsHelper,
    private val crashReporter: CrashReporter
) {
    fun observeAuthUser(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(user?.let { AuthUser(it.uid, it.email.orEmpty(), it.displayName.orEmpty()) })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(email: String, password: String) {
        crashReporter.log("login_attempt")
        auth.signInWithEmailAndPassword(email, password).await()
        val user = auth.currentUser
        analyticsHelper.logLogin()
        crashReporter.setUser(user?.uid, user?.email)
    }

    suspend fun register(input: RegisterInput) {
        crashReporter.log("register_attempt")
        auth.createUserWithEmailAndPassword(input.email, input.password).await()
        val user = requireNotNull(auth.currentUser)
        user.updateProfile(userProfileChangeRequest { displayName = input.name }).await()

        firestore.collection("users").document(user.uid).set(
            hashMapOf(
                "uid" to user.uid,
                "name" to input.name,
                "email" to input.email,
                "heightCm" to input.heightCm,
                "idealWeightKg" to input.idealWeightKg,
                "sex" to input.sex,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()

        analyticsHelper.logSignUp()
        crashReporter.setUser(user.uid, user.email)
    }

    suspend fun logout() {
        analyticsHelper.logLogout()
        auth.signOut()
    }

    suspend fun getCurrentProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        val snapshot = firestore.collection("users").document(user.uid).get().await()
        if (!snapshot.exists()) return null
        return UserProfile(
            uid = snapshot.getString("uid").orEmpty(),
            name = snapshot.getString("name").orEmpty(),
            email = snapshot.getString("email").orEmpty(),
            heightCm = snapshot.getDouble("heightCm") ?: 0.0,
            idealWeightKg = snapshot.getDouble("idealWeightKg") ?: 0.0,
            sex = snapshot.getString("sex").orEmpty(),
            createdAt = snapshot.getLong("createdAt") ?: 0L
        )
    }
}

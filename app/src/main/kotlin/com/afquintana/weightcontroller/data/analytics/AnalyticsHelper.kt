package com.afquintana.weightcontroller.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(private val analytics: FirebaseAnalytics) {
    fun logLogin() = analytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)
    fun logSignUp() = analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, null)
    fun logLogout() = analytics.logEvent("logout", null)
    fun logWeightAdded(weightKg: Double, bmi: Double) {
        analytics.logEvent("weight_added", Bundle().apply {
            putDouble("weight_kg", weightKg)
            putDouble("bmi", bmi)
        })
    }
    fun logWeightDeleted() = analytics.logEvent("weight_deleted", null)
}

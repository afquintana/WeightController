package com.afquintana.weightcontroller.app

import android.content.Context
import com.afquintana.weightcontroller.data.analytics.AnalyticsHelper
import com.afquintana.weightcontroller.data.auth.AuthRepository
import com.afquintana.weightcontroller.data.crash.CrashReporter
import com.afquintana.weightcontroller.data.remote.RetrofitFactory
import com.afquintana.weightcontroller.data.weight.WeightRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore

class AppGraph(context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    val analyticsHelper = AnalyticsHelper(analytics)
    val crashReporter = CrashReporter(crashlytics)
    val retrofitFactory = RetrofitFactory()

    val authRepository = AuthRepository(auth, firestore, analyticsHelper, crashReporter)
    val weightRepository = WeightRepository(auth, firestore, analyticsHelper, crashReporter)
}

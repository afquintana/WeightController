package com.afquintana.weightcontroller.data.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashReporter(private val crashlytics: FirebaseCrashlytics) {
    fun setUser(userId: String?, email: String?) {
        if (!userId.isNullOrBlank()) crashlytics.setUserId(userId)
        crashlytics.setCustomKey("email", email ?: "")
    }
    fun log(message: String) = crashlytics.log(message)
    fun record(throwable: Throwable) = crashlytics.recordException(throwable)
}

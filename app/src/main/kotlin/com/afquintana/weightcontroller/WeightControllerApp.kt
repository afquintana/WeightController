package com.afquintana.weightcontroller

import android.app.Application
import com.afquintana.weightcontroller.app.AppGraph

class WeightControllerApp : Application() {
    lateinit var appGraph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        appGraph = AppGraph(applicationContext)
    }
}

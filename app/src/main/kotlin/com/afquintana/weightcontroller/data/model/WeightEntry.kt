package com.afquintana.weightcontroller.data.model

data class WeightEntry(
    val id: String = "",
    val weightKg: Double = 0.0,
    val bmi: Double = 0.0,
    val createdAt: Long = 0L
)

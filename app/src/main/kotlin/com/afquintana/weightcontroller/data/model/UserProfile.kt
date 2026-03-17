package com.afquintana.weightcontroller.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val heightCm: Double = 0.0,
    val idealWeightKg: Double = 0.0,
    val sex: String = "",
    val createdAt: Long = 0L
)

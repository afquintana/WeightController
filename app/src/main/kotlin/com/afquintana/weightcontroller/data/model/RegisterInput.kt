package com.afquintana.weightcontroller.data.model

data class RegisterInput(
    val name: String,
    val email: String,
    val password: String,
    val heightCm: Double,
    val idealWeightKg: Double,
    val sex: String
)

package com.correct.correctsoc.data.auth

data class GenerateOTPBody(
    val newPhone: String,
    val userId: String,
    val email: String
)
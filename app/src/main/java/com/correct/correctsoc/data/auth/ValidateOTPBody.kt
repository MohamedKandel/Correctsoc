package com.correct.correctsoc.data.auth

data class ValidateOTPBody(
    val otp: String,
    val email: String
)
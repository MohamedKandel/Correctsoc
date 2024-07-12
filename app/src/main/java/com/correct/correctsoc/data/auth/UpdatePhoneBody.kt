package com.correct.correctsoc.data.auth

data class UpdatePhoneBody(
    val newPhone: String,
    val otp: String,
    val userId: String
)
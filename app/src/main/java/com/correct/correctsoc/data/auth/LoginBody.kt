package com.correct.correctsoc.data.auth

data class LoginBody(
    val deviceId: String,
    val password: String,
    val phoneNumber: String
)
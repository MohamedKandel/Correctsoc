package com.correct.correctsoc.data.auth

data class ResetPasswordBody(
    val newPassword: String,
    val token: String,
    val phone: String
)
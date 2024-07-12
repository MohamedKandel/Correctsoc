package com.correct.correctsoc.data.auth

data class UpdatePasswordBody(
    val newPassword: String,
    val oldPassword: String,
    val userId: String
)
package com.correct.correctsoc.data.auth.forget

data class ForgotResponse(
    val errorMessages: String?,
    val isSuccess: Boolean,
    val result: String?,
    val statusCode: Int
)
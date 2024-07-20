package com.correct.correctsoc.data.user

data class AdsResponse(
    val isSuccess: Boolean,
    val statusCode: Int,
    val errorMessages: String?,
    val result: List<AdsResult>?
)
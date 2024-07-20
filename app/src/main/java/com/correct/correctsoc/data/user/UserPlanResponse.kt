package com.correct.correctsoc.data.user

data class UserPlanResponse(
    val statusCode: Int,
    val isSuccess: Boolean,
    val errorMessages: String?,
    val result: ResultBody?
)

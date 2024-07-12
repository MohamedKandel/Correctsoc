package com.correct.correctsoc.data.auth

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("statusCode")
    val statusCode:Int,
    @SerializedName("isSuccess")
    val isSuccess:Boolean,
    @SerializedName("errorMessages")
    val errorMessages:String?,
    @SerializedName("result")
    val result: ResultResponse?
)

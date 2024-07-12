package com.correct.correctsoc.data.auth.forget

import com.google.gson.annotations.SerializedName

data class ResultResponse(
    @SerializedName("otp")
    val otp: String,
    @SerializedName("token")
    val token: String
)

package com.correct.correctsoc.data.auth

import com.google.gson.annotations.SerializedName

data class ResultResponse(
    @SerializedName("userID")
    val userid: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("token")
    val token: String
)

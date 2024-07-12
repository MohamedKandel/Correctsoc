package com.correct.correctsoc.data.auth

import com.google.gson.annotations.SerializedName

data class SignOutBody(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("deviceId")
    val device: String
)

package com.correct.correctsoc.data.auth

import com.google.gson.annotations.SerializedName

data class ConfirmPhoneBody(
    @SerializedName("phone")
    val phone:String,
    @SerializedName("otp")
    val otp: String
)

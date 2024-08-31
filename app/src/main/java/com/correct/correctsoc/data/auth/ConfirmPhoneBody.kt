package com.correct.correctsoc.data.auth

import com.google.gson.annotations.SerializedName

data class ConfirmPhoneBody(
    @SerializedName("email")
    val phone:String,
    @SerializedName("otp")
    val otp: String
)

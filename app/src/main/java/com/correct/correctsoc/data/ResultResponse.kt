package com.correct.correctsoc.data

import com.google.gson.annotations.SerializedName

data class ResultResponse(
    @SerializedName("deviceName")
    val deviceName: String,
    @SerializedName("ipAddress")
    val ipAddress: String
)
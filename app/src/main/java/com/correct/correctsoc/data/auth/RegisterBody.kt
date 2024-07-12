package com.correct.correctsoc.data.auth

import com.google.gson.annotations.SerializedName

data class RegisterBody(
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String
)

package com.correct.correctsoc.data.auth

import com.google.gson.annotations.SerializedName

data class UpdateUsernameBody(
    @SerializedName("phoneNumber")
    val phone:String,
    @SerializedName("newUserName")
    val username: String
)

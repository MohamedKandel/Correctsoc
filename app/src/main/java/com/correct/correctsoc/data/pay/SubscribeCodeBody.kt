package com.correct.correctsoc.data.pay

data class SubscribeCodeBody(
    val code: String,
    val deviceId: String,
    val phone: String
)
package com.correct.correctsoc.data.pay

data class SubscibeGooglePayBody(
    val amount: Int,
    val currencyIsoCode: String,
    val deviceData: String,
    val months: Int,
    val nonce: String,
    val phoneNumber: String,
    val discount: Int = 0
)
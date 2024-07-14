package com.correct.correctsoc.data.pay

// change after deploy API
data class DistributorsModel(
    val contact_name: String,
    val phone_number: String,
    val whatsapp: String,
    val isVodafoneAvailable: Boolean,
    val isEtisalatAvailable: Boolean,
    val isOrangeAvailable: Boolean,
    val isInstaPayAvailable: Boolean
)

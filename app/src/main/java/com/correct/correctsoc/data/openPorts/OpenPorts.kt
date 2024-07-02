package com.correct.correctsoc.data.openPorts

data class OpenPorts(
    val asn: String,
    val city: String,
    val country: String,
    val loc: String,
    val organisation: String,
    val region: String,
    val scanHostDeviceName: String,
    val scanIp: String,
    val timezone: String,
    val openPortsInfo: List<Port>
)
package com.correct.correctsoc.data.openPorts

data class OpenPorts(
    val scanHostDeviceName: String,
    val scanIp: String,
    val openPortsInfo: List<Port>
)
package com.correct.correctsoc.helper

import com.correct.correctsoc.data.DevicesData

interface OnDevicesFetchedListener {
    fun onAllDevicesFetched(devices: MutableList<DevicesData>)
}

interface OnProgressUpdatedListener {
    fun onUpdateProgressLoad(progress: Int)
}
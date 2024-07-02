package com.correct.correctsoc.data

import android.os.Parcel
import android.os.Parcelable

data class DevicesData(val ipAddress: String, val deviceName: String,
    val macAddress: String, val vendorName:String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ipAddress)
        parcel.writeString(deviceName)
        parcel.writeString(macAddress)
        parcel.writeString(vendorName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DevicesData> {
        override fun createFromParcel(parcel: Parcel): DevicesData {
            return DevicesData(parcel)
        }

        override fun newArray(size: Int): Array<DevicesData?> {
            return arrayOfNulls(size)
        }
    }
}
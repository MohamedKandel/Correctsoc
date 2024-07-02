package com.correct.correctsoc.data.openPorts

import android.os.Parcel
import android.os.Parcelable

data class Port(
    val cpe: String?,
    val port: Int,
    val protocol: String,
    val service: String,
    val version: String,
    val cvEs:List<CvEs>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.createTypedArrayList(CvEs)?.toList()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cpe)
        parcel.writeInt(port)
        parcel.writeString(protocol)
        parcel.writeString(service)
        parcel.writeString(version)
        parcel.writeTypedList(cvEs)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Port> {
        override fun createFromParcel(parcel: Parcel): Port {
            return Port(parcel)
        }

        override fun newArray(size: Int): Array<Port?> {
            return arrayOfNulls(size)
        }
    }
}
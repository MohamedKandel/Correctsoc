package com.correct.correctsoc.data.openPorts

import android.os.Parcel
import android.os.Parcelable

data class CvEs(
    val cvss: Double,
    val id: String,
    val url: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(cvss)
        parcel.writeString(id)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CvEs> {
        override fun createFromParcel(parcel: Parcel): CvEs {
            return CvEs(parcel)
        }

        override fun newArray(size: Int): Array<CvEs?> {
            return arrayOfNulls(size)
        }
    }
}
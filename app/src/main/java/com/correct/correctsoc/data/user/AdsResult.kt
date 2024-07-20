package com.correct.correctsoc.data.user

import android.os.Parcel
import android.os.Parcelable

data class AdsResult(
    val image: String,
    val description: String,
    val title: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(image)
        parcel.writeString(description)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AdsResult> {
        override fun createFromParcel(parcel: Parcel): AdsResult {
            return AdsResult(parcel)
        }

        override fun newArray(size: Int): Array<AdsResult?> {
            return arrayOfNulls(size)
        }
    }
}

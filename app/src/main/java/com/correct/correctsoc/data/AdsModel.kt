package com.correct.correctsoc.data

import android.os.Parcel
import android.os.Parcelable

data class AdsModel(
    val img: String,
    val title: String,
    val description: String,
    val id: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(img)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AdsModel> {
        override fun createFromParcel(parcel: Parcel): AdsModel {
            return AdsModel(parcel)
        }

        override fun newArray(size: Int): Array<AdsModel?> {
            return arrayOfNulls(size)
        }
    }
}

package com.correct.correctsoc.room

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AppLockData")
class App(
    @PrimaryKey
    var packageName: String,
    var appName: String,
    var password: String,
    var isAllowed: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt()
    ) {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as App

        if (packageName != other.packageName) return false
        if (appName != other.appName) return false
        if (password != other.password) return false
        if (isAllowed != other.isAllowed) return false
        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + appName.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + isAllowed.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(appName)
        parcel.writeString(password)
        parcel.writeInt(isAllowed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<App> {
        override fun createFromParcel(parcel: Parcel): App {
            return App(parcel)
        }

        override fun newArray(size: Int): Array<App?> {
            return arrayOfNulls(size)
        }
    }
}

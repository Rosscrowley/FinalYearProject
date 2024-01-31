package com.example.finalyearproject

import android.os.Parcel
import android.os.Parcelable

data class TongueTwister(
    val id: String = "",
    val content: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TongueTwister> {
        override fun createFromParcel(parcel: Parcel): TongueTwister {
            return TongueTwister(parcel)
        }

        override fun newArray(size: Int): Array<TongueTwister?> {
            return arrayOfNulls(size)
        }
    }
}
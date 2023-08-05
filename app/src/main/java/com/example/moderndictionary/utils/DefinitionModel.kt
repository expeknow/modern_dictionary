package com.example.moderndictionary.utils

import android.os.Parcel
import android.os.Parcelable

data class DefinitionModel(
    val id: Int = 0,
    val word: String,
    val definition: String,
    val example: String,
    val likes: Int,
    val dislikes: Int,
    val author: String,
    val datePublished: String,
    var isBookmarked: Boolean = false
        ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(word)
        parcel.writeString(definition)
        parcel.writeString(example)
        parcel.writeInt(likes)
        parcel.writeInt(dislikes)
        parcel.writeString(author)
        parcel.writeString(datePublished)
        parcel.writeByte(if (isBookmarked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DefinitionModel> {
        override fun createFromParcel(parcel: Parcel): DefinitionModel {
            return DefinitionModel(parcel)
        }

        override fun newArray(size: Int): Array<DefinitionModel?> {
            return arrayOfNulls(size)
        }
    }
}
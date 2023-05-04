package edu.licenta.uptconnect.model

import android.os.Parcel
import android.os.Parcelable

data class Poll(
    val pollId: String,
    val createdBy: String,
    val end_time: String,
    val start_time: String,
    val question: String,
    val options: List<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(pollId)
        parcel.writeString(createdBy)
        parcel.writeString(end_time)
        parcel.writeString(start_time)
        parcel.writeString(question)
        parcel.writeStringList(options)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Poll> {
        override fun createFromParcel(parcel: Parcel): Poll {
            return Poll(parcel)
        }

        override fun newArray(size: Int): Array<Poll?> {
            return arrayOfNulls(size)
        }
    }

}

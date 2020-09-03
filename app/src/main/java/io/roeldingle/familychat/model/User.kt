package io.roeldingle.familychat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/*
* this will enable to move obj to activities
*/
@Parcelize
class User(val uid: String, val username: String, val profileImgUrl: String): Parcelable{
    constructor() : this("","","")
}
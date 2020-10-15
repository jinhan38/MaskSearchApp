package com.masksearchapp.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class MaskData (
    val count : Int,
    val stores : List<Stores>
) : Parcelable{

    @Parcelize
    data class Stores (
        val code : String,
        val name : String,
        val addr : String,
        val type : String,
        val lat : Double,
        val lng : Double,
        val stock_at : String,
        val remain_stat : String,
        val created_at : String
    )  :Parcelable
}
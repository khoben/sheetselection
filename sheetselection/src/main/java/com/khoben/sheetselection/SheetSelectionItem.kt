package com.khoben.sheetselection

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class SheetSelectionItem(
    val key: String,
    val value: String,
    var isChecked: Boolean = false,
    @DrawableRes val icon: Int? = null
) : Parcelable
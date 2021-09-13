package com.khoben.sheetselection

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

object Utils {
    fun getColor(context: Context, @AttrRes attr: Int): Int {
        val typedValue = getTypedValue(context, attr)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }

    private fun getTypedValue(context: Context, attr: Int): TypedValue {
        return TypedValue().also { typedValue ->
            context.theme.resolveAttribute(
                attr,
                typedValue,
                true
            )
        }
    }
}
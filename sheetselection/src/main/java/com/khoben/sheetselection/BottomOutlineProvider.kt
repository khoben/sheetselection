package com.khoben.sheetselection

import android.graphics.Outline
import android.view.View

import android.view.ViewOutlineProvider

class BottomOutlineProvider(private val bottomPadding: Int) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setRect(
            -view.width,
            -view.height,
            2 * view.width,
            view.height - bottomPadding
        )
    }
}
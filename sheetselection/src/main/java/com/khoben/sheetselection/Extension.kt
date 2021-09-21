package com.khoben.sheetselection

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun AppCompatActivity.createSheetSelection(
    sheetSelectionTag: String,
    builder: SheetSelection.Builder.() -> SheetSelection.Builder
) {
    SheetSelection.Builder(sheetSelectionTag).builder().show(supportFragmentManager)
}

fun Fragment.createSheetSelection(
    sheetSelectionTag: String,
    builder: SheetSelection.Builder.() -> SheetSelection.Builder
) {
    SheetSelection.Builder(sheetSelectionTag).builder().show(childFragmentManager)
}
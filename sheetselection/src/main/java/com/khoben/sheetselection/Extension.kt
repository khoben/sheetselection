package com.khoben.sheetselection

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

inline fun AppCompatActivity.showSheetSelection(
    sheetSelectionTag: String,
    crossinline block: SheetSelection.Builder.() -> Unit
) {
    SheetSelection.Builder(sheetSelectionTag).apply(block).show(supportFragmentManager)
}

inline fun Fragment.showSheetSelection(
    sheetSelectionTag: String,
    crossinline block: SheetSelection.Builder.() -> Unit
) {
    SheetSelection.Builder(sheetSelectionTag).apply(block).show(childFragmentManager)
}
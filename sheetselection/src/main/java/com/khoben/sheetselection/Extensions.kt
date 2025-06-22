package com.khoben.sheetselection

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

inline fun AppCompatActivity.createSheetSelection(
    sheetSelectionTag: String, crossinline block: SheetSelection.Builder.() -> Unit
): SheetSelection {
    return SheetSelection.Builder(sheetSelectionTag).apply(block).build()
}

inline fun AppCompatActivity.showSheetSelection(
    sheetSelectionTag: String, fragmentManager: FragmentManager = supportFragmentManager,
    crossinline block: SheetSelection.Builder.() -> Unit
) {
    SheetSelection.Builder(sheetSelectionTag).apply(block).show(fragmentManager)
}

inline fun Fragment.createSheetSelection(
    sheetSelectionTag: String, crossinline block: SheetSelection.Builder.() -> Unit
): SheetSelection {
    return SheetSelection.Builder(sheetSelectionTag).apply(block).build()
}

inline fun Fragment.showSheetSelection(
    sheetSelectionTag: String, fragmentManager: FragmentManager = childFragmentManager,
    crossinline block: SheetSelection.Builder.() -> Unit
) {
    SheetSelection.Builder(sheetSelectionTag).apply(block).show(fragmentManager)
}
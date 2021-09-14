package com.khoben.sheetselection

interface OnSheetItemClickListener {
    fun onSheetItemClicked(
        clickedItem: SheetSelectionItem,
        adapterPosition: Int,
        tag: String? = null
    )
}
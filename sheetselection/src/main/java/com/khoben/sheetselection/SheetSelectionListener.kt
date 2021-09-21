package com.khoben.sheetselection

interface SheetSelectionListener {
    /**
     * Sheet selection callback
     *
     * @param all All items
     * @param selected Selected items
     * @param sheetSelectionTag SheetSelection's tag
     */
    fun onSheetItemsSelected(
        all: List<SheetSelectionItem>,
        selected: List<SheetSelectionItem>,
        sheetSelectionTag: String
    )
}
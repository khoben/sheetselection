package com.khoben.sheetselection

interface SheetSelectionListener {
    /**
     * Sheet selection callback
     *
     * @param all All items
     * @param selected Selected items
     * @param tag SheetSelection's tag
     */
    fun onSheetItemsSelected(
        all: List<SheetSelectionItem>,
        selected: List<SheetSelectionItem>,
        tag: String? = null
    )
}
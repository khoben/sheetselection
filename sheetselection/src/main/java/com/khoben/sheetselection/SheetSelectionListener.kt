package com.khoben.sheetselection

interface SheetSelectionListener {
    /**
     * Sheet selection callback
     *
     * @param event Sheet selection event
     */
    fun onSheetItemsSelected(event: SheetSelectionEvent)

    object NOOP : SheetSelectionListener {
        override fun onSheetItemsSelected(event: SheetSelectionEvent) = Unit
    }
}
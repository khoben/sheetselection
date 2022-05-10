package com.khoben.sheetselection

/**
 * Sheet selection callback data
 *
 * @param tag SheetSelection's tag
 * @param selected Selected items
 * @param data All items
 */
class SheetSelectionEvent(
    val tag: String,
    val selected: List<SheetSelectionItem>,
    val data: List<SheetSelectionItem>,
) {
    /**
     * Run [block] if tag matches
     */
    fun doIfMatches(tag: String, block: (selected: List<SheetSelectionItem>) -> Unit) {
        if (this.tag == tag) {
            block(selected)
        }
    }
}
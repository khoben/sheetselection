package com.khoben.sheetselection

import androidx.recyclerview.widget.DiffUtil

class SelectionDiffCallback(
    private val oldList: List<SheetSelectionItem>,
    private val newList: List<SheetSelectionItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].key == newList[newItemPosition].key
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].value == newList[newItemPosition].value &&
                oldList[oldItemPosition].isChecked == newList[newItemPosition].isChecked
    }
}
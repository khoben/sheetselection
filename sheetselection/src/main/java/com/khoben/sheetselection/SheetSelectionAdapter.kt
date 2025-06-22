package com.khoben.sheetselection

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.khoben.sheetselection.databinding.EmptyItemBinding
import com.khoben.sheetselection.databinding.RowSelectionItemBinding


class SheetSelectionAdapter(
    private val source: List<SheetSelectionItem>,
    private val emptyText: String,
    private val onItemSelected: (item: SheetSelectionItem, position: Int) -> Unit
) : RecyclerView.Adapter<SheetSelectionAdapter.BaseViewHolder>() {

    private var currentList: List<SheetSelectionItem> = source

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> EmptyViewHolder(
                EmptyItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEW_TYPE_ITEM -> ItemViewHolder(
                RowSelectionItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }


    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is EmptyViewHolder -> holder.bind(emptyText)
            is ItemViewHolder -> holder.bind(
                item = currentList[position],
                onItemSelected = onItemSelected
            )
            else -> throw IllegalArgumentException("Invalid viewHolder")
        }
    }

    override fun getItemCount() =
        when (val count = currentList.size) {
            0 -> 1
            else -> count
        }

    override fun getItemViewType(position: Int): Int =
        if (currentList.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_ITEM
        }

    fun search(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            internalUpdate(source)
        } else {
            val searchResult = source.filter { it.value.contains(keyword, true) }
            internalUpdate(searchResult)
        }
    }

    private fun internalUpdate(newList: List<SheetSelectionItem>) {
        val diffResult: DiffUtil.DiffResult =
            DiffUtil.calculateDiff(SelectionDiffCallback(currentList, newList), false)
        diffResult.dispatchUpdatesTo(this)
        currentList = newList
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class EmptyViewHolder(
        private val binding: EmptyItemBinding
    ) : BaseViewHolder(binding.root) {

        fun bind(item: String) {
            binding.emptyResultView.text = item
        }
    }

    private class ItemViewHolder(
        private val binding: RowSelectionItemBinding
    ) : BaseViewHolder(binding.root) {

        private fun getAttrColor(context: Context, @AttrRes attr: Int): Int {
            val typedValue = TypedValue().also { typedValue ->
                context.theme.resolveAttribute(attr, typedValue, true)
            }
            return ContextCompat.getColor(context, typedValue.resourceId)
        }

        fun bind(
            item: SheetSelectionItem,
            onItemSelected: (item: SheetSelectionItem, position: Int) -> Unit
        ) {
            binding.textViewItem.text = item.value
            val leftDrawable = if (item.icon != null)
                ContextCompat.getDrawable(
                    itemView.context,
                    item.icon
                ) else null
            val rightDrawable = if (item.isChecked)
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.ic_check
                )?.apply {
                    setTint(
                        getAttrColor(
                            itemView.context,
                            com.google.android.material.R.attr.colorPrimary
                        )
                    )
                } else null

            binding.textViewItem.setCompoundDrawablesWithIntrinsicBounds(
                leftDrawable,
                null,
                rightDrawable,
                null
            )

            binding.textViewItem.setOnClickListener {
                onItemSelected.invoke(item, adapterPosition)
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}
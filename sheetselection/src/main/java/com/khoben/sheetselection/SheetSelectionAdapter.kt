package com.khoben.sheetselection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.khoben.sheetselection.databinding.RowSelectionItemBinding


class SheetSelectionAdapter(
    private val source: List<SheetSelectionItem>,
    private val onItemSelectedListener: OnSheetItemClickListener?
) : RecyclerView.Adapter<SheetSelectionAdapter.ItemViewHolder>() {

    private var currentList: List<SheetSelectionItem> = source

    override fun getItemCount() = currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RowSelectionItemBinding.inflate(layoutInflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBindView(
            item = currentList[position],
            onItemSelectedListener = onItemSelectedListener
        )
    }

    private var recyclerView: EmptyRecyclerView? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (recyclerView is EmptyRecyclerView) this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    fun search(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            recyclerView?.isEmpty(false)
            updateItems(source)
        } else {
            val searchResult = source.filter { it.value.contains(keyword, true) }
            recyclerView?.isEmpty(searchResult.isEmpty())
            updateItems(searchResult)
        }
    }

    private fun updateItems(newList: List<SheetSelectionItem>) {
        val diffResult: DiffUtil.DiffResult =
            DiffUtil.calculateDiff(SelectionDiffCallback(currentList, newList), false)
        diffResult.dispatchUpdatesTo(this)
        this.currentList = newList
    }

    class ItemViewHolder(
        private val binding: RowSelectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBindView(
            item: SheetSelectionItem,
            onItemSelectedListener: OnSheetItemClickListener?
        ) {
            val selectedIcon = if (item.isChecked) R.drawable.ic_check else 0
            binding.textViewItem.setCompoundDrawablesWithIntrinsicBounds(
                item.icon ?: 0, 0, selectedIcon, 0
            )

            // set checkmark color tint
            binding.textViewItem.compoundDrawables[2]?.setTint(
                Utils.getColor(
                    itemView.context,
                    R.attr.colorPrimary
                )
            )

            binding.textViewItem.text = item.value

            binding.textViewItem.setOnClickListener {
                onItemSelectedListener?.onSheetItemClicked(item, adapterPosition)
            }
        }
    }
}
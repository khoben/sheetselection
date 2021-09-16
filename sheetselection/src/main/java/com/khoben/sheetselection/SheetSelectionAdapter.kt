package com.khoben.sheetselection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
        if (recyclerView is EmptyRecyclerView) {
            this.recyclerView = recyclerView
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    fun search(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            internalUpdate(source)
            recyclerView?.setEmptyState(false)
        } else {
            val searchResult = source.filter { it.value.contains(keyword, true) }
            internalUpdate(searchResult)
            recyclerView?.setEmptyState(searchResult.isEmpty())
        }
    }

    fun resetCheckedStates(state: Boolean) {
        currentList.forEachIndexed { index, it ->
            if (it.isChecked != state) {
                it.isChecked = state
                notifyItemChanged(index)
            }
        }
    }

    private fun internalUpdate(newList: List<SheetSelectionItem>) {
        val diffResult: DiffUtil.DiffResult =
            DiffUtil.calculateDiff(SelectionDiffCallback(currentList, newList), false)
        this.currentList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    class ItemViewHolder(
        private val binding: RowSelectionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onBindView(
            item: SheetSelectionItem,
            onItemSelectedListener: OnSheetItemClickListener?
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
                    setTint(Utils.getColor(itemView.context, R.attr.colorPrimary))
                } else null

            binding.textViewItem.setCompoundDrawablesWithIntrinsicBounds(
                leftDrawable,
                null,
                rightDrawable,
                null
            )

            binding.textViewItem.setOnClickListener {
                onItemSelectedListener?.onSheetItemClicked(item, adapterPosition)
            }
        }
    }
}
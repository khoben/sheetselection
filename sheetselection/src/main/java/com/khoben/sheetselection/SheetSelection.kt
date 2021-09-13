package com.khoben.sheetselection

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.khoben.sheetselection.databinding.DialogSheetSelectionBinding
import kotlin.math.roundToInt


class SheetSelection : BottomSheetDialogFragment() {

    private lateinit var items: List<SheetSelectionItem>
    private lateinit var adapter: SheetSelectionAdapter

    private var sheetSelectionTag: String? = null
    private var listener: SheetSelectionListener? = null

    private var _binding: DialogSheetSelectionBinding? = null
    private val binding get() = _binding!!

    private var isSearchableState: Boolean = false
    private var previousSheetState: Int = STATE_COLLAPSED
    private var previousLayoutHeight: Int = WRAP_CONTENT

    private fun screenHeight(): Int {
        val metrics = requireContext().resources.displayMetrics
        return metrics.heightPixels
    }

    override fun getTheme(): Int = arguments?.getInt(ARGS_THEME) ?: super.getTheme()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            override fun onBackPressed() {
                if (isSearchableState) {
                    exitSearchViewState()
                } else {
                    super.onBackPressed()
                }
            }
        }.apply {
            behavior.peekHeight = calcBottomSheetPeekHeight()
            if (arguments?.getBoolean(ARGS_MULTIPLE_SELECTION_ENABLED) == true) {
                setOnShowListener {
                    behavior.addBottomSheetCallback(object :
                        BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            updateStickyButton(bottomSheet, slideOffset)
                        }
                    })
                    binding.root.post { updateStickyButton(binding.root.parent as View, 0f) }
                }
            }
        }
    }

    private fun updateStickyButton(bottomSheet: View, slideOffset: Float) {
        binding.doneButtonContainer.y = if (slideOffset < 0) {
            (bottomSheet.parent as View).height -
                    bottomSheet.top - (STICKY_BOTTOM_DISAPPEARING_ACCELERATE * slideOffset + 1) *
                    binding.doneButtonContainer.height
        } else {
            (bottomSheet.parent as View).height.toFloat() - bottomSheet.top -
                    binding.doneButtonContainer.height
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogSheetSelectionBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { args ->

            items = args.getParcelableArrayList(ARGS_ITEMS)!!

            if (args.getBoolean(ARGS_SHOW_DRAGGED_INDICATOR)) {
                binding.draggedIndicator.visibility = View.VISIBLE
            }

            if (args.getBoolean(ARGS_MULTIPLE_SELECTION_ENABLED)) {
                binding.doneButtonContainer.visibility = View.VISIBLE
                binding.doneButton.text =
                    args.getString(ARGS_APPLY_BUTTON_TEXT, getString(R.string.apply))
                binding.doneButton.setOnClickListener {
                    listener?.onSheetItemsSelected(
                        items,
                        items.filter { it.isChecked },
                        sheetSelectionTag
                    )
                    dismiss()
                }
            }

            if (args.getBoolean(ARGS_SHOW_CLOSE_BTN)) {
                binding.buttonClose.visibility = View.VISIBLE
                binding.textViewTitle.updateLayoutParams<LinearLayout.LayoutParams> {
                    marginStart = 0
                }
                binding.buttonClose.setOnClickListener {
                    dismiss()
                }
            }

            args.getString(ARGS_TITLE).let { title ->
                if (title.isNullOrEmpty()) {
                    binding.textViewTitle.visibility = View.GONE
                    binding.textViewTitle.text = null
                } else {
                    binding.textViewTitle.visibility = View.VISIBLE
                    binding.textViewTitle.text = title
                }
            }

            if (args.getBoolean(ARGS_SEARCH_ENABLED)) {
                binding.buttonSearch.visibility = View.VISIBLE
                binding.buttonSearch.setOnClickListener(onSearchClickListener)
                binding.searchView.setOnCloseListener(onSearchCloseListener)
                binding.searchView.setOnQueryTextListener(onSearchQueryTextListener)
            }

            binding.recyclerViewSelectionItems.adapter = SheetSelectionAdapter(
                source = items,
                onItemSelectedListener = onItemSelectedListener
            ).also { adapter = it }
            binding.recyclerViewSelectionItems.itemAnimator = null
            binding.recyclerViewSelectionItems.setHasFixedSize(true)

            binding.recyclerViewSelectionItems.setEmptyView(binding.recyclerViewSelectionEmpty.apply {
                findViewById<TextView>(R.id.recyclerViewSelectionEmpty).text =
                    args.getString(ARGS_SEARCH_NOT_FOUND_TEXT) ?: getString(R.string.not_found)
            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sheetSelectionTag = arguments?.getString(ARGS_TAG)
        if (context is SheetSelectionListener && sheetSelectionTag != null) {
            listener = context
        } else {
            throw RuntimeException("$context should implement 'SheetSelectionListener' and tag(String) should be set")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun updateSheetHeight(viewHeight: Int): Int {
        val savedHeight = binding.root.layoutParams.height
        binding.root.layoutParams = binding.root.layoutParams
            .apply { height = viewHeight }
        return savedHeight
    }

    private val onItemSelectedListener = object : OnSheetItemClickListener {
        override fun onSheetItemClicked(
            clickedItem: SheetSelectionItem,
            adapterPosition: Int,
            tag: String?
        ) {
            if (arguments?.getBoolean(ARGS_MULTIPLE_SELECTION_ENABLED, false) == true) {
                clickedItem.isChecked = clickedItem.isChecked.not()
                adapter.notifyItemChanged(adapterPosition)
            } else {
                items.forEach { selectionItem ->
                    selectionItem.isChecked = false
                }
                clickedItem.isChecked = true
                listener?.onSheetItemsSelected(items, listOf(clickedItem), sheetSelectionTag)
                dismiss()
            }
        }
    }
    private val onSearchClickListener = View.OnClickListener {
        enterSearchViewState()
    }

    private val onSearchCloseListener = SearchView.OnCloseListener {
        exitSearchViewState()
        true
    }

    private fun enterSearchViewState() {
        forceExpand()
        toggleSearchState(true)
    }

    private fun exitSearchViewState() {
        toggleSearchState(false)
        restoreBottomSheetState()
    }

    private fun forceExpand() {
        (dialog as? BottomSheetDialog)?.apply {
            previousSheetState = behavior.state
            behavior.state = STATE_EXPANDED
        }
        previousLayoutHeight = updateSheetHeight(MATCH_PARENT)
    }

    private fun restoreBottomSheetState() {
        (dialog as? BottomSheetDialog)?.apply {
            behavior.state = previousSheetState
        }
        updateSheetHeight(previousLayoutHeight)
    }

    private fun toggleSearchState(isSearchable: Boolean) {
        this.isSearchableState = isSearchable
        if (isSearchable) {
            binding.viewSwitcherHeader.displayedChild = 1
            binding.searchView.isIconified = false
        } else {
            binding.searchView.setQuery("", false)
            binding.viewSwitcherHeader.displayedChild = 0
        }
        with(from(binding.root.parent as View)) {
            isDraggable = !isSearchable
        }
    }

    private val onSearchQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(newText: String?): Boolean {
            adapter.search(newText)
            return true
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            adapter.search(query)
            return true
        }
    }

    private fun calcBottomSheetPeekHeight(): Int {
        val metrics = requireContext().resources.displayMetrics
        val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels
        return when {
            ratio >= PEEK_HEIGHT_AUTO_RATIO_THRESHOLD -> PEEK_HEIGHT_AUTO
            else -> (metrics.heightPixels * WIDE_SCREEN_PEEK_HEIGHT_RATIO).roundToInt()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_PREV_STATE, previousSheetState)
        outState.putInt(STATE_PREV_HEIGHT, previousLayoutHeight)
        outState.putBoolean(STATE_SEARCH, isSearchableState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { savedState ->
            previousSheetState = savedState.getInt(STATE_PREV_STATE)
            previousLayoutHeight = savedState.getInt(STATE_PREV_HEIGHT)
            if (savedState.getBoolean(STATE_SEARCH)) {
                enterSearchViewState()
            }
        }
    }

    class Builder(private val tag: String) {
        @StyleRes
        private var themeId: Int = R.style.Theme_SheetSelection
        private var title: String? = null
        private var items: List<SheetSelectionItem> = emptyList()
        private var showDraggedIndicator: Boolean = false
        private var searchEnabled: Boolean = false
        private var multipleSelectionEnabled: Boolean = false
        private var searchNotFoundText: String? = null
        private var applyButtonText: String? = null
        private var showCloseButton: Boolean = false

        fun theme(@StyleRes themeId: Int) = apply {
            this.themeId = themeId
        }

        fun title(title: String?) = apply {
            this.title = title
        }

        fun items(items: List<SheetSelectionItem>) = apply {
            this.items = items
        }

        fun <T> items(
            source: List<T>,
            mapper: (T) -> SheetSelectionItem,
        ) = items(source.map { item -> mapper.invoke(item) })

        fun showDraggedIndicator(show: Boolean) = apply {
            this.showDraggedIndicator = show
        }

        fun searchEnabled(enabled: Boolean) = apply {
            this.searchEnabled = enabled
        }

        fun searchNotFoundText(text: String) = apply {
            this.searchNotFoundText = text
        }

        fun enableMultiSelection(enabled: Boolean) = apply {
            this.multipleSelectionEnabled = enabled
        }

        fun setApplyFilterButtonText(text: String) = apply {
            this.applyButtonText = text
        }

        fun showCloseButton(showCloseButton: Boolean) = apply {
            this.showCloseButton = showCloseButton
        }

        fun build() = SheetSelection().apply {
            arguments = Bundle()
                .apply {
                    putInt(ARGS_THEME, this@Builder.themeId)
                    putString(ARGS_TITLE, this@Builder.title)
                    putParcelableArrayList(ARGS_ITEMS, ArrayList(this@Builder.items))
                    putBoolean(ARGS_SHOW_DRAGGED_INDICATOR, this@Builder.showDraggedIndicator)
                    putBoolean(ARGS_SEARCH_ENABLED, this@Builder.searchEnabled)
                    putString(ARGS_SEARCH_NOT_FOUND_TEXT, this@Builder.searchNotFoundText)
                    putBoolean(ARGS_MULTIPLE_SELECTION_ENABLED, multipleSelectionEnabled)
                    putString(ARGS_APPLY_BUTTON_TEXT, applyButtonText)
                    putBoolean(ARGS_SHOW_CLOSE_BTN, showCloseButton)
                    putString(ARGS_TAG, this@Builder.tag)
                }
        }

        fun show(manager: FragmentManager) {
            build().show(manager, TAG)
        }
    }

    companion object {
        const val TAG = "SheetSelection"
        private const val ARGS_THEME = "SheetSelection:ARGS_THEME"
        private const val ARGS_TITLE = "SheetSelection:ARGS_TITLE"
        private const val ARGS_ITEMS = "SheetSelection:ARGS_ITEMS"
        private const val ARGS_SEARCH_NOT_FOUND_TEXT = "SheetSelection:ARGS_SEARCH_NOT_FOUND_TEXT"
        private const val ARGS_SHOW_DRAGGED_INDICATOR = "SheetSelection:ARGS_SHOW_DRAGGED_INDICATOR"
        private const val ARGS_SEARCH_ENABLED = "SheetSelection:ARGS_SEARCH_ENABLED"
        private const val ARGS_MULTIPLE_SELECTION_ENABLED =
            "SheetSelection:ARGS_FOR_MULTIPLE_SELECTION"
        private const val ARGS_APPLY_BUTTON_TEXT =
            "SheetSelection:ARGS_APPLY_BUTTON_TEXT"
        private const val ARGS_SHOW_CLOSE_BTN =
            "SheetSelection:ARGS_SHOW_CLOSE_BTN"
        private const val ARGS_TAG = "SheetSelection:ARGS_TAG"

        private const val STATE_PREV_HEIGHT = "STATE:PREV_HEIGHT"
        private const val STATE_PREV_STATE = "STATE:PREV_STATE"
        private const val STATE_SEARCH = "STATE:SEARCH"

        private const val STICKY_BOTTOM_DISAPPEARING_ACCELERATE = 6
        private const val PEEK_HEIGHT_AUTO_RATIO_THRESHOLD = 1.1f
        private const val WIDE_SCREEN_PEEK_HEIGHT_RATIO = 0.6f
    }
}
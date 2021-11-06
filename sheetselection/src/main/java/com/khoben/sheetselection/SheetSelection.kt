package com.khoben.sheetselection

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.IntDef
import androidx.annotation.StyleRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.khoben.sheetselection.databinding.DialogSheetSelectionBinding
import kotlin.math.roundToInt


class SheetSelection : BottomSheetDialogFragment() {

    private lateinit var items: List<SheetSelectionItem>
    private lateinit var selectionAdapter: SheetSelectionAdapter

    private var sheetSelectionTag: String? = null
    private var listener: SheetSelectionListener? = null

    private var _binding: DialogSheetSelectionBinding? = null
    private val binding get() = _binding!!

    private var isSearchableState: Boolean = false
    private var previousSheetState: Int = STATE_COLLAPSED

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
        binding.root.post { updateStickyButton(binding.root.parent as View, 0f) }
        arguments?.let { args ->

            sheetSelectionTag = args.getString(ARGS_TAG)

            items = args.getParcelableArrayList(ARGS_ITEMS)!!

            if (args.getBoolean(ARGS_SHOW_DRAGGED_INDICATOR)) {
                binding.draggedIndicator.visibility = View.VISIBLE
            }

            if (args.getBoolean(ARGS_MULTIPLE_SELECTION_ENABLED)) {
                binding.doneButtonContainer.visibility = View.VISIBLE
                binding.doneButton.text =
                    args.getString(ARGS_APPLY_BUTTON_TEXT, getString(R.string.apply))
                binding.doneButtonContainer.setOnClickListener {
                    listener?.onSheetItemsSelected(
                        items,
                        items.filter { it.isChecked },
                        sheetSelectionTag!!
                    )
                    dismiss()
                }
            }

            if (args.getBoolean(ARGS_SHOW_CLOSE_BTN)) {
                binding.buttonClose.visibility = View.VISIBLE
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
                binding.buttonSearch.setOnClickListener { enterSearchViewState() }
                binding.searchView.setOnCloseListener {
                    exitSearchViewState()
                    true
                }
                binding.searchView.setOnQueryTextListener(onSearchQueryTextListener)
            }

            binding.recyclerViewSelectionItems.adapter = SheetSelectionAdapter(
                source = items,
                onItemSelectedListener = onItemSelectedListener
            ).also { selectionAdapter = it }
            binding.recyclerViewSelectionItems.itemAnimator = null
            binding.recyclerViewSelectionItems.setEmptyView(binding.recyclerViewSelectionEmpty.apply {
                text = args.getString(ARGS_SEARCH_NOT_FOUND_TEXT) ?: getString(R.string.not_found)
            })

            if (args.getBoolean(ARGS_SHOW_RESET_BTN)) {
                val resetMode = args.getInt(ARGS_SHOW_RESET_MODE)
                if (resetMode == ResetMode.SELECT_ALL &&
                    !args.getBoolean(ARGS_MULTIPLE_SELECTION_ENABLED)
                ) {
                    throw IllegalArgumentException("ResetMode.SELECT_ALL is not compatible with single selection mode")
                }
                binding.buttonReset.visibility = View.VISIBLE
                updateResetButtonState(resetMode)
                binding.buttonReset.setOnClickListener {
                    selectionAdapter.resetCheckedStates(resetMode)
                }
                selectionAdapter.registerAdapterDataObserver(object :
                    RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                        super.onItemRangeChanged(positionStart, itemCount)
                        updateResetButtonState(resetMode)
                    }
                })
            }
        }
    }

    private fun updateResetButtonState(@ResetMode resetMode: Int) {
        val checkedCount = items.count { it.isChecked }
        binding.buttonReset.isVisible =
            !((checkedCount == selectionAdapter.itemCount && resetMode == ResetMode.SELECT_ALL) ||
                    (checkedCount == 0 && resetMode == ResetMode.NO_SELECTION))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (parentFragment != null) {
            if (parentFragment is SheetSelectionListener) {
                parentFragment as SheetSelectionListener
            } else {
                throw RuntimeException("$parentFragment should implement SheetSelectionListener interface")
            }
        } else {
            if (context is SheetSelectionListener) {
                context
            } else {
                throw RuntimeException("$context should implement SheetSelectionListener interface")
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun updateSheetHeight(height: Int) {
        val view = binding.root.parent as View
        view.updateLayoutParams { this.height = height }
    }

    private val onItemSelectedListener = object : OnSheetItemClickListener {
        override fun onSheetItemClicked(
            clickedItem: SheetSelectionItem,
            adapterPosition: Int
        ) {
            if (arguments?.getBoolean(ARGS_MULTIPLE_SELECTION_ENABLED, false) == true) {
                clickedItem.isChecked = !clickedItem.isChecked
                selectionAdapter.notifyItemChanged(adapterPosition)
            } else {
                items.forEach { selectionItem ->
                    selectionItem.isChecked = false
                }
                clickedItem.isChecked = true
                listener?.onSheetItemsSelected(
                    items, listOf(clickedItem),
                    sheetSelectionTag!!
                )
                dismiss()
            }
        }
    }

    private fun enterSearchViewState() {
        toSearchableBottomSheetState()
        toggleSearchState(true)
    }

    private fun exitSearchViewState() {
        toggleSearchState(false)
    }

    private fun toSearchableBottomSheetState() {
        updateSheetHeight(MATCH_PARENT)
        (dialog as BottomSheetDialog).apply {
            previousSheetState = getStableSheetBehaviourState(behavior.state)
            behavior.state = STATE_EXPANDED
        }
    }

    /**
     * Returns only stable state with fallback to [STATE_COLLAPSED]
     */
    private fun getStableSheetBehaviourState(state: Int): Int {
        if (state == STATE_SETTLING || state == STATE_DRAGGING)
            return STATE_COLLAPSED
        return state
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
        (dialog as BottomSheetDialog).apply {
            behavior.isDraggable = !isSearchable
        }
    }

    private val onSearchQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(newText: String?): Boolean {
            selectionAdapter.search(newText)
            return true
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            selectionAdapter.search(query)
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
        outState.putBoolean(STATE_SEARCH, isSearchableState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { savedState ->
            previousSheetState = savedState.getInt(STATE_PREV_STATE)
            if (savedState.getBoolean(STATE_SEARCH)) {
                enterSearchViewState()
            }
        }
    }

    class Builder(private val sheetSelectionTag: String) {
        @StyleRes
        private var themeId: Int = R.style.Theme_SheetSelection
        private var title: String? = null
        private var items: List<SheetSelectionItem> = emptyList()

        private var searchNotFoundText: String? = null
        private var applyButtonText: String? = null

        private var searchEnabled: Boolean = false
        private var draggableIndicatorEnabled: Boolean = false
        private var closeButtonEnabled: Boolean = false
        private var resetButtonEnabled: Boolean = false
        private var multipleSelectionEnabled: Boolean = false

        @ResetMode
        private var resetMode: Int = ResetMode.NO_SELECTION

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

        fun enableDraggableIndicator(enable: Boolean) = apply {
            this.draggableIndicatorEnabled = enable
        }

        fun enableSearch(enable: Boolean) = apply {
            this.searchEnabled = enable
        }

        fun searchNotFoundText(text: String) = apply {
            this.searchNotFoundText = text
        }

        fun enableMultiSelection(enabled: Boolean) = apply {
            this.multipleSelectionEnabled = enabled
        }

        fun applyMultiSelectionButtonText(text: String) = apply {
            this.applyButtonText = text
        }

        fun enableCloseButton(enable: Boolean) = apply {
            this.closeButtonEnabled = enable
        }

        fun enableResetButton(
            enable: Boolean,
            @ResetMode resetMode: Int = ResetMode.NO_SELECTION
        ) = apply {
            this.resetButtonEnabled = enable
            this.resetMode = resetMode
        }

        fun build() = SheetSelection().apply {
            arguments = Bundle()
                .apply {
                    putInt(ARGS_THEME, this@Builder.themeId)
                    putString(ARGS_TITLE, this@Builder.title)
                    putParcelableArrayList(ARGS_ITEMS, ArrayList(this@Builder.items))
                    putBoolean(ARGS_SHOW_DRAGGED_INDICATOR, this@Builder.draggableIndicatorEnabled)
                    putBoolean(ARGS_SEARCH_ENABLED, this@Builder.searchEnabled)
                    putString(ARGS_SEARCH_NOT_FOUND_TEXT, this@Builder.searchNotFoundText)
                    putBoolean(ARGS_MULTIPLE_SELECTION_ENABLED, multipleSelectionEnabled)
                    putString(ARGS_APPLY_BUTTON_TEXT, applyButtonText)
                    putBoolean(ARGS_SHOW_CLOSE_BTN, closeButtonEnabled)
                    putBoolean(ARGS_SHOW_RESET_BTN, resetButtonEnabled)
                    putInt(ARGS_SHOW_RESET_MODE, resetMode)
                    putString(ARGS_TAG, this@Builder.sheetSelectionTag)
                }
        }

        fun show(manager: FragmentManager) {
            build().show(manager, TAG)
        }
    }

    @IntDef(
        ResetMode.SELECT_ALL,
        ResetMode.NO_SELECTION
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ResetMode {
        companion object {
            const val SELECT_ALL = 0
            const val NO_SELECTION = 1
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
        private const val ARGS_SHOW_RESET_BTN =
            "SheetSelection:ARGS_SHOW_RESET_BTN"
        private const val ARGS_SHOW_RESET_MODE =
            "SheetSelection:ARGS_SHOW_RESET_MODE"
        private const val ARGS_TAG = "SheetSelection:ARGS_TAG"

        private const val STATE_PREV_STATE = "STATE:PREV_STATE"
        private const val STATE_SEARCH = "STATE:SEARCH"

        private const val STICKY_BOTTOM_DISAPPEARING_ACCELERATE = 6
        private const val PEEK_HEIGHT_AUTO_RATIO_THRESHOLD = 1.1f
        private const val WIDE_SCREEN_PEEK_HEIGHT_RATIO = 0.85f
    }
}
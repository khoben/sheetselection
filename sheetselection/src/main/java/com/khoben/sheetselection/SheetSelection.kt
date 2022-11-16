package com.khoben.sheetselection

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StyleRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.khoben.sheetselection.databinding.DialogSheetSelectionBinding
import kotlin.math.roundToInt


class SheetSelection : BottomSheetDialogFragment() {

    private var _binding: DialogSheetSelectionBinding? = null
    private val binding get() = _binding!!

    private var sheetSelectionTag: String = EMPTY_TAG
    private var listener: SheetSelectionListener = SheetSelectionListener.NOOP

    private var searchableState: Boolean = false
    private var previousSheetState: Int = STATE_COLLAPSED

    // FrameLayout:@+id/design_bottom_sheet
    private lateinit var designBottomSheet: View

    private lateinit var items: List<SheetSelectionItem>
    private lateinit var selectionAdapter: SheetSelectionAdapter

    override fun getTheme(): Int = arguments?.getInt(ARGS_THEME) ?: super.getTheme()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (searchableState) {
                            exitSearchViewState()
                        } else {
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                })
            }
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

        designBottomSheet = binding.root.parent as View

        arguments?.let { args ->

            sheetSelectionTag = args.getString(ARGS_TAG) ?: EMPTY_TAG

            items = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                args.getParcelableArrayList(
                    ARGS_ITEMS,
                    SheetSelectionItem::class.java
                )!!
            else
                @Suppress("DEPRECATION")
                args.getParcelableArrayList(ARGS_ITEMS)!!

            if (args.getBoolean(ARGS_SHOW_DRAGGED_INDICATOR)) {
                binding.draggedIndicator.visibility = View.VISIBLE
            }

            if (args.getBoolean(ARGS_MULTIPLE_SELECTION_ENABLED)) {
                binding.doneButtonContainer.visibility = View.VISIBLE
                binding.doneButton.text =
                    args.getString(ARGS_MULTIPLE_SELECTION_BUTTON_TEXT, getString(R.string.apply))
                binding.doneButtonContainer.setOnClickListener {
                    listener.onSheetItemsSelected(
                        SheetSelectionEvent(
                            sheetSelectionTag,
                            items.filter { it.isChecked },
                            items
                        )
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
                binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(query: String): Boolean {
                        selectionAdapter.search(query)
                        return true
                    }

                    override fun onQueryTextSubmit(query: String): Boolean {
                        selectionAdapter.search(query)
                        return true
                    }
                })
            }

            with(binding.selectionItemList) {
                adapter = SheetSelectionAdapter(
                    source = items,
                    emptyText = args.getString(ARGS_SEARCH_NOT_FOUND_TEXT)
                        ?: getString(R.string.not_found),
                    onItemSelected = ::onItemSelected
                ).also { selectionAdapter = it }
                itemAnimator = null
                addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    private val SHADOW_ANIMATION_DURATION_MS: Long = 150L
                    private var shadowHeaderVisible: Boolean = false

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val showHeaderShadow = recyclerView.canScrollVertically(-1)
                        if (showHeaderShadow && !shadowHeaderVisible) {
                            binding.headerShadow.animate().cancel()
                            binding.headerShadow.animate().alpha(1f)
                                .setDuration(SHADOW_ANIMATION_DURATION_MS).start()
                            shadowHeaderVisible = true
                        } else if (!showHeaderShadow && shadowHeaderVisible) {
                            binding.headerShadow.animate().cancel()
                            binding.headerShadow.animate().alpha(0f)
                                .setDuration(SHADOW_ANIMATION_DURATION_MS).start()
                            shadowHeaderVisible = false
                        }
                    }
                })
            }
        }

        with(requireDialog() as BottomSheetDialog) {
            behavior.peekHeight = calcBottomSheetPeekHeight()
            val stickyButton: View = binding.doneButtonContainer
            if (arguments?.getBoolean(ARGS_MULTIPLE_SELECTION_ENABLED) == true) {
                behavior.addBottomSheetCallback(object : BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        updateBottomStickyView(
                            bottomSheet,
                            stickyButton,
                            slideOffset
                        )
                    }
                })
                // Initial sticky bottom placement
                designBottomSheet.post {
                    stickyButton.post {
                        updateBottomStickyView(designBottomSheet, stickyButton, 0f)
                    }
                }
            }
        }
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
        listener = SheetSelectionListener.NOOP
        super.onDetach()
    }

    private fun updateBottomStickyView(
        designBottomSheet: View,
        stickyBottomView: View,
        slideOffset: Float
    ) {
        // CoordinatorLayout:@+id/coordinator height
        val parentHeight: Float = (designBottomSheet.parent as View).measuredHeight.toFloat()
        stickyBottomView.y = if (slideOffset < 0) { // down
            parentHeight -
                    designBottomSheet.top - (STICKY_BOTTOM_DISAPPEARING_ACCELERATE * slideOffset + 1) *
                    stickyBottomView.measuredHeight
        } else { // up
            parentHeight - designBottomSheet.top - stickyBottomView.measuredHeight
        }
    }

    private fun updateSheetHeight(height: Int) {
        designBottomSheet.updateLayoutParams { this.height = height }
    }

    private fun onItemSelected(item: SheetSelectionItem, position: Int) {
        if (arguments?.getBoolean(ARGS_MULTIPLE_SELECTION_ENABLED, false) == true) {
            item.isChecked = !item.isChecked
            selectionAdapter.notifyItemChanged(position)
        } else {
            items.forEach { selectionItem ->
                selectionItem.isChecked = false
            }
            item.isChecked = true
            listener.onSheetItemsSelected(
                SheetSelectionEvent(sheetSelectionTag, listOf(item), items)
            )
            dismiss()
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
        this.searchableState = isSearchable
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
        outState.putBoolean(STATE_SEARCH, searchableState)
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
        private var multiSelectionButtonText: String? = null

        private var searchEnabled: Boolean = false
        private var draggableIndicatorEnabled: Boolean = false
        private var closeButtonEnabled: Boolean = false
        private var multipleSelectionEnabled: Boolean = false

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

        fun multiSelectionButtonText(text: String) = apply {
            this.multiSelectionButtonText = text
        }

        fun enableCloseButton(enable: Boolean) = apply {
            this.closeButtonEnabled = enable
        }

        fun build() = SheetSelection().apply {
            arguments = Bundle().apply {
                putInt(ARGS_THEME, this@Builder.themeId)
                putString(ARGS_TITLE, this@Builder.title)
                putParcelableArrayList(ARGS_ITEMS, ArrayList(this@Builder.items))
                putBoolean(ARGS_SHOW_DRAGGED_INDICATOR, this@Builder.draggableIndicatorEnabled)
                putBoolean(ARGS_SEARCH_ENABLED, this@Builder.searchEnabled)
                putString(ARGS_SEARCH_NOT_FOUND_TEXT, this@Builder.searchNotFoundText)
                putBoolean(ARGS_MULTIPLE_SELECTION_ENABLED, multipleSelectionEnabled)
                putString(ARGS_MULTIPLE_SELECTION_BUTTON_TEXT, multiSelectionButtonText)
                putBoolean(ARGS_SHOW_CLOSE_BTN, closeButtonEnabled)
                putString(ARGS_TAG, this@Builder.sheetSelectionTag)
            }
        }

        fun show(manager: FragmentManager) = build().show(manager, TAG)
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
        private const val ARGS_MULTIPLE_SELECTION_BUTTON_TEXT =
            "SheetSelection:ARGS_MULTIPLE_SELECTION_BUTTON_TEXT"
        private const val ARGS_SHOW_CLOSE_BTN =
            "SheetSelection:ARGS_SHOW_CLOSE_BTN"
        private const val ARGS_TAG = "SheetSelection:ARGS_TAG"

        private const val STATE_PREV_STATE = "STATE:PREV_STATE"
        private const val STATE_SEARCH = "STATE:SEARCH"

        private const val STICKY_BOTTOM_DISAPPEARING_ACCELERATE = 6
        private const val PEEK_HEIGHT_AUTO_RATIO_THRESHOLD = 1.1f
        private const val WIDE_SCREEN_PEEK_HEIGHT_RATIO = 0.85f
        private const val EMPTY_TAG = ""
    }
}
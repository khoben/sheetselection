package com.khoben.sheetselection.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.khoben.sheetselection.SheetSelection
import com.khoben.sheetselection.SheetSelectionItem
import com.khoben.sheetselection.SheetSelectionListener
import com.khoben.sheetselection.sample.databinding.ActivityMainBinding
import com.khoben.sheetselection.showSheetSelection

class MainActivity : AppCompatActivity(), SheetSelectionListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.content.showbs.setOnClickListener {
            showSheetSelection(SHEET_SELECTION_TAG) {
                title("Sheet Selection")
                items(List(25) { SheetSelectionItem("$it", "#$it") })
                enableDraggableIndicator(true)
                enableMultiSelection(true)
                enableResetButton(true, SheetSelection.ResetMode.SELECT_ALL)
                enableSearch(true)
                enableCloseButton(true)
                searchNotFoundText("Empty")
            }
        }
    }

    override fun onSheetItemsSelected(
        all: List<SheetSelectionItem>,
        selected: List<SheetSelectionItem>,
        sheetSelectionTag: String
    ) {
        if (sheetSelectionTag == SHEET_SELECTION_TAG) {
            Toast.makeText(
                this,
                "Selected: ${selected.joinToString { it.value }}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        private const val SHEET_SELECTION_TAG = "SAMPLE_TAG"
    }
}
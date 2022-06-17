package com.khoben.sheetselection.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.khoben.sheetselection.*
import com.khoben.sheetselection.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SheetSelectionListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.showbs.setOnClickListener {
            showSheetSelection(SHEET_SELECTION_TAG) {
                title("Sheet Selection")
                items(List(25) { SheetSelectionItem("$it", "#$it") })
                enableDraggableIndicator(true)
                enableMultiSelection(true)
                enableSearch(true)
                enableCloseButton(true)
                searchNotFoundText("Empty")
                multiSelectionButtonText("Select")
            }
        }
    }

    override fun onSheetItemsSelected(event: SheetSelectionEvent) {
        event.doIfMatches(SHEET_SELECTION_TAG) { selected ->
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
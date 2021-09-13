package com.khoben.sheetselection.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.khoben.sheetselection.SheetSelection
import com.khoben.sheetselection.SheetSelectionItem
import com.khoben.sheetselection.SheetSelectionListener
import com.khoben.sheetselection.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SheetSelectionListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.content.showbs.setOnClickListener {
            SheetSelection.Builder("SAMPLE")
                .title("Sheet Selection")
                .enableMultiSelection(true)
                .showCloseButton(true)
                .items(
                    listOf(
                        SheetSelectionItem("1", "#1"),
                        SheetSelectionItem("2", "#2"),
                        SheetSelectionItem("3", "#3"),
                        SheetSelectionItem("4", "#4"),
                        SheetSelectionItem("11", "#1"),
                        SheetSelectionItem("21", "#2"),
                        SheetSelectionItem("31", "#3"),
                        SheetSelectionItem("41", "#4"),
                        SheetSelectionItem("12", "#1"),
                        SheetSelectionItem("22", "#2"),
                        SheetSelectionItem("32", "#3"),
                        SheetSelectionItem("42", "#4"),
                        SheetSelectionItem("13", "#1"),
                        SheetSelectionItem("23", "#2"),
                        SheetSelectionItem("33", "#3"),
                        SheetSelectionItem("43", "#4"),
                        SheetSelectionItem("14", "#1"),
                        SheetSelectionItem("24", "#2"),
                        SheetSelectionItem("34", "#3"),
                        SheetSelectionItem("44", "#4"),
                        SheetSelectionItem("15", "#1"),
                        SheetSelectionItem("25", "#2"),
                        SheetSelectionItem("35", "#3"),
                        SheetSelectionItem("45", "#4"),
                        SheetSelectionItem("16", "#1"),
                        SheetSelectionItem("26", "#2"),
                        SheetSelectionItem("36", "#3"),
                        SheetSelectionItem("46", "#4"),
                    )
                )
                .showDraggedIndicator(true)
                .searchEnabled(true)
                .searchNotFoundText("Nothing!!")
                .show(supportFragmentManager)
        }
    }

    override fun onSheetItemsSelected(
        all: List<SheetSelectionItem>,
        selected: List<SheetSelectionItem>,
        tag: String?
    ) {
        if (tag == "SAMPLE") {
            Toast.makeText(
                this,
                "Selected: ${selected.joinToString { it.value }}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
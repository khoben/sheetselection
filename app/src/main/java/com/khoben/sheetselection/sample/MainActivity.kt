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
                .items(
                    List(25) { SheetSelectionItem("$it", "#$it") }
                )
                .enableMultiSelection(true)
                .showDraggedIndicator(true)
                .searchEnabled(true)
                .showCloseButton(true)
                .showResetButton(true, SheetSelection.ResetMode.SELECT_ALL)
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
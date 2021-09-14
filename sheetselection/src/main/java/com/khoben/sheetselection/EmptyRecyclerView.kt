package com.khoben.sheetselection

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EmptyRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var emptyView: View? = null

    fun setEmptyView(emptyView: View) {
        this.emptyView = emptyView
    }

    fun setEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            this.visibility = View.INVISIBLE
            emptyView?.visibility = View.VISIBLE
        } else {
            this.visibility = View.VISIBLE
            emptyView?.visibility = View.GONE
        }
    }
}
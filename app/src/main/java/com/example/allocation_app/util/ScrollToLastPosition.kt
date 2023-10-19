package com.example.allocation_app.util

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView

object ScrollToLastPosition {
    fun withDelay(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>, delayMillis: Long=1000) {

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            val lastPosition = adapter.itemCount - 1
            recyclerView.smoothScrollToPosition(lastPosition)
        }, delayMillis)
    }
}
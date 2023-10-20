package com.example.allocation_app.util

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView


/* Facilitar a visualizaçao quando se cria o item
*/

object ScrollToLastPosition {

    fun withDelay(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        delayMillis: Long = 1000
    ) {
        val lastPosition = adapter.itemCount
        if (lastPosition >= 0) {
            val handler = Handler(Looper.getMainLooper())

            handler.postDelayed({
                recyclerView.smoothScrollToPosition(lastPosition)
            }, delayMillis)
        } else {
            // Lide com a situação em que o adaptador está vazio
        }
    }
}
package com.example.allocation_app.adapter

import com.example.allocation_app.AdapterDefault
import com.example.allocation_app.model.Allocation

class AllocationAdapter (items: MutableList<Allocation>) : AdapterDefault<Allocation>(items) {
    override fun filterCondition(item: Allocation, query: String): Boolean {
        return item.dayOfWeek.contains(query, true)
    }
}


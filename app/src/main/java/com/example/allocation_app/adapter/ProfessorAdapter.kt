package com.example.allocation_app.adapter

import com.example.allocation_app.AdapterDefault
import com.example.allocation_app.model.Professor

class ProfessorAdapter (items: MutableList<Professor>) : AdapterDefault<Professor>(items) {
    override fun filterCondition(item: Professor, query: String): Boolean {
        return item.name.contains(query, true)
    }
}


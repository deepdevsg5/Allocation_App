package com.example.allocation_app.adapter

// CourseAdapter.kt
import com.example.allocation_app.AdapterDefault
import com.example.allocation_app.model.Department

class DepartmentAdapter(items: MutableList<Department>) : AdapterDefault<Department>(items) {
    override fun filterCondition(item: Department, query: String): Boolean {
        return item.name.contains(query, true)
    }
}

package com.example.allocation_app.adapter

// CourseAdapter.kt
import com.example.allocation_app.AdapterDefault
import com.example.allocation_app.model.Course

class CourseAdapter(items: MutableList<Course>) : AdapterDefault<Course>(items) {
    override fun filterCondition(item: Course, query: String): Boolean {
        return item.name.contains(query, true)
    }
}

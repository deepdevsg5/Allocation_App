package com.example.allocation_app.adapters

import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.allocation_app.AdapterDefault
import com.example.allocation_app.model.Course

class CourseAdapter(itens: MutableList<Course>):AdapterDefault<Course>(itens) {
    override fun filterCondition(item:Course,query: String):Boolean{
        return item.name.contains(query,true)
    }
}
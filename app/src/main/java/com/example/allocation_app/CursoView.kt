package com.example.allocation_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.model.Course
import com.example.allocation_app.model.testeShowInfoCourse

class CursoView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curso_view)

       val courses = testeShowInfoCourse()
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_registered)
        recyclerView.adapter = Adapter(courses)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}
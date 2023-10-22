package com.example.allocation_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.allocation_app.views.CursoView
import com.example.allocation_app.views.DepartmentView

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //troca tudo que for course por department
        val btCourse = findViewById<ImageButton>(R.id.ibCourse)
        btCourse.setOnClickListener {
            startNewActivity(CursoView::class.java)
        }

        val btDepartment = findViewById<ImageButton>(R.id.ibDepartment)
         btDepartment.setOnClickListener {
             startNewActivity(DepartmentView::class.java)
         }

    }

    // Função genérica para iniciar uma nova atividade
    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}
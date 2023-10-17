package com.example.allocation_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.allocation_app.views.CursoView
import com.example.allocation_app.views.DepartmentView
import com.example.allocation_app.views.ProfessorView

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btCourse = findViewById<ImageButton>(R.id.ibCourse)
        btCourse.setOnClickListener {
            startNewActivity(CursoView::class.java)
        }

        val btDepartment: ImageButton = findViewById(R.id.ibDepartment)
        btDepartment.setOnClickListener {
            startNewActivity(DepartmentView::class.java)
        }

        val btProfessor: ImageButton = findViewById(R.id.ibProfessor)
        btProfessor.setOnClickListener{
            startNewActivity(ProfessorView::class.java)
        }


    }

    // Função genérica para iniciar uma nova atividade
    private fun startNewActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}

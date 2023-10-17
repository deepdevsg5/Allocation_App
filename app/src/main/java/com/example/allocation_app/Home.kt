package com.example.allocation_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.allocation_app.views.CursoView

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btcourse = findViewById<ImageButton>(R.id.ibCourse)
        btcourse.setOnClickListener{
            val intent = Intent (this, CursoView::class.java)
            startActivity(intent)
       }

    }
}
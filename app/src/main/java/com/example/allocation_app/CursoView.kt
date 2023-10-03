package com.example.allocation_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CursoView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curso_view)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_registered)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val itens = listOf("teste 1", "Item 2", "Item 3") // Substitua isso com a lista de itens que você deseja exibir
        val meuAdaptador = MeuAdaptador(itens)
        recyclerView.adapter = meuAdaptador

    }
}
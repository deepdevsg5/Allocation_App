package com.example.allocation_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MeuAdaptador(val itens: List<String>) : RecyclerView.Adapter<MeuAdaptador.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tx_test_nomes) // Substitua com o ID correto do TextView no layout "teste_nomes"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.teste_nomes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.textView.text = item
    }

    override fun getItemCount(): Int {
        return itens.size
    }
}

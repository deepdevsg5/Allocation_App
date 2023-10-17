package com.example.allocation_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.model.Course

abstract class AdapterDefault<T : Any>(val itens: MutableList<T>):RecyclerView.Adapter<AdapterDefault<T>.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = itens.size

    abstract fun filterCondition(item:T,query:String):Boolean


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itens[position])
    }


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
          fun bind(itens: T){
             val txtFirstChar = itemView.findViewById<TextView>(R.id.text_first_char)
             val txtFieldId = itemView.findViewById<TextView>(R.id.text_field_id)
             val txtFieldName = itemView.findViewById<TextView>(R.id.text_field_name)

              when(itens){
               is Course -> {
                   val course = itens as Course
                   txtFirstChar.text = course.name.substring(0,1)
                   txtFieldId.text = course.id.toString()
                   txtFieldName.text = course.name
               }
              }
          }
    }


}

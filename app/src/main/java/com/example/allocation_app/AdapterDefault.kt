package com.example.allocation_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.model.Course

abstract class AdapterDefault<T : Any>(var itens: MutableList<T>):RecyclerView.Adapter<AdapterDefault<T>.ViewHolder>(){


    var filteredList = itens.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    fun setFilteredList(query: String): Boolean{
        filteredList.clear()
        filteredList.addAll(itens.filter { filterCondition(it,query) })
        notifyDataSetChanged()
        return filteredList.isEmpty()
    }

    fun clearSearch(){

        filteredList.clear()
        filteredList.addAll(itens)
        notifyDataSetChanged()

    }
    abstract fun filterCondition(item: T, query: String):Boolean
    fun reloadList(courses: List<T>){
        itens.clear()
        courses?.let { // ? igual a IF (se a lista de Cursos for vazia
            itens.addAll(it)

        }
        filteredList.clear()
             courses?.let {
                 filteredList.addAll(it)
             }
        notifyDataSetChanged()
    }



    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            fun bind(item: T){
                val txtFirstChar = itemView.findViewById<TextView>(R.id.text_first_char)
                val txtFieldId = itemView.findViewById<TextView>(R.id.text_field_id)
                val txtFieldName = itemView.findViewById<TextView>(R.id.text_field_name)

                when (item) {
                    is Course -> {
                        val course = item as Course
                        txtFirstChar.text = course.name.substring(0, 1)
                        txtFieldId.text = course.id.toString()
                        txtFieldName.text = course.name
                    }
                }
            }

    }


}

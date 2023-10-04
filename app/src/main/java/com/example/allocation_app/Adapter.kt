package com.example.allocation_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.model.Course

class Adapter(val courses: MutableList<Course>):RecyclerView.Adapter<Adapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = courses.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(courses[position])
    }


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
          fun bind(course: Course){
              with(course){
                  itemView.findViewById<TextView>(R.id.text_field_id).text= id.toString()
                  itemView.findViewById<TextView>(R.id.text_field_name).text= name
                  itemView.findViewById<TextView>(R.id.text_first_char).text= name.substring(0,1)
              }
          }
    }


}

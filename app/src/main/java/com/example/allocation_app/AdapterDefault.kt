package com.example.allocation_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.model.Allocation
import com.example.allocation_app.model.Course
import com.example.allocation_app.model.Department
import com.example.allocation_app.model.Professor

abstract class AdapterDefault<T : Any>(var itens: MutableList<T>):RecyclerView.Adapter<AdapterDefault<T>.ViewHolder>(){


    var filteredList = itens.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size




    var onItemClick : ((Int)-> Unit)? = null



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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(position)
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            fun bind(item: T){
                val txtFirstChar = itemView.findViewById<TextView>(R.id.txt_first_char)
                val txtFieldId = itemView.findViewById<TextView>(R.id.txt_fieldId)
                val txtFieldName = itemView.findViewById<TextView>(R.id.txt_fieldName)
                val txtFieldCpf = itemView.findViewById<TextView>(R.id.txt_fieldCpf)
                val txtFieldCourseid = itemView.findViewById<TextView>(R.id.txt_fieldCourse)
                val txtFieldProfessorId = itemView.findViewById<TextView>(R.id.txt_fieldProfessor)
                val txtFieldDepartmentid = itemView.findViewById<TextView>(R.id.txt_fieldDepartment)
                val txtFieldStartHour = itemView.findViewById<TextView>(R.id.txt_fieldHourStart)
                val txtFieldEndHour = itemView.findViewById<TextView>(R.id.txt_fieldHourEnd)


                when (item) {
                    is Course -> {
                        val course = item as Course
                        txtFirstChar.text = course.name.substring(0, 1)
                        txtFieldId.text = course.id.toString()
                        txtFieldName.text = course.name
                    }

                    is Department -> {
                        val department = item as Department
                        txtFirstChar.text = department.name.substring(0, 1)
                        txtFieldId.text = department.id.toString()
                        txtFieldName.text = department.name
                    }

                    is Professor -> {
                      val professor = item as Professor
                      txtFieldId.text = "id: ${professor.id.toString()}"
                      txtFieldName.text = professor.name
                      txtFirstChar.text = professor.name.substring(0,1)


                      txtFieldCpf.text = "CPF: ${professor.cpf.substring(0,3)}." +
                              "${professor.cpf.substring(3,6)}." +
                              "${professor.cpf.substring(6,9)}" +
                              "-${professor.cpf.substring(9,11)}"
                      txtFieldCpf.visibility = View.VISIBLE

                      txtFieldDepartmentid.text = "DPTO:${professor.departmentName}"
                      txtFieldDepartmentid.visibility  = View.VISIBLE

                  }

                    is Allocation ->{
                        val allocation = item as Allocation

                        txtFieldName.text = allocation.weekDay
                        txtFirstChar.text = allocation.weekDay.substring(0,1)

                        txtFieldCourseid.text = "Curso: ${allocation.courseName}"
                        txtFieldCourseid.visibility = View.VISIBLE

                        txtFieldProfessorId.text = "Prof: ${allocation.professorName}"
                        txtFieldProfessorId.visibility = View.VISIBLE

                        txtFieldStartHour.text =  "Inicio: ${allocation.startHour}"
                        txtFieldStartHour.visibility = View.VISIBLE

                        txtFieldEndHour.text =  "Termino: ${allocation.endhour}"
                        txtFieldEndHour.visibility = View.VISIBLE

                    }

                }

            }

    }


}

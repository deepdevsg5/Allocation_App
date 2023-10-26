package com.example.allocation_app.adapters
import com.example.allocation_app.AdapterDefault
import com.example.allocation_app.model.Department
import com.example.allocation_app.model.Professor

class ProfessorAdapter (itens: MutableList<Professor>): AdapterDefault<Professor>(itens){

    override fun filterCondition(item:Professor,query: String):Boolean{
        return item.name.contains(query,true)
    }

}
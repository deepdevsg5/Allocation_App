package com.example.allocation_app.adapters
import com.example.allocation_app.AdapterDefault
import com.example.allocation_app.model.Department

class DepartmentAdapter (itens: MutableList<Department>): AdapterDefault<Department>(itens){

    override fun filterCondition(item:Department,query: String):Boolean{
        return item.name.contains(query,true)
    }

}
package com.example.allocation_app.adapters
import com.example.allocation_app.AdapterDefault
import com.example.allocation_app.model.Allocation


class AllocationAdapter (itens: MutableList<Allocation>): AdapterDefault<Allocation>(itens){

    override fun filterCondition(item:Allocation,query: String):Boolean{
        return item.weekDay.contains(query,true)
    }

}
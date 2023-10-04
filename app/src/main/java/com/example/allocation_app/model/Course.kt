package com.example.allocation_app.model

data class Course(
    val id:Number,
    val name:String,
    val selected:Boolean= false

)

class CourseBuilder {
    var id: Number = 0
    var name: String =""


    fun build(): Course {
        return Course(id,name)
    }
}

fun showInfo(block: CourseBuilder.() -> Unit):Course =CourseBuilder().apply(block).build()

fun testeShowInfoCourse()= mutableListOf(
    showInfo {
        id=1
        name="Física"
    } ,
    showInfo {
        id=2
        name="Matemática"
    },
     showInfo {
        id=3
        name="Algoritmo"
    },
      showInfo {
        id=4
        name="Programação"
    }

)




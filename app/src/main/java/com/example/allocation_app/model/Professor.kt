package com.example.allocation_app.model

import com.google.gson.annotations.SerializedName

data class Professor(
    @SerializedName("id")
    val id:Int? = null,
    @SerializedName("name")
    val name:String  ,
    @SerializedName("cpf")
    val cpf:String,


    @SerializedName("department_id")
    val departmentId:Int ,

    var departmentName :String? = null


)

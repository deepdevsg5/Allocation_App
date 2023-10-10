package com.example.allocation_app.model

import com.google.gson.annotations.SerializedName

data class Course(
    @SerializedName("id")
    val id:Int? = null,
    @SerializedName("name")
    val name:String  ,
    @SerializedName("selected")
    val selected:Boolean?= null


)

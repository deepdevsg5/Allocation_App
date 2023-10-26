package com.example.allocation_app.model

import com.google.gson.annotations.SerializedName

data class Allocation(
    @SerializedName("id")
    val id:Int? = null,
    @SerializedName("week_day")
    val weekDay:String  ,
    @SerializedName("start_hour")
    val startHour:String,
    @SerializedName("end_hour")
    val endhour:String,

    @SerializedName("course_id")
    val courseId:Int ,

    var courseName :String? = null,

    @SerializedName("professor_id")
    val professor_id:Int ,

    var professorName :String? = null

)

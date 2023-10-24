package com.example.allocation_app.model

import com.google.gson.annotations.SerializedName

data class Allocation(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("week_day")
    val week_day: String,

    @SerializedName("professor_id")
    val professorId: Int?,

    @SerializedName("course_id")
    val courseId: Int?,

    @SerializedName("start_hour")
    val startHour: String,

    @SerializedName("end_hour")
    val hourEnd: String,

     // Campo para armazenar o nome dos campos
    var professorName: String? = null,

    var courseName: String? = null,

    ) {
    companion object
}

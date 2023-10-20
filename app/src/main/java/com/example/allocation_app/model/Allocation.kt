package com.example.allocation_app.model

import com.google.gson.annotations.SerializedName

data class Allocation(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("dayOfWeek")
    val dayOfWeek: String,

    @SerializedName("professorId")
    val professorId: Int? ,

    @SerializedName("courseId")
    val courseId: Int? ,

    @SerializedName("hourStart")
    val hourStart: String,

    @SerializedName("hourEnd")
    val hourEnd: String,

     // Campo para armazenar o nome dos campos
    var professorName: String? = null,

    var courseName: String? = null,

) {
    companion object
}

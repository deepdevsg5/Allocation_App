package com.example.allocation_app.model

import com.google.gson.annotations.SerializedName

data class Course(
    @SerializedName("id")
    val id: Int? = null, // Você pode manter essa propriedade se precisar dela em algum lugar da sua aplicação

    @SerializedName("name")
    val name: String,

    @SerializedName("selected")
    val selected: Boolean? = null
)


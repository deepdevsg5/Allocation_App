package com.example.allocation_app.model

import com.google.gson.annotations.SerializedName

data class Department(
    @SerializedName("id")
    var id: Int? = null, // Você pode manter essa propriedade se precisar dela em algum lugar da sua aplicação

    @SerializedName("name")
    var name: String,

    @SerializedName("selected")
    val selected: Boolean? = null
)


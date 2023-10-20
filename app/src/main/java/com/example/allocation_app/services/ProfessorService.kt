package com.example.allocation_app.services

import com.example.allocation_app.model.Professor
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProfessorService {

    @GET("professor/list")
    fun listAll(): Call<java.util.List<Professor>>

    @POST("professor/new")
    fun save(@Body professor: Professor): Call<Professor>

    @PUT("professor/update/{id}")
    fun update(
        @Path("id") id: Int,
        @Body professor: Professor
    ): Call<Professor>

    @DELETE("professor/{id}")
    fun deleteById(
        @Path("id") id: Int
    ): Call<Void>


}
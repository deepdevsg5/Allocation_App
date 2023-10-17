package com.example.allocation_app.services

import com.example.allocation_app.model.Professor
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProfessorService {

    @GET("professor/list")
    fun listAll(): Call<List<Professor>>

    @POST("professor/new")
    fun save(@Body professor: Professor): Call<Professor>



}
package com.example.allocation_app.services

import com.example.allocation_app.model.Course
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
// inclusao de novas requisições

interface CourseService {

    @GET("course/list")
    fun listAll(): Call<List<Course>>

    @POST("course/new")
    fun save(@Body course: Course): Call<Course>




    @DELETE("course/{id}")
    fun deleteById(@Path("id") id : Int): Call<Void>




}
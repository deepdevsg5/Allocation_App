package com.example.allocation_app.services

import com.example.allocation_app.model.Course
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface CourseService {

    @GET("course/list")
    fun listAll(): Call<List<Course>>

    @DELETE("course/{id}")
    fun deleteById(@Path("id") id : Int): Call<Void>




}
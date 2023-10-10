package com.example.allocation_app.services

import com.example.allocation_app.model.Course
import retrofit2.Call
import retrofit2.http.GET

interface CourseService {

    @GET("course/list")
    fun listAll(): Call<List<Course>>





}
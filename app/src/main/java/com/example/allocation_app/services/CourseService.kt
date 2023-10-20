package com.example.allocation_app.services
import com.example.allocation_app.model.Course;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CourseService {

    @GET("course/list")
    fun listAll(): Call<List<Course>>


    @POST("course/new")
    fun save(
        @Body course:Course
    ): Call<Course>

    @PUT("course/update/{id}")
    fun update(
        @Path("id") courseId: Int,
        @Body course: Course
    ): Call<Course>

    @DELETE("course/{id}")
    fun deleteById(
        @Path("id") id: Int
    ): Call<Void>




}
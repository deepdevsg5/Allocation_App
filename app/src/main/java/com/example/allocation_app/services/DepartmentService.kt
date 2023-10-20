package com.example.allocation_app.services
import com.example.allocation_app.model.Department

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DepartmentService {

    @GET("department/list")
    fun listAll(): Call<List<Department>>

    @POST("department/new")
    fun save(
        @Body department:Department
    ): Call<Department>

    @PUT("department/update/{id}")
    fun update(
        @Path("id") courseId: Int,
        @Body department: Department
    ): Call<Department>

    @DELETE("department/{id}")
    fun deleteById(
        @Path("id") id: Int
    ): Call<Void>




}
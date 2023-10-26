package com.example.allocation_app.services



import com.example.allocation_app.model.Allocation
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AllocationService {
    @GET("allocation/list")
    fun listAll(): Call<List<Allocation>>

    @POST("allocation/new")
    fun save(@Body allocation: Allocation): Call<Allocation>


    @PUT("allocation/update/{id}")
    fun update(
        @Path("id") allocationrId :Int,
        @Body allocation: Allocation
    ): Call<Allocation>

    @DELETE("allocation/{id}")
    fun deleteById(@Path("id") id : Int): Call<Void>


}
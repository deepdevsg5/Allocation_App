package com.example.allocation_app.config

import com.example.allocation_app.services.CourseService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitConfig {
  private lateinit var retrofit : Retrofit

  fun getUrl(){
       retrofit = Retrofit.Builder()
           .baseUrl("https://professor-allocation-node-git.onrender.com/")
           .addConverterFactory(GsonConverterFactory.create())
           .build()

  }
  fun courseService():CourseService{
      return retrofit.create(CourseService::class.java)
  }

}

package com.example.allocation_app.config
import com.example.allocation_app.services.CourseService
import com.example.allocation_app.services.DepartmentService
import com.example.allocation_app.services.ProfessorService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitConfig {

    private lateinit var retrofit: Retrofit

    fun getUrl(){
        retrofit = Retrofit.Builder()
            .baseUrl("https://professor-allocation-node-git.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun courseService(): CourseService {
        return retrofit.create(CourseService::class.java)

    }

    fun departmentService(): DepartmentService {
        return retrofit.create(DepartmentService::class.java)

    }

    fun professorService(): ProfessorService{
        return retrofit.create(ProfessorService::class.java)
    }



}
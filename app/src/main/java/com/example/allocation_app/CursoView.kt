package com.example.allocation_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.config.RetrofitConfig
import com.example.allocation_app.model.Course
import com.example.allocation_app.services.CourseService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CursoView : AppCompatActivity() {

    private lateinit var adapter: Adapter
    private lateinit var courseService: CourseService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curso_view)

        // Inicialize o Retrofit e o serviço
        RetrofitConfig.getUrl()
        courseService = RetrofitConfig.courseService()

        adapter = Adapter(mutableListOf())

        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_registered)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        addButtom.setOnClickListener {
            addCourse()
            recyclerView.scrollToPosition(0)
        }

        // Conectar o ItemTouchHelper ao RecyclerView
        val itemTouchHelperCallback = ItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Aqui você deve chamar a função para carregar os cursos da API
        loadCourses()
    }

    // Adicionar curso
    fun addCourse() {
        // Implemente a lógica para adicionar um novo curso à lista aqui
    }

    private fun deleteCourse(courseId: Int){
        val call = courseService.deleteById(courseId)
        call.enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
               if (response.isSuccessful) {
                   Toast.makeText(
                       applicationContext,
                       "Curso excluido com Sucesso.",
                       Toast.LENGTH_LONG
                   ).show()
               }

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar a requisição.",
                    Toast.LENGTH_LONG
                ).show()
            }

        })

    }

    // Implemente a classe ItemTouchHelperCallback para lidar com arrastar e excluir
    inner class ItemTouchHelperCallback : ItemTouchHelper.SimpleCallback(
        0, // Não estamos implementando arrastar, então definimos para 0
        ItemTouchHelper.LEFT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false // Não estamos implementando arrastar, então retornamos false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
           val position = viewHolder.adapterPosition
           val deletedCourse = adapter.courses[position]

           //Remove o curso do Recicleview
            adapter.courses.removeAt(viewHolder.adapterPosition)
            adapter.notifyItemRemoved(viewHolder.adapterPosition)

            // agora se consegue excluir o curso da API usando ID
            deletedCourse.id?.let { deleteCourse(it) }

        }
    }



    private fun loadCourses() {
        executeAsync(courseService.listAll(), object : Callback<List<Course>> {

            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful) {
                    val courses = response.body() // Obtenha os cursos da resposta
                    adapter.courses.addAll(

                   courses ?: emptyList()
                    ) // Atualize o adaptador com os cursos
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar Requisição.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    // Função genérica para fazer chamadas assíncronas
    private fun <T> executeAsync(call: Call<T>, callback: Callback<T>) {
        call.enqueue(callback)
    }

}
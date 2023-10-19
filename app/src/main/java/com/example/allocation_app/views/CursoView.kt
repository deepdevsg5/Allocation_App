package com.example.allocation_app.views

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.R
import com.example.allocation_app.adapters.CourseAdapter
import com.example.allocation_app.config.RetrofitConfig
import com.example.allocation_app.model.Course
import com.example.allocation_app.services.CourseService
import com.example.allocation_app.util.ScrollToLastPosition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.List


class CursoView : AppCompatActivity() {

    private lateinit var adapter: CourseAdapter
    private lateinit var courseService: CourseService
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_fields)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Cursos"

        // Inicialize o Retrofit e o serviço
        RetrofitConfig.getUrl()
        courseService = RetrofitConfig.courseService()

        // Inicialize o Adapter e o RecyclerView e atribua a recyclerView diretamente à propriedade da classe
        recyclerView = findViewById(R.id.recycler_view_registered) // Initialize the recyclerView here
        adapter = CourseAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Conectar o ItemTouchHelper ao RecyclerView
        val itemTouchHelperCallback = ItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // iniciar ferramenta de procura por nome
        initSearchView()

        // Aqui você deve chamar a função para carregar os cursos da API
        loadCourses()

        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        addButtom.setOnClickListener {
            showAddCourseDialog()

        }

        val findButton: FloatingActionButton = findViewById(R.id.fab_find)
        findButton.setOnClickListener {
            showIdLocationDialog()
        }

    }

    private fun showIdLocationDialog() {
        val view = layoutInflater.inflate(R.layout.layout_modal_find , null)
        val findIdTxt = view.findViewById<EditText>(R.id.txt_modal_find_id)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(view)
        dialogBuilder.setPositiveButton("Procurar"){ dialog, which ->
            val courseId = findIdTxt.text.toString().toIntOrNull()
            if (courseId != null){
                val position = findCoursePosition(courseId)
                if(position != -1){
                  findById(courseId)
                } else{
                   recyclerView.postDelayed({Toast.makeText(
                       applicationContext,
                       "Curso com ID $courseId Não Encontrado. ",
                       Toast.LENGTH_LONG
                   ).show()}, 400)
                }

            }

        }
        dialogBuilder.setNegativeButton("Cancelar", null)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()


    }

    private fun findById(courseId: Int) {
        try {
            val position = findCoursePosition(courseId)
            if(position != -1){
                recyclerView.smoothScrollToPosition(position)
            } else{
                Toast.makeText(
                    applicationContext,
                    "Curso com ID $courseId Não Encontrado. ",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(applicationContext, "Erro ao buscar o Curso", Toast.LENGTH_SHORT).show()
        }



    }

    private fun findCoursePosition(courseId: Int) : Int {
       for (i in adapter.itens.indices){
           if(adapter.itens[i].id == courseId){
               return i
           }
       }


        return -1 // O Curso não foi encontrado na lista

    }

    private fun initSearchView(){
        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if(query != null) {
                    val listIsEmpty = adapter.setFilteredList(query)
                    if(listIsEmpty){
                        Toast.makeText(applicationContext,"Curso Não Encontrado",Toast.LENGTH_LONG).show()
                    }
                } else{
                    adapter.clearSearch()
                }
                loadCourses()
                return true


            }

        })

    }

    // abre uma janela para interação com usuário
    private fun showAddCourseDialog(){
       val dialog = AlertDialog.Builder(this)
       val view = layoutInflater.inflate(R.layout.layout_modal_add_course, null)

        // abre o modal
        dialog.setView(view)

      // Botões - salvar e cancelar
        dialog.setPositiveButton("Adicionar") {_,_ ->
        //extrai os detalhes do curso do modal
        val courseName = view.findViewById<EditText>(R.id.txt_modal_add_name).text.toString()

        if (courseName.isNotBlank()){
            val newCourse = Course(name = courseName)
            addCourse(newCourse)
            ScrollToLastPosition.withDelay(recyclerView,adapter,2000)
        } else {
            Toast.makeText(
                applicationContext,
                "Nome de curso não pode estar Branco",
                Toast.LENGTH_LONG
            ).show()
        }


        }

        dialog.setNegativeButton("Cancelar",null)
        dialog.show()



    }





    // Adicionar curso
    fun addCourse(newCourse: Course) {
        // Implemente a lógica para adicionar um novo curso à lista aqui
        val call = courseService.save(newCourse)
        call.enqueue(object : Callback<Course> {
            override fun onResponse(call: Call<Course>, response: Response<Course>) {
                if (response.isSuccessful) {
                   Toast.makeText(
                       applicationContext,
                       "Curso adicionado com sucesso",
                       Toast.LENGTH_LONG
                   ).show()
                    loadCourses()
                }

            }

            override fun onFailure(call: Call<Course>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar a requisição.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
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
           val deletedCourse = adapter.filteredList[position]

           //Remove o curso do Recicleview
            adapter.itens.removeAt(viewHolder.adapterPosition)
            adapter.notifyItemRemoved(viewHolder.adapterPosition)

            // agora se consegue excluir o curso da API usando ID
            deletedCourse.id?.let { deleteCourse(it) }

        }
    }



    // Função para carregar cursos da API
    private fun loadCourses() {
        val call = courseService.listAll()
        call.enqueue( object : Callback<kotlin.collections.List<Course>>{
            override fun onResponse(
                call: Call<kotlin.collections.List<Course>>,
                response: Response<kotlin.collections.List<Course>>
            ) {
                val courses = response.body() // Obtenha os cursos da resposta

                if (courses != null) {
                    // Certifique-se de que courses não seja nulo
                    adapter.itens.clear()
                    adapter.itens.addAll(courses)// Adicionando os cursos carregados na lista filteredList durante a inicialização da tela, você garante que a lista completa de cursos seja exibida assim que a tela é carregada
                    adapter.filteredList.addAll(adapter.itens) // adiciona os cursos para poder ser consultado
                    adapter.notifyDataSetChanged()

                 }
            }

            override fun onFailure(call: Call<kotlin.collections.List<Course>>, t: Throwable) {
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





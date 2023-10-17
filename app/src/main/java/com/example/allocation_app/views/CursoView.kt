package com.example.allocation_app.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.AdapterDefault
import com.example.allocation_app.R
import com.example.allocation_app.adapters.CourseAdapter
import com.example.allocation_app.config.RetrofitConfig
import com.example.allocation_app.model.Course
import com.example.allocation_app.services.CourseService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CursoView : AppCompatActivity() {

    private lateinit var adapter: CourseAdapter
    private lateinit var courseService: CourseService
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_curso_view)

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


        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        addButtom.setOnClickListener {
            showAddCourseDialog()

        }

        // Aqui você deve chamar a função para carregar os cursos da API
        loadCourses()
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
            scrollToLastPositionWithDelay(recyclerView,adapter,2000)
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

 private fun scrollToLastPositionWithDelay(recyclerView: RecyclerView,adapter: RecyclerView.Adapter<*>,delayMillis: Long=1000){

     val handler = Handler(Looper.getMainLooper())
     handler.postDelayed({

         val lastPosition = adapter.itemCount -1
         recyclerView.smoothScrollToPosition(lastPosition)
     }, delayMillis)
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
           val deletedCourse = adapter.itens[position]

           //Remove o curso do Recicleview
            adapter.itens.removeAt(viewHolder.adapterPosition)
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
                    adapter.itens.addAll(

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




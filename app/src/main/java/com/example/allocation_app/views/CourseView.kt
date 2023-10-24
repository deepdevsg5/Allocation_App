package com.example.allocation_app.views

import android.os.Bundle
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
import com.example.allocation_app.adapter.CourseAdapter
import com.example.allocation_app.config.RetrofitConfig
import com.example.allocation_app.model.Course
import com.example.allocation_app.services.CourseService
import com.example.allocation_app.util.ScrollToLastPosition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.List


class CourseView : AppCompatActivity() {

    private lateinit var adapter: CourseAdapter
    private lateinit var courseService: CourseService
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_fields)

        //Muda o Titulo da ToolBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Cursos"

        // Inicialize o Retrofit e o serviço
        RetrofitConfig.getUrl()
        courseService = RetrofitConfig.courseService()

        // Inicialize o Adapter e o RecyclerView e atribua a recyclerView diretamente à propriedade da classe
        adapter = CourseAdapter(mutableListOf())
        recyclerView = findViewById(R.id.recycler_view_registered)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Conectar o ItemTouchHelper ao RecyclerView
        val itemTouchHelperCallback = ItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        //Tornar clicacel os itens do adpter
        adapter.onItemClick = { position ->
            val course = adapter.filteredList[position]
            course.id?.let { courseId ->
                showUpdateCourseDialog(course)
            }
        }

        // Carregar cursos da API
        loadCourses()

        // Inicializar o SearchView
        initiSearchView()

        val searchButtom: FloatingActionButton = findViewById(R.id.fab_find)
        searchButtom.setOnClickListener {
            showIdLocation()
        }

        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        addButtom.setOnClickListener {
            showAddCourseDialog()
        }


    }


    private fun showAddCourseDialog() {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_add, null)
        dialog.setView(view)
        val courseNameEditText = view.findViewById<EditText>(R.id.txt_add_name)

        dialog.setPositiveButton("Adicionar") { _, _ ->
            val courseName = courseNameEditText.text.toString()
            if (courseName.isNotBlank()) {
                val newCourse = Course(name = courseName)
                addCourse(newCourse)
                ScrollToLastPosition.withDelay(recyclerView,adapter,2000)
            } else {
                Toast.makeText(applicationContext, "Nome do curso não pode estar em branco", Toast.LENGTH_LONG).show()
            }
        }

        dialog.setNegativeButton("Cancelar", null)

        dialog.show()
    }

    private fun addCourse(newCourse: Course) {
        val call = courseService.save(newCourse)

        call.enqueue(object : Callback<Course> {
            override fun onResponse(call: Call<Course>, response: Response<Course>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Curso adicionado com sucesso.", Toast.LENGTH_LONG).show()
                    loadCourses()
                } else {
                    Toast.makeText(applicationContext, "Falha ao adicionar o curso.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Course>, t: Throwable) {
                Toast.makeText(applicationContext, "Falha ao executar Requisição.", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Função para carregar cursos da API
    private fun loadCourses() {
        val call = courseService.listAll()
        call.enqueue( object : Callback<List<Course>> {
            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful) {
                    val courses = response.body() // Obtenha os cursos da resposta

                    if (courses != null) {
                        adapter.reloadList(courses)
                    }
                }
            }

            override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar Requisição.",
                    Toast.LENGTH_LONG
                ).show()
                loadCourses()

            }
        })
    }

    // Novo método público para carregar cursos
    fun loadCoursesExternally(callback: (List<Course>?) -> Unit) {
        loadCourses()
    }


    private fun showIdLocation() {
        val view = layoutInflater.inflate(R.layout.layout_modal_find, null)
        val findIdTxt = view.findViewById<EditText>(R.id.txt_find_id)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(view)

        dialogBuilder.setPositiveButton("Procurar") { dialog, which ->
            val courseId = findIdTxt.text.toString().toIntOrNull()

            if (courseId != null) {
                val position = findCoursePosition(courseId)
                if (position != -1) {
                    recyclerView.smoothScrollToPosition(position)
                } else {
                    Toast.makeText(applicationContext, "Curso com ID $courseId não encontrado.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(applicationContext, "ID de curso inválido.", Toast.LENGTH_LONG).show()
            }
        }

        dialogBuilder.setNegativeButton("Cancelar", null)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun findCoursePosition(courseId: Int): Int {
        for (i in adapter.items.indices) {
            if (adapter.items[i].id == courseId) {
                return i
            }
        }
        return -1 // Retorna -1 se o curso não for encontrado na lista
    }

    //encontrar por Nome usando SearchView
    private fun initiSearchView() {
        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    val isEmpty = adapter.setFilteredList(query) // Filtra a lista no adaptador
                    if (isEmpty) {
                        Toast.makeText(
                            applicationContext,
                            "Curso não encontrado.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    //  adapter.notifyDataSetChanged()// Se a consulta estiver vazia, você pode realizar ações relevantes aqui
                    adapter.clearSearch()

                }
                return true
            }

        })


    }

    private fun showUpdateCourseDialog(course: Course) {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_update, null)
        dialog.setView(view)

        val updateName: EditText = view.findViewById(R.id.txt_att_Name)
        val idModalUpdate: TextView = view.findViewById(R.id.txt_att_id)

        idModalUpdate.text = course.id.toString()
        updateName.setText(course.name)

        dialog.setPositiveButton("Atualizar") { _, _ ->
            val newName = updateName.text.toString()

            if (newName.isNotBlank()) {
                /*
                Verifique se course.id não é nulo (usando a operação segura de chamada ?.).
                Se course.id não for nulo, execute o bloco de código dentro de { }.
                Dentro do bloco, it representa o valor de course.id (que não é nulo),
                e updateCourseName(it, newName) é chamado com esse valor.
                 */
                course.id?.let{ updateCourseName(it,newName)}

            } else {
                Toast.makeText(applicationContext, "Nome do curso não pode estar em branco", Toast.LENGTH_LONG).show()
            }
        }

        dialog.setNegativeButton("Cancelar", null)

        dialog.show()
    }

    private fun updateCourseName(courseId: Int, newName: String) {
        val call = courseService.update(courseId, Course(name = newName))

        call.enqueue(object : Callback<Course> {
            override fun onResponse(call: Call<Course>, response: Response<Course>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Nome do curso atualizado com sucesso.", Toast.LENGTH_LONG).show()
                    loadCourses()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(applicationContext, "Falha ao atualizar o nome do curso: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Course>, t: Throwable) {
                Toast.makeText(applicationContext, "Falha ao executar Requisição.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteCourse(courseId: Int, callback: (success: Boolean) -> Unit) {
        val call = courseService.deleteById(courseId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Curso Excluído com Sucesso.",
                        Toast.LENGTH_LONG
                    ).show()
                    callback(true) // Indica que a exclusão foi bem-sucedida
                } else {
                    // Trate o caso em que a exclusão falha
                    Toast.makeText(
                        applicationContext,
                        "Falha ao excluir o curso",
                        Toast.LENGTH_LONG
                    ).show()
                    callback(false) // Indica que a exclusão falhou
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Trate a falha na chamada à API
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar Requisição.",
                    Toast.LENGTH_LONG
                ).show()
                callback(false) // Indica que a exclusão falhou devido a uma falha na chamada à API
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
            val courseId = deletedCourse.id ?: -1 // Certifique-se de que courseId não seja nulo

            // Agora você pode excluir o curso da API usando o ID do curso excluído
            deleteCourse(courseId) { success ->
                if (success) {
                    // A exclusão foi bem-sucedida, você pode tomar ações adicionais, se necessário.
                    // Por exemplo, recarregar a lista de cursos após a exclusão.
                    adapter.filteredList.removeAt(viewHolder.adapterPosition) // Remove da filteredList
                    adapter.items.remove(deletedCourse) // Remove da lista original
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                } else {
                    // A exclusão falhou, você pode lidar com isso de acordo com as necessidades do seu aplicativo.
                    Toast.makeText(
                        applicationContext,
                        "Falha ao excluir o curso",
                        Toast.LENGTH_LONG
                    ).show()
                    // Como a exclusão não foi bem-sucedida, você pode precisar recarregar os cursos do servidor
                }
            }
        }

    }


}
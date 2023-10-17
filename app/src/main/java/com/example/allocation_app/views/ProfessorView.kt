package com.example.allocation_app.views

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
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
import com.example.allocation_app.adapter.ProfessorAdapter
import com.example.allocation_app.config.RetrofitConfig
import com.example.allocation_app.model.Department
import com.example.allocation_app.model.Professor
import com.example.allocation_app.services.DepartmentService
import com.example.allocation_app.services.ProfessorService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class ProfessorView : AppCompatActivity() {

    private lateinit var adapter: ProfessorAdapter
    private lateinit var professorService: ProfessorService
    private lateinit var recyclerView: RecyclerView
   // private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_fields)

        //Muda o Titulo da ToolBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Professores"



        // Inicialize o Retrofit e o serviço
        RetrofitConfig.getUrl()
        professorService = RetrofitConfig.professorService()

        // Inicialize o Adapter e o RecyclerView e atribua a recyclerView diretamente à propriedade da classe
        adapter = ProfessorAdapter(mutableListOf())
        recyclerView = findViewById(R.id.recycler_view_registered)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Conectar o ItemTouchHelper ao RecyclerView
       // val itemTouchHelperCallback = ItemTouchHelperCallback()
       // val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
       // itemTouchHelper.attachToRecyclerView(recyclerView)



        //Tornar clicacel os itens do adpter
        adapter.onItemClick = { position ->
            val professor = adapter.itens[position]
            professor.id?.let { professorId ->
               // showUpdateProfessorDialog(professorId, professor.name)
            }
        }

        // Carregar cursos da API
        loadProfessors()

        // Inicializar o SearchView
        //initiSearchView()

        val searchButtom: FloatingActionButton = findViewById(R.id.fab_find)
        searchButtom.setOnClickListener {
           // showIdLocation()
        }

        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        addButtom.setOnClickListener {
           // showAddProfessorDialog()
        }



    }


    private fun showAddProfessorDialog() {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_add, null)
        dialog.setView(view)

        // Configurar os elementos do modal, como EditTexts, botões, etc., para permitir ao usuário inserir os detalhes do curso.

        dialog.setPositiveButton("Adicionar") { _, _ ->
            // Extrair os detalhes do curso do modal
            val professorName = view.findViewById<EditText>(R.id.txt_add_name).text.toString()
            val cpf = view.findViewById<EditText>(R.id.txt_add_cpf).text.toString()
            val departmentIdText = view.findViewById<EditText>(R.id.txt_add_idDepartment).text.toString()
            val departmentId = departmentIdText.toIntOrNull()

            if (professorName.isNotBlank()) {
                val newProfessor =
                    departmentId?.let { Professor(name = professorName, cpf = cpf, departmentId = departmentId) }
                addDepartment(newProfessor) // Enviar o novo curso para o servidor usando o Retrofit
                scrollToLastPositionWithDelay(recyclerView, adapter, 2000)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Nome do Professor não pode estar em branco",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialog.setNegativeButton("Cancelar", null)

        dialog.show()
    }

    fun scrollToLastPositionWithDelay(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        delayMillis: Long = 1000 // Valor padrão de 1000 milissegundos (1 segundo)
    ) {
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            val lastPosition = adapter.itemCount - 1
            recyclerView.smoothScrollToPosition(lastPosition)
        }, delayMillis)
    }

    // Adicionar curso
    private fun addDepartment(newProfessor: Professor) {
        val call = professorService.save(newProfessor)


        call.enqueue(object : Callback<Professor> {
            override fun onResponse(call: Call<Professor>, response: Response<Professor>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Professor adicionado com sucesso.",
                        Toast.LENGTH_LONG
                    ).show()
                    //val addedCourse = response.body() // Curso adicionado retornado pelo servidor
                    // Atualize a UI ou faça qualquer ação necessária após adicionar o curso
                    adapter.filteredList.clear()
                    loadProfessors()

                }
            }

            override fun onFailure(call: Call<Professor>, t: Throwable) {
                // Lida com falhas na chamada à API, se necessário
            }
        })
    }

    // Função para carregar cursos da API
    private fun loadProfessors() {
        val call = professorService.listAll()
        call.enqueue(object : Callback<List<Professor>> {

            override fun onResponse(
                call: Call<List<Professor>>,
                response: Response<List<Professor>>
            ) {
                if (response.isSuccessful) {
                    val professors = response.body()
                    if (professors != null) {
                        val departmentIds = professors.map { it.departmentId } //Aqui estamos mapeando os IDs dos departamentos de cada professor e criando uma lista de chamadas para buscar informações do departamento.
                        val departmentCalls =
                            departmentIds.map { RetrofitConfig.departmentService().findById(it) } //Usamos o map para extrair os IDs dos departamentos de cada professor e criar uma lista de chamadas para buscar informações do departamento com base nesses IDs.

                        //estamos iterando pelas chamadas para buscar informações do departamento e,
                        // para cada chamada, definimos um callback para tratar a resposta.
                        departmentCalls.forEach { departmentCall ->
                            departmentCall.enqueue(object : Callback<Department> {
                                override fun onResponse(
                                    call: Call<Department>,
                                    response: Response<Department>
                                ) {
                                    if (response.isSuccessful) {
                                        val json = Gson().toJson(professors)
                                        Log.d("JSON Response", json)
                                        val department = response.body()
                                        department?.let { departmentResponse -> // Quando a resposta é bem-sucedida,
                                            // obtemos as informações do departamento da resposta.

                                            // Atualiza os professores com informações de departamento
                                            //Dentro deste bloco, estamos iterando por todos os professores
                                            // e verificando se o departmentId do professor corresponde
                                            // ao ID do departamento da resposta. Se houver correspondência,
                                            // atualizamos as propriedades departmentName e department do professor
                                            // com as informações do departamento.
                                            professors.forEach { professor ->
                                                if (professor.departmentId == departmentResponse.id) {
                                                    professor.departmentName = departmentResponse.name
                                                    professor.department = departmentResponse
                                                }
                                            }
                                        }

                                        // Verifique se todos os professores foram atualizados
                                        if (professors.all { it.departmentName != null && it.department != null }) {
                                            // Atualize o adaptador com a lista de professores atualizada
                                            adapter.itens.clear()
                                            adapter.itens.addAll(professors)
                                            adapter.filteredList.addAll(professors)
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<Department>, t: Throwable) {
                                    Toast.makeText(applicationContext,
                                              "Falha ao buscar informações do departamento.",
                                                   Toast.LENGTH_LONG).show()

                                }
                            })
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Professor>>, t: Throwable) {
                Toast.makeText(applicationContext,
                    "Falha ao buscar informações dos Professores.",
                    Toast.LENGTH_LONG).show()
            }
        })
    }





}
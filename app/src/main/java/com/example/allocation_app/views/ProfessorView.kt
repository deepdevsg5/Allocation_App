package com.example.allocation_app.views

import android.os.Bundle
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
import com.example.allocation_app.services.ProfessorService
import com.example.allocation_app.util.ScrollToLastPosition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.List

class ProfessorView : AppCompatActivity() {

    private lateinit var adapter: ProfessorAdapter
    private lateinit var professorService: ProfessorService
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_fields)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Professores"


        RetrofitConfig.getUrl()
        professorService = RetrofitConfig.professorService()

        // inicializar o adpter e o RecycleView e atribuir ã propriedade de classe
        adapter = ProfessorAdapter(mutableListOf())
        recyclerView = findViewById(R.id.recycler_view_registered)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Conectar o ItemTouchHelper ao RecyclerView
        val itemTouchHelperCallback = ItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)



        //Tornar clicacel os itens do adpter
        adapter.onItemClick = { position ->
            val professor = adapter.filteredList[position]
            professor.id?.let { professorId ->
                professor.departmentId?.let {
                    showUpdateProfessorDialog(professor)
                }
            }
        }

        // Carregar cursos da API
        loadProfessors()

        // Inicializar o SearchView
        initiSearchView()

        val searchButtom: FloatingActionButton = findViewById(R.id.fab_find)
        searchButtom.setOnClickListener {
           showIdLocation()
        }

        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        addButtom.setOnClickListener {
           showAddProfessorDialog()
        }



    }

    private fun showAddProfessorDialog() {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_add, null)
        view.findViewById<EditText>(R.id.txt_add_cpf).visibility = View.VISIBLE
        view.findViewById<EditText>(R.id.txt_add_idDepartment).visibility = View.VISIBLE
        dialog.setView(view)

        val professorName = view.findViewById<EditText>(R.id.txt_add_name).text.toString()
        val cpf = view.findViewById<EditText>(R.id.txt_add_cpf).text.toString()
        val departmentIdText = view.findViewById<EditText>(R.id.txt_add_idDepartment).text.toString()

        dialog.setPositiveButton("Adicionar") { _, _ ->
            val departmentId = departmentIdText.toIntOrNull()

            if (professorName.isNotBlank()) {
                val newProfessor = Professor(
                    name = professorName,
                    cpf = cpf,
                    departmentId = departmentId

                )
                addProfessor(newProfessor) // Enviar o novo curso para o servidor usando o Retrofit

                ScrollToLastPosition.withDelay(recyclerView,adapter,2000)
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

    // Adicionar curso
    private fun addProfessor(newProfessor: Professor) {
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
                    loadProfessors()


                }
            }

            override fun onFailure(call: Call<Professor>, t: Throwable) {
                // Lida com falhas na chamada à API, se necessário
            }
        })
    }

    /*
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
                            departmentIds.map { RetrofitConfig.departmentService().findById(it ?:0) } //Usamos o map para extrair os IDs dos departamentos de cada professor e criar uma lista de chamadas para buscar informações do departamento com base nesses IDs.

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
                                        Log.d("JSON Response", json) // log para ver se tud foi respondido pela API e se foi trocado os nomes dos departamentos
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
                                            adapter.reloadList(professors)
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<Department>, t: Throwable) {
                                    Toast.makeText(applicationContext,
                                              "Falha ao buscar informações dos Departamentos.",
                                                   Toast.LENGTH_LONG).show()
                                    loadProfessors()
                                }
                            })
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Professor>>, t: Throwable) {
                Toast.makeText(applicationContext,
                    "Falha ao buscar informações dos Professores.",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
*/


    //Tentativa de melhorar velocidade de requests dos profesores
    private fun loadProfessors() {
        val departmentsCall = RetrofitConfig.departmentService().listAll()
        departmentsCall.enqueue(object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {

                if (response.isSuccessful) {
                    val departments = response.body()
                    val json = Gson().toJson(departments)
                    Log.d("JSON Response", json)
                    loadProfessorsWithDepartments(departments)
                } else {
                    // Tratamento de erro para a chamada de departamentos
                    Toast.makeText(applicationContext, "Falha ao buscar informações de departamentos.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                // Tratamento de erro para a chamada de departamentos
                Toast.makeText(applicationContext, "Falha na requisição dos departamentos.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadProfessorsWithDepartments(departments: List<Department>?) {
        val professorsCall = professorService.listAll()
        professorsCall.enqueue(object : Callback<List<Professor>> {
            override fun onResponse(call: Call<List<Professor>>, response: Response<List<Professor>>) {
                if (response.isSuccessful) {
                    val professors = response.body()
                    val json = Gson().toJson(professors)
                    Log.d("JSON Response", json)
                    if (departments != null) {
                        val departmentsMap = departments.associateBy { it.id }
                        professors?.forEach { professor ->
                            val department = departmentsMap[professor.departmentId]
                            if (department != null) {
                                professor.departmentName = department.name
                                professor.department = department
                            }
                        }
                    }

                    adapter.reloadList(professors)
                } else {
                    // Tratamento de erro para a chamada de professores
                    Toast.makeText(applicationContext, "Falha ao buscar informações de professores.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Professor>>, t: Throwable) {
                // Tratamento de erro para a chamada de professores
                Toast.makeText(applicationContext, "Falha na requisição dos Professores..", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun loadProfessorsExternally(callback: (List<Professor>?) -> Unit){
        loadProfessors()
    }

    private fun showIdLocation() {
        val view = layoutInflater.inflate(R.layout.layout_modal_find, null)
        val editText = view.findViewById<EditText>(R.id.txt_find_id)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(view)
        dialogBuilder.setPositiveButton("Procurar") { dialog, which ->
            val professorId = editText.text.toString().toIntOrNull()

            if (professorId != null) {
                val position = findProfessorPosition(professorId)
                if (position != -1) {
                    recyclerView.smoothScrollToPosition(position)
                } else {
                    // Agende a exibição do Toast após um pequeno atraso
                    recyclerView.postDelayed({
                        Toast.makeText(
                            applicationContext,
                            "Professor com ID $professorId não encontrado.",
                            Toast.LENGTH_LONG
                        ).show()
                    }, 400) // 100 milliseconds de atraso

                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "ID de Professor inválido.",
                    Toast.LENGTH_LONG
                ).show()
                loadProfessors()
            }
        }

        dialogBuilder.setNegativeButton("Cancelar", null)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()


    }


    //Encontrar por Id com scroll ate a posicao
    private fun findProfessorPosition(professorId: Int): Int {
        for (i in adapter.items.indices) {
            if (adapter.items[i].id == professorId) {
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
                            "Professor não encontrado.",
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

    private fun showUpdateProfessorDialog(professor: Professor) {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_update, null)
        dialog.run {
            setView(view)
        }
        val updateName: EditText = view.findViewById(R.id.txt_att_Name)
        val updateCpf: EditText = view.findViewById(R.id.txt_att_cpf)
        val updateDepartmentId: EditText = view.findViewById(R.id.txt_att_idDepartment)
        val idModalUpdate: TextView = view.findViewById(R.id.txt_att_id)

        // Tornar visíveis os campos
        updateCpf.visibility = View.VISIBLE
        updateDepartmentId.visibility = View.VISIBLE

        // Use o objeto Professor para preencher os campos do diálogo
        idModalUpdate.text = professor.id.toString()
        updateName.setText(professor.name)
        updateCpf.setText(professor.cpf)
        updateDepartmentId.setText(professor.departmentId?.toString())

        dialog.setPositiveButton("Atualizar") { _, _ ->
            val newName = updateName.text.toString()
            val newCpf = updateCpf.text.toString()
            val newDepartmentIdtxt = updateDepartmentId.text.toString()
            val newDepartmentId = newDepartmentIdtxt.toIntOrNull()

            if (newName.isNotBlank()) {
                professor.id?.let { updateProfessor(it, newName, newCpf, newDepartmentId ?: 0) }
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

    // funcao para editar curso
    private fun updateProfessor(professorId: Int, newName: String, newCpf: String, newDepartmentId: Int) {
        val call = professorService.update(professorId, Professor(name = newName, cpf = newCpf, departmentId = newDepartmentId))

        call.enqueue(object : Callback<Professor> {
            override fun onResponse(call: Call<Professor>, response: Response<Professor>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Nome do Professor atualizado com sucesso.",
                        Toast.LENGTH_LONG
                    ).show()
                    loadProfessors()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UpdateProfessorError", "Erro ao atualizar o nome do Professor: $errorBody")
                    Toast.makeText(
                        applicationContext,
                        "Falha ao atualizar o nome do Professor.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Professor>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar Requisição.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun deleteProfessor(DepartmentId: Int, callback: (success: Boolean) -> Unit) {
        val call = professorService.deleteById(DepartmentId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Professor Excluído com Sucesso.",
                        Toast.LENGTH_LONG
                    ).show()
                    callback(true) // Indica que a exclusão foi bem-sucedida
                } else {
                    // Trate o caso em que a exclusão falha
                    Toast.makeText(
                        applicationContext,
                        "Falha ao excluir o Professor",
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
            val deletedProfessor = adapter.filteredList[position]
            val ProfessorId = deletedProfessor.id ?: -1 // Certifique-se de que courseId não seja nulo

            // Agora você pode excluir o curso da API usando o ID do curso excluído
            deleteProfessor(ProfessorId) { success ->
                if (success) {
                    // A exclusão foi bem-sucedida, você pode tomar ações adicionais, se necessário.
                    // Por exemplo, recarregar a lista de cursos após a exclusão.
                    adapter.filteredList.removeAt(viewHolder.adapterPosition) // Remove da filteredList
                    adapter.items.remove(deletedProfessor) // Remove da lista original
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                } else {
                    // A exclusão falhou, você pode lidar com isso de acordo com as necessidades do seu aplicativo.
                    Toast.makeText(
                        applicationContext,
                        "Falha ao excluir o Professor",
                        Toast.LENGTH_LONG
                    ).show()
                    // Como a exclusão não foi bem-sucedida, você pode precisar recarregar os cursos do servidor
                }
            }
        }

    }

}



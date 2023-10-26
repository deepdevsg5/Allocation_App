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
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.R
import com.example.allocation_app.adapters.ProfessorAdapter
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

        // Inicialize o Retrofit e o serviço
        RetrofitConfig.getUrl()
        professorService =
            RetrofitConfig.professorService() // Inicialize o Adapter e o RecyclerView
        //e atribua a recyclerView diretamente à propriedade da classe
        recyclerView =
            findViewById(R.id.recycler_view_registered) // Initialize the recyclerView here
        adapter = ProfessorAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // DELETE - arrastando-se o mouse ,Conecta o ItemTouchHelper ao RecyclerView
        val itemTouchHelperCallback = ItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)


        // UPDATE -habilitar o click nos itens do adpter
        adapter.onItemClick = { position ->
            val professor = adapter.filteredList[position]
            professor.id?.let { professorId ->
                showUpdateProfessorDialog(professor)
            }

        }

        loadProfessors()
        //POST
        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        addButtom.setOnClickListener {
            showAddProfessorDialog()
        }
        val findButton: FloatingActionButton = findViewById(R.id.fab_find)
        findButton.setOnClickListener {
            showIdLocationDialog()
        }

        // iniciar Consulta por Nome
        initSearchView()
        //fim da função oncreate
    }

    private fun showUpdateProfessorDialog(professor: Professor) {

        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_update, null)
        dialog.setView(view)

        // atribuir os campos do modal à função
        val updateName: EditText = view.findViewById(R.id.txt_modal_att_name)
        val idModalUpdate: TextView = view.findViewById(R.id.title_modal_att_id)
        val updateCpf : EditText = view.findViewById(R.id.txt_att_cpf)
        val updateDepartmentid : EditText = view.findViewById(R.id.txt_att_idDepartment)

        updateCpf.visibility = View.VISIBLE
        updateDepartmentid.visibility = View.VISIBLE



        //atribuir o id da classe , em vez de pegar o id do RecyclerView
        idModalUpdate.text = professor.id.toString()
        updateName.setText(professor.name)
        updateCpf.setText(professor.cpf)
        updateDepartmentid.setText(professor.departmentId.toString())


        dialog.setPositiveButton("Atualizar") { _, _ ->
            val newName = updateName.text.toString()
            val newCpf = updateCpf.text.toString()
            val newDepartmentidTxt = updateDepartmentid.text.toString()
            val newDepartmentid = newDepartmentidTxt.toIntOrNull()
            if (newName.isNotBlank()) {
                professor.id?.let { updateProfessorName(it, newName, newCpf, newDepartmentid?:0 ) }
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

    private fun updateProfessorName(professorId: Int, newName: String, newCpf: String, newDepartmentid: Int) {
        val call = professorService.update(professorId, Professor(name = newName , cpf = newCpf, departmentId = newDepartmentid))
        call.enqueue(object : Callback<Professor> {
            override fun onResponse(call: Call<Professor>, response: Response<Professor>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Nome do Professor Atualizado com Sucesso",
                        Toast.LENGTH_LONG
                    ).show()
                    loadProfessors()

                } else {
                    val erroBody = response.errorBody()?.string()
                    Toast.makeText(
                        applicationContext,
                        "Falha ao atualizar nome do Professor: $erroBody",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Professor>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar a requisição ",
                    Toast.LENGTH_LONG
                ).show()
            }

        })


    }

    private fun initSearchView() {

        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
                    adapter.clearSearch()

                }
                return true
            }

        })


    }

    //Procurar Professor por Id
    private fun showIdLocationDialog() {
        val view = layoutInflater.inflate(R.layout.layout_modal_find, null)
        val findIdTxt = view.findViewById<EditText>(R.id.txt_modal_find_id)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(view)

        dialogBuilder.setPositiveButton("Procurar") { dialog, which ->
            val professorId = findIdTxt.text.toString().toIntOrNull()

            if (professorId != null) {
                val position = findProfessorPosition(professorId)
                if (position != -1) {
                    recyclerView.smoothScrollToPosition(position)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Professor com ID $professorId não encontrado.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "ID de Professor inválido.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialogBuilder.setNegativeButton("Cancelar", null)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun findProfessorPosition(professorId: Int): Int {
        for (i in adapter.itens.indices) {
            if (adapter.itens[i].id == professorId) {
                return i
            }
        }


        return -1 // O Curso não foi encontrado na lista


    }

    private fun addProfessor(newProfessor: Professor) {
        val call = professorService.save(newProfessor)
        call.enqueue(object : Callback<Professor> {
            override fun onResponse(call: Call<Professor>, response: Response<Professor>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "professor adicionado com sucesso",
                        Toast.LENGTH_LONG
                    ).show()

                }

            }

            override fun onFailure(call: Call<Professor>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar a requisição.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun showAddProfessorDialog() {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_add, null)
        dialog.setView(view)
        val professorNameEditText = view.findViewById<EditText>(R.id.txt_modal_add_name)

        val professorCpf :EditText = view.findViewById(R.id.txt_add_cpf)
        val department_id : EditText =  view.findViewById(R.id.txt_add_idDepartment)

        //Tornar visivel os campos
        professorCpf.visibility = View.VISIBLE
        department_id.visibility = View.VISIBLE

        dialog.setPositiveButton("Adicionar") { _, _ ->
            // Extrair os detalhes do Professor do modal
            val professorName = professorNameEditText.text.toString()
            val cpfProfessor = professorCpf.text.toString()
            val department_idTxt = department_id.text.toString()
            val department_id = department_idTxt.toIntOrNull()
            if (professorName.isNotBlank()) {
                val newProfessor =
                    Professor(name = professorName, cpf = cpfProfessor , departmentId = department_id?:0) // Criar um objeto  com os detalhes inseridos
                addProfessor(newProfessor) // Enviar o novo professor para o servidor usando o Retrofit
                loadProfessors()
                ScrollToLastPosition.withDelay(recyclerView, adapter, 2000)
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

    //Botões Salvar e Cancelar


    private fun loadProfessors(){
        val departmentsCall = RetrofitConfig.departmentService().listAll()
        departmentsCall.enqueue(object :Callback<List<Department>> {
            override fun onResponse(
                call: Call<List<Department>>,
                response: Response<List<Department>>
            ) {
                if(response.isSuccessful) {
                    val departments = response.body()
                    // Mostrar o LOG no Terminal
                    val json = Gson().toJson(departments)
                    Log.d("JSON Response DPT", json)
                    loadProfessorsWithDepartments(departments)

                } else{
                    Toast.makeText(applicationContext,"Falha ao buscar informações dos Departamentos",Toast.LENGTH_SHORT ).show()
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(applicationContext,"Falha na Requisição dos Departamentos",Toast.LENGTH_LONG ).show()
            }

        })
    }

    private fun loadProfessorsWithDepartments(departments:List<Department>?) {
        val call = professorService.listAll()
        call.enqueue(object : Callback<List<Professor>> {
            override fun onResponse(
                call: Call<List<Professor>>,
                response: Response<List<Professor>>
            ) {
                if (response.isSuccessful) {
                    val professors = response.body()

                    val json = Gson().toJson(professors)
                    Log.d("JSON Response Prof",json)
                    if (departments != null) {
                        val departmentsMap = departments.associateBy { it.id }
                        professors?.forEach{professor ->
                        val department = departmentsMap[professor.departmentId]
                        if(department != null  ){
                            professor.departmentName = department.name

                        }

                        }
                        professors?.let { adapter.reloadList(it) }
                    } else{
                        Toast.makeText(applicationContext, "Não foi possível encontrar os departamentos dos professores", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onFailure(call: Call<kotlin.collections.List<Professor>>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar Requisição.",
                    Toast.LENGTH_LONG
                ).show()
                loadProfessors()
            }
        })


    }
    // Início a classe ProfessorView

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
            val professorId = deletedProfessor.id ?: -1 // Certifique-se de que courseId não seja nulo

            // Agora você pode excluir o curso da API usando o ID do curso excluído
            deleteProfessor(professorId) { success ->
                if (success) {
                    // A exclusão foi bem-sucedida, você pode tomar ações adicionais, se necessário.
                    // Por exemplo, recarregar a lista de cursos após a exclusão.
                    adapter.filteredList.removeAt(viewHolder.adapterPosition) // Remove da filteredList
                    adapter.itens.remove(deletedProfessor) // Remove da lista original
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                    loadProfessors()
                } else {
                    // A exclusão falhou, você pode lidar com isso de acordo com as necessidades do seu aplicativo.
                    Toast.makeText(
                        applicationContext,
                        "Falha ao excluir o Professor",
                        Toast.LENGTH_LONG
                    ).show()
                    // Como a exclusão não foi bem-sucedida, você pode precisar recarregar os cursos do servidor
                    loadProfessors()
                }
            }
        }

    }
    private fun deleteProfessor(ProfessorId: Int, callback: (success: Boolean) -> Unit) {
        val call = professorService.deleteById(ProfessorId)

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



}
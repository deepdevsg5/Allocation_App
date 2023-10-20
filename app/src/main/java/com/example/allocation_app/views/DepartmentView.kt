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
import com.example.allocation_app.adapter.DepartmentAdapter
import com.example.allocation_app.config.RetrofitConfig
import com.example.allocation_app.model.Department
import com.example.allocation_app.services.DepartmentService
import com.example.allocation_app.util.ScrollToLastPosition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.List


class DepartmentView : AppCompatActivity() {

    private lateinit var adapter: DepartmentAdapter
    private lateinit var departmentService: DepartmentService
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_fields)

        //Muda o Titulo da ToolBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Departamentos"

        // Inicialize o Retrofit e o serviço
        RetrofitConfig.getUrl()
        departmentService = RetrofitConfig.departmentService()

        // Inicialize o Adapter e o RecyclerView e atribua a recyclerView diretamente à propriedade da classe
        adapter = DepartmentAdapter(mutableListOf())
        recyclerView = findViewById(R.id.recycler_view_registered)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Conectar o ItemTouchHelper ao RecyclerView
        val itemTouchHelperCallback = ItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        //Tornar clicacel os itens do adpter
        adapter.onItemClick = { position ->
            val department = adapter.filteredList[position]
            department.id?.let { departmentId ->
                showUpdateDepartmentDialog(department)
            }
        }

        // Carregar cursos da API
        loadDepartments()

        // Inicializar o SearchView
        initiSearchView()

        val searchButtom: FloatingActionButton = findViewById(R.id.fab_find)
        searchButtom.setOnClickListener {
            showIdLocation()
        }

        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        addButtom.setOnClickListener {
            showAddDepartmentDialog()
        }


    }


    private fun showAddDepartmentDialog() {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_add, null)
        dialog.setView(view)
        val departmentNameEditText = view.findViewById<EditText>(R.id.txt_add_name)

        // Configurar os elementos do modal, como EditTexts, botões, etc., para permitir ao usuário inserir os detalhes do curso.

        dialog.setPositiveButton("Adicionar") { _, _ ->
            // Extrair os detalhes do department do modal
            val departmentName = departmentNameEditText.text.toString()
            if (departmentName.isNotBlank()) {
                val newDeparment = Department(name = departmentName) // Criar um objeto  com os detalhes inseridos
                addDepartment(newDeparment) // Enviar o novo curso para o servidor usando o Retrofit
                ScrollToLastPosition.withDelay(recyclerView,adapter,2000)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Nome do Departamento não pode estar em branco",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialog.setNegativeButton("Cancelar", null)

        dialog.show()
    }

    // Adicionar curso
    private fun addDepartment(newDepartment: Department) {
        val call = departmentService.save(newDepartment)

        call.enqueue(object : Callback<Department> {
            override fun onResponse(call: Call<Department>, response: Response<Department>) {
                if (response.isSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Departamento adicionado com sucesso.",
                            Toast.LENGTH_LONG
                        ).show()
                    //val addedCourse = response.body() // Curso adicionado retornado pelo servidor
                    // Atualize a UI ou faça qualquer ação necessária após adicionar o curso
                    loadDepartments()
                }
            }

            override fun onFailure(call: Call<Department>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar a Requisição"
                    ,Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    // Função para carregar cursos da API
    private fun loadDepartments() {
        val call = departmentService.listAll()
        call.enqueue( object : Callback<List<Department>> {
            override fun onResponse(call: Call<List<Department>>, response: Response<List<Department>>) {
                if (response.isSuccessful) {
                    val departments = response.body() // Obtenha os cursos da resposta

                    if (departments != null) {
                        adapter.reloadList(departments)
                    }
                }
            }

            override fun onFailure(call: Call<List<Department>>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar Requisição.",
                    Toast.LENGTH_LONG
                ).show()
                loadDepartments()
            }
        })
    }

    private fun showIdLocation() {
        val view = layoutInflater.inflate(R.layout.layout_modal_find, null)
        val editText = view.findViewById<EditText>(R.id.txt_find_id)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(view)

        dialogBuilder.setPositiveButton("Procurar") { dialog, which ->
            val departmentId = editText.text.toString().toIntOrNull()

            if (departmentId != null) {
                val position = findDepartmentPosition(departmentId)
                if (position != -1) {
                    recyclerView.smoothScrollToPosition(position)
                } else {
                    // Agende a exibição do Toast após um pequeno atraso
                    recyclerView.postDelayed({
                        Toast.makeText(
                            applicationContext,
                            "Departamento com ID $departmentId não encontrado.",
                            Toast.LENGTH_LONG
                        ).show()
                    }, 400) // 100 milliseconds de atraso

                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "ID de Departamento inválido.",
                    Toast.LENGTH_LONG
                ).show()
                loadDepartments()
            }
        }

        dialogBuilder.setNegativeButton("Cancelar", null)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()


    }


    private fun findDepartmentPosition(departmentId: Int): Int {
        for (i in adapter.items.indices) {
            if (adapter.items[i].id == departmentId) {
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
                            "Departamento não encontrado.",
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


    private fun showUpdateDepartmentDialog(department: Department) {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_update, null)
        dialog.run {
            setView(view)
        }

        val updateName: EditText = view.findViewById(R.id.txt_att_Name)
        val idModalUpdate: TextView = view.findViewById(R.id.txt_att_id)

        idModalUpdate.text = department.id.toString()
        updateName.setText(department.name)

        dialog.setPositiveButton("Atualiza") {_,_ ->
            val newName = updateName.text.toString()
            if (newName.isNotBlank()) {
                department.id?.let { updateDepartmentName(it, newName) }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Nome do Departamento não pode estar em branco",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialog.setNegativeButton("Cancelar", null)

        dialog.show()

    }

    // funcao para editar curso
    private fun updateDepartmentName(departmentId: Int, newName: String) {
        val call = departmentService.update(departmentId, Department(name = newName))

        call.enqueue(object : Callback<Department> {
            override fun onResponse(call: Call<Department>, response: Response<Department>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Nome do Departamento atualizado com sucesso.",
                        Toast.LENGTH_LONG
                    ).show()
                    loadDepartments()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UpdateDepartmentError", "Erro ao atualizar o nome do Departamento: $errorBody")
                    Toast.makeText(
                        applicationContext,
                        "Falha ao atualizar o nome do Departamento.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<Department>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar Requisição.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }




    private fun deleteDepartment(DepartmentId: Int, callback: (success: Boolean) -> Unit) {
        val call = departmentService.deleteById(DepartmentId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Departamento Excluído com Sucesso.",
                        Toast.LENGTH_LONG
                    ).show()
                    callback(true) // Indica que a exclusão foi bem-sucedida
                } else {
                    // Trate o caso em que a exclusão falha
                    Toast.makeText(
                        applicationContext,
                        "Falha ao excluir o Departamento",
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
            val deletedDepartment = adapter.filteredList[position]
            val DepartmentId = deletedDepartment.id ?: -1 // Certifique-se de que courseId não seja nulo

            // Agora você pode excluir o curso da API usando o ID do curso excluído
            deleteDepartment(DepartmentId) { success ->
                if (success) {
                    // A exclusão foi bem-sucedida, você pode tomar ações adicionais, se necessário.
                    // Por exemplo, recarregar a lista de cursos após a exclusão.
                    adapter.filteredList.removeAt(viewHolder.adapterPosition) // Remove da filteredList
                    adapter.items.remove(deletedDepartment) // Remove da lista original
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                    loadDepartments()
                } else {
                    // A exclusão falhou, você pode lidar com isso de acordo com as necessidades do seu aplicativo.
                    Toast.makeText(
                        applicationContext,
                        "Falha ao excluir o Departamento",
                        Toast.LENGTH_LONG
                    ).show()
                    // Como a exclusão não foi bem-sucedida, você pode precisar recarregar os cursos do servidor
                    loadDepartments()
                }
            }
        }

    }


}
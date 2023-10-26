package com.example.allocation_app.views

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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
import com.example.allocation_app.adapters.AllocationAdapter
import com.example.allocation_app.config.RetrofitConfig
import com.example.allocation_app.model.Allocation
import com.example.allocation_app.model.Course
import com.example.allocation_app.model.Professor
import com.example.allocation_app.services.AllocationService
import com.example.allocation_app.util.ScrollToLastPosition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar


class AllocationView : AppCompatActivity() {
    private lateinit var adapter: AllocationAdapter
    private lateinit var allocationService: AllocationService
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_fields)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Alocações"


        // Inicialize o Retrofit e o serviço
        RetrofitConfig.getUrl()
        allocationService =
            RetrofitConfig.allocationService() // Inicialize o Adapter e o RecyclerView
        //e atribua a recyclerView diretamente à propriedade da classe
        recyclerView =
            findViewById(R.id.recycler_view_registered) // Initialize the recyclerView here
        adapter = AllocationAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // DELETE - arrastando-se o mouse ,Conecta o ItemTouchHelper ao RecyclerView
        val itemTouchHelperCallback = ItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)


        // UPDATE -habilitar o click nos itens do adpter
        adapter.onItemClick = { position ->
            val allocation = adapter.filteredList[position]
            allocation.id?.let { allocationId ->
                showUpdateAllocationDialog(allocation)
            }

        }

        loadAllocations()
        //POST
        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        addButtom.setOnClickListener {
            showAddAllocationDialog()
        }
        val findButton: FloatingActionButton = findViewById(R.id.fab_find)
        findButton.setOnClickListener {
            showIdLocationDialog()
        }

        // iniciar Consulta por Nome
        initSearchView()
        //fim da função oncreate
    }


    private fun loadAllocations() {
        // Chame a API para carregar a lista de cursos
        val coursesCall = RetrofitConfig.courseService().listAll()

        val courseList = mutableListOf<Course>()
        val professorList = mutableListOf<Professor>()

        coursesCall.enqueue(object : Callback<List<Course>> {
            override fun onResponse(call: Call<List<Course>>, response: Response<List<Course>>) {
                if (response.isSuccessful) {
                    val courses = response.body()
                    val json = Gson().toJson(courses)
                    Log.d("JSON Response Course Api", json)
                    if (courses != null) {
                        courseList.clear()
                        courseList.addAll(courses)
                        val jsonCourseList = Gson().toJson(courseList)
                        Log.d("JSON Response Course List", jsonCourseList)
                        // Após carregar a lista de cursos, chame a função para carregar alocações

                        val professorsCall = RetrofitConfig.professorService().listAll()
                        professorsCall.enqueue(object : Callback<List<Professor>> {
                            override fun onResponse(
                                call: Call<List<Professor>>,
                                response: Response<List<Professor>>
                            ) {
                                if (response.isSuccessful) {
                                    val professors = response.body()
                                    val json = Gson().toJson(professors)
                                    Log.d("JSON Response Professor API", json)
                                    if (professors != null) {
                                        professorList.clear()
                                        professorList.addAll(professors)

                                        val jsonProfessorList = Gson().toJson(courseList)
                                        Log.d("JSON Response Professor List", jsonProfessorList)


                                        loadAllocationsWithProfessorCourseNames(
                                            courseList,
                                            professorList
                                        )
                                    }
                                }
                            }

                            override fun onFailure(call: Call<List<Professor>>, t: Throwable) {
                                Toast.makeText(
                                    applicationContext,
                                    "Falha na requisição de professores.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadAllocations()
                            }

                        })

                    }
                } else {
                    // Tratamento de erro para a chamada de cursos
                    Toast.makeText(
                        applicationContext,
                        "Falha ao buscar informações de cursos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                // Tratamento de erro para a chamada de cursos
                Toast.makeText(applicationContext, "Falha na requisição.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun loadAllocationsWithProfessorCourseNames(
        courseList: MutableList<Course>,
        professorList: MutableList<Professor>
    ) {
        val allocationsCall = allocationService.listAll()
        allocationsCall.enqueue(object : Callback<List<Allocation>> {
            override fun onResponse(
                call: Call<List<Allocation>>,
                response: Response<List<Allocation>>
            ) {
                if (response.isSuccessful) {
                    val allocations = response.body()

                    val json = Gson().toJson(allocations)
                    Log.d("JSON Response Prof", json)
                    if (allocations != null) {
                        allocations.forEach { allocation ->

                            val course = courseList.find { it.id == allocation.courseId }
                            if (course != null) {
                                allocation.courseName = course.name
                            }
                            val professor = professorList.find { it.id == allocation.professor_id }
                            if (professor != null) {
                                allocation.professorName = professor.name

                            }

                        }
                        adapter.reloadList(allocations)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Não foi possível encontrar os departamentos dos professores",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<kotlin.collections.List<Allocation>>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar Requisição.",
                    Toast.LENGTH_LONG
                ).show()
                loadAllocations()
            }
        })


    }


    private fun showUpdateAllocationDialog(allocation: Allocation) {

        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_update, null)
        dialog.setView(view)

        // atribuir os campos do modal à função
        val updateName: EditText = view.findViewById(R.id.txt_modal_att_name)
        updateName.visibility = View.GONE

        val updateCpf: EditText = view.findViewById(R.id.txt_att_cpf)
        updateCpf.visibility = View.GONE
        val updateAllocationid: EditText = view.findViewById(R.id.txt_att_idDepartment)
        updateAllocationid.visibility = View.GONE

        //configurar o Spinner dos dias da semana
        val daysOfWeek = arrayOf(
            "Domingo",
            "Segunda",
            "Terça",
            "Quarta",
            "Quinta",
            "Sexta",
            "Sábado"
        )// criar um array de dias
        val spinnerDayOfWeek =
            view.findViewById<Spinner>(R.id.att_spinnerDayOfWeek) // acessa aonde eles esta la no modal add
        spinnerDayOfWeek.visibility = View.VISIBLE
        val adapterSpin = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            daysOfWeek
        )//tem que criar um adaptador para preencher o spinner
        adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) //existem layouts padroes no android que ajudam a nao ficar criando toda vez
        spinnerDayOfWeek.adapter = adapterSpin // aqui vai preencher com os dados

        //Configurar a chamada do TimePicker
        val updateTxtHourStart = view.findViewById<EditText>(R.id.txt_att_hourStart)
        val updateTxtHourEnd = view.findViewById<EditText>(R.id.txt_att_hourEnd)

        // Clique no campo txt_add_hourStart para exibir o TimePickerDialog
        updateTxtHourStart.setOnClickListener {
            showTimePickerDialog(updateTxtHourStart)
        }

        // Clique no campo txt_add_hourEnd para exibir o TimePickerDialog
        updateTxtHourEnd.setOnClickListener {
            showTimePickerDialog(updateTxtHourEnd)
        }

        val updateCourse: EditText = view.findViewById(R.id.txt_att_idCurso)
        val updateProfessor: EditText = view.findViewById(R.id.txt_att_idProfessor)
        val idModalUpdate: TextView = view.findViewById(R.id.title_modal_att_id)

        // Tornar visíveis os campos
        updateCourse.visibility = View.VISIBLE
        updateProfessor.visibility = View.VISIBLE
        updateTxtHourStart.visibility = View.VISIBLE
        updateTxtHourEnd.visibility = View.VISIBLE

        dialog.setPositiveButton("Atualizar") { _, _ ->
            val selectedDay = spinnerDayOfWeek.selectedItem.toString()
            val newCourseIdtxt = updateCourse.text.toString()
            val newCourseId = newCourseIdtxt.toIntOrNull()
            val newProfessorIdtxt = updateProfessor.text.toString()
            val newProfessorId = newProfessorIdtxt.toIntOrNull()
            val newHourStart = updateTxtHourStart.text.toString()
            val newHourEnd = updateTxtHourEnd.text.toString()

            if (newCourseId != null && newProfessorId != null &&
                newHourStart.isNotBlank() && newHourEnd.isNotBlank()
            ) {

                allocation.id?.let {
                    updateAllocation(
                        it,
                        selectedDay,
                        newCourseId,
                        newProfessorId,
                        newHourStart,
                        newHourEnd
                    )
                }

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
    private fun updateAllocation(
        AllocationId: Int,
        selectedDay: String,
        newCourseId: Int,
        newProfessorId: Int,
        newHourStart: String,
        newHourEnd: String ) {

        val call = allocationService.update(AllocationId, Allocation( weekDay = selectedDay,
            courseId = newCourseId,
             professor_id = newProfessorId,
            startHour = newHourStart,
            endhour = newHourEnd ))

        call.enqueue(object : Callback<Allocation> {
            override fun onResponse(call: Call<Allocation>, response: Response<Allocation>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Allocação atualizada com sucesso.",
                        Toast.LENGTH_LONG
                    ).show()
                    loadAllocations()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UpdateAllocationError", "Erro ao atualizar a Alocação: $errorBody")
                    Toast.makeText(
                        applicationContext,
                        "Falha ao atualizar a alocação.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            override fun onFailure(call: Call<Allocation>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar Requisição.",
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

    //Procurar Alocação por Id
    private fun showIdLocationDialog() {
        val view = layoutInflater.inflate(R.layout.layout_modal_find, null)
        val findIdTxt = view.findViewById<EditText>(R.id.txt_modal_find_id)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(view)

        dialogBuilder.setPositiveButton("Procurar") { dialog, which ->
            val allocationId = findIdTxt.text.toString().toIntOrNull()
            if (allocationId != null) {
                val position = findAllocationPosition(allocationId)
                if (position != -1) {
                    recyclerView.smoothScrollToPosition(position)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Professor com ID $allocationId não encontrado.",
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

    private fun findAllocationPosition(allocationId: Int): Int {
        for (i in adapter.itens.indices) {
            if (adapter.itens[i].id == allocationId) {
                return i
            }
        }


        return -1 // O Curso não foi encontrado na lista


    }


    private fun showAddAllocationDialog() {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_add, null)

        //esconde o campo nome do layout
        view.findViewById<EditText>(R.id.txt_modal_add_name).visibility = View.GONE
        view.findViewById<EditText>(R.id.txt_add_idDepartment).visibility = View.GONE

        //configurar o Spinner dos dias da semana
        val daysOfWeek = arrayOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")// criar um array de dias
        val spinnerDayOfWeek = view.findViewById<Spinner>(R.id.spinnerDayOfWeek) // acessa aonde eles esta la no modal add
        spinnerDayOfWeek.visibility = View.VISIBLE
        val adapterSpin = ArrayAdapter(this, android.R.layout.simple_spinner_item, daysOfWeek)//tem que criar um adaptador para preencher o spinner
        adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) //existem layouts padroes no android que ajudam a nao ficar criando toda vez
        spinnerDayOfWeek.adapter = adapterSpin // aqui vai preencher com os dados

        //Mostra os campos no layout

        view.findViewById<EditText>(R.id.txt_add_idCourse).visibility = View.VISIBLE
        view.findViewById<EditText>(R.id.txt_add_idProfessor).visibility = View.VISIBLE
        view.findViewById<EditText>(R.id.txt_add_hourStart).visibility = View.VISIBLE
        view.findViewById<EditText>(R.id.txt_add_hourEnd).visibility = View.VISIBLE

        val courseIdText: EditText =  view.findViewById(R.id.txt_add_idCourse)
        val professorIdText :EditText =  view.findViewById(R.id.txt_add_idProfessor)


        //Configurar a chamada do TimePicker
        val txtHourStart = view.findViewById<EditText>(R.id.txt_add_hourStart)
        val txtHourEnd = view.findViewById<EditText>(R.id.txt_add_hourEnd)

        // Clique no campo txt_add_hourStart para exibir o TimePickerDialog
        txtHourStart.setOnClickListener {
            showTimePickerDialog(txtHourStart)
        }

        // Clique no campo txt_add_hourEnd para exibir o TimePickerDialog
        txtHourEnd.setOnClickListener {
            showTimePickerDialog(txtHourEnd)
        }


        dialog.setView(view)

        dialog.setPositiveButton("Adicionar") { _, _ ->
            val selectedDay = spinnerDayOfWeek.selectedItem.toString()
            val courseid = courseIdText.text.toString().toIntOrNull()
            val professorid = professorIdText.text.toString().toIntOrNull()
            val selectHourStart = txtHourStart.text.toString()
            val selectHourEnd = txtHourEnd.text.toString()

            if(txtHourEnd.text.isNotBlank() && txtHourStart.text.isNotBlank()){
                val newAllocation = Allocation(
                    weekDay = selectedDay,
                    professor_id = professorid?:0,
                    courseId = courseid?:0,
                    startHour = selectHourStart,
                    endhour = selectHourEnd)

                addAllocation(newAllocation) // Enviar o novo curso para o servidor usando o Retrofit
                loadAllocations()
                ScrollToLastPosition.withDelay(recyclerView, adapter ,2500)
            }else {
                Toast.makeText(
                    applicationContext,
                    "Todos os campos devem ser preenchidos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialog.setNegativeButton("Cancelar", null)

        dialog.show()
    }

    //Pegar o tempo com o relogio
    private fun showTimePickerDialog(editText: EditText) {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                editText.setText(selectedTime)
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    private fun addAllocation(newAllocation: Allocation) {
        val call = allocationService.save(newAllocation)
        call.enqueue(object : Callback<Allocation> {
            override fun onResponse(call: Call<Allocation>, response: Response<Allocation>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Alocação adicionada com sucesso",
                        Toast.LENGTH_LONG
                    ).show()
                    loadAllocations()
                }

            }

            override fun onFailure(call: Call<Allocation>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Falha ao executar a requisição.",
                    Toast.LENGTH_LONG
                ).show()
            }


        })
    }


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
            val deletedAllocation = adapter.filteredList[position]
            val allocationId = deletedAllocation.id ?: -1 // Certifique-se de que courseId não seja nulo

            // Agora você pode excluir o curso da API usando o ID do curso excluído
            deleteAllocation(allocationId) { success ->
                if (success) {
                    // A exclusão foi bem-sucedida, você pode tomar ações adicionais, se necessário.
                    // Por exemplo, recarregar a lista de cursos após a exclusão.
                    adapter.filteredList.removeAt(viewHolder.adapterPosition) // Remove da filteredList
                    adapter.itens.remove(deletedAllocation) // Remove da lista original
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                    loadAllocations()
                } else {
                    // A exclusão falhou, você pode lidar com isso de acordo com as necessidades do seu aplicativo.
                    Toast.makeText(
                        applicationContext,
                        "Falha ao excluir Alocação",
                        Toast.LENGTH_LONG
                    ).show()
                    // Como a exclusão não foi bem-sucedida, você pode precisar recarregar os cursos do servidor
                    loadAllocations()
                }
            }
        }

    }
    private fun deleteAllocation(allocationId: Int, callback: (success: Boolean) -> Unit) {
        val call = allocationService.deleteById(allocationId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Alocação Excluída com Sucesso.",
                        Toast.LENGTH_LONG
                    ).show()
                    callback(true) // Indica que a exclusão foi bem-sucedida
                } else {
                    // Trate o caso em que a exclusão falha
                    Toast.makeText(
                        applicationContext,
                        "Falha ao excluir Alocação",
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
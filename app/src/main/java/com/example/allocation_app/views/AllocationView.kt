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
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allocation_app.R
import com.example.allocation_app.adapter.AllocationAdapter
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
import java.util.List

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


        RetrofitConfig.getUrl()
        allocationService = RetrofitConfig.allocationService()

        // inicializar o adpter e o RecycleView e atribuir ã propriedade de classe
        adapter = AllocationAdapter(mutableListOf())
        recyclerView = findViewById(R.id.recycler_view_registered)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Conectar o ItemTouchHelper ao RecyclerView
        val itemTouchHelperCallback = ItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)



        //Tornar clicacel os itens do adpter
        adapter.onItemClick = { position ->
            val allocation = adapter.filteredList[position]
            allocation.id?.let { professorId ->
                allocation.courseId?.let {
                    showUpdateAllocationDialog(allocation)
                }
            }
        }

        // Carregar cursos da API
        loadAllocations()

        // Inicializar o SearchView
        initiSearchView()


        val searchButtom: FloatingActionButton = findViewById(R.id.fab_find)
        searchButtom.setOnClickListener {
           showIdLocation()
        }

        val addButtom: FloatingActionButton = findViewById(R.id.fab_add)
        addButtom.setOnClickListener {
           showAddAllocationDialog()
        }
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


                                        loadAllocationsWithProfessorCourseNames(courseList, professorList)
                                    }
                                }
                            }

                            override fun onFailure(call: Call<List<Professor>>, t: Throwable) {
                                Toast.makeText(applicationContext, "Falha na requisição de professores.", Toast.LENGTH_SHORT).show()
                                loadAllocations()
                            }

                        })

                    }
                } else {
                    // Tratamento de erro para a chamada de cursos
                    Toast.makeText(applicationContext, "Falha ao buscar informações de cursos.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Course>>, t: Throwable) {
                // Tratamento de erro para a chamada de cursos
                Toast.makeText(applicationContext, "Falha na requisição.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadAllocationsWithProfessorCourseNames(
        coursesList: MutableList<Course>,
        professorsList: MutableList<Professor>
    ) {
        val allocationsCall = allocationService.listAll()
        allocationsCall.enqueue(object : Callback<List<Allocation>> {
            override fun onResponse(call: Call<List<Allocation>>, response: Response<List<Allocation>>) {
                if (response.isSuccessful) {
                    val allocations = response.body()
                    val json = Gson().toJson(allocations)
                    Log.d("JSON Response Allocation Api", json)
                    if (allocations != null) {
                        // Associar informações de cursos e professores às alocações
                        allocations.forEach { allocation ->
                            val course = coursesList.find { it.id == allocation.courseId }
                            if (course != null) {
                                allocation.courseName = course.name
                            }

                            val professor = professorsList.find { it.id == allocation.professorId }
                            if (professor != null) {
                                allocation.professorName = professor.name
                            }
                        }

                        // Recarregar a lista de alocações no adaptador
                        adapter.reloadList(allocations)
                    }
                } else {
                    // Tratamento de erro para a chamada de alocações
                    Toast.makeText(applicationContext, "Falha ao buscar informações de alocações.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Allocation>>, t: Throwable) {
                // Tratamento de erro para a chamada de alocações
                Toast.makeText(applicationContext, "Falha na requisição de alocações.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddAllocationDialog() {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_add, null)

        //esconde o campo nome do layout
        view.findViewById<EditText>(R.id.txt_add_name).visibility = View.GONE
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
            val course_id = courseIdText.text.toString().toIntOrNull()
            val professor_id = professorIdText.text.toString().toIntOrNull()
            val selectHourStart = txtHourStart.text.toString()
            val selectHourEnd = txtHourEnd.text.toString()

                if(course_id != null && professorIdText != null
                    && txtHourEnd.text.isNotBlank() && txtHourStart.text.isNotBlank()){
                    val newAllocation = Allocation(
                        week_day = selectedDay,
                        professorId = professor_id,
                        courseId = course_id,
                        startHour = selectHourStart,
                        hourEnd = selectHourEnd)

                    addAllocation(newAllocation) // Enviar o novo curso para o servidor usando o Retrofit
                    loadAllocations()
                    ScrollToLastPosition.withDelay(recyclerView, adapter ,2000)
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

    // Adicionar curso
    private fun addAllocation(newAllocation: Allocation) {
        val call = allocationService.save(newAllocation)


        call.enqueue(object : Callback<Allocation> {
            override fun onResponse(call: Call<Allocation>, response: Response<Allocation>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Alocação adicionada com sucesso.",
                        Toast.LENGTH_LONG
                    ).show()
                    //val addedCourse = response.body() // Curso adicionado retornado pelo servidor
                    // Atualize a UI ou faça qualquer ação necessária após adicionar o curso



                }
            }

            override fun onFailure(call: Call<Allocation>, t: Throwable) {
                Toast.makeText(applicationContext,
                    "Falha ao adicionar o curso",
                    Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showIdLocation() {
        val view = layoutInflater.inflate(R.layout.layout_modal_find, null)
        val editText = view.findViewById<EditText>(R.id.txt_find_id)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(view)
        dialogBuilder.setPositiveButton("Procurar") { dialog, which ->
            val allocationId = editText.text.toString().toIntOrNull()

            if (allocationId != null) {
                val position = findAllocationPosition(allocationId)
                if (position != -1) {
                    recyclerView.smoothScrollToPosition(position)
                } else {
                    // Agende a exibição do Toast após um pequeno atraso
                    recyclerView.postDelayed({
                        Toast.makeText(
                            applicationContext,
                            "Allocação com ID $allocationId não encontrado.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }, 400) // 100 milliseconds de atraso

                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "ID da Alocação inválido.",
                    Toast.LENGTH_LONG
                ).show()
                loadAllocations()
            }
        }

        dialogBuilder.setNegativeButton("Cancelar", null)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()


    }

    //Encontrar por Id com scroll ate a posicao
    private fun findAllocationPosition(allocationId: Int): Int {
        for (i in adapter.items.indices) {
            if (adapter.items[i].id == allocationId) {
                return i
            }
        }
        return -1
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
                            "Alocação não encontrada.",
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

    private fun showUpdateAllocationDialog(allocation: Allocation) {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_modal_update, null)
        dialog.run {
            setView(view)
        }

        //esconde o campo nome do layout
        view.findViewById<EditText>(R.id.txt_att_Name).visibility = View.GONE
        view.findViewById<EditText>(R.id.txt_att_idDepartment).visibility = View.GONE

        //configurar o Spinner dos dias da semana
        val daysOfWeek = arrayOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")// criar um array de dias
        val spinnerDayOfWeek = view.findViewById<Spinner>(R.id.att_spinnerDayOfWeek) // acessa aonde eles esta la no modal add
        spinnerDayOfWeek.visibility = View.VISIBLE
        val adapterSpin = ArrayAdapter(this, android.R.layout.simple_spinner_item, daysOfWeek)//tem que criar um adaptador para preencher o spinner
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

        val updateCourse: EditText =  view.findViewById(R.id.txt_att_idCurso)
        val updateProfessor:EditText =  view.findViewById(R.id.txt_att_idProfessor)
        val idModalUpdate: TextView = view.findViewById(R.id.txt_att_id)

        // Tornar visíveis os campos
        updateCourse.visibility = View.VISIBLE
        updateProfessor.visibility = View.VISIBLE
        updateTxtHourStart.visibility = View.VISIBLE
        updateTxtHourEnd.visibility = View.VISIBLE


        // Use o objeto Allocation para preencher os campos do diálogo
        idModalUpdate.text = allocation.id.toString()


        dialog.setPositiveButton("Atualizar") { _, _ ->
            val selectedDay = spinnerDayOfWeek.selectedItem.toString()
            val newCourseIdtxt = updateCourse.text.toString()
            val newCourseId = newCourseIdtxt.toIntOrNull()
            val newProfessorIdtxt = updateProfessor.text.toString()
            val newProfessorId = newProfessorIdtxt.toIntOrNull()
            val newHourStart = updateTxtHourStart.text.toString()
            val newHourEnd = updateTxtHourEnd.text.toString()

            if (newCourseId != null && newProfessorId != null &&
                newHourStart.isNotBlank() && newHourEnd.isNotBlank()) {

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
        newHourEnd: String) {

        val call = allocationService.update(AllocationId, Allocation(week_day = selectedDay,
                                                                    courseId = newCourseId,
                                                                    professorId = newProfessorId,
                                                                    startHour = newHourStart,
                                                                    hourEnd = newHourEnd ))

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
                        "Falha ao excluir a Alocação",
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
            val deletedAllocation = adapter.filteredList[position]
            val alloctaionId = deletedAllocation.id ?: -1 // Certifique-se de que courseId não seja nulo

            // Agora você pode excluir o curso da API usando o ID do curso excluído
            deleteAllocation(alloctaionId) { success ->
                if (success) {
                    // A exclusão foi bem-sucedida, você pode tomar ações adicionais, se necessário.
                    // Por exemplo, recarregar a lista de cursos após a exclusão.
                    adapter.filteredList.removeAt(viewHolder.adapterPosition) // Remove da filteredList
                    adapter.items.remove(deletedAllocation) // Remove da lista original
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









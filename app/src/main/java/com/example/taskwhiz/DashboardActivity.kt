package com.example.taskwhiz

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.taskwhiz.models.Task
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

class DashboardActivity : AppCompatActivity(), OnTaskClickListener {

    private lateinit var addTaskButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        addTaskButton = findViewById(R.id.addTaskButton)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configura la RecyclerView y el Adapter
        val taskRecyclerView: RecyclerView = findViewById(R.id.taskList)
        val taskAdapter = TaskAdapter(ArrayList(),this) // Puedes pasar tu lista de tareas aquí
        taskRecyclerView.adapter = taskAdapter
        taskRecyclerView.layoutManager = LinearLayoutManager(this)

        loadUserNameToToolbar()

        // Cargar tareas desde Firebase
        loadTasksFromFirebase(taskAdapter, "Pendiente")

        // Configura un listener para el botón de agregar tarea
        addTaskButton.setOnClickListener {
            openCreateTaskFragment()
        }
    }

    private fun openCreateTaskFragment() {
        val fragment = CreateTaskFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadUserNameToToolbar() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val usersCollection = firestore.collection("usuarios")
            val userDocument = usersCollection.document(userId)

            Log.d("FirebaseAuth", "UserID: $userId")

            userDocument.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userName = documentSnapshot.getString("nombre")
                        updateToolbarTitle(userName)
                    }
                }
                .addOnFailureListener { exception ->
                    // Maneja errores aquí
                }
        }
    }

    private fun updateToolbarTitle(userName: String?) {
        val toolbarTitle = findViewById<TextView>(R.id.tvToolbarTitle)
        toolbarTitle.text = userName ?: getString(R.string.dashboard)
    }

    private fun loadTasksFromFirebase(adapter: TaskAdapter, estatus: String) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val tasksCollection = firestore.collection("usuarios").document(userId).collection("tasks")

            tasksCollection
                .whereEqualTo("estatus", estatus) // Filtra por el campo "estatus"
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        // Maneja el error aquí
                        Log.e(TAG, "Error al obtener las tareas", error)
                        return@addSnapshotListener
                    }

                    val taskList = ArrayList<Task>()

                    for (document in value!!) {
                        val taskData = document.data
                        val id = document.id
                        val nombre = taskData["nombre"] as String
                        val descripcion = taskData["descripcion"] as String
                        val creacion = taskData["creacion"] as com.google.firebase.Timestamp
                        val vencimiento = taskData["vencimiento"] as com.google.firebase.Timestamp
                        val prioridad = taskData["prioridad"] as Long
                        val estatus = taskData["estatus"] as String

                        val task = Task(
                            id,
                            nombre,
                            creacion.toDate(),
                            vencimiento.toDate(),
                            prioridad.toInt(),
                            estatus,
                            descripcion,
                            userId
                        )
                        taskList.add(task)
                    }
                    adapter.updateData(taskList)
                }
        }
    }

    override fun onTaskClick(taskId: String) {
        Log.d("OnTaskClickListener", "Task clicked: $taskId")
        val fragment = DetalleTareaFragment.newInstance(taskId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
        Log.d("OnTaskClickListener", "After fragment transaction")
    }
}
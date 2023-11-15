package com.example.taskwhiz

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.taskwhiz.models.Task
import com.google.android.material.navigation.NavigationView
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

class DashboardActivity : AppCompatActivity(), OnTaskClickListener{

    private lateinit var addTaskButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        addTaskButton = findViewById(R.id.addTaskButton)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        taskAdapter = TaskAdapter(ArrayList(),this)

        val taskRecyclerView: RecyclerView = findViewById(R.id.taskList)
        taskRecyclerView.adapter = taskAdapter
        taskRecyclerView.layoutManager = LinearLayoutManager(this)

        loadUserNameToToolbar()

        // Cargar tareas desde Firebase
        loadTasksFromFirebase(taskAdapter, "Pendiente")

        addTaskButton.setOnClickListener {
            openCreateTaskFragment()
        }
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

       // Configurar eventos de clic en elementos del menú
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_completed_tasks -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    loadTasksFromFirebase(taskAdapter, "Terminado")
                    updateToolbarTitle(getString(R.string.completed_tasks))
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_incompleted_tasks -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    loadTasksFromFirebase(taskAdapter, "Pendiente")
                    updateToolbarTitle(getString(R.string.incompleted_tasks))
                    return@setNavigationItemSelectedListener true
                }
                // Agregar más casos según sea necesario
                else -> false
            }
        }

        val menuIcon = findViewById<ImageView>(R.id.menuIcon)
        menuIcon.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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

    override fun onTaskClick(taskId: String, estatus: String) {
        Log.d("OnTaskClickListener", "Task clicked: $taskId with status: $estatus\"")
        val fragment = DetalleTareaFragment.newInstance(taskId,estatus)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
        Log.d("OnTaskClickListener", "After fragment transaction")
    }
}
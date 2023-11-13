package com.example.taskwhiz

import android.app.DatePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.example.taskwhiz.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskFragment : DialogFragment()  {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura un calendario para seleccionar la fecha de vencimiento
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Muestra un diálogo de selección de fecha cuando se hace clic en el campo de fecha
        val taskDueDateEditText = view.findViewById<EditText>(R.id.taskDueDateEditText)
        taskDueDateEditText.setOnClickListener{
            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Actualiza el campo de fecha de vencimiento
                calendar.set(selectedYear, selectedMonth, selectedDay)
                taskDueDateEditText.setText(formatDate(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        val closeButton = view.findViewById<Button>(R.id.closeFragmentButton)
        closeButton.setOnClickListener{
            dismiss()
        }

        // Maneja el evento de clic en el botón "Guardar Tarea"
        val saveTaskButton = view.findViewById<Button>(R.id.saveTaskButton)
        saveTaskButton.setOnClickListener {
            // Obtén los valores de los campos de entrada
            val taskNameEditText = view.findViewById<EditText>(R.id.taskTitleEditText)
            val taskDescriptionEditText = view.findViewById<EditText>(R.id.taskDescriptionEditText)

            // Obtén la prioridad seleccionada
            val priorityRadioGroup = view.findViewById<RadioGroup>(R.id.priorityRadioGroup)
            val selectedPriorityId = priorityRadioGroup.checkedRadioButtonId
            val priority = when (selectedPriorityId) {
                R.id.highPriorityRadioButton -> 2 // Por ejemplo, asigna valores según tus necesidades
                R.id.mediumPriorityRadioButton -> 1
                R.id.lowPriorityRadioButton -> 0
                else -> 0 // Valor predeterminado si no se selecciona nada
            }

            // Realiza validaciones, por ejemplo, si el nombre de la tarea no está vacío
            if (taskNameEditText.text.isNotEmpty()) {
                // Configura la fecha y hora actual como la fecha de creación
                val creationDate = Date()

                // Crea una instancia de la tarea
                val task = Task(
                    id = "", // Este ID se generará automáticamente o se asignará en la base de datos
                    nombre = taskNameEditText.text.toString(),
                    descripcion = taskDescriptionEditText.text.toString(),
                    creacion = creationDate,
                    vencimiento = calendar.time,
                    prioridad = priority, // Asigna la prioridad según lo seleccionado
                    estatus = "Pendiente", // Estado inicial
                    userId = FirebaseAuth.getInstance().currentUser?.uid.toString() // ID del usuario actual, debes obtenerlo de la autenticación
                )

                saveTaskToDatabase(task)
                // Cierra el fragmento
                dismiss()
            } else {
                // Muestra un mensaje de error si el nombre de la tarea está vacío
                taskNameEditText.error = "El nombre de la tarea es obligatorio"
            }
        }
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    private fun saveTaskToDatabase(task: Task) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userCollection = FirebaseFirestore.getInstance().collection("usuarios").document(userId).collection("tasks")

            // Agrega la tarea a la colección "tasks"
            userCollection.add( hashMapOf(
                "nombre" to task.nombre,
                "creacion" to task.creacion,
                "vencimiento" to task.vencimiento,
                "prioridad" to task.prioridad,
                "estatus" to task.estatus,
                "descripcion" to task.descripcion)
            )
                .addOnSuccessListener { documentReference ->
                    // Obtiene el ID del documento recién creado
                    val taskId = documentReference.id

                    userCollection.document(taskId).update("id", taskId)
                        .addOnSuccessListener {
                            // Éxito al guardar el ID en el documento
                            Log.d(ContentValues.TAG, "ID de tarea guardado en Firestore: $taskId")
                        }
                        .addOnFailureListener { e ->
                            // Error al guardar el ID en el documento
                            Log.e(ContentValues.TAG, "Error al guardar el ID de tarea en Firestore", e)
                        }
                }
                .addOnFailureListener { e ->
                    // Error al agregar la tarea a la colección
                    Log.e(ContentValues.TAG, "Error al agregar la tarea en Firestore", e)
                }
        } else {
            // Manejar la situación en la que el usuario actual no está autenticado
            Log.e(ContentValues.TAG, "Error: El usuario no está autenticado.")
        }
    }
}
package com.example.taskwhiz

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.taskwhiz.databinding.FragmentDetailTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DetalleTareaFragment : DialogFragment() {

    companion object {
        private const val ARG_TASK_ID = "taskId"

        fun newInstance(taskId: String, estatus: String): DetalleTareaFragment {
            val fragment = DetalleTareaFragment()
            val args = Bundle()
            args.putString(ARG_TASK_ID, taskId)
            args.putString("estatus", estatus)
            fragment.arguments = args
            return fragment
        }
    }

    private var binding: FragmentDetailTaskBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailTaskBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val updateTaskButton = view.findViewById<Button>(R.id.saveTaskButton)
        val markAsCompletedButton = view.findViewById<Button>(R.id.markAsCompletedButton)
        // Obtener el ID de la tarea desde los argumentos
        val taskId = arguments?.getString(ARG_TASK_ID)
        val estatus = arguments?.getString("estatus")



        if (estatus == "Terminado") {
            markAsCompletedButton.visibility = View.GONE
            updateTaskButton.visibility = View.GONE
        }


        updateTaskButton.setOnClickListener {
            val newTitle = binding?.taskTitleEditText?.text.toString()
            val newDescription = binding?.taskDescriptionEditText?.text.toString()
            val newDueDate = binding?.taskDueDateEditText?.text.toString()
            val priorityRadioGroup = view.findViewById<RadioGroup>(R.id.priorityRadioGroup)
            val selectedPriorityId = priorityRadioGroup.checkedRadioButtonId
            val newPriority = when (selectedPriorityId) {
                R.id.highPriorityRadioButton -> 2 // Por ejemplo, asigna valores según tus necesidades
                R.id.mediumPriorityRadioButton -> 1
                R.id.lowPriorityRadioButton -> 0
                else -> 0 // Valor predeterminado si no se selecciona nada
            }
            // Llama a la función para actualizar la tarea en la base de datos
            actualizarTareaEnBaseDeDatos(taskId, newTitle, newDescription, newDueDate, newPriority)
            // Cierra el fragmento después de actualizar la tarea
            dismiss()
        }

        // Configura un calendario para seleccionar la fecha de vencimiento
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Muestra un diálogo de selección de fecha cuando se hace clic en el campo de fecha
        val taskDueDateEditText = view.findViewById<EditText>(R.id.taskDueDateEditText)
        taskDueDateEditText.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Actualiza el campo de fecha de vencimiento
                    calendar.set(selectedYear, selectedMonth, selectedDay)

                    // Muestra el diálogo de selección de hora después de seleccionar la fecha
                    TimePickerDialog(
                        requireContext(),
                        { _, selectedHour, selectedMinute ->
                            // Actualiza la hora en el calendario
                            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                            calendar.set(Calendar.MINUTE, selectedMinute)

                            // Actualiza el campo de fecha y hora en el EditText
                            taskDueDateEditText.setText(formatDateAndTime(calendar.time))
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true // true para el formato de 24 horas
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Ahora puedes usar el ID de la tarea para cargar y mostrar detalles en la vista
        cargarDetallesDeTarea(taskId)
        val closeButton = view.findViewById<Button>(R.id.closeFragmentButton)
        closeButton.setOnClickListener{
            dismiss()
        }

        markAsCompletedButton.setOnClickListener {
            mostrarAlertaConfirmacion()
        }
    }

    private fun mostrarAlertaConfirmacion() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
            .setMessage("¿Estás seguro de marcar esta tarea como completada?")
            .setPositiveButton("Sí") { _, _ ->
                // El usuario ha confirmado, marcar la tarea como completada
                val taskId = arguments?.getString(ARG_TASK_ID)
                marcarComoCompletada(taskId)
            }
            .setNegativeButton("No") { _, _ ->
                // El usuario ha cancelado la operación
            }
            .show()
    }

    private fun marcarComoCompletada(taskId: String?) {
        // Verificar si el ID de la tarea no es nulo
        if (taskId != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val db = FirebaseFirestore.getInstance()
            val taskDocument = db.collection("usuarios").document(userId.toString()).collection("tasks").document(taskId)

            // Actualizar el campo 'estatus' a 'terminado'
            taskDocument.update("estatus", "Terminado")
                .addOnSuccessListener {
                    // La tarea se marcó como completada correctamente
                    // Puedes realizar acciones adicionales o mostrar un mensaje de éxito
                    Toast.makeText(requireContext(), "Tarea marcada como completada", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener { e ->
                    // Manejar errores al actualizar la tarea en Firestore
                    // Puedes mostrar un mensaje de error o realizar otras acciones según tus necesidades
                    Toast.makeText(requireContext(), "Error al marcar como completada", Toast.LENGTH_SHORT).show()
                    Log.e(ContentValues.TAG, "Error al marcar como completada", e)
                }
        }
    }

    private fun formatDateAndTime(date: Date): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    private fun cargarDetallesDeTarea(taskId: String?) {
        // Verificar si el ID de la tarea no es nulo
        if (taskId != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val db = FirebaseFirestore.getInstance()
            val taskDocument = db.collection("usuarios").document(userId.toString()).collection("tasks").document(taskId)

            taskDocument.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // La tarea existe, ahora puedes obtener los datos y actualizar la interfaz de usuario
                        val taskName = document.getString("nombre")
                        val taskDescription = document.getString("descripcion")
                        val taskVencimiento = document.getTimestamp("vencimiento")
                        val taskPriority = document.getLong("prioridad")

                        // Actualizar la interfaz de usuario con los detalles de la tarea
                        val taskNameEditText = view?.findViewById<EditText>(R.id.taskTitleEditText)
                        val taskDescriptionEditText = view?.findViewById<EditText>(R.id.taskDescriptionEditText)
                        val taskDueDateEditText = view?.findViewById<EditText>(R.id.taskDueDateEditText)
                        val highPriorityRadioButton = view?.findViewById<RadioButton>(R.id.highPriorityRadioButton)
                        val mediumPriorityRadioButton = view?.findViewById<RadioButton>(R.id.mediumPriorityRadioButton)
                        val lowPriorityRadioButton = view?.findViewById<RadioButton>(R.id.lowPriorityRadioButton)

                        taskNameEditText?.setText(taskName)
                        taskDescriptionEditText?.setText(taskDescription)

                        // Verificar si la fecha de vencimiento no es nula
                        if (taskVencimiento != null) {
                            // Formatear la fecha y hora y mostrarla en el EditText
                            val formattedDate = formatDateAndTime(taskVencimiento.toDate())
                            taskDueDateEditText?.setText(formattedDate)
                        }

                        // Verificar la prioridad y marcar el RadioButton correspondiente
                        when (taskPriority) {
                            2L -> highPriorityRadioButton?.isChecked = true
                            1L -> mediumPriorityRadioButton?.isChecked = true
                            0L -> lowPriorityRadioButton?.isChecked = true
                        }

                        // Puedes continuar de manera similar para otros campos si es necesario
                    } else {
                        // La tarea no existe
                        // Aquí puedes manejar el caso en el que la tarea no se encuentra
                    }
                }
                .addOnFailureListener { e ->
                    // Manejar errores al cargar la tarea desde Firestore
                    // Aquí puedes mostrar un mensaje de error o realizar otras acciones según tus necesidades
                }
        }
    }

    private fun actualizarTareaEnBaseDeDatos(taskId: String?, newTitle: String, newDescription: String, newDueDate: String, newPriority: Int) {
        // Verificar si el ID de la tarea no es nulo
        if (taskId != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val db = FirebaseFirestore.getInstance()
            val taskDocument = db.collection("usuarios").document(userId.toString()).collection("tasks").document(taskId)

            // Crear un mapa con los nuevos valores
            val newData = hashMapOf(
                "nombre" to newTitle,
                "descripcion" to newDescription,
                "vencimiento" to convertirStringADate(newDueDate),
                "prioridad" to newPriority
            )

            // Actualizar la tarea en la base de datos
            taskDocument.update(newData as Map<String, Any>)
                .addOnSuccessListener {
                    // La tarea se actualizó correctamente
                    Toast.makeText(requireContext(), "Tarea actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    dismiss() // Cierra el fragmento después de actualizar la tarea
                }
                .addOnFailureListener { e ->
                    // Manejar errores al actualizar la tarea en Firestore
                    Toast.makeText(requireContext(), "Error al actualizar la tarea: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Función para convertir una cadena de fecha y hora a un objeto Date
    private fun convertirStringADate(dateString: String): Date {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.parse(dateString) ?: Date() // Si hay un error al analizar la fecha, regresa una fecha por defecto
    }

}

package com.example.taskwhiz

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.taskwhiz.models.Task


class TaskDetailDialogFragment : DialogFragment() {
    private lateinit var task: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recupera la tarea pasada como argumento al fragmento
//        arguments?.getParcelable<Task>("task")?.let {
//            task = it
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_task_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Muestra los detalles de la tarea
//        taskNameTextView.text = task.taskName
//        taskDescriptionTextView.text = task.taskDescription
//        taskDueDateTextView.text = task.dueDate
//        taskPriorityTextView.text = task.priority.toString()
//        taskStatusTextView.text = task.status
//
//        // Configura el botón para editar la tarea
//        editTaskButton.setOnClickListener {
//            // Abre un fragmento de edición de tarea y pasa la tarea para su edición
//            val editTaskFragment = EditTaskFragment.newInstance(task)
//            editTaskFragment.show(parentFragmentManager, "edit_task")
//            dismiss()
//        }
    }

    companion object {
//        fun newInstance(task: Task): TaskDetailDialogFragment {
//            val fragment = TaskDetailDialogFragment()
//            val args = Bundle()
//            args.putParcelable("task", task)
//            fragment.arguments = args
//            return fragment
//        }
//    }
}
}
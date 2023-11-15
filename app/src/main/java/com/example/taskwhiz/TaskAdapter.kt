package com.example.taskwhiz

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskwhiz.models.Task

interface OnTaskClickListener {
    fun onTaskClick(taskId: String, estatus: String)
}

class TaskAdapter(
    private var taskList: List<Task>,
    private val onTaskClickListener: OnTaskClickListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val taskNameTextView: TextView = itemView.findViewById(R.id.taskNameTextView)
        val taskDescriptionTextView: TextView = itemView.findViewById(R.id.taskDescriptionTextView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            Log.d("TaskAdapter", "Item clicked at position $position")
            if (position != RecyclerView.NO_POSITION) {
                val task = taskList[position]
                onTaskClickListener.onTaskClick(task.id, task.estatus)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentTask = taskList[position]

        holder.taskNameTextView.text = currentTask.nombre
        holder.taskDescriptionTextView.text = currentTask.descripcion
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    fun updateData(newList: List<Task>) {
        taskList = newList
        notifyDataSetChanged()
    }
}
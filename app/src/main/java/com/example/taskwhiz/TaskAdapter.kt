package com.example.taskwhiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskwhiz.models.Task

class TaskAdapter(private var taskList: List<Task>) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskNameTextView: TextView = itemView.findViewById(R.id.taskNameTextView)
        val taskDescriptionTextView: TextView = itemView.findViewById(R.id.taskDescriptionTextView)
        // Otros campos según sea necesario
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentTask = taskList[position]

        holder.taskNameTextView.text = currentTask.nombre
        holder.taskDescriptionTextView.text = currentTask.descripcion
        // Configurar otros campos según sea necesario
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    fun updateData(newList: List<Task>) {
        taskList = newList
        notifyDataSetChanged()
    }
}
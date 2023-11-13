package com.example.taskwhiz.models

import java.util.*

data class Task(
    val id: String = "",
    val nombre: String = "",
    val creacion: Date? = null,
    val vencimiento: Date? = null,
    val prioridad: Int = 0,
    val estatus: String = "",
    val descripcion: String = "",
    val userId: String = ""
)

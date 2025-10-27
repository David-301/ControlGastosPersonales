package com.ch220048.controlgastospersonales

data class Gasto(
    var id: String = "",
    val nombre: String = "",
    val monto: Double = 0.0,
    val categoria: String = "",
    val fecha: String = "",
    val notas: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
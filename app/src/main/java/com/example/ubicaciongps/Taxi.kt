package com.example.ubicaciongps

import java.io.Serializable

data class Taxi(
    val movil: String,
    val carnet: String,
    val latitud: String,
    val longitud: String
) : Serializable
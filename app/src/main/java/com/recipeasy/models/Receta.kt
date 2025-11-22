package com.recipeasy.models

import android.content.Context
import java.io.Serializable

data class Receta(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val imagen: String,
    val tiempoPreparacion: Int,
    val porciones: Int,
    val dificultad: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    val categoria: String,
    val esFavorito: Boolean = false
) : Serializable {
    companion object {
        fun fromDatabase(
            id: Int,
            nombre: String,
            descripcion: String,
            imagen: String,
            tiempoPreparacion: Int,
            porciones: Int,
            dificultad: String,
            ingredientesString: String,
            pasosString: String,
            categoria: String
        ): Receta {
            val ingredientes = ingredientesString.split("||")
            val pasos = pasosString.split("||")
            return Receta(
                id = id,
                nombre = nombre,
                descripcion = descripcion,
                imagen = imagen,
                tiempoPreparacion = tiempoPreparacion,
                porciones = porciones,
                dificultad = dificultad,
                ingredientes = ingredientes,
                pasos = pasos,
                categoria = categoria
            )
        }
    }

    fun getImageResId(context: Context): Int =
        context.resources.getIdentifier(imagen, "drawable", context.packageName)

    fun getVideoResId(context: Context): Int =
        context.resources.getIdentifier(imagen, "raw", context.packageName)
}
package com.recipeasy.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.recipeasy.R
import com.recipeasy.database.DatabaseHelper
import com.recipeasy.models.Receta
import com.recipeasy.utils.SharedPreferencesHelper

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefs: SharedPreferencesHelper
    private var receta: Receta? = null
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        dbHelper = DatabaseHelper(this)
        prefs = SharedPreferencesHelper(this)

        // Obtenemos la receta pasada desde el fragment
        receta = intent.getSerializableExtra("RECETA") as? Receta

        if (receta != null) {
            setupViews(receta!!)
            checkIfFavorite()
            setupFavoriteButton()
        } else {
            finish() // Si no hay receta, cerramos la actividad
        }
    }

    private fun setupViews(receta: Receta) {
        // Configuramos los views con los datos de la receta
        findViewById<TextView>(R.id.tvRecipeTitle).text = receta.nombre
        findViewById<TextView>(R.id.tvRecipeDescription).text = receta.descripcion
        findViewById<TextView>(R.id.tvTime).text = "${receta.tiempoPreparacion} min"
        findViewById<TextView>(R.id.tvPortions).text = receta.porciones.toString()
        findViewById<TextView>(R.id.tvDifficulty).text = receta.dificultad

        // Configuramos la lista de ingredientes
        val ingredientsText = receta.ingredientes.joinToString("\n") { "• $it" }
        findViewById<TextView>(R.id.tvIngredients).text = ingredientsText

        // Configuramos los pasos de preparación
        val stepsText = receta.pasos.mapIndexed { index, paso ->
            "${index + 1}. $paso"
        }.joinToString("\n\n")
        findViewById<TextView>(R.id.tvSteps).text = stepsText

        // Por ahora usamos el placeholder, luego podemos cargar imágenes reales
        findViewById<ImageView>(R.id.ivRecipeImage).setImageResource(R.drawable.ic_placeholder_food)
    }

    private fun checkIfFavorite() {
        val usuarioId = prefs.getUserId()
        if (usuarioId != -1 && receta != null) {
            isFavorite = dbHelper.isFavorite(usuarioId, receta!!.id)
            updateFavoriteButton()
        }
    }

    private fun setupFavoriteButton() {
        val favoriteButton = findViewById<ImageView>(R.id.ivFavorite)
        favoriteButton.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun toggleFavorite() {
        val usuarioId = prefs.getUserId()
        if (usuarioId == -1) {
            Toast.makeText(this, "Debes iniciar sesión para guardar favoritos", Toast.LENGTH_SHORT).show()
            return
        }

        if (receta != null) {
            if (isFavorite) {
                // Quitar de favoritos
                dbHelper.removeFavorite(usuarioId, receta!!.id)
                isFavorite = false
                Toast.makeText(this, "Receta quitada de favoritos", Toast.LENGTH_SHORT).show()
            } else {
                // Agregar a favoritos
                dbHelper.addFavorite(usuarioId, receta!!.id)
                isFavorite = true
                Toast.makeText(this, "Receta agregada a favoritos", Toast.LENGTH_SHORT).show()
            }
            updateFavoriteButton()
        }
    }

    private fun updateFavoriteButton() {
        val favoriteButton = findViewById<ImageView>(R.id.ivFavorite)
        val drawable = if (isFavorite) {
            R.drawable.ic_favorite_filled
        } else {
            R.drawable.ic_favorite_border
        }
        favoriteButton.setImageResource(drawable)
    }
}
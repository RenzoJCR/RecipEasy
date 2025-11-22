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
import android.content.Intent
import android.net.Uri
import android.widget.VideoView



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
            setupShareButton()
        } else {
            finish() // Si no hay receta, cerramos la actividad
        }
    }

    private fun setupShareButton() {
        val shareButton = findViewById<ImageView>(R.id.ivShare)

        shareButton.setOnClickListener {
            receta?.let { receta ->
                compartirReceta(receta)
            }
        }
    }

    private fun compartirReceta(receta: Receta) {

        val ingredientesTexto = receta.ingredientes.joinToString("\n") { "• $it" }

        val pasosTexto = receta.pasos
            .mapIndexed { index, paso -> "${index + 1}. $paso" }
            .joinToString("\n")

        val textoCompartido = """
🍽 *${receta.nombre}*
———————————————

📝 *Descripción*  
${receta.descripcion}

🧂 *Ingredientes*  
$ingredientesTexto

👩‍🍳 *Preparación*  
$pasosTexto

""".trimIndent()

        // 1) Intent genérico para compartir texto
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textoCompartido)
        }

        // 2) Mostramos el chooser para que el usuario elija app (WhatsApp, Telegram, Gmail, etc.)
        val shareIntent = Intent.createChooser(sendIntent, "Compartir receta con...")
        startActivity(shareIntent)
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

        // 🎬 Video en el detalle
        val videoView = findViewById<VideoView>(R.id.vvRecipeVideo)

        // usamos el mismo nombre que en la imagen: receta.imagen
        val videoResId = resources.getIdentifier(
            receta.imagen,   // ej: "breakfast_tamal_verde"
            "raw",
            packageName
        )

        if (videoResId != 0) {
            val uri = Uri.parse("android.resource://$packageName/$videoResId")
            videoView.setVideoURI(uri)

            videoView.setOnPreparedListener { mp ->
                mp.isLooping = true
                mp.setVolume(0f, 0f)
                mp.start()
            }
        } else {
            // Fallback opcional: si no hay video, mostramos imagen
            val imageView = findViewById<ImageView>(R.id.ivRecipeImage)
            val imageResId = resources.getIdentifier(
                receta.imagen,
                "drawable",
                packageName
            )

            if (imageResId != 0) {
                imageView.setImageResource(imageResId)
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder_food)
            }

            Toast.makeText(this, "Video no encontrado, mostrando imagen.", Toast.LENGTH_SHORT).show()
        }
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
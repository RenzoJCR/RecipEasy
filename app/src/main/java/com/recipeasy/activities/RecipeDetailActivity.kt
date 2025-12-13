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
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.os.Build
import android.webkit.CookieManager



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
        // ---------- TEXTOS ----------
        findViewById<TextView>(R.id.tvRecipeTitle).text = receta.nombre
        findViewById<TextView>(R.id.tvRecipeDescription).text = receta.descripcion
        findViewById<TextView>(R.id.tvTime).text = "${receta.tiempoPreparacion} min"
        findViewById<TextView>(R.id.tvPortions).text = receta.porciones.toString()
        findViewById<TextView>(R.id.tvDifficulty).text = receta.dificultad

        findViewById<TextView>(R.id.tvIngredients).text =
            receta.ingredientes.joinToString("\n") { "• $it" }

        findViewById<TextView>(R.id.tvSteps).text =
            receta.pasos.mapIndexed { i, p -> "${i + 1}. $p" }.joinToString("\n\n")

        // ---------- VIDEO / IMAGEN ----------
        val imageView = findViewById<ImageView>(R.id.ivRecipeImage)
        val webView = findViewById<WebView>(R.id.wvRecipeVideo)
        val videoUrl = receta.videoUrl

        // Si no hay URL, fallback imagen
        if (videoUrl.isNullOrBlank() || !videoUrl.contains("youtu")) {
            showImageFallback(receta, imageView, webView)
            return
        }

        val videoId = extractYouTubeId(videoUrl)
        if (videoId.isNullOrBlank()) {
            showImageFallback(receta, imageView, webView)
            return
        }

        // Mostrar WebView y ocultar imagen (evita overlay)
        webView.visibility = View.VISIBLE
        imageView.visibility = View.GONE
        findViewById<ImageView>(R.id.ivFavorite).bringToFront()
        findViewById<ImageView>(R.id.ivShare).bringToFront()


        // Settings base
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        // Cookies (YouTube a veces depende de esto en embeds)
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        //Importante:
        // - Usamos youtube-nocookie.com (suele reducir bloqueos)
        // - Agregamos referrerpolicy recomendado por YouTube para embeds
        // - Base URL NO es youtube.com (le damos una “identidad” propia al embebedor)
        val embedUrl = "https://www.youtube-nocookie.com/embed/$videoId" +
                "?autoplay=1&mute=1&loop=1&playlist=$videoId&playsinline=1"

        val html = """
        <!DOCTYPE html>
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            <style>
              body, html { margin:0; padding:0; background:#000; height:100%; }
              iframe { width:100%; height:100%; border:0; }
            </style>
          </head>
          <body>
            <iframe
              src="$embedUrl"
              allow="autoplay; encrypted-media; picture-in-picture"
              allowfullscreen
              referrerpolicy="strict-origin-when-cross-origin">
            </iframe>
          </body>
        </html>
    """.trimIndent()

        // BaseURL “neutral” (no youtube.com) para evitar validaciones raras
        webView.loadDataWithBaseURL(
            "https://example.com",
            html,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun showImageFallback(receta: Receta, imageView: ImageView, webView: WebView) {
        webView.visibility = View.GONE
        imageView.visibility = View.VISIBLE

        val imageResId = resources.getIdentifier(receta.imagen, "drawable", packageName)
        if (imageResId != 0) imageView.setImageResource(imageResId)
        else imageView.setImageResource(R.drawable.ic_placeholder_food)

        Toast.makeText(this, "Mostrando imagen (video no disponible).", Toast.LENGTH_SHORT).show()
    }

    private fun extractYouTubeId(url: String): String? {
        val regex = "(?<=v=|be/|embed/)[^&?\\n]+".toRegex()
        return regex.find(url)?.value
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

    override fun onDestroy() {
        val webView = findViewById<WebView>(R.id.wvRecipeVideo)
        webView.apply {
            loadUrl("about:blank")
            stopLoading()
            clearHistory()
            clearCache(true)
            clearFormData()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }



}
package com.recipeasy.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.recipeasy.R
import com.recipeasy.database.DatabaseHelper
import com.recipeasy.utils.SharedPreferencesHelper

class UserProfileActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferencesHelper
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvFavoritesCount: TextView
    private lateinit var tvRecipesCount: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        prefs = SharedPreferencesHelper(this)
        dbHelper = DatabaseHelper(this)

        initViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvFavoritesCount = findViewById(R.id.tvFavoritesCount)
        tvRecipesCount = findViewById(R.id.tvRecipesCount)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun loadUserData() {
        val userName = prefs.getUserName()
        val userEmail = prefs.getUserEmail()
        val userId = prefs.getUserId()

        tvUserName.text = userName
        tvUserEmail.text = userEmail

        // Cargar estadísticas
        if (userId != -1) {
            val favoritesCount = dbHelper.getFavorites(userId).size
            val recipesCount = dbHelper.getAllRecipes().size

            tvFavoritesCount.text = favoritesCount.toString()
            tvRecipesCount.text = recipesCount.toString()
        }
    }

    private fun setupClickListeners() {
        btnLogout.setOnClickListener {
            // Cerrar sesión: limpiar preferencias y redirigir al login
            prefs.clearUserData()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
package com.recipeasy.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.recipeasy.database.DatabaseHelper
import com.recipeasy.utils.SharedPreferencesHelper
import com.recipeasy.R

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefs: SharedPreferencesHelper

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)
        prefs = SharedPreferencesHelper(this)

        // Si ya está logueado, ir a MainActivity
        if (prefs.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToRegister = findViewById(R.id.btnGoToRegister)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.checkUser(email, password)) {
                // OBTENER USUARIO REAL DE LA BASE DE DATOS
                val usuario = dbHelper.getUserByEmail(email)
                if (usuario != null) {
                    // Login exitoso - GUARDAR DATOS REALES
                    prefs.setLoggedIn(true)
                    prefs.setUserInfo(usuario.email, usuario.nombre, usuario.id)

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
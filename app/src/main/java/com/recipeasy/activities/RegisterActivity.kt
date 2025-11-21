package com.recipeasy.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.recipeasy.database.DatabaseHelper
import com.recipeasy.models.Usuario
import com.recipeasy.R
import com.recipeasy.utils.SharedPreferencesHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefs: SharedPreferencesHelper

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)
        prefs = SharedPreferencesHelper(this)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.btnGoToLogin)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.checkEmailExists(email)) {
                Toast.makeText(this, "El email ya está registrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuario = Usuario(
                nombre = name,
                email = email,
                password = password
            )

            val result = dbHelper.addUser(usuario)
            if (result != -1L) {
                // OBTENER USUARIO RECIÉN CREADO
                val usuarioCreado = dbHelper.getUserByEmail(email)
                if (usuarioCreado != null) {
                    prefs.setLoggedIn(true)
                    prefs.setUserInfo(usuarioCreado.email, usuarioCreado.nombre, usuarioCreado.id)

                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
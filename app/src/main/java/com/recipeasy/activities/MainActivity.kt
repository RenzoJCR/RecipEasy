package com.recipeasy.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.recipeasy.adapters.CategoryPagerAdapter
import com.recipeasy.R
import com.recipeasy.database.DatabaseHelper
import com.recipeasy.utils.NotificationHelper
import com.recipeasy.utils.NotificationScheduler
import com.recipeasy.utils.PermissionHelper
import com.recipeasy.utils.SharedPreferencesHelper

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var pagerAdapter: CategoryPagerAdapter
    private lateinit var prefs: SharedPreferencesHelper
    private lateinit var toolbar: Toolbar
    private lateinit var notificationScheduler: NotificationScheduler

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar base de datos y llenar URLs de video
        val dbHelper = DatabaseHelper(this)
        dbHelper.backfillVideoUrls()

        prefs = SharedPreferencesHelper(this)
        notificationScheduler = NotificationScheduler(this)

        // Configurar la Toolbar
        setupToolbar()

        initViews()
        setupViewPager()
        setupSearchView()

        // Pedir permiso de notificaciones y programarlas
        setupNotifications()

        // Manejar si venimos de una notificación
        handleNotificationIntent()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "RecipEasy"
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        searchView = findViewById(R.id.searchView)
    }

    private fun setupViewPager() {
        pagerAdapter = CategoryPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_desayuno)
                1 -> getString(R.string.tab_almuerzo)
                2 -> getString(R.string.tab_cena)
                3 -> getString(R.string.tab_postres)
                4 -> getString(R.string.tab_favoritos)
                else -> null
            }
        }.attach()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                pagerAdapter.notifyFragmentsAboutSearch(newText ?: "")
                return true
            }
        })
    }

    private fun setupNotifications() {
        // Para Android 13+ necesitamos pedir permiso
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Pedir permiso
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // Ya tenemos permiso, iniciar notificaciones
                startScheduledNotifications()
            }
        } else {
            // Android 12 o inferior, no necesita permiso explícito
            startScheduledNotifications()
        }
    }

    private fun startScheduledNotifications() {
        // Mostrar primera notificación inmediatamente
        Handler(Looper.getMainLooper()).postDelayed({
            val notificationHelper = NotificationHelper(this)
            notificationHelper.showRecipeNotification()
            Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
        }, 2000) // 2 segundos de delay al abrir la app

        // Iniciar programación cada 10 segundos
        notificationScheduler.startNotifications()
    }

    private fun handleNotificationIntent() {
        intent?.extras?.let { extras ->
            if (extras.getBoolean("FROM_NOTIFICATION", false)) {
                val recipeName = extras.getString("RECIPE_NAME")
                if (!recipeName.isNullOrEmpty()) {
                    Toast.makeText(this, "Receta sugerida: $recipeName", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // AGREGAR MENÚ DE USUARIO (PERFIL) - ESTO FALTABA
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                val intent = Intent(this, UserProfileActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // UN SOLO MÉTODO onRequestPermissionsResult QUE MANEJA TODOS LOS PERMISOS
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionHelper.STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
                }
            }

            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScheduledNotifications()
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener notificaciones al cerrar la app (opcional)
        // notificationScheduler.stopNotifications()
    }
}
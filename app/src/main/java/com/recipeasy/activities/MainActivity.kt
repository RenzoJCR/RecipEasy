package com.recipeasy.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.recipeasy.adapters.CategoryPagerAdapter
import com.recipeasy.R
import com.recipeasy.database.DatabaseHelper
import com.recipeasy.utils.SharedPreferencesHelper

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.recipeasy.utils.PermissionHelper

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: SearchView
    private lateinit var pagerAdapter: CategoryPagerAdapter
    private lateinit var prefs: SharedPreferencesHelper
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dbHelper = DatabaseHelper(this)
        dbHelper.backfillVideoUrls()


        prefs = SharedPreferencesHelper(this)

        // Configurar la Toolbar
        setupToolbar()

        initViews()
        setupViewPager()
        setupSearchView()
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

    // AGREGAR MENÚ DE USUARIO
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

    // AÑADE ESTE MÉTODO AL FINAL DE LA CLASE
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionHelper.STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso denegado. No se puede guardar PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} // FIN DE LA CLASE

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
import com.recipeasy.utils.SharedPreferencesHelper

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
}
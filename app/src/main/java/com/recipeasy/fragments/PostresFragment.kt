package com.recipeasy.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.recipeasy.activities.RecipeDetailActivity
import com.recipeasy.adapters.RecipeAdapter
import com.recipeasy.database.DatabaseHelper
import com.recipeasy.interfaces.OnRecipeClickListener
import com.recipeasy.interfaces.SearchableFragment
import com.recipeasy.models.Receta
import com.recipeasy.R
import com.recipeasy.utils.Constants

class PostresFragment : Fragment(), OnRecipeClickListener, SearchableFragment {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter

    private val dbHelper: DatabaseHelper? by lazy {
        if (isAdded && context != null) {
            DatabaseHelper(requireContext())
        } else {
            null
        }
    }

    private var recipeList: List<Receta> = listOf()
    private var filteredList: List<Receta> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_postres, container, false)
        initViews(view)
        loadRecipes()
        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewPostres)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recipeAdapter = RecipeAdapter(filteredList, this)
        recyclerView.adapter = recipeAdapter
    }

    private fun loadRecipes() {
        dbHelper?.let { helper ->
            recipeList = helper.getRecipesByCategory(Constants.CATEGORY_POSTRES)
            filteredList = recipeList
            recipeAdapter.updateRecipes(filteredList)
        }
    }

    override fun onRecipeClick(receta: Receta) {
        val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
        intent.putExtra("RECETA", receta)
        startActivity(intent)
    }

    override fun onSearchQuery(query: String) {
        dbHelper?.let { helper ->
            filteredList = if (query.isEmpty()) {
                recipeList
            } else {
                helper.searchRecipesGlobal(query)
            }
            recipeAdapter.updateRecipes(filteredList)
        }
    }
}
package com.recipeasy.interfaces

import com.recipeasy.models.Receta

interface OnRecipeClickListener {
    fun onRecipeClick(receta: Receta)
}
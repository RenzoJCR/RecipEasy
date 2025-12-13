package com.recipeasy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.recipeasy.R
import com.recipeasy.interfaces.OnRecipeClickListener
import com.recipeasy.models.Receta

class RecipeAdapter(
    private var recipes: List<Receta>,
    private val listener: OnRecipeClickListener
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.recipe_image)
        val title: TextView = itemView.findViewById(R.id.recipe_title)
        val description: TextView = itemView.findViewById(R.id.recipe_desc)
        val time: TextView = itemView.findViewById(R.id.recipe_time)
        val difficulty: TextView = itemView.findViewById(R.id.recipe_difficulty)
        val category: TextView = itemView.findViewById(R.id.recipe_category)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onRecipeClick(recipes[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]

        holder.title.text = recipe.nombre
        holder.description.text = recipe.descripcion
        holder.time.text = "${recipe.tiempoPreparacion} min"
        holder.difficulty.text = recipe.dificultad
        holder.category.text = recipe.categoria.replaceFirstChar { it.uppercase() }

        val context = holder.itemView.context

        val imageResId = context.resources.getIdentifier(
            recipe.imagen,     // "breakfast_tamal_verde"
            "drawable",
            context.packageName
        )

        if (imageResId != 0) {
            holder.image.setImageResource(imageResId)
        } else {
            holder.image.setImageResource(R.drawable.ic_placeholder_food)
        }
    }



    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Receta>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}
package com.recipeasy.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.recipeasy.fragments.*
import com.recipeasy.interfaces.SearchableFragment

class CategoryPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragments = mutableListOf<Fragment>()

    init {
        // Crear los fragments y guardarlos en la lista
        fragments.add(DesayunoFragment())
        fragments.add(AlmuerzoFragment())
        fragments.add(CenaFragment())
        fragments.add(PostresFragment())
        fragments.add(FavoritosFragment())
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun notifyFragmentsAboutSearch(query: String) {
        fragments.forEach { fragment ->
            if (fragment is SearchableFragment) {
                fragment.onSearchQuery(query)
            }
        }
    }
}
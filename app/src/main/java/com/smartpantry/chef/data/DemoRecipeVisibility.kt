package com.smartpantry.chef.data

import android.content.Context

/** On-device demo screens hide pre-existing test recipes but never delete them. */
object DemoRecipeVisibility {
    fun visibleOnDiscover(context: Context, recipes: List<Recipe>): List<Recipe> {
        val prefs = context.getSharedPreferences("smartpantry_demo_v1", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("installed", false)) return recipes
        val legacyIds = prefs.getString("legacy_recipe_ids", "").orEmpty()
            .split(",").mapNotNull { it.toIntOrNull() }.toSet()
        return recipes.filterNot { it.id in legacyIds }
    }
}

package com.smartpantry.chef.data

import android.content.Context
import org.json.JSONObject
import java.util.Locale

/** Private, on-device counts of recipe searches; no external profile upload. */
object SearchHistoryStore {
    private const val PREFS = "smartpantry_search_history_v1"
    private fun normalize(input: String) = input.trim()
        .lowercase(Locale.forLanguageTag("tr-TR")).replace(Regex("\\s+"), " ")

    fun counts(context: Context): Map<String, Int> {
        val text = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("counts", "{}") ?: "{}"
        return runCatching {
            val json = JSONObject(text)
            json.keys().asSequence().associateWith { json.optInt(it) }
        }.getOrDefault(emptyMap())
    }

    fun record(context: Context, query: String) {
        val clean = normalize(query)
        if (clean.length < 3 || clean.length > 50) return
        val current = counts(context).toMutableMap()
        current[clean] = ((current[clean] ?: 0) + 1).coerceAtMost(1000)
        if (current.size > 80) {
            val smallest = current.entries.minByOrNull { it.value }?.key
            if (smallest != null) current.remove(smallest)
        }
        val json = JSONObject()
        current.forEach { (k, v) -> json.put(k, v) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("counts", json.toString()).apply()
    }

    fun relevance(recipe: Recipe, history: Map<String, Int>): Int =
        history.entries.sumOf { (term, times) ->
            val score = when {
                recipe.name.contains(term, ignoreCase = true) -> 8
                recipe.category.contains(term, ignoreCase = true) -> 5
                recipe.ingredients.contains(term, ignoreCase = true) -> 3
                else -> 0
            }
            score * times
        }
}

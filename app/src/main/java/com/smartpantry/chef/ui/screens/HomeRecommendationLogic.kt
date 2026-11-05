package com.smartpantry.chef.ui.screens

import com.smartpantry.chef.data.Ingredient
import com.smartpantry.chef.data.MatchStatus
import com.smartpantry.chef.data.RecipeIngredient
import com.smartpantry.chef.data.RecipeMatch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

internal fun rankHomeRecipeMatches(
    matches: List<RecipeMatch>
): List<RecipeMatch> {

    return matches.sortedWith(
        compareBy<RecipeMatch> {
            when (it.status) {
                MatchStatus.CAN_MAKE -> 0
                MatchStatus.ALMOST_READY -> 1
                MatchStatus.MISSING -> 2
            }
        }.thenByDescending {
            it.compatibilityPercent
        }.thenBy {
            it.recipe.name
        }
    )
}

internal fun pantryDaysLeft(
    expirationDate: String,
    today: LocalDate = LocalDate.now()
): Long? {

    val formatter =
        DateTimeFormatter.ofPattern(
            "dd.MM.uuuu"
        )

    val expiration =
        try {
            LocalDate.parse(
                expirationDate,
                formatter
            )
        } catch (_: DateTimeParseException) {
            return null
        }

    return ChronoUnit.DAYS.between(
        today,
        expiration
    )
}

internal fun expiringSoonIngredients(
    ingredients: List<Ingredient>,
    today: LocalDate = LocalDate.now()
): List<Ingredient> {

    return ingredients
        .filter {
            it.remainingQuantity > 0
        }
        .mapNotNull { ingredient ->

            val daysLeft =
                pantryDaysLeft(
                    ingredient.expirationDate,
                    today
                ) ?: return@mapNotNull null

            if (daysLeft in 0L..5L) {
                ingredient to daysLeft
            } else {
                null
            }
        }
        .sortedBy {
            it.second
        }
        .map {
            it.first
        }
}

internal fun recipeUsesAnyIngredient(
    recipeIngredients: List<RecipeIngredient>,
    pantryIngredients: List<Ingredient>
): Boolean {

    if (
        recipeIngredients.isEmpty() ||
        pantryIngredients.isEmpty()
    ) {
        return false
    }

    val pantryNames =
        pantryIngredients
            .map {
                normalizeHomeIngredientName(
                    it.name
                )
            }
            .toSet()

    return recipeIngredients.any {
        normalizeHomeIngredientName(
            it.name
        ) in pantryNames
    }
}

private fun normalizeHomeIngredientName(
    value: String
): String {

    return value
        .trim()
        .lowercase(
            Locale("tr", "TR")
        )
        .replace("ı", "i")
        .replace("ş", "s")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ö", "o")
        .replace("ç", "c")
        .replace(
            Regex("[^a-z0-9\\s]"),
            " "
        )
        .replace(
            Regex("\\s+"),
            " "
        )
        .trim()
}

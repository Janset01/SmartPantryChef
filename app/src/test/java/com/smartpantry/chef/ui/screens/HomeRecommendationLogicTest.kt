package com.smartpantry.chef.ui.screens

import com.smartpantry.chef.data.Ingredient
import com.smartpantry.chef.data.MatchStatus
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.RecipeIngredient
import com.smartpantry.chef.data.RecipeMatch
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRecommendationLogicTest {

    @Test
    fun canMake_isRankedBeforeAlmostReady() {

        val recipe =
            Recipe(
                id = 1,
                name = "Tarif",
                category = "Akşam Yemeği",
                preparationTime = "20 dk",
                servings = "2 kişi",
                ingredients = "",
                instructions = "",
                imageUri = null
            )

        val almost =
            RecipeMatch(
                recipe = recipe.copy(
                    id = 2,
                    name = "Almost"
                ),
                totalIngredients = 2,
                matchedIngredients = 1,
                missingIngredients = emptyList(),
                status =
                    MatchStatus.ALMOST_READY,
                compatibilityPercent = 90
            )

        val canMake =
            RecipeMatch(
                recipe = recipe.copy(
                    id = 3,
                    name = "Can Make"
                ),
                totalIngredients = 2,
                matchedIngredients = 2,
                missingIngredients = emptyList(),
                status =
                    MatchStatus.CAN_MAKE,
                compatibilityPercent = 100
            )

        val result =
            rankHomeRecipeMatches(
                listOf(
                    almost,
                    canMake
                )
            )

        assertEquals(
            MatchStatus.CAN_MAKE,
            result.first().status
        )
    }

    @Test
    fun expiringSoon_includesTodayThroughFiveDays() {

        val today =
            LocalDate.of(
                2026,
                11,
                5
            )

        val ingredients =
            listOf(
                ingredient(
                    "Süt",
                    "05.11.2026"
                ),
                ingredient(
                    "Yoğurt",
                    "10.11.2026"
                ),
                ingredient(
                    "Peynir",
                    "11.11.2026"
                )
            )

        val result =
            expiringSoonIngredients(
                ingredients,
                today
            )

        assertEquals(
            listOf(
                "Süt",
                "Yoğurt"
            ),
            result.map {
                it.name
            }
        )
    }

    @Test
    fun recipeUsesExpiringIngredient_matchesTurkishCharacters() {

        val recipeIngredients =
            listOf(
                RecipeIngredient(
                    recipeId = 1,
                    name = "SÜT",
                    quantity = 200.0,
                    unit = "Mililitre"
                )
            )

        val pantry =
            listOf(
                ingredient(
                    "süt",
                    "08.11.2026"
                )
            )

        assertTrue(
            recipeUsesAnyIngredient(
                recipeIngredients,
                pantry
            )
        )

        assertFalse(
            recipeUsesAnyIngredient(
                listOf(
                    RecipeIngredient(
                        recipeId = 1,
                        name = "Un",
                        quantity = 100.0,
                        unit = "Gram"
                    )
                ),
                pantry
            )
        )
    }

    private fun ingredient(
        name: String,
        expirationDate: String
    ): Ingredient {

        return Ingredient(
            name = name,
            initialQuantity = 1.0,
            remainingQuantity = 1.0,
            unit = "Adet",
            expirationDate =
                expirationDate
        )
    }
}

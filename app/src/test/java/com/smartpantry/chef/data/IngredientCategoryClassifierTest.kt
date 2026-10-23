package com.smartpantry.chef.data

import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientCategoryClassifierTest {

    @Test
    fun exactMilk_goesToDairyBreakfast() {
        val result =
            IngredientCategoryClassifier
                .classify("süt")

        assertEquals(
            PantryMainCategory.DAIRY_BREAKFAST,
            result.mainCategory
        )
    }

    @Test
    fun typoBeefMince_stillFindsRedMeat() {
        val result =
            IngredientCategoryClassifier
                .classify("dana kıyam")

        assertEquals(
            PantryMainCategory.RED_MEAT,
            result.mainCategory
        )
    }

    @Test
    fun typoSalmon_stillFindsFish() {
        val result =
            IngredientCategoryClassifier
                .classify("somn")

        assertEquals(
            PantryMainCategory.FISH_SEAFOOD,
            result.mainCategory
        )
    }

    @Test
    fun gibberish_fallsBackToOther() {
        val result =
            IngredientCategoryClassifier
                .classify("asdasdxyz")

        assertEquals(
            PantryMainCategory.OTHER,
            result.mainCategory
        )
    }

    @Test
    fun manualCategoryOverridesAutomaticCategory() {
        val ingredient =
            Ingredient(
                id = 1,
                name = "süt",
                initialQuantity = 1.0,
                remainingQuantity = 1.0,
                unit = "Litre",
                expirationDate = "30.11.2026",
                imageUri = null,
                manualMainCategory =
                    PantryMainCategory.BEVERAGES.name,
                manualSubCategory = "Soğuk"
            )

        val result =
            IngredientCategoryClassifier
                .classify(ingredient)

        assertEquals(
            PantryMainCategory.BEVERAGES,
            result.mainCategory
        )

        assertEquals(
            "Soğuk",
            result.subCategory
        )
    }
}

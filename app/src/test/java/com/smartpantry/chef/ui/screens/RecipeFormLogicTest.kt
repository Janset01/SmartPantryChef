package com.smartpantry.chef.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeFormLogicTest {

    @Test
    fun sameIngredientAndSameUnit_areMerged() {
        val current =
            listOf(
                RecipeIngredientInput(
                    name = "Süt",
                    quantity = 200.0,
                    unit = "Mililitre"
                )
            )

        val result =
            mergeRecipeIngredient(
                current = current,
                incoming =
                    RecipeIngredientInput(
                        name = "süt",
                        quantity = 300.0,
                        unit = "Mililitre"
                    )
            )

        assertEquals(1, result.size)
        assertEquals(500.0, result.first().quantity, 0.0)
    }

    @Test
    fun sameIngredientDifferentUnit_areNotMerged() {
        val current =
            listOf(
                RecipeIngredientInput(
                    name = "Süt",
                    quantity = 200.0,
                    unit = "Mililitre"
                )
            )

        val result =
            mergeRecipeIngredient(
                current = current,
                incoming =
                    RecipeIngredientInput(
                        name = "Süt",
                        quantity = 1.0,
                        unit = "Litre"
                    )
            )

        assertEquals(2, result.size)
    }

    @Test
    fun turkishCharactersAndSpaces_areNormalized() {
        val normalized =
            normalizeRecipeIngredientName(
                "  SÜT   ÜRÜNÜ  "
            )

        assertEquals(
            "sut urunu",
            normalized
        )
    }

    @Test
    fun positiveInt_rejectsZeroNegativeAndInvalid() {
        assertNull(parsePositiveInt("0"))
        assertNull(parsePositiveInt("-2"))
        assertNull(parsePositiveInt("abc"))
        assertEquals(30, parsePositiveInt("30"))
    }
}

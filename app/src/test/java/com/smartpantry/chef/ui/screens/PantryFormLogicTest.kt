package com.smartpantry.chef.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PantryFormLogicTest {

    @Test
    fun sameIngredientName_normalizesTurkishAndSpaces() {
        assertTrue(
            pantryIngredientNamesMatch(
                "  SÜT  ",
                "süt"
            )
        )
    }

    @Test
    fun simplePluralSuffix_isNormalized() {
        assertTrue(
            pantryIngredientNamesMatch(
                "Domatesler",
                "domates"
            )
        )
    }

    @Test
    fun sameExpirationDate_matchesByDigits() {
        assertTrue(
            pantryExpirationDatesMatch(
                "30.11.2026",
                "30-11-2026"
            )
        )
    }

    @Test
    fun differentExpirationDates_doNotMatch() {
        assertFalse(
            pantryExpirationDatesMatch(
                "30.11.2026",
                "01.12.2026"
            )
        )
    }

    @Test
    fun gramsAndKilograms_areCompatible() {
        assertTrue(
            pantryCanConvertUnits(
                fromUnit = "Gram",
                toUnit = "Kilogram"
            )
        )
    }

    @Test
    fun millilitresAndLitres_areCompatible() {
        assertTrue(
            pantryCanConvertUnits(
                fromUnit = "Mililitre",
                toUnit = "Litre"
            )
        )
    }

    @Test
    fun massAndVolume_areNotCompatible() {
        assertFalse(
            pantryCanConvertUnits(
                fromUnit = "Gram",
                toUnit = "Litre"
            )
        )
    }

    @Test
    fun kilogramToGram_convertsCorrectly() {
        assertEquals(
            1000.0,
            pantryConvertQuantity(
                quantity = 1.0,
                fromUnit = "Kilogram",
                toUnit = "Gram"
            ),
            0.0
        )
    }

    @Test
    fun gramToKilogram_convertsCorrectly() {
        assertEquals(
            1.0,
            pantryConvertQuantity(
                quantity = 1000.0,
                fromUnit = "Gram",
                toUnit = "Kilogram"
            ),
            0.0
        )
    }

    @Test
    fun litreToMillilitre_convertsCorrectly() {
        assertEquals(
            1000.0,
            pantryConvertQuantity(
                quantity = 1.0,
                fromUnit = "Litre",
                toUnit = "Mililitre"
            ),
            0.0
        )
    }

    @Test
    fun incompatibleUnits_returnNull() {
        assertNull(
            pantryConvertQuantity(
                quantity = 1.0,
                fromUnit = "Adet",
                toUnit = "Gram"
            )
        )
    }

    @Test
    fun positiveQuantity_acceptsCommaDecimal() {
        assertEquals(
            1.5,
            parsePositivePantryQuantity("1,5"),
            0.0
        )
    }

    @Test
    fun positiveQuantity_rejectsZeroNegativeAndInvalid() {
        assertNull(
            parsePositivePantryQuantity("0")
        )
        assertNull(
            parsePositivePantryQuantity("-1")
        )
        assertNull(
            parsePositivePantryQuantity("abc")
        )
    }
}

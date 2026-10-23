package com.smartpantry.chef.ui.screens

import java.util.Locale

internal fun pantryIngredientNamesMatch(
    firstName: String,
    secondName: String
): Boolean {
    return normalizePantryIngredientName(firstName) ==
            normalizePantryIngredientName(secondName)
}

internal fun normalizePantryIngredientName(
    name: String
): String {
    val cleaned =
        name
            .trim()
            .lowercase(Locale("tr", "TR"))
            .replace("ı", "i")
            .replace("ş", "s")
            .replace("ğ", "g")
            .replace("ü", "u")
            .replace("ö", "o")
            .replace("ç", "c")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    return when {
        cleaned.length > 5 &&
                cleaned.endsWith("lar") ->
            cleaned.dropLast(3).trim()

        cleaned.length > 5 &&
                cleaned.endsWith("ler") ->
            cleaned.dropLast(3).trim()

        else ->
            cleaned
    }
}

internal fun pantryExpirationDatesMatch(
    firstDate: String,
    secondDate: String
): Boolean {
    return firstDate.filter { it.isDigit() } ==
            secondDate.filter { it.isDigit() }
}

private enum class PantryUnitFamily {
    COUNT,
    MASS,
    VOLUME,
    UNKNOWN
}

internal fun normalizePantryUnit(
    unit: String
): String {
    return unit
        .trim()
        .lowercase(Locale("tr", "TR"))
        .replace("ı", "i")
        .replace("ş", "s")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ö", "o")
        .replace("ç", "c")
        .replace(Regex("[^a-z0-9]"), "")
}

private fun pantryUnitFamily(
    unit: String
): PantryUnitFamily {
    return when (normalizePantryUnit(unit)) {
        "adet",
        "tane" ->
            PantryUnitFamily.COUNT

        "g",
        "gr",
        "gram",
        "kg",
        "kilogram" ->
            PantryUnitFamily.MASS

        "ml",
        "mililitre",
        "mililiter",
        "millilitre",
        "milliliter",
        "l",
        "lt",
        "litre",
        "liter" ->
            PantryUnitFamily.VOLUME

        else ->
            PantryUnitFamily.UNKNOWN
    }
}

internal fun pantryCanConvertUnits(
    fromUnit: String,
    toUnit: String
): Boolean {
    val from = pantryUnitFamily(fromUnit)
    val to = pantryUnitFamily(toUnit)

    return if (
        normalizePantryUnit(fromUnit) ==
        normalizePantryUnit(toUnit)
    ) {
        true
    } else {
        from != PantryUnitFamily.UNKNOWN &&
                from == to
    }
}

internal fun pantryConvertQuantity(
    quantity: Double,
    fromUnit: String,
    toUnit: String
): Double? {
    if (
        normalizePantryUnit(fromUnit) ==
        normalizePantryUnit(toUnit)
    ) {
        return quantity
    }

    val fromFamily = pantryUnitFamily(fromUnit)
    val toFamily = pantryUnitFamily(toUnit)

    if (
        fromFamily == PantryUnitFamily.UNKNOWN ||
        fromFamily != toFamily
    ) {
        return null
    }

    val base =
        when (normalizePantryUnit(fromUnit)) {
            "kg",
            "kilogram" ->
                quantity * 1000.0

            "l",
            "lt",
            "litre",
            "liter" ->
                quantity * 1000.0

            else ->
                quantity
        }

    return when (normalizePantryUnit(toUnit)) {
        "kg",
        "kilogram" ->
            base / 1000.0

        "l",
        "lt",
        "litre",
        "liter" ->
            base / 1000.0

        else ->
            base
    }
}

internal fun parsePositivePantryQuantity(
    value: String
): Double? {
    return value
        .trim()
        .replace(",", ".")
        .toDoubleOrNull()
        ?.takeIf { it > 0.0 }
}

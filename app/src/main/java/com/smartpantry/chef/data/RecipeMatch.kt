package com.smartpantry.chef.data

import java.util.Locale
import kotlin.math.roundToInt

data class MissingIngredient(
    val name: String,
    val missingQuantity: Double,
    val unit: String
)

data class RecipeMatch(
    val recipe: Recipe,
    val totalIngredients: Int,
    val matchedIngredients: Int,
    val missingIngredients: List<MissingIngredient>,
    val status: MatchStatus,
    val compatibilityPercent: Int
)

enum class MatchStatus {
    CAN_MAKE,
    ALMOST_READY,
    MISSING
}

object RecipeMatcher {

    fun matchRecipe(
        recipe: Recipe,
        recipeIngredients: List<RecipeIngredient>,
        pantryIngredients: List<Ingredient>
    ): RecipeMatch {

        if (recipeIngredients.isEmpty()) {
            return RecipeMatch(
                recipe = recipe,
                totalIngredients = 0,
                matchedIngredients = 0,
                missingIngredients = emptyList(),
                status = MatchStatus.MISSING,
                compatibilityPercent = 0
            )
        }

        var matchedCount = 0
        val missingList = mutableListOf<MissingIngredient>()

        recipeIngredients.forEach { required ->

            val requiredBase =
                convertToBase(
                    required.quantity,
                    required.unit
                )

            if (requiredBase == null) {
                missingList.add(
                    MissingIngredient(
                        name = required.name,
                        missingQuantity = required.quantity,
                        unit = required.unit
                    )
                )

                return@forEach
            }

            val matchingPantryIngredients =
                pantryIngredients.filter { pantry ->

                    pantry.remainingQuantity > 0 &&
                            ingredientNamesMatch(
                                pantry.name,
                                required.name
                            ) &&
                            areUnitsCompatible(
                                required.unit,
                                pantry.unit
                            )
                }

            if (matchingPantryIngredients.isEmpty()) {
                missingList.add(
                    MissingIngredient(
                        name = required.name,
                        missingQuantity = required.quantity,
                        unit = required.unit
                    )
                )

                return@forEach
            }

            val totalPantryBase =
                matchingPantryIngredients.sumOf { pantry ->
                    convertToBase(
                        pantry.remainingQuantity,
                        pantry.unit
                    ) ?: 0.0
                }

            if (totalPantryBase >= requiredBase) {
                matchedCount++
            } else {
                val missingBase =
                    requiredBase - totalPantryBase

                val missingQuantity =
                    convertFromBase(
                        missingBase,
                        required.unit
                    )

                missingList.add(
                    MissingIngredient(
                        name = required.name,
                        missingQuantity = missingQuantity,
                        unit = required.unit
                    )
                )
            }
        }

        val total = recipeIngredients.size

        val percent =
            (
                    matchedCount.toDouble() /
                            total.toDouble() *
                            100
                    ).roundToInt()

        val status =
            when {
                missingList.isEmpty() ->
                    MatchStatus.CAN_MAKE

                missingList.size == 1 ->
                    MatchStatus.ALMOST_READY

                else ->
                    MatchStatus.MISSING
            }

        return RecipeMatch(
            recipe = recipe,
            totalIngredients = total,
            matchedIngredients = matchedCount,
            missingIngredients = missingList,
            status = status,
            compatibilityPercent = percent
        )
    }

    private fun ingredientNamesMatch(
        firstName: String,
        secondName: String
    ): Boolean {

        return normalizeName(firstName) ==
                normalizeName(secondName)
    }

    private fun normalizeName(
        name: String
    ): String {

        val cleanedName =
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
            cleanedName.length > 5 &&
                    cleanedName.endsWith("lar") ->
                cleanedName.dropLast(3).trim()

            cleanedName.length > 5 &&
                    cleanedName.endsWith("ler") ->
                cleanedName.dropLast(3).trim()

            else ->
                cleanedName
        }
    }

    private fun normalizeUnit(
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
    }

    private fun areUnitsCompatible(
        firstUnit: String,
        secondUnit: String
    ): Boolean {

        val firstType =
            unitType(firstUnit)

        val secondType =
            unitType(secondUnit)

        return firstType != "UNKNOWN" &&
                secondType != "UNKNOWN" &&
                firstType == secondType
    }

    private fun unitType(
        unit: String
    ): String {

        return when (
            normalizeUnit(unit)
        ) {

            "gram",
            "kilogram" ->
                "WEIGHT"

            "mililitre",
            "litre" ->
                "VOLUME"

            "adet" ->
                "COUNT"

            else ->
                "UNKNOWN"
        }
    }

    private fun convertToBase(
        quantity: Double,
        unit: String
    ): Double? {

        return when (
            normalizeUnit(unit)
        ) {

            "gram" ->
                quantity

            "kilogram" ->
                quantity * 1000

            "mililitre" ->
                quantity

            "litre" ->
                quantity * 1000

            "adet" ->
                quantity

            else ->
                null
        }
    }

    private fun convertFromBase(
        quantity: Double,
        targetUnit: String
    ): Double {

        return when (
            normalizeUnit(targetUnit)
        ) {

            "kilogram" ->
                quantity / 1000

            "litre" ->
                quantity / 1000

            else ->
                quantity
        }
    }
}

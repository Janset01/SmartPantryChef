package com.smartpantry.chef.data

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

        // Tarifin yapılandırılmış malzemesi yoksa
        // "yapabilirsin" dememeliyiz.
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

        val missingList =
            mutableListOf<MissingIngredient>()

        recipeIngredients.forEach { required ->

            val pantryIngredient =
                pantryIngredients.firstOrNull { pantry ->

                    normalizeName(pantry.name) ==
                            normalizeName(required.name)
                }

            if (pantryIngredient == null) {

                missingList.add(
                    MissingIngredient(
                        name = required.name,
                        missingQuantity =
                            required.quantity,
                        unit = required.unit
                    )
                )

                return@forEach
            }

            // Gram/Kilogram kendi arasında,
            // ml/Litre kendi arasında,
            // Adet de sadece Adet ile karşılaştırılır.
            if (
                !areUnitsCompatible(
                    required.unit,
                    pantryIngredient.unit
                )
            ) {

                missingList.add(
                    MissingIngredient(
                        name = required.name,
                        missingQuantity =
                            required.quantity,
                        unit = required.unit
                    )
                )

                return@forEach
            }

            val requiredBase =
                convertToBase(
                    required.quantity,
                    required.unit
                )

            val pantryBase =
                convertToBase(
                    pantryIngredient.remainingQuantity,
                    pantryIngredient.unit
                )

            if (
                requiredBase == null ||
                pantryBase == null
            ) {

                missingList.add(
                    MissingIngredient(
                        name = required.name,
                        missingQuantity =
                            required.quantity,
                        unit = required.unit
                    )
                )

                return@forEach
            }

            if (pantryBase >= requiredBase) {

                matchedCount++

            } else {

                val missingBase =
                    requiredBase - pantryBase

                val missingQuantity =
                    convertFromBase(
                        missingBase,
                        required.unit
                    )

                missingList.add(
                    MissingIngredient(
                        name = required.name,
                        missingQuantity =
                            missingQuantity,
                        unit = required.unit
                    )
                )
            }
        }

        val total =
            recipeIngredients.size

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

                // Sadece 1 malzeme eksikse
                // neredeyse hazır kabul ediyoruz.
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

    private fun normalizeName(
        name: String
    ): String {

        return name
            .trim()
            .lowercase()
            .replace("ı", "i")
            .replace("ş", "s")
            .replace("ğ", "g")
            .replace("ü", "u")
            .replace("ö", "o")
            .replace("ç", "c")
    }

    private fun normalizeUnit(
        unit: String
    ): String {

        return unit
            .trim()
            .lowercase()
    }

    private fun areUnitsCompatible(
        firstUnit: String,
        secondUnit: String
    ): Boolean {

        return unitType(firstUnit) ==
                unitType(secondUnit)
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

            // Ağırlığın temel birimi Gram
            "gram" ->
                quantity

            "kilogram" ->
                quantity * 1000

            // Hacmin temel birimi Mililitre
            "mililitre" ->
                quantity

            "litre" ->
                quantity * 1000

            // Adet kendi başına
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
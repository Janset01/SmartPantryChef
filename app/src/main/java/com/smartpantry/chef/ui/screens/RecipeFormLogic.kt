package com.smartpantry.chef.ui.screens

import java.util.Locale

data class RecipeIngredientInput(
    val name: String,
    val quantity: Double,
    val unit: String
)

internal fun normalizeRecipeIngredientName(
    value: String
): String {
    return value
        .trim()
        .lowercase(Locale("tr", "TR"))
        .replace("ı", "i")
        .replace("ş", "s")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ö", "o")
        .replace("ç", "c")
        .replace(Regex("\\s+"), " ")
}

internal fun mergeRecipeIngredient(
    current: List<RecipeIngredientInput>,
    incoming: RecipeIngredientInput
): List<RecipeIngredientInput> {
    val cleanIncoming =
        incoming.copy(
            name = incoming.name.trim(),
            unit = incoming.unit.trim()
        )

    val existingIndex =
        current.indexOfFirst { existing ->
            normalizeRecipeIngredientName(existing.name) ==
                    normalizeRecipeIngredientName(cleanIncoming.name) &&
                    existing.unit.equals(
                        cleanIncoming.unit,
                        ignoreCase = true
                    )
        }

    if (existingIndex < 0) {
        return current + cleanIncoming
    }

    return current.mapIndexed { index, existing ->
        if (index == existingIndex) {
            existing.copy(
                quantity =
                    existing.quantity +
                            cleanIncoming.quantity
            )
        } else {
            existing
        }
    }
}

internal fun parsePositiveInt(
    value: String
): Int? {
    return value
        .trim()
        .toIntOrNull()
        ?.takeIf { it > 0 }
}

package com.smartpantry.chef.data

import java.util.Locale

/**
 * Calculates a user's new vote without double-counting rating edits.
 *
 * Keep the full Double in Room: rounding the persisted average before the
 * next vote would progressively corrupt the weighted mean. Format only at
 * the presentation boundary.
 */
object RecipeRatingCalculator {
    fun applyPersonalRating(current: RecipeEngagement, rating: Int): RecipeEngagement {
        require(rating in 1..5) { "Puan 1 ile 5 arasında olmalı." }

        val count = current.ratingCount.coerceAtLeast(0)
        val previousPersonal = current.myRating.takeIf { it in 1..5 } ?: 0
        if (count > 0 && previousPersonal == rating) return current

        val previousAverage = current.averageRating
            .takeIf { it.isFinite() && it in 0.0..5.0 } ?: 0.0
        val wasRated = count > 0 && previousPersonal != 0
        val updatedCount = if (wasRated) count else count + 1
        val updatedTotal = previousAverage * count -
            (if (wasRated) previousPersonal else 0) + rating

        return current.copy(
            myRating = rating,
            ratingCount = updatedCount,
            averageRating = (updatedTotal / updatedCount).coerceIn(0.0, 5.0)
        )
    }

    fun displayAverage(average: Double): String =
        String.format(Locale.US, "%.1f", average.takeIf { it.isFinite() } ?: 0.0)
}

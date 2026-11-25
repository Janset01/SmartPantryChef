package com.smartpantry.chef.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeRatingCalculatorTest {
    @Test fun firstVoteStartsWithCorrectAverage() {
        val updated = RecipeRatingCalculator.applyPersonalRating(RecipeEngagement(recipeId = 5), 4)
        assertEquals(1, updated.ratingCount)
        assertEquals(4.0, updated.averageRating, 0.000001)
        assertEquals(4, updated.myRating)
    }

    @Test fun demoVotesPlusFirstUserVoteProduceMathematicallyCorrectMean() {
        val demo = RecipeEngagement(recipeId = 10, averageRating = 4.5, ratingCount = 2)
        val updated = RecipeRatingCalculator.applyPersonalRating(demo, 4)
        assertEquals(3, updated.ratingCount)
        assertEquals(13.0 / 3.0, updated.averageRating, 0.000001)
        assertEquals("4.3", RecipeRatingCalculator.displayAverage(updated.averageRating))
    }

    @Test fun changingExistingVoteReplacesItInsteadOfIncreasingCount() {
        val first = RecipeRatingCalculator.applyPersonalRating(
            RecipeEngagement(recipeId = 7, averageRating = 4.5, ratingCount = 2), 4
        )
        val revised = RecipeRatingCalculator.applyPersonalRating(first, 5)
        assertEquals(3, revised.ratingCount)
        assertEquals(14.0 / 3.0, revised.averageRating, 0.000001)
        assertEquals("4.7", RecipeRatingCalculator.displayAverage(revised.averageRating))
    }

    @Test fun repeatedSameStarDoesNotAddAnotherRating() {
        val first = RecipeRatingCalculator.applyPersonalRating(RecipeEngagement(recipeId = 4), 5)
        val repeated = RecipeRatingCalculator.applyPersonalRating(first, 5)
        assertEquals(1, repeated.ratingCount)
        assertEquals(5.0, repeated.averageRating, 0.000001)
    }

    @Test fun repeatedEditsRetainAccurateTotal() {
        var result = RecipeRatingCalculator.applyPersonalRating(
            RecipeEngagement(recipeId = 3, averageRating = 4.5, ratingCount = 2), 4
        )
        repeat(10) {
            result = RecipeRatingCalculator.applyPersonalRating(result, 5)
            result = RecipeRatingCalculator.applyPersonalRating(result, 4)
        }
        assertEquals(3, result.ratingCount)
        assertEquals(13.0 / 3.0, result.averageRating, 0.000001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun outOfRangeVoteIsRejected() {
        RecipeRatingCalculator.applyPersonalRating(RecipeEngagement(recipeId = 1), 6)
    }
}

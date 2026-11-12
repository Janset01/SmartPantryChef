package com.smartpantry.chef.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

data class WeeklyRecipeViews(val recipeId: Int, val viewCount: Int)

@Dao
interface RecipeViewEventDao {
    @Insert
    suspend fun record(event: RecipeViewEvent)

    @Query("SELECT recipeId, COUNT(*) AS viewCount FROM recipe_view_events WHERE viewedAt >= :since GROUP BY recipeId ORDER BY viewCount DESC, recipeId DESC")
    suspend fun topSince(since: Long): List<WeeklyRecipeViews>

    @Query("DELETE FROM recipe_view_events WHERE viewedAt < :before")
    suspend fun deleteBefore(before: Long)
}

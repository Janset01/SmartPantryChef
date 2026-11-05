package com.smartpantry.chef.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeEngagementDao {

    @Query(
        "SELECT * FROM recipe_engagement " +
                "WHERE recipeId = :recipeId LIMIT 1"
    )
    suspend fun getForRecipe(
        recipeId: Int
    ): RecipeEngagement?

    @Query("SELECT * FROM recipe_engagement")
    suspend fun getAll():
            List<RecipeEngagement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(
        engagement: RecipeEngagement
    )
}

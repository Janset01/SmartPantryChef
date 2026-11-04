package com.smartpantry.chef.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeMediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(
        media: List<RecipeMedia>
    )

    @Query(
        "SELECT * FROM recipe_media " +
                "WHERE recipeId = :recipeId " +
                "ORDER BY sortOrder ASC, id ASC"
    )
    suspend fun getMediaForRecipe(
        recipeId: Int
    ): List<RecipeMedia>

    @Query(
        "DELETE FROM recipe_media " +
                "WHERE recipeId = :recipeId"
    )
    suspend fun deleteMediaForRecipe(
        recipeId: Int
    )
}

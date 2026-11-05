package com.smartpantry.chef.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeCommentDao {

    @Insert
    suspend fun insertComment(
        comment: RecipeComment
    ): Long

    @Query(
        "SELECT * FROM recipe_comments " +
                "WHERE recipeId = :recipeId " +
                "ORDER BY createdAt DESC, id DESC"
    )
    suspend fun getCommentsForRecipe(
        recipeId: Int
    ): List<RecipeComment>
}

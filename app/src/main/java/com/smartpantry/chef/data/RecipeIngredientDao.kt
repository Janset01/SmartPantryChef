package com.smartpantry.chef.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeIngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(
        recipeIngredient: RecipeIngredient
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(
        recipeIngredients: List<RecipeIngredient>
    )

    @Query(
        "SELECT * FROM recipe_ingredients " +
                "WHERE recipeId = :recipeId ORDER BY id ASC"
    )
    suspend fun getIngredientsForRecipe(
        recipeId: Int
    ): List<RecipeIngredient>

    @Query(
        "SELECT * FROM recipe_ingredients"
    )
    suspend fun getAllRecipeIngredients():
            List<RecipeIngredient>

    @Query(
        "DELETE FROM recipe_ingredients " +
                "WHERE recipeId = :recipeId"
    )
    suspend fun deleteIngredientsForRecipe(
        recipeId: Int
    )
}
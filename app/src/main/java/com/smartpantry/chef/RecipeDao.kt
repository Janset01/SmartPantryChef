package com.smartpantry.chef.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // tarif ekler
    suspend fun insertRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe) // tarif siler

    @Query("SELECT * FROM recipes ORDER BY id DESC") // kayıtlı tarifleri getirir
    suspend fun getAllRecipes(): List<Recipe>
}
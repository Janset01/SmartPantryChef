package com.smartpantry.chef.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_view_events",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["recipeId"]), Index(value = ["viewedAt"])]
)
data class RecipeViewEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Int,
    val viewedAt: Long = System.currentTimeMillis()
)

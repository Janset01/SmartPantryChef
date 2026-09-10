package com.smartpantry.chef.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"])
    ]
)
data class RecipeIngredient(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val recipeId: Int,

    val name: String,

    val quantity: Double,

    val unit: String
)
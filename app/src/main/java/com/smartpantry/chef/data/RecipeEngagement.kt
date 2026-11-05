package com.smartpantry.chef.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_engagement",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeEngagement(
    @PrimaryKey
    val recipeId: Int,
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val favoriteCount: Int = 0,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isFavorite: Boolean = false,
    val isLiked: Boolean = false,
    val myRating: Int = 0
)

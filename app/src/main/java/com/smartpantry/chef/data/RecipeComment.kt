package com.smartpantry.chef.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_comments",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("recipeId"),
        Index("parentCommentId")
    ]
)
data class RecipeComment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: Int,
    val authorNickname: String,
    val text: String,
    val createdAt: Long,
    val parentCommentId: Int? = null,
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val myReaction: Int = 0
)

package com.smartpantry.chef.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val category: String,
    val preparationTime: String,
    val servings: String,
    val ingredients: String,
    val instructions: String,
    val imageUri: String?
)
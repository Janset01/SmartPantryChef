package com.smartpantry.chef.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    // İlk eklenen miktar
    val initialQuantity: Double,

    // Şu an kalan miktar
    val remainingQuantity: Double,

    // Adet, Gram, Kilogram, Mililitre, Litre
    val unit: String,

    // Son kullanma tarihi
    val expirationDate: String,

    // Kamera / galeri fotoğrafı
    val imageUri: String? = null
)
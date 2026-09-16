package com.smartpantry.chef.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ShoppingItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem): Long

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)

    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC, id DESC")
    suspend fun getAllShoppingItems(): List<ShoppingItem>

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAllShoppingItems()
}
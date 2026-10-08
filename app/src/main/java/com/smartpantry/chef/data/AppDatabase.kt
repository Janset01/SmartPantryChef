package com.smartpantry.chef.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Recipe::class,
        Ingredient::class,
        RecipeIngredient::class,
        ShoppingItem::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    abstract fun ingredientDao(): IngredientDao

    abstract fun recipeIngredientDao(): RecipeIngredientDao

    abstract fun shoppingItemDao(): ShoppingItemDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 =
            object : Migration(5, 6) {

                override fun migrate(database: SupportSQLiteDatabase) {

                    database.execSQL(
                        "ALTER TABLE ingredients " +
                                "ADD COLUMN manualMainCategory TEXT"
                    )

                    database.execSQL(
                        "ALTER TABLE ingredients " +
                                "ADD COLUMN manualSubCategory TEXT"
                    )
                }
            }

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "smart_pantry_database"
                    )
                        .addMigrations(MIGRATION_5_6)
                        .fallbackToDestructiveMigration()
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}

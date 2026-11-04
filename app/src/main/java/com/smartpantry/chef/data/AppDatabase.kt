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
        RecipeMedia::class,
        ShoppingItem::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    abstract fun ingredientDao(): IngredientDao

    abstract fun recipeIngredientDao(): RecipeIngredientDao

    abstract fun recipeMediaDao(): RecipeMediaDao

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

        private val MIGRATION_6_7 =
            object : Migration(6, 7) {

                override fun migrate(database: SupportSQLiteDatabase) {

                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS recipe_media (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "recipeId INTEGER NOT NULL, " +
                                "mediaType TEXT NOT NULL, " +
                                "uri TEXT NOT NULL, " +
                                "sortOrder INTEGER NOT NULL, " +
                                "FOREIGN KEY(recipeId) REFERENCES recipes(id) " +
                                "ON UPDATE NO ACTION ON DELETE CASCADE)"
                    )

                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS " +
                                "index_recipe_media_recipeId " +
                                "ON recipe_media(recipeId)"
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
                        .addMigrations(
                            MIGRATION_5_6,
                            MIGRATION_6_7
                        )
                        .fallbackToDestructiveMigration()
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}

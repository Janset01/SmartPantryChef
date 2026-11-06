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
        RecipeEngagement::class,
        RecipeComment::class,
        ShoppingItem::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    abstract fun ingredientDao(): IngredientDao

    abstract fun recipeIngredientDao(): RecipeIngredientDao

    abstract fun recipeMediaDao(): RecipeMediaDao

    abstract fun recipeEngagementDao(): RecipeEngagementDao

    abstract fun recipeCommentDao(): RecipeCommentDao

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

        private val MIGRATION_7_8 =
            object : Migration(7, 8) {

                override fun migrate(database: SupportSQLiteDatabase) {

                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS recipe_engagement (" +
                                "recipeId INTEGER NOT NULL PRIMARY KEY, " +
                                "averageRating REAL NOT NULL, " +
                                "ratingCount INTEGER NOT NULL, " +
                                "favoriteCount INTEGER NOT NULL, " +
                                "likeCount INTEGER NOT NULL, " +
                                "commentCount INTEGER NOT NULL, " +
                                "FOREIGN KEY(recipeId) REFERENCES recipes(id) " +
                                "ON UPDATE NO ACTION ON DELETE CASCADE)"
                    )
                }
            }

        private val MIGRATION_8_9 =
            object : Migration(8, 9) {

                override fun migrate(database: SupportSQLiteDatabase) {

                    database.execSQL(
                        "ALTER TABLE recipe_engagement " +
                                "ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0"
                    )
                }
            }

        private val MIGRATION_9_10 =
            object : Migration(9, 10) {

                override fun migrate(database: SupportSQLiteDatabase) {

                    database.execSQL(
                        "ALTER TABLE recipe_engagement " +
                                "ADD COLUMN isLiked INTEGER NOT NULL DEFAULT 0"
                    )
                }
            }

        private val MIGRATION_10_11 =
            object : Migration(10, 11) {

                override fun migrate(database: SupportSQLiteDatabase) {

                    database.execSQL(
                        "ALTER TABLE recipe_engagement " +
                                "ADD COLUMN myRating INTEGER NOT NULL DEFAULT 0"
                    )

                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS recipe_comments (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "recipeId INTEGER NOT NULL, " +
                                "authorNickname TEXT NOT NULL, " +
                                "text TEXT NOT NULL, " +
                                "createdAt INTEGER NOT NULL, " +
                                "FOREIGN KEY(recipeId) REFERENCES recipes(id) " +
                                "ON UPDATE NO ACTION ON DELETE CASCADE)"
                    )

                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS " +
                                "index_recipe_comments_recipeId " +
                                "ON recipe_comments(recipeId)"
                    )
                }
            }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE recipe_comments ADD COLUMN parentCommentId INTEGER")
                database.execSQL("ALTER TABLE recipe_comments ADD COLUMN likeCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE recipe_comments ADD COLUMN dislikeCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE recipe_comments ADD COLUMN myReaction INTEGER NOT NULL DEFAULT 0")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recipe_comments_parentCommentId ON recipe_comments(parentCommentId)")
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
                            MIGRATION_6_7,
                            MIGRATION_7_8,
                            MIGRATION_8_9,
                            MIGRATION_9_10,
                            MIGRATION_10_11,
                            MIGRATION_11_12
                        )
                        .fallbackToDestructiveMigration()
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }
}

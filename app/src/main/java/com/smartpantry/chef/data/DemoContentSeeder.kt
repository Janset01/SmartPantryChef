package com.smartpantry.chef.data

import android.content.Context
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Test-only starter catalog. Never deletes user data. */
object DemoContentSeeder {
    private val locale = Locale.forLanguageTag("tr-TR")
    suspend fun ensureInstalled(context: Context) {
        val prefs = context.getSharedPreferences("smartpantry_demo_v1", Context.MODE_PRIVATE)
        val db = AppDatabase.getDatabase(context)
        val data = JSONArray(context.assets.open("demo_recipes.json").bufferedReader().use { it.readText() })
        if (prefs.getBoolean("installed", false)) {
            seedNewLeekRecipes(db, data, prefs)
            refreshDemoPhotosAndLegacyFilter(db, data, prefs)
            return
        }
        val ids = mutableMapOf<String, Int>()
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", locale)
        db.withTransaction {
            val names = db.recipeDao().getAllRecipes()
                .map { it.name.trim().lowercase(locale) }.toMutableSet()
            for (i in 0 until data.length()) {
                val item = data.getJSONObject(i)
                val name = item.getString("name").trim()
                if (!names.add(name.lowercase(locale))) continue
                val rawIngredients = item.getJSONArray("ingredients")
                val ingredients = (0 until rawIngredients.length()).map { n ->
                    val it = rawIngredients.getJSONObject(n)
                    Triple(it.getString("name"), it.getDouble("quantity"), it.getString("unit"))
                }
                val steps = item.getJSONArray("steps")
                val instructions = (0 until steps.length())
                    .joinToString("\n\n") { n -> (n + 1).toString() + ". " + steps.getString(n) }
                val recipe = Recipe(name = name, category = item.getString("category"),
                    preparationTime = item.getInt("minutes").toString() + " dk",
                    servings = item.getInt("servings").toString() + " kişi",
                    ingredients = ingredients.joinToString("\n") { (title, qty, unit) ->
                        (if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()) + " " + unit + " " + title
                    },
                    instructions = instructions,
                    imageUri = item.optString("image").takeIf { it.startsWith("https://") }
                )
                val id = db.recipeDao().insertRecipe(recipe).toInt()
                ids[name] = id
                val demoMedia = mutableListOf<RecipeMedia>()
                recipe.imageUri?.let { demoPhoto ->
                    demoMedia += RecipeMedia(recipeId = id, mediaType = RecipeMedia.TYPE_IMAGE,
                        uri = demoPhoto, sortOrder = 0)
                }
                val tutorialUrl = when (name) {
                    "Domates Soslu Makarna" ->
                        "https://commons.wikimedia.org/wiki/File:Cooking_with_Laura_Homemade_Pasta.webm"
                    "Nohutlu Pirinç Pilavı" ->
                        "https://commons.wikimedia.org/wiki/File:Rice_Cooking_on_a_gas_stove.webm"
                    "Tavuklu Nohutlu Bulgur" ->
                        "https://commons.wikimedia.org/wiki/File:Cooking_Chicken_and_Rice.webm"
                    else -> null
                }
                tutorialUrl?.let { url ->
                    demoMedia += RecipeMedia(recipeId = id, mediaType = RecipeMedia.TYPE_VIDEO,
                        uri = url, sortOrder = 1)
                }
                if (demoMedia.isNotEmpty()) db.recipeMediaDao().insertMedia(demoMedia)
                db.recipeIngredientDao().insertRecipeIngredients(ingredients.map { (title, qty, unit) ->
                    RecipeIngredient(recipeId = id, name = title, quantity = qty, unit = unit)
                })
                db.recipeCommentDao().insertComment(
                    RecipeComment(recipeId = id, authorNickname = "Demo • Tarif Denemesi",
                        text = listOf("Demo yorum: Anlatım anlaşılır, denemeye değer.",
                            "Demo yorum: Lezzet dengesi hoşuma gitti.",
                            "Demo yorum: Malzeme listesi pratik.")[i % 3],
                        createdAt = System.currentTimeMillis() - 120000L))
                db.recipeCommentDao().insertComment(
                    RecipeComment(recipeId = id, authorNickname = "Demo • Test Yorumu",
                        text = listOf("Demo yorum: Ben biraz daha baharat eklerdim.",
                            "Demo yorum: Hazırlık süresi biraz daha uzun sürdü.",
                            "Demo yorum: Bir sonraki denemede porsiyonu değiştireceğim.")[i % 3],
                        createdAt = System.currentTimeMillis() - 60000L))
                db.recipeEngagementDao().upsert(RecipeEngagement(
                    recipeId = id, averageRating = if (i % 4 == 0) 3.5 else 4.5,
                    ratingCount = 2, commentCount = 2))
            }
            val existingPantry = db.ingredientDao().getAllIngredients()
                .map { it.name.trim().lowercase(locale) }.toSet()
            val stock = listOf(
                Stock("yumurta", 10.0, "Adet", 4), Stock("süt", 2.0, "Litre", 3),
                Stock("domates", 6.0, "Adet", 2), Stock("salatalık", 3.0, "Adet", 5),
                Stock("beyaz peynir", 250.0, "Gram", 6),
                Stock("tavuk göğsü", 500.0, "Gram", 1),
                Stock("patates", 4.0, "Adet", 10),
                Stock("kırmızı mercimek", 500.0, "Gram", 100),
                Stock("pirinç", 750.0, "Gram", 130),
                Stock("yoğurt", 500.0, "Gram", 5), Stock("kabak", 2.0, "Adet", 3),
                Stock("ceviz", 200.0, "Gram", 45),
                Stock("taze fasulye", 450.0, "Gram", 2),
                Stock("avokado", 2.0, "Adet", 3),
                Stock("karides", 300.0, "Gram", 1)
            )
            stock.filterNot { it.name.lowercase(locale) in existingPantry }.forEach {
                db.ingredientDao().insertIngredient(
                    Ingredient(name = it.name, initialQuantity = it.quantity,
                        remainingQuantity = it.quantity, unit = it.unit,
                        expirationDate = today.plusDays(it.days).format(dateFormatter)))
            }
            if (db.shoppingItemDao().getAllShoppingItems().isEmpty()) {
                listOf(Triple("mantar",250.0,"Gram"),Triple("limon",3.0,"Adet"),
                    Triple("zeytinyağı",1.0,"Litre"),Triple("kaşar peyniri",300.0,"Gram"),
                    Triple("bulgur",500.0,"Gram"),Triple("marul",1.0,"Adet")).forEach { (name,qty,unit) ->
                    db.shoppingItemDao().insertShoppingItem(ShoppingItem(name=name,quantity=qty,unit=unit))
                }
            }
        }
        val planPrefs = context.getSharedPreferences("weekly_meal_plans_v1", Context.MODE_PRIVATE)
        if (planPrefs.getString("entries", "[]") == "[]") {
            val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
            val selections = listOf(Triple(0,0,"Menemen"),Triple(0,1,"Sebzeli Bulgur Pilavı"),
                Triple(0,2,"Tavuk Sote"),Triple(1,0,"Pankek"),
                Triple(1,1,"Domates Soslu Makarna"),Triple(1,2,"Mercimek Çorbası"),
                Triple(2,0,"Peynirli Tost"),Triple(2,2,"Fırında Sebzeli Köfte"),
                Triple(3,1,"Ton Balıklı Salata"),Triple(4,2,"Etli Patates Yemeği"))
            val json = JSONArray()
            selections.forEachIndexed { i, (day,slot,name) ->
                json.put(JSONObject().put("id",System.currentTimeMillis()+i)
                    .put("date",start.plusDays(day.toLong()).toString()).put("slot",slot)
                    .put("name",name).put("recipeId",ids[name] ?: JSONObject.NULL).put("done",false))
            }
            planPrefs.edit().putString("entries",json.toString()).apply()
        }
        prefs.edit().putBoolean("installed",true)
            .putString("recipe_ids", ids.values.joinToString(","))
            .apply()
    }

    private suspend fun seedNewLeekRecipes(
        db: AppDatabase, data: JSONArray,
        prefs: android.content.SharedPreferences
    ) {
        val titles = setOf("Zeytinyağlı Pırasa", "Pırasalı Börek")
        val known = db.recipeDao().getAllRecipes().map { it.name }.toSet()
        val ids = prefs.getString("recipe_ids", "").orEmpty()
            .split(",").mapNotNull { it.toIntOrNull() }.toMutableSet()
        db.withTransaction {
            for (index in 0 until data.length()) {
                val obj = data.getJSONObject(index)
                val name = obj.getString("name")
                if (name !in titles || name in known) continue
                val input = obj.getJSONArray("ingredients")
                val items = (0 until input.length()).map { n ->
                    val it = input.getJSONObject(n)
                    Triple(it.getString("name"), it.getDouble("quantity"), it.getString("unit"))
                }
                val steps = obj.getJSONArray("steps")
                val text = (0 until steps.length()).joinToString("\n\n") { n ->
                    (n + 1).toString() + ". " + steps.getString(n)
                }
                val photo = obj.optString("image").takeIf { it.startsWith("https://") }
                val recipe = Recipe(
                    name = name, category = obj.getString("category"),
                    preparationTime = obj.getInt("minutes").toString() + " dk",
                    servings = obj.getInt("servings").toString() + " kişi",
                    ingredients = items.joinToString("\n") { (title, q, unit) ->
                        (if (q % 1.0 == 0.0) q.toInt().toString() else q.toString()) +
                            " " + unit + " " + title
                    },
                    instructions = text, imageUri = photo
                )
                val id = db.recipeDao().insertRecipe(recipe).toInt()
                ids.add(id)
                db.recipeIngredientDao().insertRecipeIngredients(items.map { (title, qty, unit) ->
                    RecipeIngredient(recipeId = id, name = title, quantity = qty, unit = unit)
                })
                if (photo != null) {
                    db.recipeMediaDao().insertMedia(listOf(
                        RecipeMedia(recipeId = id, mediaType = RecipeMedia.TYPE_IMAGE,
                            uri = photo, sortOrder = 0)
                    ))
                }
                db.recipeCommentDao().insertComment(
                    RecipeComment(recipeId = id, authorNickname = "Demo • Örnek Yorum",
                        text = "Demo yorum: Tarifin sebze oranını sevdim.",
                        createdAt = System.currentTimeMillis())
                )
                db.recipeEngagementDao().upsert(RecipeEngagement(
                    recipeId = id, averageRating = 4.5, ratingCount = 2, commentCount = 1
                ))
            }
        }
        prefs.edit().putString("recipe_ids", ids.joinToString(",")).apply()
    }

    private suspend fun refreshDemoPhotosAndLegacyFilter(
        db: AppDatabase,
        data: JSONArray,
        prefs: android.content.SharedPreferences
    ) {
        val demoIds = prefs.getString("recipe_ids", "").orEmpty()
            .split(",").mapNotNull { it.toIntOrNull() }.toSet()
        if (demoIds.isEmpty()) return
        val recipes = db.recipeDao().getAllRecipes()
        if (!prefs.contains("legacy_recipe_ids")) {
            val legacyIds = recipes.filter { it.id !in demoIds }.map { it.id }
            prefs.edit().putString("legacy_recipe_ids", legacyIds.joinToString(",")).apply()
        }
        val curated = (0 until data.length()).associate { index ->
            val obj = data.getJSONObject(index)
            obj.getString("name") to obj.optString("image").takeIf { it.isNotBlank() }
        }
        db.withTransaction {
            recipes.filter { it.id in demoIds }.forEach { recipe ->
                val photo = curated[recipe.name]
                val oldPhoto = recipe.imageUri.orEmpty()
                if (oldPhoto.startsWith("https://images.unsplash.com/photo-") &&
                    oldPhoto != photo) {
                    db.recipeDao().updateRecipe(recipe.copy(imageUri = photo))
                    if (photo != null) {
                        db.recipeMediaDao().updateOldDemoImage(recipe.id, photo)
                    } else {
                        db.recipeMediaDao().removeOldDemoImage(recipe.id)
                    }
                }
            }
        }
        prefs.edit().putBoolean("photos_v2", true).apply()
    }

    private data class Stock(val name:String,val quantity:Double,val unit:String,val days:Long)
}

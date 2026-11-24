package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.data.*

@Composable
fun ExploreAllRecipesScreen(
    mode: Int,
    onBack: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getDatabase(context) }
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var engagements by remember { mutableStateOf<Map<Int, RecipeEngagement>>(emptyMap()) }
    var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        recipes = database.recipeDao().getAllRecipes()
        engagements = database.recipeEngagementDao().getAll().associateBy { it.recipeId }
    }
    val title = when(mode) {
        1 -> "Bu Hafta Popüler"
        2 -> "Yeni Tarifler"
        else -> "Sana Özel Öneriler"
    }
    val searchCounts = remember(context) { SearchHistoryStore.counts(context) }
    val sorted = remember(recipes, engagements, mode, searchCounts, query) {
        recipes.filter {
            query.isBlank() || it.name.contains(query, true) ||
                it.category.contains(query, true) || it.ingredients.contains(query, true)
        }.let { filtered ->
            when (mode) {
                1 -> filtered.sortedWith(compareByDescending<Recipe> {
                    engagements[it.id]?.averageRating ?: 0.0
                }.thenByDescending { engagements[it.id]?.ratingCount ?: 0 })
                2 -> filtered.sortedByDescending { it.id }
                else -> filtered.sortedWith(compareByDescending<Recipe> {
                    SearchHistoryStore.relevance(it, searchCounts)
                }.thenByDescending { engagements[it.id]?.isFavorite == true }
                    .thenByDescending { it.id })
            }
        }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFFFFF8F2))) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Geri", color = Color(0xFFEC651D)) }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 21.sp, color = Color(0xFF38231B))
        }
        OutlinedTextField(value = query, onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
            label = { Text("Bu tariflerde ara") }, singleLine = true)
        Text(sorted.size.toString() + " tarif", color = Color(0xFF86665A),
            fontSize = 12.sp, modifier = Modifier.padding(16.dp))
        LazyColumn(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sorted, key = { it.id }) { recipe ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onRecipeClick(recipe) },
                    shape = RoundedCornerShape(17.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!recipe.imageUri.isNullOrBlank()) {
                            Image(painter = rememberAsyncImagePainter(recipe.imageUri),
                                contentDescription = recipe.name,
                                modifier = Modifier.size(95.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.size(95.dp).background(Color(0xFFFFE5D1)),
                                contentAlignment = Alignment.Center) { Text("🍽️",fontSize=35.sp) }
                        }
                        Column(Modifier.weight(1f).padding(12.dp)) {
                            Text(recipe.name, fontWeight = FontWeight.Bold, color = Color(0xFF38231B))
                            Spacer(Modifier.height(5.dp))
                            Text(recipe.category + "  •  " + recipe.preparationTime,
                                color = Color(0xFF947366), fontSize = 12.sp)
                            Text("★ " + (engagements[recipe.id]?.averageRating ?: 0.0).toString(),
                                color = Color(0xFFED6A23), fontSize = 12.sp)
                        }
                        Text("›", fontSize = 27.sp, color = Color(0xFFED6A23),
                            modifier = Modifier.padding(end = 14.dp))
                    }
                }
            }
        }
    }
}

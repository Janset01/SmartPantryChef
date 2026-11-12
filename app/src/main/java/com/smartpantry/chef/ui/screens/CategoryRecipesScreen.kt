package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe

private val categoryCream = Color(0xFFFFF8F2)
private val categoryOrange = Color(0xFFE94D10)
private val categoryInk = Color(0xFF201612)

@Composable
fun CategoryRecipesScreen(
    category: String?,
    onBack: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getDatabase(context) }
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    LaunchedEffect(category) {
        recipes = db.recipeDao().getAllRecipes()
    }
    val visible = remember(recipes, category, query) {
        recipes.filter { recipe ->
            (category == null || recipe.category.trim().equals(category.trim(), ignoreCase = true)) &&
                (query.isBlank() || recipe.name.contains(query.trim(), ignoreCase = true) ||
                    recipe.ingredients.contains(query.trim(), ignoreCase = true))
        }
    }
    Column(Modifier.fillMaxSize().background(categoryCream)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = categoryInk)
            }
            Column(Modifier.weight(1f)) {
                Text(category ?: "Tüm Kategoriler", fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold, color = categoryInk)
                Text("${visible.size} tarif", fontSize = 12.sp, color = Color(0xFF756052))
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("Bu tariflerde ara...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = categoryOrange) },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = categoryOrange
            )
        )
        Spacer(Modifier.height(14.dp))
        if (visible.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (query.isNotBlank()) "Aramana uygun tarif bulunamadı."
                    else "Bu kategoride henüz tarif yok. Yeni tarifler eklendikçe burada görünecek.",
                    color = Color(0xFF756052)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visible, key = { it.id }) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onRecipeClick(recipe) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(width = 112.dp, height = 112.dp)
                                    .background(Color(0xFFFFDFCC)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!recipe.imageUri.isNullOrBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(recipe.imageUri),
                                        contentDescription = recipe.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else Text("🍲", fontSize = 34.sp)
                            }
                            Column(Modifier.weight(1f).padding(12.dp)) {
                                Text(recipe.name, fontWeight = FontWeight.Bold,
                                    color = categoryInk, fontSize = 16.sp, maxLines = 2,
                                    overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(6.dp))
                                Text(recipe.category, color = categoryOrange, fontSize = 12.sp)
                                Spacer(Modifier.height(5.dp))
                                Text("◷ ${recipe.preparationTime}   🍴 ${recipe.servings}",
                                    color = Color(0xFF756052), fontSize = 11.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

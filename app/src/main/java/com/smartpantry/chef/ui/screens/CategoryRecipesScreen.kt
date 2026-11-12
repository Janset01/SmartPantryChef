package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import com.smartpantry.chef.data.RecipeEngagement
import kotlinx.coroutines.launch
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
    var engagements by remember { mutableStateOf<Map<Int, RecipeEngagement>>(emptyMap()) }
    val scope = rememberCoroutineScope()
    var filterOpen by remember { mutableStateOf(false) }
    var servingFilter by remember { mutableStateOf("Tümü") }
    var timeFilter by remember { mutableIntStateOf(0) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("En yeni tarifler") }
    val servingOptions = listOf("Tümü", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "10+")
    val sortOptions = listOf("En yeni tarifler", "En çok beğenilen", "En yüksek puan",
        "En çok yorumlanan", "En kısa sürede hazırlanan")
    fun numberFrom(value: String): Int? = Regex("\\d+").find(value)?.value?.toIntOrNull()

    LaunchedEffect(category) {
        recipes = db.recipeDao().getAllRecipes()
        engagements = db.recipeEngagementDao().getAll().associateBy { it.recipeId }
    }
    val visible = recipes.filter { recipe ->
        val servings = numberFrom(recipe.servings)
        val minutes = numberFrom(recipe.preparationTime)
        (category == null || recipe.category.trim().equals(category.trim(), ignoreCase = true)) &&
            (query.isBlank() || recipe.name.contains(query.trim(), ignoreCase = true) ||
                recipe.ingredients.contains(query.trim(), ignoreCase = true)) &&
            (servingFilter == "Tümü" ||
                (servingFilter == "10+" && servings != null && servings >= 10) ||
                (servingFilter != "10+" && servings == servingFilter.toIntOrNull())) &&
            (timeFilter == 0 || (minutes != null && minutes <= timeFilter)) &&
            (!favoritesOnly || engagements[recipe.id]?.isFavorite == true)
    }.let { matched ->
        when (sortOrder) {
            "En çok beğenilen" -> matched.sortedWith(compareByDescending<Recipe> {
                engagements[it.id]?.likeCount ?: 0
            }.thenByDescending { it.id })
            "En yüksek puan" -> matched.sortedWith(compareByDescending<Recipe> {
                engagements[it.id]?.averageRating ?: 0.0
            }.thenByDescending { it.id })
            "En çok yorumlanan" -> matched.sortedWith(compareByDescending<Recipe> {
                engagements[it.id]?.commentCount ?: 0
            }.thenByDescending { it.id })
            "En kısa sürede hazırlanan" -> matched.sortedWith(compareBy<Recipe> {
                numberFrom(it.preparationTime) ?: Int.MAX_VALUE
            }.thenByDescending { it.id })
            else -> matched.sortedByDescending { it.id }
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
            trailingIcon = {
                IconButton(onClick = { filterOpen = true }) {
                    Icon(Icons.Default.Tune, "Filtrele ve sırala", tint = categoryOrange)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = categoryOrange
            )
        )
        if (servingFilter != "Tümü" || timeFilter != 0 || favoritesOnly ||
            sortOrder != "En yeni tarifler") {
            TextButton(
                onClick = { filterOpen = true },
                modifier = Modifier.padding(start = 12.dp)
            ) { Text("Aktif filtreler · Düzenle", color = categoryOrange) }
        }
        Spacer(Modifier.height(10.dp))
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
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
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
                                Modifier.size(width = 104.dp, height = 104.dp)
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
                            Column(Modifier.weight(1f).padding(start = 11.dp, top = 7.dp, bottom = 7.dp)) {
                                Text(recipe.name, fontWeight = FontWeight.Bold,
                                    color = categoryInk, fontSize = 14.sp, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                                Text(recipe.category, color = categoryOrange, fontSize = 11.sp,
                                    maxLines = 1)
                                val engagement = engagements[recipe.id]
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFAA00),
                                        modifier = Modifier.size(14.dp))
                                    Text(
                                        if ((engagement?.ratingCount ?: 0) > 0)
                                            String.format(java.util.Locale.US, "%.1f", engagement?.averageRating ?: 0.0)
                                        else "—",
                                        fontWeight = FontWeight.Bold, color = categoryInk, fontSize = 11.sp
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text("(${engagement?.commentCount ?: 0} yorum)",
                                        color = Color(0xFF756052), fontSize = 10.sp)
                                }
                                Text("◷ ${recipe.preparationTime}  |  ${recipe.servings}",
                                    color = Color(0xFF756052), fontSize = 10.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(
                                onClick = {
                                    val current = engagements[recipe.id] ?: RecipeEngagement(recipeId = recipe.id)
                                    val updated = current.copy(isFavorite = !current.isFavorite)
                                    engagements = engagements + (recipe.id to updated)
                                    scope.launch { db.recipeEngagementDao().upsert(updated) }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    if (engagements[recipe.id]?.isFavorite == true)
                                        Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    "Favori",
                                    tint = categoryOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (filterOpen) {
        AlertDialog(
            onDismissRequest = { filterOpen = false },
            title = { Text("Filtrele ve Sırala", color = categoryInk) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Kaç kişilik?", fontWeight = FontWeight.Bold, color = categoryInk)
                    servingOptions.chunked(4).forEach { options ->
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            options.forEach { option ->
                                FilterChip(
                                    selected = servingFilter == option,
                                    onClick = { servingFilter = option },
                                    label = { Text(if (option == "Tümü") option else option + " kişi", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Hazırlama süresi", fontWeight = FontWeight.Bold, color = categoryInk)
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf(0, 15, 30, 60).forEach { minutes ->
                            FilterChip(
                                selected = timeFilter == minutes,
                                onClick = { timeFilter = minutes },
                                label = { Text(if (minutes == 0) "Tümü" else "≤ ${minutes} dk", fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = favoritesOnly, onCheckedChange = { favoritesOnly = it },
                            colors = CheckboxDefaults.colors(checkedColor = categoryOrange)
                        )
                        Text("Yalnızca favorilerim", color = categoryInk, fontSize = 13.sp)
                    }
                    Text("Sıralama", fontWeight = FontWeight.Bold, color = categoryInk)
                    sortOptions.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth().clickable { sortOrder = option },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortOrder == option,
                                onClick = { sortOrder = option },
                                colors = RadioButtonDefaults.colors(selectedColor = categoryOrange)
                            )
                            Text(option, fontSize = 12.sp, color = categoryInk)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { filterOpen = false },
                    colors = ButtonDefaults.buttonColors(containerColor = categoryOrange)
                ) { Text("Sonuçları Göster") }
            },
            dismissButton = {
                TextButton(onClick = {
                    servingFilter = "Tümü"
                    timeFilter = 0
                    favoritesOnly = false
                    sortOrder = "En yeni tarifler"
                }) { Text("Temizle", color = categoryOrange) }
            },
            containerColor = categoryCream
        )
    }

}

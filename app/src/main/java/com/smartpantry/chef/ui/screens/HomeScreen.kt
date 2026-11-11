package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.smartpantry.chef.data.RecipeEngagement
import kotlinx.coroutines.launch

private val homeInk = Color(0xFF201612)
private val homeOrange = Color(0xFFE94D10)
private val homeCream = Color(0xFFFFF8F2)
private val homeMuted = Color(0xFF51453D)
private val homeGradient = Brush.horizontalGradient(listOf(Color(0xFFFF8D34), Color(0xFFF05A15), Color(0xFFD83D0A)))

@Composable
fun HomeScreen(
    onRecipeClick: (Recipe) -> Unit = {},
    onExplore: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onPantryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var engagements by remember { mutableStateOf<Map<Int, RecipeEngagement>>(emptyMap()) }
    var search by remember { mutableStateOf("") }
    var selectedDay by remember { mutableIntStateOf(0) }
    var showPlanner by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var featuredIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        recipes = db.recipeDao().getAllRecipes()
        engagements = db.recipeEngagementDao().getAll().associateBy { it.recipeId }
    }
    val filtered = recipes.filter { recipe ->
        (recipe.name.contains(search, true) || recipe.category.contains(search, true) ||
            recipe.ingredients.contains(search, true)) &&
            (selectedCategory == null || recipe.category.contains(selectedCategory!!, true))
    }
    val featured = filtered.sortedByDescending { it.id }.take(5)
    val inspiration = filtered.sortedWith(
        compareByDescending<Recipe> { engagements[it.id]?.averageRating ?: 0.0 }
            .thenByDescending { it.id }
    ).take(8)
    val toggleFavorite: (Recipe) -> Unit = { recipe ->
        val current = engagements[recipe.id] ?: RecipeEngagement(recipeId = recipe.id)
        val updated = current.copy(isFavorite = !current.isFavorite)
        engagements = engagements + (recipe.id to updated)
        scope.launch { db.recipeEngagementDao().upsert(updated) }
    }
    val categoryItems = listOf("Ana Yemek" to "🍲", "Tatlı" to "🍰", "Salata" to "🥗", "Çorba" to "🥣", "Kahvaltı" to "🥐", "Atıştırmalık" to "🍪")
    Column(Modifier.fillMaxSize().background(homeCream).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("👨‍🍳", fontSize = 27.sp, color = homeOrange)
            Spacer(Modifier.width(6.dp))
            Text("SmartPantryChef", Modifier.weight(1f), fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold, color = homeInk)
            IconButton(onClick = { showNotifications = true }) {
                Icon(Icons.Default.NotificationsNone, "Bildirimler", tint = homeInk)
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = search, onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            placeholder = { Text("Tarif, malzeme veya kategori ara...", fontSize = 15.sp, color = homeMuted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = homeMuted) },
            trailingIcon = { Icon(Icons.Default.Tune, "Filtreler", tint = homeOrange,
                modifier = Modifier.clickable { onExplore() }) },
            singleLine = true, shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = homeOrange, unfocusedBorderColor = Color(0xFFEAD6C9))
        )
        Spacer(Modifier.height(14.dp))
        if (featured.isNotEmpty()) {
            val recipe = featured[featuredIndex.coerceIn(0, featured.lastIndex)]
            Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(186.dp)
                .clip(RoundedCornerShape(24.dp)).clickable { onRecipeClick(recipe) }) {
                if (!recipe.imageUri.isNullOrBlank()) {
                    Image(rememberAsyncImagePainter(recipe.imageUri), null,
                        Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(listOf(Color(0xFF6E341C), Color(0xFFDB7B38)))))
                }
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
                    listOf(Color(0xE826120A), Color(0xB82B150A), Color.Transparent))))
                Column(Modifier.align(Alignment.CenterStart).padding(16.dp).fillMaxWidth(0.64f)) {
                    Text("HAFTANIN ÖNE ÇIKANI", fontSize = 12.sp, letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFFFFDDBF))
                    Spacer(Modifier.height(12.dp))
                    Text(recipe.name.replaceFirstChar { it.uppercase() }, fontSize = 24.sp,
                        lineHeight = 26.sp, maxLines = 3, overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("Mutfağına ilham veren lezzet", fontSize = 14.sp, color = Color.White)
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.clip(RoundedCornerShape(28.dp)).background(homeGradient)
                        .clickable { onRecipeClick(recipe) }.padding(horizontal = 15.dp, vertical = 10.dp)) {
                        Text("Tarife Göz At  →", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.Center) {
                featured.forEachIndexed { index, _ ->
                    Box(Modifier.padding(horizontal = 3.dp).size(width = if (index == featuredIndex) 19.dp else 7.dp, height = 7.dp)
                        .clip(CircleShape).background(if (index == featuredIndex) homeOrange else Color(0xFFE9D0C0))
                        .clickable { featuredIndex = index })
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(155.dp)
                .clip(RoundedCornerShape(22.dp)).background(Brush.horizontalGradient(
                    listOf(Color(0xFF71351D), Color(0xFFE66B25)))).clickable { onExplore() },
                contentAlignment = Alignment.CenterStart) {
                Column(Modifier.padding(20.dp)) {
                    Text("Mutfağında yeni lezzetlere yer aç!", color = Color.White,
                        fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Tarifleri keşfet →", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(15.dp))
        HomeSectionTitle("▦", "Kategorilere Göz At", "Tümünü Gör ›") {
            selectedCategory = null
            onExplore()
        }
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            categoryItems.forEach { (name, emoji) ->
                Column(Modifier.width(88.dp).clip(RoundedCornerShape(18.dp))
                    .background(if (selectedCategory == name) Color(0xFFFFCFAB) else Color(0xFFFFE9DB))
                    .clickable {
                        selectedCategory = if (selectedCategory == name) null else name
                        featuredIndex = 0
                    }.padding(vertical = 13.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(emoji, fontSize = 29.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(name, fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.Bold, color = homeInk)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        HomeSectionTitle("▣", "Günün Menüsü", "Bugün ⌄") {
            selectedDay = (selectedDay + 1) % 3
        }
        Spacer(Modifier.height(12.dp))
        val menuGroups = listOf("Kahvaltı" to "☀", "Öğle Yemeği" to "🍽", "Akşam Yemeği" to "☾")
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            menuGroups.forEach { (category, icon) ->
                val menuRecipe = recipes.firstOrNull { it.category.equals(category, true) }
                    ?: recipes.firstOrNull { it.category.contains(category.substringBefore(" "), true) }
                Card(Modifier.weight(1f).clickable {
                    if (menuRecipe != null) onRecipeClick(menuRecipe) else onCategoryClick(category)
                }, shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
                    Column {
                        Box(Modifier.fillMaxWidth().height(79.dp).background(Color(0xFFFFE4D0)),
                            contentAlignment = Alignment.Center) {
                            if (!menuRecipe?.imageUri.isNullOrBlank()) {
                                Image(rememberAsyncImagePainter(menuRecipe?.imageUri), null,
                                    Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else Text(icon, fontSize = 36.sp)
                        }
                        Text(category, Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, color = homeInk)
                    }
                }
            }
        }
        Spacer(Modifier.height(25.dp))
        HomeSectionTitle("🍂", "İlham Veren Tarifler", "Tümünü Gör ›") { onExplore() }
        Spacer(Modifier.height(12.dp))
        if (inspiration.isEmpty()) {
            Text("Tarif ekledikçe burada ilham veren lezzetler görünecek.",
                Modifier.padding(horizontal = 18.dp), color = homeMuted, fontSize = 13.sp)
        } else {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                inspiration.forEach { recipe ->
                    Box(Modifier.width(270.dp).height(143.dp).clip(RoundedCornerShape(21.dp))
                        .background(Color(0xFFF1D4BF)).clickable { onRecipeClick(recipe) }) {
                        if (!recipe.imageUri.isNullOrBlank()) {
                            Image(rememberAsyncImagePainter(recipe.imageUri), null,
                                Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xD927150F)))))
                        Column(Modifier.align(Alignment.BottomStart).padding(13.dp)) {
                            Text(recipe.name.replaceFirstChar { it.uppercase() }, color = Color.White,
                                fontSize = 19.sp, fontWeight = FontWeight.ExtraBold,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(5.dp))
                            val rating = engagements[recipe.id]?.averageRating ?: 0.0
                            Text("⭐ " + String.format(java.util.Locale.US, "%.1f", rating) +
                                "    ◷ " + recipe.preparationTime,
                                color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(36.dp)
                            .clip(CircleShape).background(Color.White)
                            .clickable { toggleFavorite(recipe) }, contentAlignment = Alignment.Center) {
                            Text(if (engagements[recipe.id]?.isFavorite == true) "🍲" else "🥣",
                                fontSize = 19.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(17.dp))
        Card(Modifier.fillMaxWidth().padding(horizontal = 18.dp).clickable { showPlanner = true },
            shape = RoundedCornerShape(21.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE3D0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🗓", fontSize = 29.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Haftalık Yemek Planı", fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp, color = homeInk)
                    Text("Daha düzenli ve kolay bir mutfak.", fontSize = 14.sp, color = homeMuted)
                }
                Box(Modifier.size(37.dp).clip(CircleShape).background(homeGradient),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
    if (showNotifications) {
        AlertDialog(onDismissRequest = { showNotifications = false },
            title = { Text("Bildirimler") },
            text = { Text("Şu anda yeni bildirimin yok.") },
            confirmButton = { TextButton(onClick = { showNotifications = false }) { Text("Tamam") } })
    }
    if (showPlanner) {
        AlertDialog(onDismissRequest = { showPlanner = false },
            title = { Text("Haftalık Yemek Planı") },
            text = { Text("Haftalık plan oluşturma özelliği hazırlanıyor. Tariflerin ve mevcut kayıtların değişmeyecek.") },
            confirmButton = { TextButton(onClick = { showPlanner = false }) { Text("Tamam") } })
    }
}

@Composable
private fun HomeSectionTitle(icon: String, title: String, action: String, onAction: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 23.sp, color = homeOrange)
        Spacer(Modifier.width(8.dp))
        Text(title, Modifier.weight(1f), fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold, color = homeInk)
        Text(action, Modifier.clickable { onAction() }, fontSize = 13.sp,
            fontWeight = FontWeight.Bold, color = homeOrange)
    }
}

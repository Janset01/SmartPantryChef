package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Canvas
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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
    onPantryClick: () -> Unit = {},
    onMyRecipesClick: () -> Unit = {},
    onAddRecipeClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var engagements by remember { mutableStateOf<Map<Int, RecipeEngagement>>(emptyMap()) }
    var search by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
    var filtersOpen by remember { mutableStateOf(false) }
    var searchCategory by remember { mutableStateOf("Tümü") }
    var searchMaxMinutes by remember { mutableIntStateOf(0) }
    var searchMinRating by remember { mutableStateOf(false) }
    var searchSort by remember { mutableStateOf("En yeni") }
    val searchCategories = listOf("Tümü", "Ana Yemek", "Salata", "Tatlı", "Çorba",
        "Fırın", "Kahvaltı", "Atıştırmalık", "Öğle Yemeği", "Brunch", "Akşam Yemeği", "Fit")
    val filteredHomeRecipes = remember(recipes, engagements, search, searchCategory, searchMaxMinutes, searchMinRating, searchSort) {
        val query = search.trim()
        val matched = recipes.filter { recipe ->
            (query.isEmpty() || recipe.name.contains(query, ignoreCase = true) ||
                recipe.category.contains(query, ignoreCase = true) ||
                recipe.ingredients.contains(query, ignoreCase = true)) &&
                (searchCategory == "Tümü" || recipe.category == searchCategory) &&
                (searchMaxMinutes == 0 || (Regex("\\d+").find(recipe.preparationTime)?.value?.toIntOrNull()
                    ?: Int.MAX_VALUE) <= searchMaxMinutes) &&
                (!searchMinRating || (engagements[recipe.id]?.averageRating ?: 0.0) >= 4.0)
        }
        if (searchSort == "En yüksek puan") matched.sortedWith(
            compareByDescending<Recipe> { engagements[it.id]?.averageRating ?: 0.0 }
                .thenByDescending { it.id }
        ) else matched.sortedByDescending { it.id }
    }
    var selectedDay by remember { mutableIntStateOf(0) }
    var showPlanner by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var featuredIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        recipes = db.recipeDao().getAllRecipes()
        engagements = db.recipeEngagementDao().getAll().associateBy { it.recipeId }
    }
    val featured = recipes.sortedByDescending { it.id }.take(5)
    val toggleFavorite: (Recipe) -> Unit = { recipe ->
        val current = engagements[recipe.id] ?: RecipeEngagement(recipeId = recipe.id)
        val updated = current.copy(isFavorite = !current.isFavorite)
        engagements = engagements + (recipe.id to updated)
        scope.launch { db.recipeEngagementDao().upsert(updated) }
    }
    val homeScrollState = rememberScrollState()
    val darkness by animateFloatAsState(
        targetValue = (homeScrollState.value / 560f).coerceIn(0f, 0.50f),
        animationSpec = tween(durationMillis = 180),
        label = "heroDarkness"
    )
    Box(Modifier.fillMaxSize().background(homeCream)) {
        // Hero stays fixed behind the scrolling foreground sheet.
        Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(124.dp))
        if (featured.isNotEmpty()) {
            val recipe = featured[featuredIndex.coerceIn(0, featured.lastIndex)]
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                    .height(344.dp).clip(RoundedCornerShape(25.dp))
                    .clickable { onRecipeClick(recipe) }
            ) {
                if (!recipe.imageUri.isNullOrBlank()) {
                    Image(rememberAsyncImagePainter(recipe.imageUri), recipe.name,
                        Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(listOf(Color(0xFF452114), Color(0xFFB75A26)))))
                }
                Box(Modifier.fillMaxSize()
                    .background(Brush.horizontalGradient(
                    listOf(Color(0xF0220F09), Color(0xA12A1209), Color(0x082A1209)))))
                Column(
                    Modifier.align(Alignment.CenterStart)
                        .fillMaxWidth(0.78f).padding(start = 21.dp, end = 6.dp, bottom = 20.dp)
                ) {
                    Text("HAFTANIN ÖNE ÇIKANI", fontSize = 11.sp, letterSpacing = 1.6.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFFFFE9D9))
                    Spacer(Modifier.height(18.dp))
                    Text(recipe.name.replaceFirstChar { it.uppercase() },
                        fontSize = 34.sp, lineHeight = 36.sp,
                        maxLines = 3, overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(13.dp))
                    Text("Az malzemeyle\nlezzetli sofralar",
                        fontSize = 15.sp, lineHeight = 19.sp, color = Color.White)
                    Spacer(Modifier.height(21.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(28.dp))
                            .background(homeGradient)
                            .clickable { onRecipeClick(recipe) }
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text("Tariflere Göz At  →", fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
                Row(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 62.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    featured.forEachIndexed { index, _ ->
                        Box(
                            Modifier.padding(horizontal = 4.dp)
                                .size(width = if (index == featuredIndex) 16.dp else 7.dp, height = 7.dp)
                                .clip(CircleShape)
                                .background(if (index == featuredIndex) homeOrange else Color(0xFFDAB09A))
                                .clickable { featuredIndex = index }
                        )
                    }
                }

            }
        } else {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .height(310.dp).clip(RoundedCornerShape(27.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF432117), Color(0xFFB85A29))))
                    .clickable { onExplore() }
            ) {
                Column(Modifier.align(Alignment.CenterStart).padding(24.dp)) {
                    Text("HAFTANIN ÖNE ÇIKANI", fontSize = 12.sp,
                        color = Color(0xFFFFE9D9), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(20.dp))
                    Text("Pratik\nLezzetler", fontSize = 37.sp, lineHeight = 40.sp,
                        fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(18.dp))
                    Text("Tarif ekleyerek mutfağını keşfet", color = Color.White)
                    Spacer(Modifier.height(18.dp))
                    Text("Tariflere Göz At  →",
                        Modifier.clip(RoundedCornerShape(28.dp)).background(homeGradient)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        }
        Box(
            Modifier.fillMaxWidth().padding(top = 124.dp).height(344.dp)
                .background(Color.Black.copy(alpha = darkness))
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(homeScrollState)
        ) {
            Spacer(Modifier.height(455.dp))
            Column(
                Modifier.fillMaxWidth().background(homeCream)
            ) {
                // One continuous wave is the leading edge of the scrolling content.
                // It covers the hero as the user scrolls, without a second sheet or handle.
                Box(
                    Modifier.fillMaxWidth().height(28.dp)
                        .background(homeCream)
                ) {
                    Canvas(
                        Modifier.fillMaxWidth().height(30.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-24).dp)
                    ) {
                        val wavePath = Path().apply {
                            moveTo(0f, size.height * 0.60f)
                            cubicTo(size.width * 0.13f, -size.height * 0.10f,
                                size.width * 0.34f, size.height * 0.92f,
                                size.width * 0.50f, size.height * 0.42f)
                            cubicTo(size.width * 0.68f, -size.height * 0.06f,
                                size.width * 0.87f, size.height * 0.90f,
                                size.width, size.height * 0.60f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(wavePath, homeCream)
                    }
                    Box(
                        Modifier.align(Alignment.TopCenter)
                            .padding(top = 1.dp)
                            .size(width = 38.dp, height = 5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7C4B2))
                    )
                }
                Spacer(Modifier.height(7.dp))
        HomeSectionTitle("▦", "Kategorilere Göz At", "Tüm Kategoriler ›") { onExplore() }
        Spacer(Modifier.height(7.dp))
        val categoryItems = listOf(
            "Ana Yemek" to "🍲", "Salata" to "🥗", "Tatlı" to "🍰",
            "Çorba" to "🥣", "Fırın" to "♨️", "Kahvaltı" to "🥐",
            "Atıştırmalık" to "🍪", "Öğle Yemeği" to "🍛",
            "Brunch" to "🥞", "Akşam Yemeği" to "🍽️", "Fit" to "🥑"
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 5.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            categoryItems.forEach { (category, emoji) ->
                Column(
                    Modifier.width(57.dp)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEDE2))
                        .clickable { onCategoryClick(category) }
                        .padding(top = 6.dp, bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(emoji, fontSize = 23.sp, lineHeight = 26.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        category, fontSize = 9.sp, maxLines = 1,
                        fontWeight = FontWeight.ExtraBold, color = homeInk,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(Modifier.height(23.dp))
        HomeSectionTitle("▣", "Günün Menüsü", "Menüyü Değiştir ↻") {
            selectedDay += 1
        }
        Spacer(Modifier.height(9.dp))
        val dailyMenu = recipes.getOrNull(
            if (recipes.isEmpty()) 0 else selectedDay % recipes.size
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                .clickable {
                    if (dailyMenu != null) onRecipeClick(dailyMenu)
                    else onExplore()
                },
            shape = RoundedCornerShape(17.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().height(88.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.width(108.dp).fillMaxHeight()
                        .background(Color(0xFFFFD9C2)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!dailyMenu?.imageUri.isNullOrBlank()) {
                        Image(
                            rememberAsyncImagePainter(dailyMenu?.imageUri),
                            dailyMenu?.name,
                            Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🍽️", fontSize = 34.sp)
                    }
                }
                Column(
                    Modifier.weight(1f).padding(horizontal = 11.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        dailyMenu?.name?.replaceFirstChar { it.uppercase() }
                            ?: "Bugün ne pişirsek?",
                        color = homeInk, fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        if (dailyMenu == null) "Tarif ekleyerek menünü oluştur"
                        else "◷ ${dailyMenu.preparationTime}   🍴 ${dailyMenu.servings}",
                        fontSize = 11.sp, color = homeMuted,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    Modifier.padding(end = 12.dp).size(30.dp)
                        .clip(CircleShape).background(homeGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        HomeSectionTitle("🍂", "İlham Veren Tarifler", "Tümünü Gör ›") { onExplore() }
        Spacer(Modifier.height(11.dp))
        val inspiration = recipes.sortedWith(
            compareByDescending<Recipe> { engagements[it.id]?.averageRating ?: 0.0 }
                .thenByDescending { it.id }
        ).take(8)
        if (inspiration.isEmpty()) {
            Text("Tarif ekledikçe ilham veren lezzetler burada görünecek.",
                Modifier.padding(horizontal = 18.dp), fontSize = 13.sp, color = homeMuted)
        } else {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                inspiration.forEach { recipe ->
                    Box(
                        Modifier.width(285.dp).height(166.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(Color(0xFFDDAF91))
                            .clickable { onRecipeClick(recipe) }
                    ) {
                        if (!recipe.imageUri.isNullOrBlank()) {
                            Image(rememberAsyncImagePainter(recipe.imageUri), recipe.name,
                                Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xD92A130A)))))
                        Column(Modifier.align(Alignment.BottomStart).padding(13.dp)) {
                            Text(recipe.name.replaceFirstChar { it.uppercase() },
                                fontSize = 19.sp, fontWeight = FontWeight.ExtraBold,
                                maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.White)
                            Spacer(Modifier.height(6.dp))
                            val rating = engagements[recipe.id]?.averageRating ?: 0.0
                            Text(
                                "⭐ " + String.format(java.util.Locale.US, "%.1f", rating) +
                                    "   ◷ " + recipe.preparationTime,
                                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(10.dp).size(37.dp)
                                .clip(CircleShape).background(Color.White)
                                .clickable { toggleFavorite(recipe) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (engagements[recipe.id]?.isFavorite == true) "🍲" else "🥣",
                                fontSize = 20.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        HomeSectionTitle("👨‍🍳", "Tanınmış Şeflerin Tarifleri", "Tümünü Gör ›") {
            val url = Uri.parse("https://www.google.com/search?q=" +
                Uri.encode("ünlü şeflerin tarifleri"))
            context.startActivity(Intent(Intent.ACTION_VIEW, url))
        }
        Spacer(Modifier.height(10.dp))
        val featuredChefs = listOf(
            "Danilo Zanna", "Somer Sivrioğlu", "Mehmet Yalçınkaya"
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            featuredChefs.forEach { chef ->
                Card(
                    Modifier.width(145.dp).clickable {
                        val url = Uri.parse("https://www.google.com/search?q=" +
                            Uri.encode(chef + " tarifleri"))
                        context.startActivity(Intent(Intent.ACTION_VIEW, url))
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE9DC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Box(
                            Modifier.fillMaxWidth().height(82.dp)
                                .background(Brush.verticalGradient(
                                    listOf(Color(0xFFFFD6B9), Color(0xFFB65C36))
                                )),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍🍳", fontSize = 39.sp)
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                chef, Modifier.weight(1f), color = homeInk,
                                fontWeight = FontWeight.Bold, fontSize = 12.sp,
                                maxLines = 2
                            )
                            Text("↗", color = homeOrange, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Şef kartları web üzerindeki tarif aramalarını açar.",
            Modifier.padding(horizontal = 14.dp),
            fontSize = 10.sp, color = homeMuted
        )
        Spacer(Modifier.height(21.dp))
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                .clickable { showPlanner = true },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE5D5)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗓️", fontSize = 30.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Haftalık Yemek Planı", fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold, color = homeInk)
                    Text("Daha düzenli ve kolay bir mutfak.",
                        fontSize = 12.sp, color = homeMuted)
                }
                Box(
                    Modifier.size(39.dp).clip(CircleShape).background(homeGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowForward, null, tint = Color.White)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
            }
        }
        // Fixed header masks the scrolling sheet: it cannot cover the brand or search.
        Column(
            Modifier.align(Alignment.TopCenter)
                .fillMaxWidth().background(homeCream)
                .padding(top = 8.dp, bottom = 10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("♨", fontSize = 30.sp, color = homeOrange)
                Spacer(Modifier.width(7.dp))
                Text("SmartPantryChef", Modifier.weight(1f), fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold, color = homeInk)
                IconButton(onClick = { showNotifications = true }) {
                    Icon(Icons.Default.NotificationsNone, "Bildirimler", tint = homeInk)
                }
            }
            OutlinedTextField(
                value = search,
                onValueChange = { search = it; searchOpen = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
                    .height(52.dp),
                placeholder = { Text("Tarif, malzeme veya kategori ara...", fontSize = 13.sp) },
                leadingIcon = {
                    IconButton(onClick = { searchOpen = true }) {
                        Icon(Icons.Default.Search, "Tarif ara", tint = homeMuted)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { filtersOpen = true }) {
                        Icon(Icons.Default.Tune, "Filtreler", tint = homeOrange)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = homeOrange,
                    unfocusedBorderColor = Color(0xFFECD5C7)
                )
            )
        }
        if (searchOpen || search.isNotBlank() || searchCategory != "Tümü" ||
            searchMaxMinutes != 0 || searchMinRating) {
            Column(
                Modifier.fillMaxSize().padding(top = 124.dp)
                    .background(homeCream).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tarif Ara", Modifier.weight(1f), fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold, color = homeInk)
                    TextButton(onClick = {
                        searchOpen = false
                        search = ""
                        searchCategory = "Tümü"
                        searchMaxMinutes = 0
                        searchMinRating = false
                    }) { Text("Kapat", color = homeOrange) }
                }
                Text("${filteredHomeRecipes.size} tarif bulundu", color = homeMuted, fontSize = 12.sp)
                if (searchCategory != "Tümü" || searchMaxMinutes != 0 || searchMinRating) {
                    TextButton(onClick = { filtersOpen = true }) {
                        Text("Aktif filtreler · Düzenle", color = homeOrange)
                    }
                }
                Spacer(Modifier.height(12.dp))
                filteredHomeRecipes.forEach { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            .clickable { searchOpen = false; onRecipeClick(recipe) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(width = 104.dp, height = 96.dp)
                                    .background(Color(0xFFFFE2CE)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!recipe.imageUri.isNullOrBlank()) {
                                    Image(rememberAsyncImagePainter(recipe.imageUri), recipe.name,
                                        Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else Text("🍲", fontSize = 34.sp)
                            }
                            Column(Modifier.weight(1f).padding(12.dp)) {
                                Text(recipe.name, color = homeInk, fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold, maxLines = 2,
                                    overflow = TextOverflow.Ellipsis)
                                Text(recipe.category + " · " + recipe.preparationTime,
                                    color = homeMuted, fontSize = 12.sp)
                                Text("★ " + String.format(java.util.Locale.US, "%.1f",
                                    engagements[recipe.id]?.averageRating ?: 0.0),
                                    color = homeOrange, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ArrowForward, null, tint = homeOrange,
                                modifier = Modifier.padding(end = 12.dp))
                        }
                    }
                }
                if (filteredHomeRecipes.isEmpty()) {
                    Text("Bu arama ve filtrelerle eşleşen tarif bulunamadı.",
                        color = homeMuted, modifier = Modifier.padding(top = 24.dp))
                }
            }
        }
    }
    if (filtersOpen) {
        AlertDialog(
            onDismissRequest = { filtersOpen = false },
            title = { Text("Tarif Filtreleri", color = homeInk) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Kategori", fontWeight = FontWeight.Bold, color = homeInk)
                    Column {
                        searchCategories.chunked(3).forEach { group ->
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                group.forEach { category ->
                                    FilterChip(
                                        selected = searchCategory == category,
                                        onClick = { searchCategory = category },
                                        label = { Text(category, fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFFD4B8),
                                            selectedLabelColor = homeInk
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Hazırlama süresi", fontWeight = FontWeight.Bold, color = homeInk)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 15, 30, 60).forEach { minutes ->
                            FilterChip(
                                selected = searchMaxMinutes == minutes,
                                onClick = { searchMaxMinutes = minutes },
                                label = { Text(if (minutes == 0) "Tümü" else "≤ ${minutes} dk",
                                    fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = searchMinRating,
                            onCheckedChange = { searchMinRating = it },
                            colors = CheckboxDefaults.colors(checkedColor = homeOrange))
                        Text("4.0 ve üzeri puan", color = homeInk)
                    }
                    Text("Sıralama", fontWeight = FontWeight.Bold, color = homeInk)
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("En yeni", "En yüksek puan").forEach { option ->
                            FilterChip(
                                selected = searchSort == option,
                                onClick = { searchSort = option },
                                label = { Text(option, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { filtersOpen = false; searchOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = homeOrange)
                ) { Text("Sonuçları Göster") }
            },
            dismissButton = {
                TextButton(onClick = {
                    searchCategory = "Tümü"
                    searchMaxMinutes = 0
                    searchMinRating = false
                    searchSort = "En yeni"
                }) { Text("Sıfırla", color = homeOrange) }
            },
            containerColor = homeCream
        )
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

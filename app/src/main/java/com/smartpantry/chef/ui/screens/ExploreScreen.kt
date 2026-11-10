package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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

private val exploreGreen = Color(0xFFD94A12)
private val exploreGradient = Brush.verticalGradient(
    listOf(Color(0xFFFF8C35), Color(0xFFF25B18), Color(0xFFD9410C))
)
private val exploreInk = Color(0xFF38231B)
private val exploreCream = Color(0xFFFFF8F2)
private val exploreCard = Color(0xFFFFFDF9)
private val exploreMuted = Color(0xFF766A62)

private fun displayRecipeTitle(name: String): String {
    val locale = java.util.Locale("tr", "TR")
    return name.trim().split(Regex("\\s+")).joinToString(" ") { word ->
        word.replaceFirstChar { char -> char.titlecase(locale) }
    }
}

@Composable
fun ExploreScreen(onRecipeClick: (Recipe) -> Unit) {
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getDatabase(context) }
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var engagements by remember { mutableStateOf<Map<Int, RecipeEngagement>>(emptyMap()) }
    var search by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(0) }
    var category by remember { mutableStateOf("Tümü") }
    var showAllRecommendations by remember { mutableStateOf(false) }
    var showAllPopular by remember { mutableStateOf(false) }
    var ingredientMode by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        recipes = database.recipeDao().getAllRecipes()
        engagements = database.recipeEngagementDao().getAll().associateBy { it.recipeId }
    }

    val searched = recipes.filter {
        (it.name.contains(search, ignoreCase = true) ||
            it.category.contains(search, ignoreCase = true) ||
            it.ingredients.contains(search, ignoreCase = true)) &&
            (category == "Tümü" || it.category == category)
    }
    val shown = when (tab) {
        0 -> searched.sortedWith(
            compareByDescending<Recipe> { engagements[it.id]?.myRating ?: 0 }
                .thenByDescending { engagements[it.id]?.isFavorite == true }
        )
        1 -> searched.sortedWith(
            compareByDescending<Recipe> { engagements[it.id]?.averageRating ?: 0.0 }
                .thenByDescending { engagements[it.id]?.ratingCount ?: 0 }
        )
        else -> searched.sortedByDescending { it.id }
    }
    val categories = listOf("Tümü", "Kahvaltı", "Öğle Yemeği", "Akşam Yemeği", "Tatlı", "Fit", "Atıştırmalık")

    Column(
        Modifier.fillMaxSize().background(exploreCream)
            .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Keşfet", modifier = Modifier.weight(1f), fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold, color = exploreInk)
            Box(Modifier.size(44.dp).clip(CircleShape).clickable { searchOpen = !searchOpen },
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Search, contentDescription = "Tarif ara",
                    tint = exploreInk, modifier = Modifier.size(27.dp))
            }
        }
        if (searchOpen) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (ingredientMode) "Malzemeni yaz..." else "Tarif veya malzeme ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true, shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                    focusedBorderColor = exploreGreen, unfocusedBorderColor = Color(0xFFE9DCD2))
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            listOf("✨ Sana Özel", "🔥 Trendler", "🌿 Yeni Tarifler").forEachIndexed { index, label ->
                Box(
                    Modifier.clip(RoundedCornerShape(24.dp))
                        .background(if (tab == index) exploreGradient else Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFFF1E6))))
                        .clickable { tab = index }
                        .padding(horizontal = 17.dp, vertical = 13.dp)
                ) {
                    Text(label, color = if (tab == index) Color.White else exploreInk,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(23.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                when (tab) {
                    0 -> "Sana Özel Öneriler ✨"
                    1 -> "🔥 Trend Tarifler"
                    else -> "🌿 Yeni Tarifler"
                },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = exploreInk
            )
            Text("Tümünü Gör ›", modifier = Modifier.clickable {
                showAllRecommendations = !showAllRecommendations
            }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = exploreGreen)
        }
        Spacer(Modifier.height(5.dp))
        Text(
            when (tab) {
                0 -> "Puanlarına ve favorilerine göre sıralandı."
                1 -> "Tariflerin aldığı puanlara göre sıralandı."
                else -> "En son eklenen tariflere göz at."
            },
            fontSize = 13.sp, color = exploreMuted
        )
        Spacer(Modifier.height(15.dp))
        if (shown.isEmpty()) {
            ExploreEmpty("Henüz bu bölümde tarif bulunmuyor.")
        } else {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                (if (showAllRecommendations) shown else shown.take(5)).forEach { recipe ->
                    ExplorePhotoCard(recipe, engagements[recipe.id], onClick = { onRecipeClick(recipe) })
                }
            }
        }
        Spacer(Modifier.height(21.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                ingredientMode = true
                searchOpen = true
                search = ""
                category = "Tümü"
                showAllRecommendations = true
            },
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0A6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFF6E3), Color(0xFFFFE5B8), Color(0xFFFFCE82))
                    )
                ).padding(horizontal = 9.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExploreCookingPot(Modifier.size(52.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Elindeki Malzemelerle Keşfet", fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF432414), fontSize = 13.sp, lineHeight = 16.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(if (ingredientMode) "Malzemeni yaz, tarifleri keşfet."
                        else "Buzdolabındaki malzemelerle tarif bul.",
                        color = Color(0xFF795038), fontSize = 10.sp, lineHeight = 12.sp)
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.size(32.dp)
                        .shadow(5.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFF8C27), Color(0xFFEC510E), Color(0xFFC9380B)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("➜", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(23.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("🔥 Bu Hafta Popüler", modifier = Modifier.weight(1f),
                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = exploreInk)
            Text("Tümünü Gör ›", modifier = Modifier.clickable {
                showAllPopular = !showAllPopular
            }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = exploreGreen)
        }
        Spacer(Modifier.height(13.dp))
        val popular = searched.filter { (engagements[it.id]?.ratingCount ?: 0) > 0 }
            .sortedByDescending { engagements[it.id]?.averageRating ?: 0.0 }
        if (popular.isEmpty()) {
            ExploreEmpty("Puanlanan tarifler burada görünecek.")
        } else {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (if (showAllPopular) popular else popular.take(5)).forEachIndexed { index, recipe ->
                    Box {
                        ExplorePhotoCard(recipe, engagements[recipe.id], isPopular = true) { onRecipeClick(recipe) }
                        Box(Modifier.padding(7.dp).size(30.dp).clip(CircleShape)
                            .background(Color(0xFFFFC64D)), contentAlignment = Alignment.Center) {
                            Text((index + 1).toString(), fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(27.dp))
        Text("🥬 Temalara Göre Keşfet", fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold, color = exploreInk)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.drop(1).forEachIndexed { index, name ->
                val shades = listOf(0xFFFFE5CC, 0xFFFFD9BE, 0xFFFFEBD6, 0xFFF5DCD0)
                Column(
                    Modifier.width(132.dp).clip(RoundedCornerShape(20.dp))
                        .background(Color(shades[index % shades.size]))
                        .clickable { category = if (category == name) "Tümü" else name }
                        .padding(vertical = 19.dp, horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(listOf("🥐", "🍲", "🍝", "🍰", "🥗", "🥪")[index], fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(name, fontWeight = FontWeight.Bold, color = exploreInk,
                        fontSize = 13.sp, maxLines = 1)
                    if (category == name) Text("✓ Seçili", color = exploreGreen, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun ExploreCookingPot(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val orange = Color(0xFFF36B0A)
        val dark = Color(0xFFD94B06)
        val highlight = Color(0xFFFFA72B)
        // Steam curls, lid and two handles: a compact illustrated cooking pot.
        for (x in listOf(0.33f, 0.51f, 0.69f)) {
            val steam = Path().apply {
                moveTo(w * x, h * 0.06f)
                cubicTo(w * (x - 0.13f), h * 0.16f, w * (x + 0.11f), h * 0.19f, w * x, h * 0.32f)
            }
            drawPath(steam, Color(0xFFFFA318), style = Stroke(width = w * 0.035f))
        }
        drawRoundRect(dark, topLeft = Offset(w * 0.06f, h * 0.49f),
            size = Size(w * 0.88f, h * 0.16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.07f))
        drawRoundRect(orange, topLeft = Offset(w * 0.17f, h * 0.43f),
            size = Size(w * 0.66f, h * 0.48f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f))
        drawRoundRect(highlight, topLeft = Offset(w * 0.22f, h * 0.48f),
            size = Size(w * 0.14f, h * 0.35f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f))
        drawRoundRect(Color(0xFFFF9B17), topLeft = Offset(w * 0.12f, h * 0.39f),
            size = Size(w * 0.76f, h * 0.13f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.07f))
        drawRoundRect(dark, topLeft = Offset(w * 0.41f, h * 0.32f),
            size = Size(w * 0.18f, h * 0.08f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f))
    }
}

@Composable
private fun ExplorePhotoCard(
    recipe: Recipe,
    engagement: RecipeEngagement?,
    isPopular: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        Modifier.width(if (isPopular) 112.dp else 116.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = exploreCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(if (isPopular) 100.dp else 108.dp)) {
                if (!recipe.imageUri.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(recipe.imageUri),
                        contentDescription = recipe.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFFF8E7D7)),
                        contentAlignment = Alignment.Center) {
                        Text("🍽️", fontSize = 36.sp)
                    }
                }
                if (!isPopular) Box(
                    Modifier.align(Alignment.BottomStart).padding(7.dp)
                        .clip(RoundedCornerShape(13.dp)).background(Color(0xFFFFE7B4))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    val score = engagement?.averageRating ?: 0.0
                    Text(if ((engagement?.ratingCount ?: 0) > 0)
                        "⭐ " + String.format(java.util.Locale.getDefault(), "%.1f", score)
                    else "Yeni tarif", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF402A1D))
                }
                Box(
                    Modifier.align(Alignment.TopEnd).padding(5.dp)
                        .size(28.dp).clip(CircleShape)
                        .background(Color(0xFFFDFBF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🥣",
                        fontSize = 16.sp,
                        modifier = Modifier.alpha(if (engagement?.isFavorite == true) 1f else 0.48f)
                    )
                }
            }
            Column(Modifier.height(65.dp).padding(horizontal = 8.dp, vertical = 6.dp)) {
                Text(displayRecipeTitle(recipe.name), fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2B1D16), maxLines = 2, minLines = 2,
                    overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                Spacer(Modifier.height(3.dp))
                if (isPopular) {
                    val count = engagement?.ratingCount ?: 0
                    val score = engagement?.averageRating ?: 0.0
                    Text(if (count > 0) "⭐ " +
                        String.format(java.util.Locale.getDefault(), "%.1f", score)
                        else "☆ Henüz puan yok",
                        fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF493026), maxLines = 1)
                } else {
                    Text("◷  " + recipe.preparationTime, fontSize = 12.sp,
                        color = Color(0xFF6B5547), maxLines = 1,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ExploreEmpty(message: String) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = exploreCard)
    ) {
        Text(message, Modifier.padding(19.dp), color = exploreMuted, fontSize = 13.sp)
    }
}

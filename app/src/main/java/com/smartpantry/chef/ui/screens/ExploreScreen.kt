package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
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

private val exploreGreen = Color(0xFF226515)
private val exploreInk = Color(0xFF27382B)
private val exploreCream = Color(0xFFFFF8ED)

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
                fontWeight = FontWeight.ExtraBold, color = Color(0xFF151A12))
            Box(Modifier.size(44.dp).clip(CircleShape).clickable { searchOpen = !searchOpen },
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Search, contentDescription = "Tarif ara",
                    tint = Color(0xFF172918), modifier = Modifier.size(27.dp))
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
                    focusedBorderColor = exploreGreen, unfocusedBorderColor = Color(0xFFE7E5DF))
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
                        .background(if (tab == index) exploreGreen else Color.White)
                        .clickable { tab = index }
                        .padding(horizontal = 17.dp, vertical = 13.dp)
                ) {
                    Text(label, color = if (tab == index) Color.White else exploreInk,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(27.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                when (tab) {
                    0 -> "Sana Özel Öneriler ✨"
                    1 -> "🔥 Trend Tarifler"
                    else -> "🌿 Yeni Tarifler"
                },
                modifier = Modifier.weight(1f),
                fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black
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
            fontSize = 12.sp, color = Color(0xFF777D73)
        )
        Spacer(Modifier.height(15.dp))
        if (shown.isEmpty()) {
            ExploreEmpty("Henüz bu bölümde tarif bulunmuyor.")
        } else {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                (if (showAllRecommendations) shown else shown.take(5)).forEach { recipe ->
                    ExplorePhotoCard(recipe, engagements[recipe.id], onClick = { onRecipeClick(recipe) })
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                ingredientMode = true
                searchOpen = true
                search = ""
                category = "Tümü"
                showAllRecommendations = true
            },
            shape = RoundedCornerShape(23.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDDEBC4)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFFF5F6C6), Color(0xFFC7E894), Color(0xFF7DAE43)))
                ).padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(76.dp).height(91.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(69.dp).clip(RoundedCornerShape(17.dp))
                        .background(Color(0xFFF6FAE9)), contentAlignment = Alignment.Center) {
                        Text("🧊", fontSize = 44.sp)
                    }
                    Text("🥕🥬", fontSize = 21.sp,
                        modifier = Modifier.align(Alignment.BottomStart))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Elindeki Malzemelerle Keşfet ✨", fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF173E17), fontSize = 16.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(if (ingredientMode) "Malzemeni yaz, tarifleri keşfet."
                        else "Buzdolabındaki malzemelerle tarif bul.",
                        color = Color(0xFF365B30), fontSize = 12.sp)
                }
                Box(
                    Modifier.size(42.dp).clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color(0xFF378C17), Color(0xFF0C450B)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("➜", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(25.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("🔥 Bu Hafta Popüler", modifier = Modifier.weight(1f),
                fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                (if (showAllPopular) popular else popular.take(5)).forEachIndexed { index, recipe ->
                    Box {
                        ExplorePhotoCard(recipe, engagements[recipe.id]) { onRecipeClick(recipe) }
                        Box(Modifier.padding(7.dp).size(30.dp).clip(CircleShape)
                            .background(Color(0xFFFFCA40)), contentAlignment = Alignment.Center) {
                            Text((index + 1).toString(), fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(27.dp))
        Text("🥬 Temalara Göre Keşfet", fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold, color = Color.Black)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.drop(1).forEachIndexed { index, name ->
                val shades = listOf(0xFFDCECBF, 0xFFFFDFB2, 0xFFD3EACB, 0xFFFFD7D1)
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
private fun ExplorePhotoCard(
    recipe: Recipe,
    engagement: RecipeEngagement?,
    onClick: () -> Unit
) {
    Card(
        Modifier.width(143.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(115.dp)) {
                if (!recipe.imageUri.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(recipe.imageUri),
                        contentDescription = recipe.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Color(0xFFE3EACB)),
                        contentAlignment = Alignment.Center) {
                        Text("🍽️", fontSize = 36.sp)
                    }
                }
                Box(
                    Modifier.align(Alignment.BottomStart).padding(8.dp)
                        .clip(RoundedCornerShape(13.dp)).background(Color(0xFFFCE8AE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val score = engagement?.averageRating ?: 0.0
                    Text(if ((engagement?.ratingCount ?: 0) > 0)
                        "⭐ " + String.format(java.util.Locale.getDefault(), "%.1f", score)
                    else "Yeni tarif", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                if (engagement?.isFavorite == true) {
                    Text("🍲", modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        .clip(CircleShape).background(Color.White).padding(6.dp), fontSize = 17.sp)
                }
            }
            Column(Modifier.padding(11.dp)) {
                Text(recipe.name, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = exploreInk, maxLines = 2, minLines = 2,
                    overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
                Spacer(Modifier.height(5.dp))
                Text("◷ " + recipe.preparationTime, fontSize = 12.sp,
                    color = Color(0xFF626B61), maxLines = 1)
            }
        }
    }
}

@Composable
private fun ExploreEmpty(message: String) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(message, Modifier.padding(19.dp), color = Color(0xFF6E786C), fontSize = 13.sp)
    }
}

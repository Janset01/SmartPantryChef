package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Ingredient
import com.smartpantry.chef.data.MatchStatus
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.RecipeIngredient
import com.smartpantry.chef.data.RecipeMatch
import com.smartpantry.chef.data.RecipeMatcher

@Composable
fun HomeScreen(
    onRecipeClick: (Recipe) -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val recipeDao = database.recipeDao()
    val ingredientDao = database.ingredientDao()
    val recipeIngredientDao = database.recipeIngredientDao()

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tümü") }
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var pantryIngredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }
    var allRecipeIngredients by remember { mutableStateOf<List<RecipeIngredient>>(emptyList()) }
    var recipeMatches by remember { mutableStateOf<List<RecipeMatch>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true

        val loadedRecipes = recipeDao.getAllRecipes()
        val loadedPantry = ingredientDao.getAllIngredients()
        val loadedRecipeIngredients =
            recipeIngredientDao.getAllRecipeIngredients()

        recipes = loadedRecipes
        pantryIngredients = loadedPantry
        allRecipeIngredients = loadedRecipeIngredients

        recipeMatches =
            loadedRecipes.map { recipe ->
                RecipeMatcher.matchRecipe(
                    recipe = recipe,
                    recipeIngredients =
                        loadedRecipeIngredients.filter {
                            it.recipeId == recipe.id
                        },
                    pantryIngredients = loadedPantry
                )
            }

        loading = false
    }

    val categories = listOf(
        "Tümü",
        "Kahvaltı",
        "Öğle Yemeği",
        "Akşam Yemeği",
        "Tatlı",
        "Fit",
        "Atıştırmalık"
    )

    val rankedMatches =
        rankHomeRecipeMatches(recipeMatches)

    val expiringIngredients =
        expiringSoonIngredients(pantryIngredients)

    val canMakeRecipes =
        rankedMatches.filter {
            it.status == MatchStatus.CAN_MAKE
        }

    val almostReadyRecipes =
        rankedMatches.filter {
            it.status == MatchStatus.ALMOST_READY
        }

    val consumeFirstRecipes =
        rankedMatches.filter { match ->
            val ingredientsForRecipe =
                allRecipeIngredients.filter {
                    it.recipeId == match.recipe.id
                }

            recipeUsesAnyIngredient(
                ingredientsForRecipe,
                expiringIngredients
            )
        }

    val visibleRecipes =
        recipes.filter { recipe ->
            val matchesSearch =
                searchText.isBlank() ||
                        recipe.name.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        recipe.ingredients.contains(
                            searchText,
                            ignoreCase = true
                        )

            val matchesCategory =
                selectedCategory == "Tümü" ||
                        recipe.category == selectedCategory

            matchesSearch && matchesCategory
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F2))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Smart Pantry & Chef",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Bugün ne pişirsem derdine son 🍳",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Tarif veya malzeme ara...")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Ara"
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF7A9E7E),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected =
                        selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                    },
                    label = {
                        Text(category)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        PantryOverviewCard(
            totalItems =
                pantryIngredients.count {
                    it.remainingQuantity > 0
                },
            expiringCount =
                expiringIngredients.size,
            canMakeCount =
                canMakeRecipes.size
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (loading) {
            Text(
                text = "Öneriler hazırlanıyor... 🍳",
                color = Color.Gray
            )
        } else if (recipes.isEmpty()) {
            EmptyHomeCard(
                "Henüz tarif eklenmemiş. Tarif Ekle bölümünden ilk tarifini oluşturabilirsin."
            )
        } else {
            HomeSection(
                title = "🍳 Bugün ne pişirsem?",
                subtitle =
                    "Buzdolabındaki malzemelere göre en uygun tarifler",
                matches =
                    rankedMatches.take(4),
                onRecipeClick =
                    onRecipeClick
            )

            if (canMakeRecipes.isNotEmpty()) {
                HomeSection(
                    title =
                        "✅ Şimdi Yapabilirsin",
                    subtitle =
                        "Eksik malzeme olmadan hazırlayabileceğin tarifler",
                    matches =
                        canMakeRecipes.take(4),
                    onRecipeClick =
                        onRecipeClick
                )
            }

            if (almostReadyRecipes.isNotEmpty()) {
                HomeSection(
                    title =
                        "🟡 Neredeyse Hazır",
                    subtitle =
                        "Sadece 1 malzemen eksik",
                    matches =
                        almostReadyRecipes.take(4),
                    onRecipeClick =
                        onRecipeClick
                )
            }

            if (
                expiringIngredients.isNotEmpty() &&
                consumeFirstRecipes.isNotEmpty()
            ) {
                HomeSection(
                    title =
                        "⏰ Önce Bunları Tüket",
                    subtitle =
                        "SKT'si yaklaşan malzemeleri değerlendiren tarifler",
                    matches =
                        consumeFirstRecipes.take(4),
                    onRecipeClick =
                        onRecipeClick
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text =
                if (
                    searchText.isBlank() &&
                    selectedCategory == "Tümü"
                ) {
                    "📚 Tüm Tariflerin"
                } else {
                    "🔎 Sonuçlar"
                },
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (visibleRecipes.isEmpty()) {
            EmptyHomeCard(
                "Bu arama veya kategori için tarif bulunamadı."
            )
        } else {
            visibleRecipes.forEach { recipe ->
                HomeRecipeCard(
                    recipe = recipe,
                    match =
                        recipeMatches.firstOrNull {
                            it.recipe.id == recipe.id
                        },
                    onClick = {
                        onRecipeClick(recipe)
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun PantryOverviewCard(
    totalItems: Int,
    expiringCount: Int,
    canMakeCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDDE9DF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            HomeStat(
                emoji = "🧊",
                value = totalItems,
                label = "Malzeme"
            )
            HomeStat(
                emoji = "⏰",
                value = expiringCount,
                label = "SKT Yakın"
            )
            HomeStat(
                emoji = "🍳",
                value = canMakeCount,
                label = "Yapılabilir"
            )
        }
    }
}

@Composable
private fun HomeStat(
    emoji: String,
    value: Int,
    label: String
) {
    Column {
        Text(
            text = "$emoji $value",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF607066)
        )
    }
}

@Composable
private fun HomeSection(
    title: String,
    subtitle: String,
    matches: List<RecipeMatch>,
    onRecipeClick: (Recipe) -> Unit
) {
    if (matches.isEmpty()) {
        return
    }

    Text(
        text = title,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2F3E34)
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = subtitle,
        fontSize = 13.sp,
        color = Color.Gray
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        matches.forEach { match ->
            SmartRecipeCard(
                match = match,
                onClick = {
                    onRecipeClick(
                        match.recipe
                    )
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(26.dp))
}

@Composable
private fun SmartRecipeCard(
    match: RecipeMatch,
    onClick: () -> Unit
) {
    val recipe = match.recipe

    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column {
            recipe.imageUri?.let { uri ->
                Image(
                    painter =
                        rememberAsyncImagePainter(
                            uri
                        ),
                    contentDescription =
                        recipe.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale =
                        ContentScale.Crop
                )
            }

            Column(
                modifier =
                    Modifier.padding(14.dp)
            ) {
                Text(
                    text = recipe.name,
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color(0xFF2F3E34)
                )

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text =
                        matchStatusText(match),
                    fontSize = 13.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        matchStatusColor(
                            match.status
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text =
                        "⏱️ " +
                                recipe.preparationTime +
                                "   🍽️ " +
                                recipe.servings,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun HomeRecipeCard(
    recipe: Recipe,
    match: RecipeMatch?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            recipe.imageUri?.let { uri ->
                Image(
                    painter =
                        rememberAsyncImagePainter(
                            uri
                        ),
                    contentDescription =
                        recipe.name,
                    modifier = Modifier
                        .width(100.dp)
                        .height(90.dp),
                    contentScale =
                        ContentScale.Crop
                )
            }

            Column(
                modifier =
                    Modifier.padding(
                        start =
                            if (
                                recipe.imageUri != null
                            ) {
                                14.dp
                            } else {
                                0.dp
                            }
                    )
            ) {
                Text(
                    text = recipe.name,
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color(0xFF2F3E34)
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text = recipe.category,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                match?.let {
                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )

                    Text(
                        text =
                            matchStatusText(it),
                        fontSize = 12.sp,
                        color =
                            matchStatusColor(
                                it.status
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHomeCard(
    text: String
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFF0F4F1)
            )
    ) {
        Text(
            text = text,
            modifier =
                Modifier.padding(18.dp),
            fontSize = 14.sp,
            color = Color(0xFF55645A)
        )
    }
}

private fun matchStatusText(
    match: RecipeMatch
): String {
    return when (match.status) {
        MatchStatus.CAN_MAKE ->
            "✅ Yapabilirsin • %" +
                    match.compatibilityPercent

        MatchStatus.ALMOST_READY ->
            "🟡 Neredeyse hazır • %" +
                    match.compatibilityPercent

        MatchStatus.MISSING ->
            "❌ " +
                    match.missingIngredients.size +
                    " eksik • %" +
                    match.compatibilityPercent
    }
}

private fun matchStatusColor(
    status: MatchStatus
): Color {
    return when (status) {
        MatchStatus.CAN_MAKE ->
            Color(0xFF2E7D32)

        MatchStatus.ALMOST_READY ->
            Color(0xFF9A6B00)

        MatchStatus.MISSING ->
            Color(0xFFB3261E)
    }
}

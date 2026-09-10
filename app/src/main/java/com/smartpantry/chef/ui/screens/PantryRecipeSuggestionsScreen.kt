package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.MatchStatus
import com.smartpantry.chef.data.RecipeMatch
import com.smartpantry.chef.data.RecipeMatcher
import com.smartpantry.chef.data.Recipe
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

private data class MealCategory(
    val databaseValue: String,
    val title: String,
    val emoji: String
)

private data class ExpiringRecipeRecommendation(
    val match: RecipeMatch,
    val ingredientName: String,
    val daysLeft: Long
)

private enum class SuggestionFilter {
    BEST,
    READY,
    ALMOST
}

private val mealCategories = listOf(
    MealCategory("Kahvaltı", "Kahvaltı", "☀️"),
    MealCategory("Öğle Yemeği", "Öğle", "🍲"),
    MealCategory("Akşam Yemeği", "Akşam", "🌙"),
    MealCategory("Atıştırmalık", "Atıştırmalık", "🍿"),
    MealCategory("Fit", "Fit", "🥗"),
    MealCategory("Tatlı", "Tatlı", "🍰")
)

@Composable
fun PantryRecipeSuggestionsScreen(
    onBack: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }

    val recipeDao = database.recipeDao()
    val ingredientDao = database.ingredientDao()
    val recipeIngredientDao = database.recipeIngredientDao()

    var matches by remember {
        mutableStateOf<List<RecipeMatch>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var selectedCategory by remember {
        mutableStateOf<MealCategory?>(null)
    }

    var selectedFilter by remember {
        mutableStateOf(SuggestionFilter.BEST)
    }
    var expiringRecommendations by remember {
        mutableStateOf<List<ExpiringRecipeRecommendation>>(emptyList())
    }

    LaunchedEffect(Unit) {
        val recipes = recipeDao.getAllRecipes()
        val pantryIngredients = ingredientDao.getAllIngredients()
        val allRecipeIngredients =
            recipeIngredientDao.getAllRecipeIngredients()

        val calculatedMatches =
            recipes.map { recipe ->
                val ingredientsForRecipe =
                    allRecipeIngredients.filter {
                        it.recipeId == recipe.id
                    }

                RecipeMatcher.matchRecipe(
                    recipe = recipe,
                    recipeIngredients = ingredientsForRecipe,
                    pantryIngredients = pantryIngredients
                )
            }
                .filter {
                    it.totalIngredients > 0
                }

        expiringRecommendations =
            buildExpiringRecommendations(
                matches = calculatedMatches,
                pantryIngredients = pantryIngredients,
                allRecipeIngredients = allRecipeIngredients
            )

        matches =
            calculatedMatches.sortedWith(
                compareBy<RecipeMatch> {
                    when (it.status) {
                        MatchStatus.CAN_MAKE -> 0
                        MatchStatus.ALMOST_READY -> 1
                        MatchStatus.MISSING -> 2
                    }
                }.thenByDescending {
                    it.compatibilityPercent
                }
            )

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F2))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Button(
            onClick = {
                if (selectedCategory == null) {
                    onBack()
                } else {
                    selectedCategory = null
                    selectedFilter = SuggestionFilter.BEST
                }
            }
        ) {
            Text(
                if (selectedCategory == null) {
                    "← Buzdolabıma Dön"
                } else {
                    "← Kategoriler"
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Text(
                    text = "🍳 Tarifler kontrol ediliyor...",
                    fontSize = 18.sp,
                    color = Color(0xFF55645A)
                )
            }

            matches.isEmpty() -> {
                SmallInfoCard(
                    text = "Henüz karşılaştırabileceğim miktarlı bir tarif yok. Önce bir tarif ekleyebilirsin. 🍳"
                )
            }

            selectedCategory == null -> {
                CategorySelectionContent(
                    matches = matches,
                    expiringRecommendations = expiringRecommendations,
                    onRecipeClick = onRecipeClick,
                    onCategoryClick = {
                        selectedCategory = it
                        selectedFilter = SuggestionFilter.BEST
                    }
                )
            }

            else -> {
                CategoryRecipesContent(
                    category = selectedCategory!!,
                    matches = matches,
                    selectedFilter = selectedFilter,
                    onFilterChange = {
                        selectedFilter = it
                    },
                    onRecipeClick = onRecipeClick
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun CategorySelectionContent(
    matches: List<RecipeMatch>,
    expiringRecommendations: List<ExpiringRecipeRecommendation>,
    onRecipeClick: (Recipe) -> Unit,
    onCategoryClick: (MealCategory) -> Unit
) {

    Text(
        text = "🍳 Elimdekilerle Ne Yapabilirim?",
        fontSize = 27.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2F3E34)
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Önce ne yemek istediğini seç, sana en uygun tarifleri gösterelim.",
        fontSize = 15.sp,
        color = Color.Gray
    )

    Spacer(modifier = Modifier.height(20.dp))

    if (expiringRecommendations.isNotEmpty()) {
        ExpiringRecipesSection(
            recommendations = expiringRecommendations,
            onRecipeClick = onRecipeClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    mealCategories
        .chunked(2)
        .forEach { rowCategories ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                rowCategories.forEach { category ->

                    val categoryMatches =
                        matches.filter {
                            it.recipe.category == category.databaseValue
                        }

                    val suitableCount =
                        categoryMatches.count {
                            it.status == MatchStatus.CAN_MAKE ||
                                    it.status == MatchStatus.ALMOST_READY
                        }

                    MealCategoryCard(
                        category = category,
                        suitableCount = suitableCount,
                        totalCount = categoryMatches.size,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCategoryClick(category)
                        }
                    )
                }

                if (rowCategories.size == 1) {
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
}

@Composable
private fun MealCategoryCard(
    category: MealCategory,
    suitableCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier
            .height(138.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = category.emoji,
                fontSize = 28.sp
            )

            Column {

                Text(
                    text = category.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3E34)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text =
                        when {
                            totalCount == 0 ->
                                "Henüz tarif yok"

                            suitableCount == 0 ->
                                "$totalCount tarif var"

                            else ->
                                "$suitableCount uygun tarif"
                        },
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun CategoryRecipesContent(
    category: MealCategory,
    matches: List<RecipeMatch>,
    selectedFilter: SuggestionFilter,
    onFilterChange: (SuggestionFilter) -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {

    val categoryMatches =
        matches.filter {
            it.recipe.category == category.databaseValue
        }

    val filteredMatches =
        when (selectedFilter) {
            SuggestionFilter.BEST ->
                categoryMatches

            SuggestionFilter.READY ->
                categoryMatches.filter {
                    it.status == MatchStatus.CAN_MAKE
                }

            SuggestionFilter.ALMOST ->
                categoryMatches.filter {
                    it.status == MatchStatus.ALMOST_READY
                }
        }

    Text(
        text = "${category.emoji} ${category.title}",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2F3E34)
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "Buzdolabındaki stoklarına göre sıralandı",
        fontSize = 14.sp,
        color = Color.Gray
    )

    Spacer(modifier = Modifier.height(18.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        FilterChip(
            selected = selectedFilter == SuggestionFilter.BEST,
            onClick = {
                onFilterChange(SuggestionFilter.BEST)
            },
            label = {
                Text("⭐ En Uygun")
            },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = selectedFilter == SuggestionFilter.READY,
            onClick = {
                onFilterChange(SuggestionFilter.READY)
            },
            label = {
                Text("✅ Hazır")
            },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = selectedFilter == SuggestionFilter.ALMOST,
            onClick = {
                onFilterChange(SuggestionFilter.ALMOST)
            },
            label = {
                Text("🛒 Az Eksik")
            },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    if (categoryMatches.isEmpty()) {

        SmallInfoCard(
            text = "Bu kategoride henüz tarif yok."
        )

    } else if (filteredMatches.isEmpty()) {

        SmallInfoCard(
            text =
                when (selectedFilter) {
                    SuggestionFilter.READY ->
                        "Şu anda bu kategoride tamamen hazır bir tarif yok."

                    SuggestionFilter.ALMOST ->
                        "Bu kategoride yalnızca bir malzemesi eksik tarif yok."

                    SuggestionFilter.BEST ->
                        "Bu kategoride öneri bulunamadı."
                }
        )

    } else {

        filteredMatches
            .forEachIndexed { index, match ->

                CompactRecipeCard(
                    match = match,
                    rank =
                        if (selectedFilter == SuggestionFilter.BEST) {
                            index + 1
                        } else {
                            null
                        },
                    onClick = {
                        onRecipeClick(match.recipe)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
    }
}

@Composable
private fun CompactRecipeCard(
    match: RecipeMatch,
    rank: Int?,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(17.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        buildString {
                            if (rank != null && rank <= 3) {
                                append(
                                    when (rank) {
                                        1 -> "🥇 "
                                        2 -> "🥈 "
                                        else -> "🥉 "
                                    }
                                )
                            }

                            append(match.recipe.name)
                        },
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3E34)
                )

                Text(
                    text = "%${match.compatibilityPercent}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4F7F65)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = {
                    match.compatibilityPercent / 100f
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(11.dp))

            Text(
                text =
                    when (match.status) {
                        MatchStatus.CAN_MAKE ->
                            "✅ Evde her şey var"

                        MatchStatus.ALMOST_READY ->
                            "🛒 Sadece 1 malzeme eksik"

                        MatchStatus.MISSING -> {
                            val count =
                                match.missingIngredients.size

                            "🛒 $count malzeme eksik"
                        }
                    },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color =
                    when (match.status) {
                        MatchStatus.CAN_MAKE ->
                            Color(0xFF3F6F52)

                        else ->
                            Color(0xFF715B2E)
                    }
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text =
                    "⏱ ${match.recipe.preparationTime}   •   ${match.recipe.servings}",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ExpiringRecipesSection(
    recommendations: List<ExpiringRecipeRecommendation>,
    onRecipeClick: (Recipe) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "♻️ Önce Bunları Tüket",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3E34)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Önümüzdeki 5 gün içinde tüketmen iyi olacak malzemeleri değerlendirebileceğin tarifler.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            recommendations
                .take(3)
                .forEach { recommendation ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onRecipeClick(recommendation.match.recipe)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = recommendation.match.recipe.name,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2F3E34)
                                )

                                Text(
                                    text = expiryBadgeText(
                                        recommendation.daysLeft
                                    ),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7A5A21)
                                )
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = expiryDescription(
                                    ingredientName = recommendation.ingredientName,
                                    daysLeft = recommendation.daysLeft
                                ),
                                fontSize = 13.sp,
                                color = Color(0xFF4F7F65)
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = "Uygunluk: %${recommendation.match.compatibilityPercent}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
        }
    }
}

private fun buildExpiringRecommendations(
    matches: List<RecipeMatch>,
    pantryIngredients: List<com.smartpantry.chef.data.Ingredient>,
    allRecipeIngredients: List<com.smartpantry.chef.data.RecipeIngredient>
): List<ExpiringRecipeRecommendation> {

    val formatter = SimpleDateFormat(
        "dd.MM.yyyy",
        Locale.getDefault()
    ).apply {
        isLenient = false
    }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val expiringPantry =
        pantryIngredients.mapNotNull { pantry ->

            if (pantry.remainingQuantity <= 0.0) {
                return@mapNotNull null
            }

            try {
                val expiration =
                    formatter.parse(pantry.expirationDate)
                        ?: return@mapNotNull null

                val expirationCalendar =
                    Calendar.getInstance().apply {
                        time = expiration
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                val daysLeft =
                    TimeUnit.MILLISECONDS.toDays(
                        expirationCalendar.timeInMillis -
                                today.timeInMillis
                    )

                if (daysLeft in 0L..5L) {
                    pantry to daysLeft
                } else {
                    null
                }

            } catch (_: Exception) {
                null
            }
        }

    return matches.mapNotNull { match ->

        val recipeIngredients =
            allRecipeIngredients.filter {
                it.recipeId == match.recipe.id
            }

        val urgentIngredient =
            expiringPantry
                .filter { (pantry, _) ->
                    recipeIngredients.any { recipeIngredient ->
                        normalizeIngredientName(recipeIngredient.name) ==
                                normalizeIngredientName(pantry.name)
                    }
                }
                .minByOrNull { (_, daysLeft) ->
                    daysLeft
                }

        urgentIngredient?.let { (pantry, daysLeft) ->
            ExpiringRecipeRecommendation(
                match = match,
                ingredientName = pantry.name,
                daysLeft = daysLeft
            )
        }

    }.sortedWith(
        compareBy<ExpiringRecipeRecommendation> {
            it.daysLeft
        }.thenBy {
            when (it.match.status) {
                MatchStatus.CAN_MAKE -> 0
                MatchStatus.ALMOST_READY -> 1
                MatchStatus.MISSING -> 2
            }
        }.thenByDescending {
            it.match.compatibilityPercent
        }
    )
}

private fun normalizeIngredientName(
    name: String
): String {
    return name
        .trim()
        .lowercase(Locale.getDefault())
        .replace("ı", "i")
        .replace("ş", "s")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ö", "o")
        .replace("ç", "c")
}

private fun expiryBadgeText(
    daysLeft: Long
): String {
    return when (daysLeft) {
        0L -> "BUGÜN"
        1L -> "1 GÜN"
        else -> "$daysLeft GÜN"
    }
}

private fun expiryDescription(
    ingredientName: String,
    daysLeft: Long
): String {
    return when (daysLeft) {
        0L -> "$ingredientName bugün tüketilmeli."
        1L -> "$ingredientName için 1 gün kaldı."
        else -> "$ingredientName için $daysLeft gün kaldı."
    }
}

@Composable
private fun SmallInfoCard(
    text: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDDE9DF)
        )
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            color = Color(0xFF55645A)
        )
    }
}

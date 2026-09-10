package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val recipeDao = database.recipeDao()
    val ingredientDao = database.ingredientDao()
    val recipeIngredientDao = database.recipeIngredientDao()

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var recipeMatch by remember {
        mutableStateOf<RecipeMatch?>(null)
    }

    var structuredIngredients by remember {
        mutableStateOf<List<RecipeIngredient>>(emptyList())
    }

    var pantryCheckLoading by remember {
        mutableStateOf(true)
    }

    var pantryIngredients by remember {
        mutableStateOf<List<Ingredient>>(emptyList())
    }

    var showCookDialog by remember {
        mutableStateOf(false)
    }

    var stockMessage by remember {
        mutableStateOf<String?>(null)
    }

    var refreshPantry by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(recipe.id, refreshPantry) {
        pantryCheckLoading = true

        val recipeIngredients =
            recipeIngredientDao.getIngredientsForRecipe(recipe.id)

        val loadedPantryIngredients =
            ingredientDao.getAllIngredients()

        pantryIngredients = loadedPantryIngredients
        structuredIngredients = recipeIngredients

        recipeMatch =
            RecipeMatcher.matchRecipe(
                recipe = recipe,
                recipeIngredients = recipeIngredients,
                pantryIngredients = loadedPantryIngredients
            )

        pantryCheckLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F2))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Geri")
        }

        Spacer(modifier = Modifier.height(18.dp))

        recipe.imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = recipe.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = recipe.category,
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "⏱️ Süre: ${recipe.preparationTime}",
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🍽️ Porsiyon: ${recipe.servings}",
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PantryCompatibilitySection(
            recipeMatch = recipeMatch,
            structuredIngredients = structuredIngredients,
            isLoading = pantryCheckLoading
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                showCookDialog = true
            },
            enabled = recipeMatch?.status == MatchStatus.CAN_MAKE &&
                    structuredIngredients.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🍽️ Yemeği Yaptım",
                fontSize = 16.sp
            )
        }

        stockMessage?.let { message ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3F6F52)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "🥕 Malzemeler",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = recipe.ingredients,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "👩‍🍳 Hazırlanışı",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = recipe.instructions,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "✏️ Tarifi Düzenle",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                showDeleteDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB3261E)
            )
        ) {
            Text(
                text = "🗑️ Tarifi Sil",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(25.dp))
    }

    if (showCookDialog) {
        AlertDialog(
            onDismissRequest = {
                showCookDialog = false
            },
            title = {
                Text("🍽️ Yemeği Yaptım")
            },
            text = {
                Text(
                    "Bu tarifi yaptığını onaylarsan kullanılan malzemeler " +
                            "buzdolabı stoklarından otomatik düşülecek."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val success = deductRecipeFromPantry(
                                recipeIngredients = structuredIngredients,
                                pantryIngredients = pantryIngredients,
                                updateIngredient = { ingredient ->
                                    ingredientDao.updateIngredient(ingredient)
                                }
                            )

                            showCookDialog = false

                            if (success) {
                                stockMessage =
                                    "✅ Tarif tamamlandı. Buzdolabı stokların güncellendi."
                                refreshPantry++
                            } else {
                                stockMessage =
                                    "⚠️ Stoklar değişti veya yeterli değil. Buzdolabını kontrol et."
                                refreshPantry++
                            }
                        }
                    }
                ) {
                    Text("Evet, Stoktan Düş")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCookDialog = false
                    }
                ) {
                    Text("Vazgeç")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Tarifi Sil")
            },
            text = {
                Text(
                    "\"${recipe.name}\" tarifini silmek istediğine emin misin?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            recipeDao.deleteRecipe(recipe)
                            showDeleteDialog = false
                            onBack()
                        }
                    }
                ) {
                    Text(
                        text = "Evet, Sil",
                        color = Color(0xFFB3261E)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun PantryCompatibilitySection(
    recipeMatch: RecipeMatch?,
    structuredIngredients: List<RecipeIngredient>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = "🧊 Buzdolabına Göre Uygunluk",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3E34)
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Text(
                        text = "Malzemelerin kontrol ediliyor... 🍳",
                        color = Color.Gray
                    )
                }

                recipeMatch == null || structuredIngredients.isEmpty() -> {
                    Text(
                        text = "Bu tarifin miktarlı malzeme bilgisi bulunmuyor.",
                        color = Color.Gray
                    )
                }

                else -> {
                    val match = recipeMatch

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "%${match.compatibilityPercent} uygun",
                            fontSize = 20.sp,
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text =
                            when (match.status) {
                                MatchStatus.CAN_MAKE ->
                                    "✅ Bu tarifi şu an yapabilirsin."

                                MatchStatus.ALMOST_READY ->
                                    "🟡 Neredeyse hazırsın. Sadece 1 malzeme eksik."

                                MatchStatus.MISSING ->
                                    "🛒 Bazı malzemelerin eksik."
                            },
                        fontWeight = FontWeight.SemiBold,
                        color =
                            if (match.status == MatchStatus.CAN_MAKE) {
                                Color(0xFF3F6F52)
                            } else {
                                Color(0xFF715B2E)
                            }
                    )

                    val missingNames =
                        match.missingIngredients
                            .map {
                                normalizeIngredientName(it.name)
                            }
                            .toSet()

                    val availableIngredients =
                        structuredIngredients.filter {
                            normalizeIngredientName(it.name) !in missingNames
                        }

                    if (availableIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "✅ EVİNDE VAR",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F6F52)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        availableIngredients.forEach { ingredient ->
                            Text(
                                text =
                                    "✅ ${formatQuantity(ingredient.quantity)} " +
                                            "${ingredient.unit} ${ingredient.name}",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }

                    if (match.missingIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "🛒 EKSİK",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8A5A1F)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        match.missingIngredients.forEach { missing ->
                            Text(
                                text =
                                    "🛒 ${formatQuantity(missing.missingQuantity)} " +
                                            "${missing.unit} ${missing.name}",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text =
                                if (match.missingIngredients.size == 1) {
                                    val missing =
                                        match.missingIngredients.first()

                                    "${formatQuantity(missing.missingQuantity)} " +
                                            "${missing.unit} ${missing.name} alırsan " +
                                            "tarifi tamamen yapabilirsin."
                                } else {
                                    "Eksik malzemeleri tamamladığında bu tarifi yapabilirsin."
                                },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun normalizeIngredientName(
    name: String
): String {
    return name
        .trim()
        .lowercase()
        .replace("ı", "i")
        .replace("ş", "s")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ö", "o")
        .replace("ç", "c")
}

private fun formatQuantity(
    quantity: Double
): String {
    return if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        String.format("%.2f", quantity)
            .trimEnd('0')
            .trimEnd(',')
            .trimEnd('.')
    }
}

private suspend fun deductRecipeFromPantry(
    recipeIngredients: List<RecipeIngredient>,
    pantryIngredients: List<Ingredient>,
    updateIngredient: suspend (Ingredient) -> Unit
): Boolean {

    data class PlannedUpdate(
        val ingredient: Ingredient,
        val remainingQuantity: Double
    )

    val plannedUpdates = mutableListOf<PlannedUpdate>()
    val availableById = pantryIngredients.associateBy { it.id }.toMutableMap()

    for (required in recipeIngredients) {

        var amountNeededBase =
            convertQuantityToBase(required.quantity, required.unit)
                ?: return false

        val compatibleRows =
            availableById.values
                .filter { pantry ->
                    normalizeIngredientName(pantry.name) ==
                            normalizeIngredientName(required.name) &&
                            ingredientUnitType(pantry.unit) ==
                            ingredientUnitType(required.unit)
                }
                .sortedBy { it.id }

        for (pantry in compatibleRows) {

            if (amountNeededBase <= 0.000001) break

            val pantryBase =
                convertQuantityToBase(
                    pantry.remainingQuantity,
                    pantry.unit
                ) ?: continue

            if (pantryBase <= 0.0) continue

            val usedBase = minOf(
                pantryBase,
                amountNeededBase
            )

            val remainingBase =
                pantryBase - usedBase

            val newRemainingQuantity =
                convertBaseToUnit(
                    remainingBase,
                    pantry.unit
                ) ?: return false

            val updatedIngredient =
                pantry.copy(
                    remainingQuantity =
                        if (newRemainingQuantity < 0.000001) {
                            0.0
                        } else {
                            newRemainingQuantity
                        }
                )

            availableById[pantry.id] = updatedIngredient

            val existingIndex =
                plannedUpdates.indexOfFirst {
                    it.ingredient.id == pantry.id
                }

            val planned =
                PlannedUpdate(
                    ingredient = updatedIngredient,
                    remainingQuantity =
                        updatedIngredient.remainingQuantity
                )

            if (existingIndex >= 0) {
                plannedUpdates[existingIndex] = planned
            } else {
                plannedUpdates.add(planned)
            }

            amountNeededBase -= usedBase
        }

        if (amountNeededBase > 0.000001) {
            return false
        }
    }

    plannedUpdates.forEach { planned ->
        updateIngredient(
            planned.ingredient.copy(
                remainingQuantity =
                    planned.remainingQuantity
            )
        )
    }

    return true
}

private fun ingredientUnitType(
    unit: String
): String {

    return when (unit.trim().lowercase()) {
        "gram", "kilogram" -> "WEIGHT"
        "mililitre", "litre" -> "VOLUME"
        "adet" -> "COUNT"
        else -> "UNKNOWN"
    }
}

private fun convertQuantityToBase(
    quantity: Double,
    unit: String
): Double? {

    return when (unit.trim().lowercase()) {
        "gram" -> quantity
        "kilogram" -> quantity * 1000.0
        "mililitre" -> quantity
        "litre" -> quantity * 1000.0
        "adet" -> quantity
        else -> null
    }
}

private fun convertBaseToUnit(
    quantity: Double,
    unit: String
): Double? {

    return when (unit.trim().lowercase()) {
        "gram" -> quantity
        "kilogram" -> quantity / 1000.0
        "mililitre" -> quantity
        "litre" -> quantity / 1000.0
        "adet" -> quantity
        else -> null
    }
}


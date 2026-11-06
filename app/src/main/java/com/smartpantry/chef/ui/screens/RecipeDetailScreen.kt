package com.smartpantry.chef.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.smartpantry.chef.data.RecipeComment
import com.smartpantry.chef.data.RecipeEngagement
import com.smartpantry.chef.data.RecipeIngredient
import com.smartpantry.chef.data.RecipeMatch
import com.smartpantry.chef.data.RecipeMedia
import com.smartpantry.chef.data.RecipeMatcher
import com.smartpantry.chef.data.ShoppingItem
import kotlinx.coroutines.launch
import java.util.Locale

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
    val recipeMediaDao = database.recipeMediaDao()
    val recipeEngagementDao = database.recipeEngagementDao()
    val recipeCommentDao = database.recipeCommentDao()
    val shoppingItemDao = database.shoppingItemDao()

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var recipeMatch by remember {
        mutableStateOf<RecipeMatch?>(null)
    }

    var structuredIngredients by remember {
        mutableStateOf<List<RecipeIngredient>>(emptyList())
    }

    var recipeMedia by remember {
        mutableStateOf<List<RecipeMedia>>(emptyList())
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

    var shoppingMessage by remember {
        mutableStateOf<String?>(null)
    }

    var engagement by remember {
        mutableStateOf(
            RecipeEngagement(
                recipeId = recipe.id
            )
        )
    }

    var comments by remember {
        mutableStateOf<List<RecipeComment>>(
            emptyList()
        )
    }

    var pantryExpanded by remember { mutableStateOf(false) }

    var newComment by remember {
        mutableStateOf("")
    }

    LaunchedEffect(recipe.id, refreshPantry) {
        pantryCheckLoading = true

        val recipeIngredients =
            recipeIngredientDao.getIngredientsForRecipe(recipe.id)

        val loadedPantryIngredients =
            ingredientDao.getAllIngredients()

        val loadedRecipeMedia =
            recipeMediaDao
                .getMediaForRecipe(recipe.id)

        val loadedEngagement =
            recipeEngagementDao
                .getForRecipe(recipe.id)
                ?: RecipeEngagement(
                    recipeId = recipe.id
                )

        val loadedComments =
            recipeCommentDao
                .getCommentsForRecipe(
                    recipe.id
                )

        pantryIngredients = loadedPantryIngredients
        structuredIngredients = recipeIngredients
        recipeMedia = loadedRecipeMedia
        engagement = loadedEngagement
        comments = loadedComments

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedButton(
                onClick = onBack
            ) {
                Text("← Geri")
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = { pantryExpanded = !pantryExpanded },
                modifier = Modifier.padding(end = 8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("🧊 Uygunluk", fontSize = 12.sp)
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {

                        val updated =
                            engagement.copy(
                                isFavorite =
                                    !engagement.isFavorite
                            )

                        engagement = updated

                        scope.launch {
                            recipeEngagementDao
                                .upsert(updated)
                        }
                    },
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        if (
                            engagement.isFavorite
                        ) {
                            "🍲"
                        } else {
                            "🥣"
                        },
                    fontSize = 20.sp
                )
            }
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

        Spacer(modifier = Modifier.height(10.dp))

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

        val recipeImages =
            recipeMedia.filter {
                it.mediaType ==
                        RecipeMedia.TYPE_IMAGE
            }

        val recipeVideo =
            recipeMedia.firstOrNull {
                it.mediaType ==
                        RecipeMedia.TYPE_VIDEO
            }

        if (
            recipeImages.isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(26.dp)
            )

            Text(
                text =
                    "📸 Yapım Aşamaları",
                fontSize = 21.sp,
                fontWeight =
                    FontWeight.Bold,
                color =
                    Color(0xFF2F3E34)
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(
                text =
                    "Tarif hazırlanırken eklenen fotoğraflar",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    )
            ) {

                recipeImages
                    .forEachIndexed {
                            index,
                            media ->

                        Card(
                            modifier = Modifier
                                .width(240.dp)
                                .padding(
                                    end = 12.dp
                                ),
                            shape =
                                RoundedCornerShape(
                                    16.dp
                                )
                        ) {

                            Column {

                                Image(
                                    painter =
                                        rememberAsyncImagePainter(
                                            media.uri
                                        ),
                                    contentDescription =
                                        "Yapım aşaması fotoğrafı " +
                                                "${index + 1}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale =
                                        ContentScale.Crop
                                )

                                Text(
                                    text =
                                        "${index + 1}. fotoğraf",
                                    modifier =
                                        Modifier.padding(
                                            10.dp
                                        ),
                                    fontSize = 13.sp,
                                    color =
                                        Color(
                                            0xFF5B665E
                                        )
                                )
                            }
                        }
                    }
            }
        }

        recipeVideo?.let {
                video ->

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(18.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color(0xFFE7EEF9)
                    )
            ) {

                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {

                    Text(
                        text =
                            "🎥 Tarif Videosu",
                        fontSize = 19.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color(0xFF2F3E34)
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Tarifin yapılış videosunu açıp izleyebilirsin.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Button(
                        onClick = {

                            val intent =
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(
                                        video.uri
                                    )
                                ).apply {
                                    addFlags(
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }

                            try {
                                context.startActivity(
                                    intent
                                )
                            } catch (_: Exception) {
                                android.widget.Toast
                                    .makeText(
                                        context,
                                        "Video açılamadı.",
                                        android.widget.Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "▶ Videoyu İzle"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        RecipeRatingSection(
            selectedRating =
                engagement.myRating,
            averageRating =
                engagement.averageRating,
            ratingCount =
                engagement.ratingCount,
            onRatingSelected = { rating ->

                val oldPersonalRating =
                    engagement.myRating

                val newCount =
                    if (
                        oldPersonalRating == 0
                    ) {
                        engagement.ratingCount + 1
                    } else {
                        engagement.ratingCount
                    }

                val oldTotal =
                    engagement.averageRating *
                            engagement.ratingCount

                val adjustedTotal =
                    oldTotal -
                            oldPersonalRating +
                            rating

                val updated =
                    engagement.copy(
                        myRating = rating,
                        ratingCount = newCount,
                        averageRating =
                            if (newCount > 0) {
                                adjustedTotal /
                                        newCount
                            } else {
                                0.0
                            }
                    )

                engagement = updated

                scope.launch {
                    recipeEngagementDao
                        .upsert(updated)
                }
            }
        )

        Spacer(modifier = Modifier.height(26.dp))

        RecipeCommentsSection(
            comments = comments,
            commentText = newComment,
            onCommentTextChange = {
                newComment = it
            },
            onReply = { parentId, replyText ->
                scope.launch {
                    recipeCommentDao.insertComment(RecipeComment(recipeId = recipe.id, authorNickname = "@nickname", text = replyText, createdAt = System.currentTimeMillis(), parentCommentId = parentId))
                    comments = recipeCommentDao.getCommentsForRecipe(recipe.id)
                    val updated = engagement.copy(commentCount = comments.size)
                    engagement = updated
                    recipeEngagementDao.upsert(updated)
                }
            },
            onReact = { comment, reaction ->
                scope.launch {
                    val old = comment.myReaction
                    val newValue = if (old == reaction) 0 else reaction
                    val likes = (comment.likeCount + (if (newValue == 1) 1 else 0) - (if (old == 1) 1 else 0)).coerceAtLeast(0)
                    val dislikes = (comment.dislikeCount + (if (newValue == -1) 1 else 0) - (if (old == -1) 1 else 0)).coerceAtLeast(0)
                    recipeCommentDao.updateReaction(comment.id, likes, dislikes, newValue)
                    comments = recipeCommentDao.getCommentsForRecipe(recipe.id)
                }
            },
            onSubmit = {

                val cleanText =
                    newComment.trim()

                if (cleanText.isNotEmpty()) {

                    scope.launch {

                        recipeCommentDao
                            .insertComment(
                                RecipeComment(
                                    recipeId =
                                        recipe.id,
                                    authorNickname =
                                        "@nickname",
                                    text =
                                        cleanText,
                                    createdAt =
                                        System.currentTimeMillis()
                                )
                            )

                        comments =
                            recipeCommentDao
                                .getCommentsForRecipe(
                                    recipe.id
                                )

                        val updated =
                            engagement.copy(
                                commentCount =
                                    comments.size
                            )

                        engagement = updated

                        recipeEngagementDao
                            .upsert(updated)

                        newComment = ""
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (pantryExpanded) {
        PantryCompatibilitySection(
            recipeMatch = recipeMatch,
            structuredIngredients = structuredIngredients,
            isLoading = pantryCheckLoading,
            onAddMissingToShoppingList = {
                scope.launch {
                    addMissingIngredientsToShoppingList(
                        missingItems = recipeMatch
                            ?.missingIngredients
                            .orEmpty()
                            .map { missing ->
                                ShoppingItem(
                                    name = missing.name,
                                    quantity =
                                        missing.missingQuantity,
                                    unit = missing.unit
                                )
                            },
                        currentShoppingItems =
                            shoppingItemDao
                                .getAllShoppingItems(),
                        insertShoppingItem = { item ->
                            shoppingItemDao
                                .insertShoppingItem(item)
                        },
                        updateShoppingItem = { item ->
                            shoppingItemDao
                                .updateShoppingItem(item)
                        }
                    )

                    shoppingMessage =
                        "✅ Eksik malzemeler alışveriş listene eklendi."
                }
            }
        )

        }

        shoppingMessage?.let { message ->
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3F6F52)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                showCookDialog = true
            },
            enabled =
                recipeMatch?.status ==
                        MatchStatus.CAN_MAKE &&
                        structuredIngredients
                            .isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "🍽️ Yemeği Yaptım",
                fontSize = 15.sp
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

        Spacer(modifier = Modifier.height(30.dp))

        HorizontalDivider(
            color = Color(0xFFD9DED9)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            OutlinedButton(
                onClick = onEdit,
                shape = RoundedCornerShape(14.dp)
            ) {

                Text(
                    text = "✏️ Düzenle",
                    fontSize = 13.sp
                )
            }

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            TextButton(
                onClick = {
                    showDeleteDialog = true
                }
            ) {

                Text(
                    text = "🗑️ Tarifi Sil",
                    fontSize = 13.sp,
                    color = Color(0xFFB3261E)
                )
            }
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
    isLoading: Boolean,
    onAddMissingToShoppingList: () -> Unit
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

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onAddMissingToShoppingList,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🛒 Alışveriş Listeme Ekle",
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun normalizeIngredientName(
    name: String
): String {
    val cleanedName =
        name
            .trim()
            .lowercase(Locale("tr", "TR"))
            .replace("ı", "i")
            .replace("ş", "s")
            .replace("ğ", "g")
            .replace("ü", "u")
            .replace("ö", "o")
            .replace("ç", "c")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    return when {
        cleanedName.length > 5 && cleanedName.endsWith("lar") ->
            cleanedName.dropLast(3).trim()

        cleanedName.length > 5 && cleanedName.endsWith("ler") ->
            cleanedName.dropLast(3).trim()

        else -> cleanedName
    }
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


private suspend fun addMissingIngredientsToShoppingList(
    missingItems: List<ShoppingItem>,
    currentShoppingItems: List<ShoppingItem>,
    insertShoppingItem: suspend (ShoppingItem) -> Long,
    updateShoppingItem: suspend (ShoppingItem) -> Unit
) {
    val workingItems = currentShoppingItems.toMutableList()

    for (missing in missingItems) {
        if (missing.quantity <= 0.0) continue

        val existingIndex =
            workingItems.indexOfFirst { existing ->
                !existing.isPurchased &&
                        normalizeIngredientName(existing.name) ==
                        normalizeIngredientName(missing.name) &&
                        ingredientUnitType(existing.unit) ==
                        ingredientUnitType(missing.unit)
            }

        if (existingIndex >= 0) {
            val existing = workingItems[existingIndex]
            val existingBase =
                convertQuantityToBase(existing.quantity, existing.unit)
            val missingBase =
                convertQuantityToBase(missing.quantity, missing.unit)

            if (existingBase != null && missingBase != null) {
                val combinedQuantity =
                    convertBaseToUnit(
                        existingBase + missingBase,
                        existing.unit
                    )

                if (combinedQuantity != null) {
                    val updated = existing.copy(quantity = combinedQuantity)
                    updateShoppingItem(updated)
                    workingItems[existingIndex] = updated
                }
            }
        } else {
            val newId = insertShoppingItem(missing)
            workingItems.add(missing.copy(id = newId.toInt()))
        }
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


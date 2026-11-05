package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.RecipeEngagement
import kotlinx.coroutines.launch

private enum class ProfileRecipeFilter(
    val label: String
) {
    DEFAULT("Varsayılan"),
    FAVORITES("Favoriler"),
    LIKES("Beğeniler"),
    COMMENTS_ASC("En az yorum"),
    COMMENTS_DESC("En çok yorum")
}

@Composable
fun ProfileScreen(
    onRecipeClick: (Recipe) -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database =
        remember {
            AppDatabase.getDatabase(
                context
            )
        }

    val recipeDao =
        database.recipeDao()

    val engagementDao =
        database.recipeEngagementDao()

    var recipes by remember {
        mutableStateOf<List<Recipe>>(
            emptyList()
        )
    }

    val engagementByRecipe =
        remember {
            mutableStateMapOf<
                    Int,
                    RecipeEngagement
                    >()
        }

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("Tümü")
    }

    var categoryMenuOpen by remember {
        mutableStateOf(false)
    }

    var filterMenuOpen by remember {
        mutableStateOf(false)
    }

    var selectedFilter by remember {
        mutableStateOf(
            ProfileRecipeFilter.DEFAULT
        )
    }

    val categories =
        listOf(
            "Tümü",
            "Kahvaltı",
            "Öğle Yemeği",
            "Akşam Yemeği",
            "Tatlı",
            "Fit",
            "Atıştırmalık"
        )

    LaunchedEffect(Unit) {

        recipes =
            recipeDao.getAllRecipes()

        engagementDao
            .getAll()
            .forEach {
                engagementByRecipe[
                    it.recipeId
                ] = it
            }
    }

    val filteredRecipes =
        recipes.filter { recipe ->

            val matchesSearch =
                searchText.isBlank() ||
                        recipe.name
                            .contains(
                                searchText,
                                ignoreCase = true
                            )

            val matchesCategory =
                selectedCategory ==
                        "Tümü" ||
                        recipe.category ==
                        selectedCategory

            matchesSearch &&
                    matchesCategory
        }

    val visibleRecipes =
        when (selectedFilter) {

            ProfileRecipeFilter.DEFAULT ->
                filteredRecipes

            ProfileRecipeFilter.FAVORITES ->
                filteredRecipes
                    .filter {
                        engagementByRecipe[
                            it.id
                        ]?.isFavorite ==
                                true
                    }

            ProfileRecipeFilter.LIKES ->
                filteredRecipes
                    .sortedByDescending {
                        engagementByRecipe[
                            it.id
                        ]?.likeCount
                            ?: 0
                    }

            ProfileRecipeFilter.COMMENTS_ASC ->
                filteredRecipes
                    .sortedBy {
                        engagementByRecipe[
                            it.id
                        ]?.commentCount
                            ?: 0
                    }

            ProfileRecipeFilter.COMMENTS_DESC ->
                filteredRecipes
                    .sortedByDescending {
                        engagementByRecipe[
                            it.id
                        ]?.commentCount
                            ?: 0
                    }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF8F6F2)
            )
            .padding(
                horizontal = 16.dp
            )
    ) {

        Spacer(
            modifier =
                Modifier.height(22.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFFDDE9DF)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "👩‍🍳",
                    fontSize = 32.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 14.dp
                    )
            ) {

                Text(
                    text = "@nickname",
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color(
                            0xFF2F3E34
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            3.dp
                        )
                )

                Text(
                    text =
                        "Bugün ne pişirsem?",
                    fontSize = 14.sp,
                    color =
                        Color(
                            0xFF68736C
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            3.dp
                        )
                )

                Text(
                    text =
                        recipes.size
                            .toString() +
                                " tarif",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = {
                    // Hesap ayarları daha sonra bağlanacak.
                }
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Settings,
                    contentDescription =
                        "Ayarlar"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier =
                Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Tariflerimde ara..."
                )
            },
            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Default.Search,
                    contentDescription =
                        "Tarif ara"
                )
            },
            singleLine = true,
            shape =
                RoundedCornerShape(
                    16.dp
                ),
            colors =
                OutlinedTextFieldDefaults
                    .colors(
                        focusedContainerColor =
                            Color.White,
                        unfocusedContainerColor =
                            Color.White,
                        focusedBorderColor =
                            Color(
                                0xFF7A9E7E
                            ),
                        unfocusedBorderColor =
                            Color(
                                0xFFE3E4E1
                            )
                    )
        )

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    "TARİF DEFTERİM",
                modifier =
                    Modifier.weight(1f),
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    Color(
                        0xFF2F3E34
                    )
            )

            Box {

                TextButton(
                    onClick = {
                        categoryMenuOpen =
                            true
                    }
                ) {

                    Text(
                        text =
                            if (
                                selectedCategory ==
                                "Tümü"
                            ) {
                                "Kategoriler"
                            } else {
                                selectedCategory
                            },
                        fontSize = 13.sp,
                        fontWeight =
                            FontWeight.SemiBold,
                        color =
                            Color(
                                0xFF4F6F5A
                            )
                    )
                }

                DropdownMenu(
                    expanded =
                        categoryMenuOpen,
                    onDismissRequest = {
                        categoryMenuOpen =
                            false
                    }
                ) {

                    categories.forEach {
                            category ->

                        DropdownMenuItem(
                            text = {
                                Text(
                                    category
                                )
                            },
                            onClick = {
                                selectedCategory =
                                    category
                                categoryMenuOpen =
                                    false
                            }
                        )
                    }
                }
            }

            Box {

                IconButton(
                    onClick = {
                        filterMenuOpen =
                            true
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.FilterList,
                        contentDescription =
                            "Tarifleri filtrele",
                        tint =
                            Color(
                                0xFF4F6F5A
                            )
                    )
                }

                DropdownMenu(
                    expanded =
                        filterMenuOpen,
                    onDismissRequest = {
                        filterMenuOpen =
                            false
                    }
                ) {

                    ProfileRecipeFilter
                        .entries
                        .forEach {
                                filter ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        filter.label
                                    )
                                },
                                onClick = {
                                    selectedFilter =
                                        filter
                                    filterMenuOpen =
                                        false
                                }
                            )
                        }
                }
            }
        }

        if (
            selectedCategory != "Tümü" ||
            selectedFilter !=
            ProfileRecipeFilter.DEFAULT
        ) {

            Text(
                text =
                    buildString {

                        if (
                            selectedCategory !=
                            "Tümü"
                        ) {
                            append(
                                selectedCategory
                            )
                        }

                        if (
                            selectedFilter !=
                            ProfileRecipeFilter.DEFAULT
                        ) {

                            if (isNotEmpty()) {
                                append(" • ")
                            }

                            append(
                                selectedFilter.label
                            )
                        }
                    },
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )
        }

        HorizontalDivider(
            color =
                Color(
                    0xFFD7DDD8
                )
        )

        Spacer(
            modifier =
                Modifier.height(14.dp)
        )

        if (
            visibleRecipes
                .isEmpty()
        ) {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(
                        16.dp
                    ),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    )
            ) {

                Text(
                    text =
                        "Bu seçimlere uygun tarif bulunamadı.",
                    modifier =
                        Modifier.padding(
                            18.dp
                        ),
                    color = Color.Gray
                )
            }

        } else {

            LazyVerticalGrid(
                columns =
                    GridCells.Fixed(3),
                modifier =
                    Modifier.fillMaxSize(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        9.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                items(
                    items =
                        visibleRecipes,
                    key = {
                        it.id
                    }
                ) { recipe ->

                    ProfileRecipeCard(
                        recipe = recipe,
                        engagement =
                            engagementByRecipe[
                                recipe.id
                            ]
                                ?: RecipeEngagement(
                                    recipeId =
                                        recipe.id
                                ),
                        onClick = {
                            onRecipeClick(
                                recipe
                            )
                        },
                        onFavoriteToggle = {

                            val current =
                                engagementByRecipe[
                                    recipe.id
                                ]
                                    ?: RecipeEngagement(
                                        recipeId =
                                            recipe.id
                                    )

                            val updated =
                                current.copy(
                                    isFavorite =
                                        !current.isFavorite
                                )

                            engagementByRecipe[
                                recipe.id
                            ] = updated

                            scope.launch {
                                engagementDao
                                    .upsert(
                                        updated
                                    )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileRecipeCard(
    recipe: Recipe,
    engagement: RecipeEngagement,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable {
                onClick()
            },
        shape =
            RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    6.dp
            )
    ) {

        Column {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(
                        Color(
                            0xFFE8ECE9
                        )
                    )
            ) {

                recipe.imageUri
                    ?.let { uri ->

                        Image(
                            painter =
                                rememberAsyncImagePainter(
                                    uri
                                ),
                            contentDescription =
                                recipe.name,
                            modifier =
                                Modifier.fillMaxSize(),
                            contentScale =
                                ContentScale.Crop
                        )
                    }

                Box(
                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(5.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(
                                alpha = 0.94f
                            )
                        )
                        .clickable {
                            onFavoriteToggle()
                        },
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text =
                            if (
                                engagement
                                    .isFavorite
                            ) {
                                "🍲"
                            } else {
                                "🥣"
                            },
                        fontSize = 14.sp
                    )
                }
            }

            Column(
                modifier =
                    Modifier.padding(
                        9.dp
                    )
            ) {

                Text(
                    text =
                        recipe.name,
                    modifier =
                        Modifier.height(
                            34.dp
                        ),
                    maxLines = 2,
                    overflow =
                        TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    lineHeight = 15.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    color =
                        Color(
                            0xFF26352B
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            5.dp
                        )
                )

                Text(
                    text =
                        ratingStars(
                            engagement
                                .averageRating
                        ),
                    fontSize = 15.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        Color(
                            0xFFE2A72E
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Comment,
                        contentDescription =
                            "Yorumlar",
                        modifier =
                            Modifier.size(
                                17.dp
                            ),
                        tint =
                            Color(
                                0xFF7A817C
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.size(
                                6.dp
                            )
                    )

                    Text(
                        text =
                            engagement.commentCount
                                .toString() +
                                    " yorum",
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Medium,
                        color =
                            Color(
                                0xFF68736C
                            )
                    )
                }

            }
        }
    }
}

private fun ratingStars(
    rating: Double
): String {

    val fullStars =
        rating
            .toInt()
            .coerceIn(
                0,
                5
            )

    return buildString {

        repeat(fullStars) {
            append("★")
        }

        repeat(
            5 - fullStars
        ) {
            append("☆")
        }
    }
}

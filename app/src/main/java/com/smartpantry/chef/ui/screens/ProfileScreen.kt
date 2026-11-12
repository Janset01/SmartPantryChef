package com.smartpantry.chef.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.graphics.graphicsLayer
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

private enum class PersonalRecipeFilter(
    val label: String
) {
    LIKED("Beğendiklerim (4–5 yıldız)"),
    FAVORITED("Favorilerim")
}

private enum class ProfileRecipeSort(
    val label: String
) {
    DEFAULT("Varsayılan"),
    RECEIVED_LIKES("En beğendiklerim (yıldızıma göre)"),
    RECEIVED_FAVORITES("En çok favorilenen tariflerim"),
    COMMENTS_DESC("En çok yorumlanan"),
    COMMENTS_ASC("En az yorumlanan")
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

    var personalFilters by remember {
        mutableStateOf(
            emptySet<
                    PersonalRecipeFilter
                    >()
        )
    }

    var selectedSort by remember {
        mutableStateOf(
            ProfileRecipeSort.DEFAULT
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

            val engagement =
                engagementByRecipe[
                    recipe.id
                ]

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

            val matchesPersonalFilters =
                personalFilters
                    .isEmpty() ||
                        personalFilters
                            .all { filter ->

                                when (filter) {

                                    PersonalRecipeFilter.LIKED ->
                                        (engagement?.myRating ?: 0) >= 4

                                    PersonalRecipeFilter.FAVORITED ->
                                        engagement
                                            ?.isFavorite ==
                                                true
                                }
                            }

            matchesSearch &&
                    matchesCategory &&
                    matchesPersonalFilters
        }

    val visibleRecipes =
        when (selectedSort) {

            ProfileRecipeSort.DEFAULT ->
                filteredRecipes

            ProfileRecipeSort.RECEIVED_LIKES ->
                filteredRecipes
                    .filter { (engagementByRecipe[it.id]?.myRating ?: 0) >= 4 }
                    .sortedByDescending { engagementByRecipe[it.id]?.myRating ?: 0 }

            ProfileRecipeSort.RECEIVED_FAVORITES ->
                filteredRecipes
                    .filter { engagementByRecipe[it.id]?.isFavorite == true }
                    .sortedByDescending {
                        engagementByRecipe[it.id]?.favoriteCount ?: 0
                    }

            ProfileRecipeSort.COMMENTS_DESC ->
                filteredRecipes
                    .sortedByDescending {
                        engagementByRecipe[
                            it.id
                        ]?.commentCount
                            ?: 0
                    }

            ProfileRecipeSort.COMMENTS_ASC ->
                filteredRecipes
                    .sortedBy {
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
                Color(0xFFFFF8F2)
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
                        Color(0xFFFFD7B4)
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
                    fontSize = 23.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    color =
                        Color(
                            0xFF493028
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
                            0xFF947B6E
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
                            Color(0xFFFFFDF9),
                        unfocusedContainerColor =
                            Color(0xFFFFFDF9),
                        focusedBorderColor =
                            Color(
                                0xFFF16A24
                            ),
                        unfocusedBorderColor =
                            Color(
                                0xFFEADBD0
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
                        0xFF493028
                    )
            )

            Box {

                Card(
                    modifier =
                        Modifier.clickable {
                            categoryMenuOpen =
                                true
                        },
                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color(
                                    0xFFFFE3CA
                                )
                        ),
                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation =
                                0.dp
                        )
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    11.dp,
                                vertical =
                                    8.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                                "Kategoriler",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            color =
                                Color(
                                    0xFFE75D1C
                                )
                        )

                        Spacer(
                            modifier =
                                Modifier.size(
                                    2.dp
                                )
                        )

                        Icon(
                            imageVector =
                                Icons.Default.KeyboardArrowDown,
                            contentDescription =
                                null,
                            modifier =
                                Modifier.size(
                                    16.dp
                                ),
                            tint =
                                Color(
                                    0xFFD65B22
                                )
                        )
                    }
                }

                DropdownMenu(
                    expanded =
                        categoryMenuOpen,
                    onDismissRequest = {
                        categoryMenuOpen =
                            false
                    },
                    modifier =
                        Modifier.width(
                            210.dp
                        )
                ) {

                    Text(
                        text =
                            "Kategori seç",
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    16.dp,
                                vertical =
                                    8.dp
                            ),
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color.Gray
                    )

                    categories.forEach {
                            category ->

                        DropdownMenuItem(
                            text = {
                                Text(
                                    category,
                                    fontWeight =
                                        if (
                                            selectedCategory ==
                                            category
                                        ) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        }
                                )
                            },
                            leadingIcon = {

                                if (
                                    selectedCategory ==
                                    category
                                ) {

                                    Icon(
                                        imageVector =
                                            Icons.Default.Check,
                                        contentDescription =
                                            null,
                                        tint =
                                            Color(
                                                0xFFF16A24
                                            )
                                    )
                                }
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

            Spacer(
                modifier =
                    Modifier.size(
                        8.dp
                    )
            )

            Box {

                Card(
                    modifier =
                        Modifier.clickable {
                            filterMenuOpen =
                                true
                        },
                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (
                                    personalFilters
                                        .isNotEmpty() ||
                                    selectedSort !=
                                    ProfileRecipeSort.DEFAULT
                                ) {
                                    Color(
                                        0xFFFFD2A5
                                    )
                                } else {
                                    Color(
                                        0xFFFFE7D6
                                    )
                                }
                        ),
                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation =
                                0.dp
                        )
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    10.dp,
                                vertical =
                                    8.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.FilterList,
                            contentDescription =
                                "Filtre",
                            modifier =
                                Modifier.size(
                                    17.dp
                                ),
                            tint =
                                Color(
                                    0xFFD65B22
                                )
                        )

                        if (
                            personalFilters
                                .isNotEmpty()
                        ) {

                            Spacer(
                                modifier =
                                    Modifier.size(
                                        4.dp
                                    )
                            )

                            Text(
                                text =
                                    personalFilters
                                        .size
                                        .toString(),
                                fontSize = 11.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color =
                                    Color(
                                        0xFFD65B22
                                    )
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded =
                        filterMenuOpen,
                    onDismissRequest = {
                        filterMenuOpen =
                            false
                    },
                    modifier =
                        Modifier.width(
                            280.dp
                        )
                ) {

                    Text(
                        text =
                            "Benim seçimlerim",
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    16.dp,
                                vertical =
                                    8.dp
                            ),
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color(
                                0xFF766A62
                            )
                    )

                    PersonalRecipeFilter
                        .entries
                        .forEach {
                                filter ->

                            val selected =
                                filter in
                                        personalFilters

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        filter.label,
                                        fontWeight =
                                            if (
                                                selected
                                            ) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                    )
                                },
                                leadingIcon = {

                                    Box(
                                        modifier = Modifier
                                            .size(
                                                22.dp
                                            )
                                            .clip(
                                                CircleShape
                                            )
                                            .background(
                                                if (
                                                    selected
                                                ) {
                                                    Color(
                                                        0xFFF16A24
                                                    )
                                                } else {
                                                    Color(
                                                        0xFFFFE9D8
                                                    )
                                                }
                                            ),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        if (
                                            selected
                                        ) {

                                            Icon(
                                                imageVector =
                                                    Icons.Default.Check,
                                                contentDescription =
                                                    null,
                                                modifier =
                                                    Modifier.size(
                                                        15.dp
                                                    ),
                                                tint =
                                                    Color.White
                                            )
                                        }
                                    }
                                },
                                onClick = {

                                    personalFilters =
                                        if (
                                            selected
                                        ) {
                                            personalFilters -
                                                    filter
                                        } else {
                                            personalFilters +
                                                    filter
                                        }
                                }
                            )
                        }

                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                vertical =
                                    6.dp
                            ),
                        color =
                            Color(
                                0xFFF1DFD2
                            )
                    )

                    Text(
                        text =
                            "Sırala",
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    16.dp,
                                vertical =
                                    8.dp
                            ),
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color(
                                0xFF766A62
                            )
                    )

                    ProfileRecipeSort
                        .entries
                        .forEach {
                                sort ->

                            val selected =
                                selectedSort ==
                                        sort

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        sort.label,
                                        fontWeight =
                                            if (
                                                selected
                                            ) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                    )
                                },
                                trailingIcon = {

                                    if (
                                        selected
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Default.Check,
                                            contentDescription =
                                                null,
                                            tint =
                                                Color(
                                                    0xFFF16A24
                                                )
                                        )
                                    }
                                },
                                onClick = {
                                    selectedSort =
                                        sort
                                }
                            )
                        }

                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                top =
                                    6.dp
                            ),
                        color =
                            Color(
                                0xFFF1DFD2
                            )
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Filtreleri temizle",
                                fontWeight =
                                    FontWeight.SemiBold,
                                color =
                                    Color(
                                        0xFFD46632
                                    )
                            )
                        },
                        onClick = {
                            personalFilters =
                                emptySet()
                            selectedSort =
                                ProfileRecipeSort.DEFAULT
                            filterMenuOpen =
                                false
                        }
                    )
                }
            }
        }

        if (
            selectedCategory !=
            "Tümü" ||
            personalFilters
                .isNotEmpty() ||
            selectedSort !=
            ProfileRecipeSort.DEFAULT
        ) {

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        ),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {

                if (
                    selectedCategory !=
                    "Tümü"
                ) {

                    ActiveFilterPill(
                        text =
                            selectedCategory
                    )
                }

                personalFilters
                    .forEach {
                        filter ->

                        ActiveFilterPill(
                            text =
                                filter.label
                        )
                    }

                if (
                    selectedSort !=
                    ProfileRecipeSort.DEFAULT
                ) {

                    ActiveFilterPill(
                        text =
                            selectedSort.label
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        HorizontalDivider(
            color =
                Color(
                    0xFFE9D8CA
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
                            Color(0xFFFFFDF9)
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
                    GridCells.Fixed(2),
                modifier =
                    Modifier.fillMaxSize(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
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
private fun ActiveFilterPill(
    text: String
) {

    Card(
        shape =
            RoundedCornerShape(
                50.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(
                        0xFFFFDAB6
                    )
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    0.dp
            )
    ) {

        Text(
            text = text,
            modifier =
                Modifier.padding(
                    horizontal =
                        10.dp,
                    vertical =
                        5.dp
                ),
            fontSize = 11.sp,
            fontWeight =
                FontWeight.SemiBold,
            color =
                Color(
                    0xFFD65B22
                )
        )
    }
}

@Composable
private fun ProfileRecipeCard(
    recipe: Recipe,
    engagement: RecipeEngagement,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        label = "profileRecipePress"
    )
    Card(
        modifier = Modifier.fillMaxWidth().height(226.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().height(150.dp)) {
                Box(
                    Modifier.fillMaxSize().clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick
                    )
                ) {
                    if (!recipe.imageUri.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(recipe.imageUri),
                            contentDescription = recipe.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFE7CE), Color(0xFFFFAC6A))
                                )
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🍲", fontSize = 47.sp)
                        }
                    }
                    Box(
                        Modifier.fillMaxWidth().height(48.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0x550F0804))
                                )
                            )
                    )
                }
                Box(
                    Modifier.align(Alignment.TopEnd).padding(9.dp)
                        .size(34.dp).clip(CircleShape)
                        .background(Color(0xFFFFFAF5))
                        .clickable(onClick = onFavoriteToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (engagement.isFavorite) "🍲" else "🥣",
                        fontSize = 18.sp
                    )
                }
            }
            Column(
                Modifier.fillMaxWidth().clickable(onClick = onClick)
                    .padding(horizontal = 11.dp, vertical = 8.dp)
            ) {
                Text(
                    recipe.name,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF493028),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "★ " + String.format(java.util.Locale.US, "%.1f", engagement.averageRating),
                        color = Color(0xFFF07822),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "◷ " + recipe.preparationTime,
                        color = Color(0xFF947B6E),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

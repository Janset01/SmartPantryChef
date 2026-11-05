package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.RecipeMedia

@Composable
fun ProfileScreen(
    onRecipeClick: (Recipe) -> Unit
) {
    val context = LocalContext.current

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val recipeDao = database.recipeDao()
    val recipeMediaDao =
        database.recipeMediaDao()

    var recipes by remember {
        mutableStateOf<List<Recipe>>(emptyList())
    }

    val recipeHasVideo =
        remember {
            mutableStateMapOf<Int, Boolean>()
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

    var videoOnly by remember {
        mutableStateOf(false)
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
        val loadedRecipes =
            recipeDao.getAllRecipes()

        recipes = loadedRecipes

        loadedRecipes.forEach { recipe ->
            val media =
                recipeMediaDao
                    .getMediaForRecipe(
                        recipe.id
                    )

            recipeHasVideo[recipe.id] =
                media.any {
                    it.mediaType ==
                            RecipeMedia.TYPE_VIDEO
                }
        }
    }

    val visibleRecipes =
        recipes.filter { recipe ->

            val matchesSearch =
                searchText.isBlank() ||
                        recipe.name.contains(
                            searchText,
                            ignoreCase = true
                        )

            val matchesCategory =
                selectedCategory == "Tümü" ||
                        recipe.category ==
                        selectedCategory

            val matchesVideo =
                !videoOnly ||
                        recipeHasVideo[
                            recipe.id
                        ] == true

            matchesSearch &&
                    matchesCategory &&
                    matchesVideo
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
                Modifier.height(24.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFFDDE9DF)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = "👩‍🍳",
                    fontSize = 34.sp
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
                    text =
                        "Bugün Ne Pişirsem?",
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
                        Modifier.height(4.dp)
                )

                Text(
                    text =
                        recipes.size
                            .toString() +
                                " tarif",
                    fontSize = 13.sp,
                    color =
                        Color.Gray
                )
            }

            IconButton(
                onClick = {
                    // Ayarlar ekranı daha sonra bağlanacak.
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
                Modifier.height(20.dp)
        )

        Text(
            text = "Tarif Defterim",
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
                    14.dp
                ),
            colors =
                OutlinedTextFieldDefaults
                    .colors(
                        focusedContainerColor =
                            Color.White,
                        unfocusedContainerColor =
                            Color.White
                    )
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Card(
                modifier =
                    Modifier.clickable {
                        categoryMenuOpen =
                            true
                    },
                shape =
                    RoundedCornerShape(
                        12.dp
                    ),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    )
            ) {

                Row(
                    modifier =
                        Modifier.padding(
                            horizontal =
                                12.dp,
                            vertical =
                                10.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Menu,
                        contentDescription =
                            "Filtre"
                    )

                    Spacer(
                        modifier =
                            Modifier.size(6.dp)
                    )

                    Text(
                        text =
                            selectedCategory,
                        fontSize = 13.sp
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

            FilterChip(
                selected =
                    videoOnly,
                onClick = {
                    videoOnly =
                        !videoOnly
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Default.Videocam,
                        contentDescription =
                            "Videolu tarifler"
                    )
                },
                label = {
                    Text(
                        "Videolu"
                    )
                }
            )
        }

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        if (
            visibleRecipes
                .isEmpty()
        ) {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    )
            ) {

                Text(
                    text =
                        "Bu filtreye uygun tarif bulunamadı.",
                    modifier =
                        Modifier.padding(
                            18.dp
                        ),
                    color =
                        Color.Gray
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
                        6.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    )
            ) {

                items(
                    items =
                        visibleRecipes,
                    key = {
                        it.id
                    }
                ) { recipe ->

                    ProfileRecipeTile(
                        recipe =
                            recipe,
                        hasVideo =
                            recipeHasVideo[
                                recipe.id
                            ] == true,
                        onClick = {
                            onRecipeClick(
                                recipe
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileRecipeTile(
    recipe: Recipe,
    hasVideo: Boolean,
    onClick: () -> Unit
) {

    Column(
        modifier =
            Modifier.clickable {
                onClick()
            }
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(
                    RoundedCornerShape(
                        12.dp
                    )
                )
                .background(
                    Color(0xFFE8ECE9)
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

            if (hasVideo) {
                Box(
                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(6.dp)
                        .clip(
                            RoundedCornerShape(
                                8.dp
                            )
                        )
                        .background(
                            Color.Black.copy(
                                alpha = 0.55f
                            )
                        )
                        .padding(5.dp)
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Videocam,
                        contentDescription =
                            "Videolu tarif",
                        tint =
                            Color.White,
                        modifier =
                            Modifier.size(
                                16.dp
                            )
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(5.dp)
        )

        Text(
            text =
                recipe.name,
            maxLines = 2,
            fontSize = 12.sp,
            fontWeight =
                FontWeight.SemiBold,
            color =
                Color(0xFF2F3E34)
        )
    }
}

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.R
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe

@Composable
fun ExploreScreen(
    onRecipeClick: (Recipe) -> Unit
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("Tümü")
    }

    var savedRecipes by remember {
        mutableStateOf<List<Recipe>>(emptyList())
    }

    val context = LocalContext.current

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val recipeDao = database.recipeDao()

    LaunchedEffect(Unit) {
        savedRecipes = recipeDao.getAllRecipes()
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

    val filteredRecipes = savedRecipes.filter { recipe ->

        val matchesSearch =
            recipe.name.contains(searchText, ignoreCase = true)

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
            text = "Keşfet 🔍",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Yeni lezzetler keşfet",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Tarif ara...")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tarif ara"
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

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            categories.forEach { category ->

                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                    },
                    label = {
                        Text(category)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // -----------------------------
        // KAYITLI TARİFLER
        // -----------------------------

        Text(
            text = "🍽️ Senin Tariflerin",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredRecipes.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFDDE9DF)
                )
            ) {

                Text(
                    text = "Henüz bu alanda tarif bulunmuyor.",
                    modifier = Modifier.padding(18.dp),
                    fontSize = 14.sp,
                    color = Color(0xFF55645A)
                )
            }

        } else {

            filteredRecipes.forEach { recipe ->

                SavedRecipeCard(
                    recipe = recipe,
                    onClick = {
                        onRecipeClick(recipe)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // -----------------------------
        // TREND TARİFLER
        // -----------------------------

        Text(
            text = "🔥 Trend Tarifler",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(14.dp))

        RecipeCard(
            imageRes = R.drawable.pasta_chicken,
            title = "Kremalı Tavuklu Makarna",
            description = "Pratik • 30 dk",
            rating = "⭐ 4.9",
            likes = "❤️ 1.2K"
        )

        Spacer(modifier = Modifier.height(12.dp))

        RecipeCard(
            imageRes = R.drawable.pasta_chicken,
            title = "San Sebastian Cheesecake",
            description = "Tatlı • 45 dk",
            rating = "⭐ 4.8",
            likes = "❤️ 2.6K"
        )

        Spacer(modifier = Modifier.height(30.dp))

        // -----------------------------
        // DÜNYA MUTFAĞI
        // -----------------------------

        Text(
            text = "🌍 Dünya Mutfağı",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(14.dp))

        WorldCuisineCard(
            flag = "🇹🇷",
            country = "Türkiye",
            description = "Mantı, kebap, sarma ve daha fazlası"
        )

        Spacer(modifier = Modifier.height(10.dp))

        WorldCuisineCard(
            flag = "🇮🇹",
            country = "İtalya",
            description = "Pizza, makarna ve klasik İtalyan lezzetleri"
        )

        Spacer(modifier = Modifier.height(10.dp))

        WorldCuisineCard(
            flag = "🇯🇵",
            country = "Japonya",
            description = "Sushi, ramen ve geleneksel Japon mutfağı"
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun SavedRecipeCard(
    recipe: Recipe,
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
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            if (recipe.imageUri != null) {

                Image(
                    painter = rememberAsyncImagePainter(
                        recipe.imageUri
                    ),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .width(110.dp)
                        .height(95.dp),
                    contentScale = ContentScale.Crop
                )

            } else {

                Image(
                    painter = painterResource(
                        id = R.drawable.pasta_chicken
                    ),
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .width(110.dp)
                        .height(95.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(start = 14.dp)
            ) {

                Text(
                    text = recipe.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3E34)
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = recipe.category,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "⏱️ ${recipe.preparationTime}   🍽️ ${recipe.servings}",
                    fontSize = 13.sp,
                    color = Color(0xFF55645A)
                )
            }
        }
    }
}

@Composable
private fun RecipeCard(
    imageRes: Int,
    title: String,
    description: String,
    rating: String,
    likes: String
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .width(110.dp)
                    .height(95.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(start = 14.dp)
            ) {

                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3E34)
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$rating     $likes",
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun WorldCuisineCard(
    flag: String,
    country: String,
    description: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDDE9DF)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Text(
                text = flag,
                fontSize = 35.sp
            )

            Column(
                modifier = Modifier.padding(start = 15.dp)
            ) {

                Text(
                    text = country,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3E34)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF55645A)
                )
            }
        }
    }
}
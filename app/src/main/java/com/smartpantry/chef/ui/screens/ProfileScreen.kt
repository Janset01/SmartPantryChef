package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(onRecipeClick: (Recipe) -> Unit, onOpenRecipeBook: () -> Unit) {
    val context = LocalContext.current
    val dao = remember(context) { AppDatabase.getDatabase(context).recipeDao() }
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    LaunchedEffect(Unit) {
        recipes = withContext(Dispatchers.IO) { dao.getAllRecipes() }
    }
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFFF8F2))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(78.dp).clip(CircleShape).background(Color(0xFFF2D1BA)),
                contentAlignment = Alignment.Center) { Text("👩‍🍳", fontSize = 32.sp) }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text("@nickname", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38231B))
                Text("Bugün ne pişirsem?", fontSize = 14.sp, color = Color(0xFF80695C))
                Text("${recipes.size} tarif", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = { /* Ayarlar henüz bağlı değil */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenRecipeBook)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TARİF DEFTERİM", modifier = Modifier.weight(1f),
                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38231B))
            Text("Tüm Tariflerimi Gör", color = Color(0xFFB44713), fontSize = 12.sp)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB44713))
        }
        HorizontalDivider(color = Color(0xFFE9D8CA))
        Spacer(Modifier.height(14.dp))
        if (recipes.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9))) {
                Text("Henüz tarif eklemedin. Tarif Ekle bölümünden başlayabilirsin.",
                    modifier = Modifier.padding(18.dp), color = Color(0xFF80695C))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes.take(4), key = { it.id }) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onRecipeClick(recipe) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9))
                    ) {
                        Column {
                            Box(Modifier.fillMaxWidth().height(130.dp)) {
                                if (!recipe.imageUri.isNullOrBlank()) {
                                    Image(painter = rememberAsyncImagePainter(recipe.imageUri),
                                        contentDescription = recipe.name, modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop)
                                } else {
                                    Box(Modifier.fillMaxSize().background(Color(0xFFF2D1BA)),
                                        contentAlignment = Alignment.Center) {
                                        Text("🍽️", fontSize = 38.sp)
                                    }
                                }
                            }
                            Text(recipe.name, modifier = Modifier.padding(12.dp),
                                maxLines = 2, fontWeight = FontWeight.Bold,
                                color = Color(0xFF38231B))
                        }
                    }
                }
            }
        }
    }
}

package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.smartpantry.chef.data.CuratedMenus
import com.smartpantry.chef.data.MenuCollection
import com.smartpantry.chef.data.ReadyMenu
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.DemoRecipeVisibility
import java.util.Locale

private val menuInk = Color(0xFF38231B)
private val menuOrange = Color(0xFFE9641D)
private val menuCream = Color(0xFFFFF8F2)

@Composable
fun ReadyMenusScreen(
    initialCollectionId: String,
    onBack: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getDatabase(context) }
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var selectedMenu by remember { mutableStateOf<ReadyMenu?>(null) }
    var search by remember { mutableStateOf("") }
    val collection = remember(initialCollectionId) {
        CuratedMenus.collections.firstOrNull { it.id == initialCollectionId }
            ?: CuratedMenus.collections.first()
    }

    LaunchedEffect(Unit) {
        recipes = DemoRecipeVisibility.visibleOnDiscover(context, db.recipeDao().getAllRecipes())
    }
    val byName = recipes.associateBy { it.name.trim().lowercase(Locale.forLanguageTag("tr-TR")) }
    val matched = { name: String -> byName[name.trim().lowercase(Locale.forLanguageTag("tr-TR"))] }
    val menus = collection.menus.filter { ready ->
        search.isBlank() || ready.title.contains(search, ignoreCase = true) ||
            ready.dishes.any { it.recipeName.contains(search, ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize().background(menuCream).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                if (selectedMenu == null) onBack() else selectedMenu = null
            }) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = menuOrange) }
            Text(selectedMenu?.title ?: collection.title, fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp, color = menuInk, modifier = Modifier.weight(1f))
        }

        if (selectedMenu == null) {
            Text(collection.shortDescription, color = Color(0xFF8B695A), fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Menü veya yemek ara...") },
                singleLine = true, shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)) {
                items(menus, key = { it.title }) { ready ->
                    val available = ready.dishes.count { matched(it.recipeName) != null }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedMenu = ready },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(collection.emoji, fontSize = 30.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(ready.title, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                    color = menuInk)
                                Text(ready.detail, fontSize = 12.sp, color = Color(0xFF80695C))
                                Text("$available tarif mevcut", fontSize = 11.sp, color = menuOrange)
                            }
                            Text("›", fontSize = 25.sp, color = menuOrange)
                        }
                    }
                }
                if (menus.isEmpty()) item {
                    Text("Aramana uygun menü bulunamadı.", color = Color.Gray,
                        modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            val active = selectedMenu!!
            Text("Menündeki yemeklere dokunarak tarifine ulaşabilirsin.",
                color = Color(0xFF80695C), fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(bottom = 25.dp)) {
                items(active.dishes, key = { it.role + it.recipeName }) { dish ->
                    val recipe = matched(dish.recipeName)
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = recipe != null) {
                            if (recipe != null) onRecipeClick(recipe)
                        }, shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier.size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFE8D7)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!recipe?.imageUri.isNullOrBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(recipe!!.imageUri),
                                        contentDescription = dish.recipeName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else Text("🍽️", fontSize = 30.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(dish.role, fontSize = 11.sp, color = menuOrange,
                                    fontWeight = FontWeight.Medium)
                                Text(dish.recipeName, fontWeight = FontWeight.Bold, color = menuInk)
                                Text(
                                    if (recipe == null) "Tarif henüz eklenmedi"
                                    else recipe.preparationTime,
                                    fontSize = 11.sp, color = Color(0xFF8A766E)
                                )
                            }
                            if (recipe != null) Text("›", color = menuOrange, fontSize = 25.sp)
                        }
                    }
                }
            }
        }
    }
}

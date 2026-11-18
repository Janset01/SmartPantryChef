package com.smartpantry.chef.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.RecipeIngredient
import com.smartpantry.chef.data.ShoppingItem
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private val plannerOrange = Color(0xFFE95B19)
private val plannerCream = Color(0xFFFFF8F2)
private val plannerInk = Color(0xFF38231B)
private val mealNames = listOf("Kahvaltı", "Öğle", "Akşam")
private val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

private data class PlannedMeal(
    val id: Long, val date: String, val slot: Int, val name: String,
    val recipeId: Int? = null, val done: Boolean = false
)

private fun readPlan(context: Context): List<PlannedMeal> {
    val raw = context.getSharedPreferences("weekly_meal_plans_v1", Context.MODE_PRIVATE)
        .getString("entries", "[]") ?: "[]"
    return runCatching {
        val array = JSONArray(raw)
        (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            PlannedMeal(o.getLong("id"), o.getString("date"), o.getInt("slot"),
                o.getString("name"), if (o.isNull("recipeId")) null else o.getInt("recipeId"),
                o.optBoolean("done"))
        }
    }.getOrDefault(emptyList())
}

private fun savePlan(context: Context, entries: List<PlannedMeal>) {
    val array = JSONArray()
    entries.forEach { meal ->
        array.put(JSONObject().put("id", meal.id).put("date", meal.date)
            .put("slot", meal.slot).put("name", meal.name)
            .put("recipeId", meal.recipeId ?: JSONObject.NULL).put("done", meal.done))
    }
    context.getSharedPreferences("weekly_meal_plans_v1", Context.MODE_PRIVATE)
        .edit().putString("entries", array.toString()).apply()
}

@Composable
fun WeeklyMealPlannerScreen(onBack: () -> Unit, onOpenShoppingList: () -> Unit) {
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    var weekStart by remember { mutableStateOf(today.minusDays((today.dayOfWeek.value - 1).toLong())) }
    var selectedDate by remember { mutableStateOf(today) }
    var entries by remember { mutableStateOf(readPlan(context)) }
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var recipeIngredients by remember { mutableStateOf<List<RecipeIngredient>>(emptyList()) }
    var pantry by remember { mutableStateOf<List<com.smartpantry.chef.data.Ingredient>>(emptyList()) }
    var addSlot by remember { mutableStateOf<Int?>(null) }
    var editing by remember { mutableStateOf<PlannedMeal?>(null) }
    var manualName by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var portion by remember(selectedDate) { mutableStateOf(context.getSharedPreferences("weekly_meal_portions_v1", Context.MODE_PRIVATE).getString(selectedDate.toString(), "2") ?: "2") }
    var note by remember(selectedDate) { mutableStateOf(context.getSharedPreferences("weekly_meal_notes_v1", Context.MODE_PRIVATE).getString(selectedDate.toString(), "") ?: "") }
    var showShopping by remember { mutableStateOf(false) }
    var shoppingScope by remember { mutableStateOf(0) }
    var shoppingSelection by remember { mutableStateOf<Set<String>>(emptySet()) }
    var feedback by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        recipes = database.recipeDao().getAllRecipes()
        recipeIngredients = database.recipeIngredientDao().getAllRecipeIngredients()
        pantry = database.ingredientDao().getAllIngredients()
    }
    fun persist(next: List<PlannedMeal>) { entries = next; savePlan(context, next) }
    val dateString = selectedDate.toString()
    val daily = entries.filter { it.date == dateString }
    val weekEnd = weekStart.plusDays(6)
    val formatter = remember { DateTimeFormatter.ofPattern("d MMM", Locale("tr", "TR")) }
    val scopeDates = if (shoppingScope == 0) setOf(today.toString())
        else if (shoppingScope == 1) setOf(dateString)
        else (0..6).map { weekStart.plusDays(it.toLong()).toString() }.toSet()
    val required = entries.filter { it.date in scopeDates && !it.done && it.recipeId != null }
        .flatMap { meal -> recipeIngredients.filter { it.recipeId == meal.recipeId } }
        .groupBy { it.name.trim().lowercase(Locale("tr", "TR")) to it.unit.trim().lowercase(Locale("tr", "TR")) }
        .mapNotNull { (key, values) ->
            val total = values.sumOf { it.quantity }
            val available = pantry.filter { it.name.trim().lowercase(Locale("tr", "TR")) == key.first &&
                it.unit.trim().lowercase(Locale("tr", "TR")) == key.second }.sumOf { it.remainingQuantity }
            val missing = max(0.0, total - available)
            if (missing > 0.0001) Triple(values.first().name, missing, values.first().unit) else null
        }

    Column(Modifier.fillMaxSize().background(plannerCream).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Geri", color = plannerOrange) }
            Text("Haftalık Yemek Planım", fontWeight = FontWeight.Bold, color = plannerInk, fontSize = 20.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { weekStart = weekStart.minusWeeks(1); selectedDate = weekStart }) { Text("‹ Önceki", color = plannerOrange) }
            Text("${weekStart.format(formatter)} – ${weekEnd.format(formatter)}", fontWeight = FontWeight.Bold)
            TextButton(onClick = { weekStart = weekStart.plusWeeks(1); selectedDate = weekStart }) { Text("Sonraki ›", color = plannerOrange) }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0..6).forEach { offset ->
                val date = weekStart.plusDays(offset.toLong())
                val active = date == selectedDate
                val count = entries.count { it.date == date.toString() }
                Card(Modifier.width(62.dp).clickable { selectedDate = date },
                    colors = CardDefaults.cardColors(containerColor = if (active) plannerOrange else Color(0xFFFFE5D5)),
                    shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayNames[offset], color = if (active) Color.White else plannerInk, fontSize = 12.sp)
                        Text(date.dayOfMonth.toString(), color = if (active) Color.White else plannerInk, fontWeight = FontWeight.Bold)
                        Text("$count yemek", color = if (active) Color.White else plannerInk, fontSize = 9.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${dayNames[selectedDate.dayOfWeek.value - 1]} • ${selectedDate.format(formatter)}",
                color = plannerInk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = {
                val next = selectedDate.plusDays(1).toString()
                val source = entries.filter { it.date == dateString }
                persist(entries + source.mapIndexed { i, m -> m.copy(id = System.currentTimeMillis() + i, date = next, done = false) })
                feedback = "Günün yemekleri ertesi güne kopyalandı."
            }) { Text("Günü kopyala", color = plannerOrange) }
            TextButton(onClick = {
                val source = entries.filter { LocalDate.parse(it.date) in weekStart..weekEnd }
                persist(entries + source.mapIndexed { i, m -> m.copy(id = System.currentTimeMillis() + i, date = LocalDate.parse(m.date).plusWeeks(1).toString(), done = false) })
                feedback = "Haftalık plan sonraki haftaya kopyalandı."
            }) { Text("Haftayı kopyala", color = plannerOrange) }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            mealNames.forEachIndexed { slot, slotName ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                        shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(slotName, fontWeight = FontWeight.Bold, color = plannerInk)
                                TextButton(onClick = { addSlot = slot; editing = null; manualName = ""; selectedRecipe = null }) {
                                    Text("+ Yemek ekle", color = plannerOrange)
                                }
                            }
                            daily.filter { it.slot == slot }.forEach { meal ->
                                val recipe = recipes.firstOrNull { it.id == meal.recipeId }
                                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (recipe?.imageUri?.isNotBlank() == true) {
                                        androidx.compose.foundation.Image(
                                            painter = rememberAsyncImagePainter(recipe.imageUri),
                                            contentDescription = null, contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(48.dp))
                                    } else Text("🍽️", fontSize = 26.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(meal.name, Modifier.weight(1f), color = plannerInk)
                                    Checkbox(checked = meal.done, onCheckedChange = { checked ->
                                        persist(entries.map { if (it.id == meal.id) it.copy(done = checked) else it })
                                    })
                                    TextButton(onClick = { editing = meal; addSlot = meal.slot; manualName = meal.name; selectedRecipe = recipes.firstOrNull { it.id == meal.recipeId } }) { Text("Düzenle", fontSize = 11.sp) }
                                    TextButton(onClick = { persist(entries.filterNot { it.id == meal.id }) }) { Text("Sil", fontSize = 11.sp) }
                                }
                            }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(value = note, onValueChange = {
                    note = it
                    context.getSharedPreferences("weekly_meal_notes_v1", Context.MODE_PRIVATE).edit().putString(dateString, it).apply()
                }, modifier = Modifier.fillMaxWidth(), label = { Text("Günlük not") },
                    placeholder = { Text("Misafir gelecek, dışarıda yemek...") })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = portion, onValueChange = { portion = it.filter(Char::isDigit).take(3); context.getSharedPreferences("weekly_meal_portions_v1", Context.MODE_PRIVATE).edit().putString(dateString, portion).apply() },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Kişi sayısı (plan notu)") }, singleLine = true)
                Spacer(Modifier.height(10.dp))
                Button(onClick = { showShopping = true; shoppingSelection = emptySet() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = plannerOrange)) {
                    Text("🛒 Eksikleri alışveriş listesine hazırla")
                }
                if (feedback.isNotBlank()) Text(feedback, color = plannerInk, fontSize = 12.sp)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    if (addSlot != null) {
        AlertDialog(onDismissRequest = { addSlot = null }, title = { Text(if (editing == null) "Yemek ekle" else "Yemeği düzenle") },
            text = {
                Column {
                    OutlinedTextField(value = manualName, onValueChange = { manualName = it; selectedRecipe = null },
                        label = { Text("Yemek adı veya tarif ara") })
                    Text("Tarif defterimden seç:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                    Column(Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                        recipes.filter { it.name.contains(manualName, ignoreCase = true) || manualName.isBlank() }.take(20).forEach { recipe ->
                            TextButton(onClick = { selectedRecipe = recipe; manualName = recipe.name }) { Text(recipe.name, color = plannerOrange) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = {
                val name = manualName.trim()
                if (name.isNotBlank()) {
                    val item = PlannedMeal(editing?.id ?: System.currentTimeMillis(), dateString,
                        addSlot!!, name, selectedRecipe?.id, editing?.done ?: false)
                    persist(entries.filterNot { it.id == editing?.id } + item)
                    addSlot = null; editing = null
                }
            }) { Text("Kaydet") } },
            dismissButton = { TextButton(onClick = { addSlot = null }) { Text("Vazgeç") } })
    }
    if (showShopping) {
        AlertDialog(onDismissRequest = { showShopping = false },
            title = { Text("Eksik malzemeler") },
            text = {
                Column {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("Bugün", "Seçili gün", "Tüm hafta").forEachIndexed { index, label ->
                            FilterChip(selected = shoppingScope == index, onClick = { shoppingScope = index; shoppingSelection = emptySet() },
                                label = { Text(label) })
                            Spacer(Modifier.width(6.dp))
                        }
                    }
                    Text("Yalnızca kayıtlı tarif malzemeleri ve aynı birimdeki kiler miktarları karşılaştırılır. Elle yazılan yemekler dahil değildir.",
                        fontSize = 12.sp)
                    Column(Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
                        if (required.isEmpty()) Text("Hesaplanabilen eksik malzeme yok.")
                        required.forEach { (name, qty, unit) ->
                            val key = "$name|$unit"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = key in shoppingSelection, onCheckedChange = {
                                    shoppingSelection = if (it) shoppingSelection + key else shoppingSelection - key
                                })
                                Text("$name: ${"%.2f".format(qty)} $unit")
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = {
                val chosen = required.filter { "${it.first}|${it.third}" in shoppingSelection }
                scope.launch {
                    val existing = database.shoppingItemDao().getAllShoppingItems()
                    chosen.forEach { (name, qty, unit) ->
                        val old = existing.firstOrNull { it.name.equals(name, true) && it.unit.equals(unit, true) && !it.isPurchased }
                        if (old != null) database.shoppingItemDao().updateShoppingItem(old.copy(quantity = old.quantity + qty))
                        else database.shoppingItemDao().insertShoppingItem(ShoppingItem(name = name, quantity = qty, unit = unit))
                    }
                    feedback = "${chosen.size} malzeme mevcut alışveriş listesine eklendi (${if (shoppingScope == 2) "haftalık" else "günlük"} plan)."
                    showShopping = false
                }
            }) { Text("Seçilenleri ekle") } },
            dismissButton = { TextButton(onClick = { showShopping = false; onOpenShoppingList() }) { Text("Alışveriş listem") } })
    }
}

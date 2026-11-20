package com.smartpantry.chef.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private val kitchenOrange = Color(0xFFE95B19)
private val kitchenInk = Color(0xFF38231B)
private val kitchenCream = Color(0xFFFFF8F2)
private val kitchenPeach = Color(0xFFFFE8D9)

private data class KitchenRecipe(
    val id: String, val title: String, val emoji: String,
    val difficulty: String, val minutes: Int, val servings: Int,
    val category: String, val ingredients: String, val steps: String, val tip: String
)

private data class KitchenTip(val id: String, val title: String, val emoji: String, val explanation: String, val steps: String)

private data class KitchenMenu(val id: String, val name: String, val emoji: String, val description: String, val dishes: List<Pair<String, String>>, val minutes: Int)

private val chefRecipes = listOf(
    KitchenRecipe("r1", "Mantarlı Dana Bonfile", "🥩", "Orta", 40, 2, "Et",
        "300 g dana bonfile\n200 g mantar\n1 yemek kaşığı tereyağı\n1 yemek kaşığı zeytinyağı\n1 diş sarımsak\nTuz ve karabiber",
        "1. Bonfileyi pişirmeden önce yüzeyini kurula, hafifçe tuzla.\n2. Tavayı orta-yüksek ateşte ısıt. Yağ ekleyip bonfilenin iki tarafını mühürle.\n3. Etin kalınlığına ve hedef iç sıcaklığına göre pişirmeye devam et; güvenli pişirme için termometre kullan.\n4. Eti ayrı tabağa alıp dinlendir.\n5. Tavada mantar ve sarımsağı sotele, tereyağıyla sosu toparla.\n6. Eti mantar sosuyla servis et.",
        "Etin suyunu koruması için dilimlemeden önce birkaç dakika dinlendir."),
    KitchenRecipe("r2", "Limonlu Fırın Somon", "🐟", "Kolay", 30, 2, "Balık",
        "2 dilim somon fileto\n1 limon\n1 yemek kaşığı zeytinyağı\n1 diş sarımsak\nDereotu\nKarabiber",
        "1. Fırını 190°C'ye ısıt.\n2. Somonu yağlı kâğıt serili tepsiye yerleştir.\n3. Limon suyu, zeytinyağı, sarımsak ve karabiberi üzerine sür.\n4. Somon tamamen pişene kadar, kalınlığına göre yaklaşık 15–20 dakika fırınla.\n5. Dereotuyla servis et.",
        "Fazla pişirmemek için somonun kalınlığını dikkate al."),
    KitchenRecipe("r3", "Fırınlanmış Sebze Graten", "🥦", "Kolay", 45, 4, "Sebze",
        "1 küçük brokoli\n2 havuç\n2 patates\n200 ml süt\n1 yemek kaşığı un\n1 yemek kaşığı tereyağı\n60 g kaşar peyniri",
        "1. Sebzeleri eşit büyüklükte doğra ve hafifçe ön haşla.\n2. Tereyağı ve unu kısa süre kavur; sütü yavaşça ekleyip çırparak koyulaştır.\n3. Sebzeleri fırın kabına yerleştir, sosu dök.\n4. Kaşarı ekleyerek 190°C'de üzeri kızarana kadar pişir.",
        "Sebzeleri çok yumuşatmadan fırına vermek dokularını korur."),
    KitchenRecipe("r4", "Kremalı Mantarlı Makarna", "🍝", "Kolay", 25, 3, "Makarna",
        "250 g makarna\n200 g mantar\n150 ml krema\n1 diş sarımsak\n1 yemek kaşığı zeytinyağı\nKarabiber",
        "1. Makarnayı paket süresine göre haşla; biraz haşlama suyu ayır.\n2. Mantarları sıcak tavada sotele.\n3. Sarımsak ve kremayı ekle.\n4. Makarnayı sosla karıştır; gerekirse haşlama suyuyla kıvamı aç.\n5. Karabiberle servis et.",
        "Sosun makarnaya tutunması için biraz nişastalı haşlama suyu ekle.")
)
private val chefTips = listOf(
    KitchenTip("t1", "Eti pişirdikten sonra dinlendir", "🥩",
        "Pişen eti hemen dilimlemek yerine kısa süre beklemek, keserken daha az su kaybetmesini sağlayabilir.",
        "1. Eti tavadan al.\n2. Temiz bir tabağa yerleştir.\n3. Etin büyüklüğüne göre birkaç dakika dinlendir.\n4. Sonra liflere ters yönde dilimle."),
    KitchenTip("t2", "Makarna suyunu ayır", "🍝",
        "Nişastalı haşlama suyu sosu bağlamaya yardımcı olur.",
        "1. Makarnayı süzmeden önce bir fincan su ayır.\n2. Sosu makarnayla karıştır.\n3. Suyu azar azar ilave ederek kıvamı ayarla."),
    KitchenTip("t3", "Sebzeleri eşit doğra", "🥕",
        "Benzer boyutlar, sebzelerin daha dengeli pişmesini sağlar.",
        "1. Benzer sertlikteki sebzeleri seç.\n2. Parçaları yaklaşık eşit büyüklükte kes.\n3. Fırın tepsisine tek kat yerleştir."),
    KitchenTip("t4", "Tavayı önceden ısıt", "🍳",
        "Sote yaparken önceden ısınmış tava, yiyeceklerin fazla su salmadan renk almasına yardımcı olur.",
        "1. Tavayı orta-yüksek ateşte ısıt.\n2. Uygun yağı ekle.\n3. Malzemeleri tavayı doldurmadan partiler hâlinde pişir.")
)
private val chefMenus = listOf(
    KitchenMenu("m1", "Misafir Menüsü", "🍽️", "Özel sofralar için dengeli dört tabak.",
        listOf("Başlangıç" to "Kremalı mantar çorbası", "Ana yemek" to "Mantarlı dana bonfile",
            "Salata" to "Roka ve nar salatası", "Tatlı" to "Fırın sütlaç"), 95),
    KitchenMenu("m2", "Hafta Sonu Sofrası", "🥘", "Rahat bir sofrada paylaşılacak lezzetler.",
        listOf("Başlangıç" to "Domates çorbası", "Ana yemek" to "Limonlu fırın somon",
            "Yan yemek" to "Fırınlanmış sebze graten"), 75),
    KitchenMenu("m3", "Pratik Akşam Menüsü", "🍝", "Kısa sürede hazırlanan üç tabak.",
        listOf("Başlangıç" to "Mevsim salatası", "Ana yemek" to "Kremalı mantarlı makarna",
            "Tatlı" to "Meyveli yoğurt kasesi"), 40)
)

private fun addMenuToPlan(context: Context, menu: KitchenMenu, date: LocalDate) {
    val prefs = context.getSharedPreferences("weekly_meal_plans_v1", Context.MODE_PRIVATE)
    val array = runCatching { JSONArray(prefs.getString("entries", "[]") ?: "[]") }.getOrElse { JSONArray() }
    val existing = (0 until array.length()).mapNotNull { i ->
        runCatching { array.getJSONObject(i).optString("date") to array.getJSONObject(i).optString("name") }.getOrNull()
    }.toSet()
    val base = System.currentTimeMillis()
    menu.dishes.forEachIndexed { i, (_, name) ->
        if ((date.toString() to name) !in existing) {
            array.put(JSONObject().put("id", base + i).put("date", date.toString())
                .put("slot", 2).put("name", name).put("recipeId", JSONObject.NULL).put("done", false))
        }
    }
    prefs.edit().putString("entries", array.toString()).apply()
}

@Composable
fun ProfessionalKitchenScreen(initialSection: Int, onBack: () -> Unit, onOpenPlanner: () -> Unit) {
    var section by remember { mutableIntStateOf(initialSection.coerceIn(0, 2)) }
    var recipeDetail by remember { mutableStateOf<KitchenRecipe?>(null) }
    var tipDetail by remember { mutableStateOf<KitchenTip?>(null) }
    var menuDetail by remember { mutableStateOf<KitchenMenu?>(null) }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tümü") }
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("professional_kitchen_saved_v1", Context.MODE_PRIVATE) }
    var saved by remember { mutableStateOf(prefs.getStringSet("saved", emptySet())?.toSet() ?: emptySet()) }
    var message by remember { mutableStateOf("") }
    fun toggle(id: String) {
        val next = if (id in saved) saved - id else saved + id
        saved = next
        prefs.edit().putStringSet("saved", next).apply()
    }
    Column(Modifier.fillMaxSize().background(kitchenCream)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                when {
                    recipeDetail != null -> recipeDetail = null
                    tipDetail != null -> tipDetail = null
                    menuDetail != null -> menuDetail = null
                    else -> onBack()
                }
            }) { Icon(Icons.Default.ArrowBack, "Geri", tint = kitchenInk) }
            Text(when { recipeDetail != null -> "Tarif Detayı"; tipDetail != null -> "Mutfak İpucu"; menuDetail != null -> "Menü Detayı"; else -> "Profesyonel Mutfaktan" },
                fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, color = kitchenInk)
        }
        if (recipeDetail == null && tipDetail == null && menuDetail == null) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Usta İşi Tarifler", "Şef Dokunuşları", "Gurme Seçkisi").forEachIndexed { index, title ->
                    FilterChip(selected = section == index, onClick = { section = index; search = ""; category = "Tümü" },
                        label = { Text(title) }, colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = kitchenOrange, selectedLabelColor = Color.White))
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 28.dp)) {
            if (recipeDetail != null) {
                val recipe = recipeDetail!!
                item {
                    KitchenHero(recipe.emoji, recipe.title)
                    Spacer(Modifier.height(12.dp))
                    Text("${recipe.difficulty}  •  ${recipe.minutes} dk  •  ${recipe.servings} kişilik",
                        color = kitchenOrange, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(16.dp))
                    Text("Malzemeler", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = kitchenInk)
                    Text(recipe.ingredients, modifier = Modifier.padding(vertical = 10.dp), lineHeight = 25.sp)
                    Text("Hazırlanışı", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = kitchenInk)
                    Text(recipe.steps, modifier = Modifier.padding(vertical = 10.dp), lineHeight = 25.sp)
                    KitchenInfo("Şef dokunuşu", recipe.tip)
                    TextButton(onClick = { toggle(recipe.id) }) {
                        Icon(if (recipe.id in saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, null)
                        Text(if (recipe.id in saved) "Kaydedildi" else "Tarifi kaydet")
                    }
                }
            } else if (tipDetail != null) {
                val tip = tipDetail!!
                item {
                    KitchenHero(tip.emoji, tip.title)
                    Spacer(Modifier.height(14.dp))
                    Text(tip.explanation, fontSize = 16.sp, lineHeight = 24.sp, color = kitchenInk)
                    Spacer(Modifier.height(14.dp))
                    Text("Nasıl uygulanır?", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(tip.steps, modifier = Modifier.padding(vertical = 12.dp), lineHeight = 25.sp)
                    Button(onClick = { toggle(tip.id) }, colors = ButtonDefaults.buttonColors(containerColor = kitchenOrange)) {
                        Text(if (tip.id in saved) "Kaydı kaldır" else "Bu ipucunu kaydet")
                    }
                }
            } else if (menuDetail != null) {
                val menu = menuDetail!!
                item {
                    KitchenHero(menu.emoji, menu.name)
                    Spacer(Modifier.height(10.dp))
                    Text(menu.description, color = kitchenInk)
                    Text("${menu.dishes.size} tabak  •  Yaklaşık ${menu.minutes} dk",
                        color = kitchenOrange, modifier = Modifier.padding(vertical = 10.dp))
                }
                items(menu.dishes) { (label, food) ->
                    KitchenInfo(label, food)
                }
                item {
                    Button(onClick = {
                        addMenuToPlan(context, menu, LocalDate.now())
                        message = "Menü bugünün akşam planına eklendi. Malzemeler için tariflerini ayrıca eşleştir."
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = kitchenOrange)) {
                        Text("Menüyü bugünkü planıma ekle")
                    }
                    if (message.isNotBlank()) {
                        Text(message, color = kitchenInk, modifier = Modifier.padding(vertical = 10.dp))
                        TextButton(onClick = onOpenPlanner) { Text("Yemek planımı aç", color = kitchenOrange) }
                    }
                }
            } else when (section) {
                0 -> {
                    item {
                        Text("Usta İşi Tarifler", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = kitchenInk)
                        Text("Özgün tarifler • Adım adım hazırlanış", color = Color(0xFF80695C))
                        OutlinedTextField(value = search, onValueChange = { search = it },
                            placeholder = { Text("Tariflerde ara...") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Tümü", "Et", "Balık", "Sebze", "Makarna").forEach { c ->
                                FilterChip(selected = category == c, onClick = { category = c },
                                    label = { Text(c) })
                            }
                        }
                    }
                    items(chefRecipes.filter { (category == "Tümü" || it.category == category) &&
                        it.title.contains(search, ignoreCase = true) }) { recipe ->
                        KitchenListCard(recipe.emoji, recipe.title, "${recipe.difficulty} • ${recipe.minutes} dk • ${recipe.servings} kişilik",
                            recipe.id in saved, { toggle(recipe.id) }, { recipeDetail = recipe })
                    }
                }
                1 -> {
                    item {
                        Text("Şef Dokunuşları", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = kitchenInk)
                        Text("Kısa, uygulanabilir mutfak teknikleri", color = Color(0xFF80695C))
                    }
                    items(chefTips) { tip ->
                        KitchenListCard(tip.emoji, tip.title, tip.explanation,
                            tip.id in saved, { toggle(tip.id) }, { tipDetail = tip })
                    }
                }
                else -> {
                    item {
                        Text("Gurme Seçkisi", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = kitchenInk)
                        Text("Birlikte iyi giden yemeklerden özenli menüler", color = Color(0xFF80695C))
                    }
                    items(chefMenus) { menu ->
                        KitchenListCard(menu.emoji, menu.name,
                            "${menu.dishes.size} tabak • Yaklaşık ${menu.minutes} dk",
                            menu.id in saved, { toggle(menu.id) }, { menuDetail = menu; message = "" })
                    }
                }
            }
        }
    }
}

@Composable
private fun KitchenHero(emoji: String, title: String) {
    Box(Modifier.fillMaxWidth().height(170.dp).background(
        Brush.verticalGradient(listOf(Color(0xFFFFD2B2), Color(0xFFC57448))),
        RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 64.sp)
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun KitchenInfo(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8D9)),
        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = kitchenOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(body, color = kitchenInk, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp), lineHeight = 21.sp)
        }
    }
}

@Composable
private fun KitchenListCard(emoji: String, title: String, subtitle: String,
                            saved: Boolean, onSave: () -> Unit, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(92.dp).background(
                Brush.verticalGradient(listOf(Color(0xFFFFE0C8), Color(0xFFDE926B))),
                RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 40.sp)
            }
            Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = kitchenInk)
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF80695C),
                    maxLines = 3, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 5.dp))
            }
            IconButton(onClick = onSave) {
                Icon(if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (saved) "Kaydı kaldır" else "Kaydet",
                    tint = kitchenOrange)
            }
        }
    }
}

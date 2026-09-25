package com.smartpantry.chef.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Ingredient
import com.smartpantry.chef.data.ShoppingItem
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun ShoppingListScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val shoppingItemDao = database.shoppingItemDao()
    val ingredientDao = database.ingredientDao()

    var shoppingItems by remember {
        mutableStateOf<List<ShoppingItem>>(emptyList())
    }

    var refreshList by remember {
        mutableIntStateOf(0)
    }

    var showClearDialog by remember {
        mutableStateOf(false)
    }

    var itemToAddToPantry by remember {
        mutableStateOf<ShoppingItem?>(null)
    }

    LaunchedEffect(refreshList) {
        shoppingItems = shoppingItemDao.getAllShoppingItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F2))
            .padding(20.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Buzdolabına Dön")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "🛒 Alışveriş Listem",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Eksik malzemelerini burada takip edebilirsin.",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (shoppingItems.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "🛒 Listen şu anda boş.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Bir tarifte eksik malzeme olduğunda " +
                                "\"Alışveriş Listeme Ekle\" butonunu kullanabilirsin.",
                        color = Color.Gray
                    )
                }
            }

        } else {

            val remainingCount =
                shoppingItems.count { !it.isPurchased }

            Text(
                text = "Alınacak $remainingCount ürün var",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4F7F65)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(
                    items = shoppingItems,
                    key = { it.id }
                ) { item ->

                    ShoppingItemCard(
                        item = item,

                        onPurchasedChange = { checked ->
                            scope.launch {
                                shoppingItemDao.updateShoppingItem(
                                    item.copy(
                                        isPurchased = checked
                                    )
                                )

                                refreshList++
                            }
                        },

                        onAddToPantry = {
                            itemToAddToPantry = item
                        },

                        onDelete = {
                            scope.launch {
                                shoppingItemDao.deleteShoppingItem(item)
                                refreshList++
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    showClearDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB3261E)
                )
            ) {
                Text(
                    text = "🗑️ Listeyi Temizle",
                    color = Color.White
                )
            }
        }
    }

    itemToAddToPantry?.let { item ->
        AddShoppingItemToPantryDialog(
            item = item,
            onDismiss = {
                itemToAddToPantry = null
            },
            onConfirm = { expirationDate ->
                scope.launch {

                    val merged =
                        addOrMergeShoppingItemIntoPantry(
                            ingredientDao = ingredientDao,
                            item = item,
                            expirationDate = expirationDate
                        )

                    shoppingItemDao.deleteShoppingItem(
                        item
                    )

                    itemToAddToPantry = null
                    refreshList++

                    Toast.makeText(
                        context,
                        if (merged) {
                            "${item.name} mevcut stokla birleştirildi 🧊➕✅"
                        } else {
                            "${item.name} buzdolabına eklendi 🧊✅"
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    if (showClearDialog) {

        AlertDialog(
            onDismissRequest = {
                showClearDialog = false
            },

            title = {
                Text("Alışveriş Listesini Temizle")
            },

            text = {
                Text(
                    "Listedeki tüm ürünleri silmek istediğine emin misin?"
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            shoppingItemDao.deleteAllShoppingItems()
                            showClearDialog = false
                            refreshList++
                        }
                    }
                ) {
                    Text(
                        text = "Evet, Temizle",
                        color = Color(0xFFB3261E)
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                    }
                ) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun ShoppingItemCard(
    item: ShoppingItem,
    onPurchasedChange: (Boolean) -> Unit,
    onAddToPantry: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (item.isPurchased) {
                    Color(0xFFE8F1EA)
                } else {
                    Color.White
                }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Checkbox(
                    checked = item.isPurchased,
                    onCheckedChange = onPurchasedChange
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {

                    Text(
                        text = item.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F3E34)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text =
                            "${formatShoppingQuantity(item.quantity)} ${item.unit}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    if (item.isPurchased) {

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "✅ Alındı",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3F6F52)
                        )
                    }
                }

                TextButton(
                    onClick = onDelete
                ) {
                    Text(
                        text = "Sil",
                        color = Color(0xFFB3261E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAddToPantry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🧊 Buzdolabına Ekle")
            }
        }
    }
}

@Composable
private fun AddShoppingItemToPantryDialog(
    item: ShoppingItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current

    var expirationDate by remember(item.id) {
        mutableStateOf("")
    }

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            expirationDate = "%02d.%02d.%04d".format(
                dayOfMonth,
                month + 1,
                year
            )
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🧊 Buzdolabına Ekle")
        },
        text = {
            Column {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Miktar: ${formatShoppingQuantity(item.quantity)} ${item.unit}",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (expirationDate.isBlank()) {
                        "Son kullanma tarihi seçilmedi."
                    } else {
                        "📅 Son kullanma tarihi: $expirationDate"
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📅 Son Kullanma Tarihi Seç")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (expirationDate.isBlank()) {
                        Toast.makeText(
                            context,
                            "Lütfen son kullanma tarihi seç",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        onConfirm(expirationDate)
                    }
                }
            ) {
                Text("Buzdolabına Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

private suspend fun addOrMergeShoppingItemIntoPantry(
    ingredientDao: com.smartpantry.chef.data.IngredientDao,
    item: ShoppingItem,
    expirationDate: String
): Boolean {

    val matchingIngredients =
        ingredientDao
            .getAllIngredients()
            .filter { existing ->
                shoppingPantryNamesMatch(
                    existing.name,
                    item.name
                ) &&
                        shoppingPantryExpirationDatesMatch(
                            existing.expirationDate,
                            expirationDate
                        ) &&
                        shoppingPantryCanConvertUnits(
                            fromUnit = item.unit,
                            toUnit = existing.unit
                        )
            }

    if (matchingIngredients.isEmpty()) {

        ingredientDao.insertIngredient(
            Ingredient(
                name = item.name.trim(),
                initialQuantity = item.quantity,
                remainingQuantity = item.quantity,
                unit = item.unit.trim(),
                expirationDate = expirationDate.trim(),
                imageUri = null
            )
        )

        return false
    }

    val mainIngredient =
        matchingIngredients.first()

    var totalInitial =
        mainIngredient.initialQuantity

    var totalRemaining =
        mainIngredient.remainingQuantity

    var finalImageUri =
        mainIngredient.imageUri

    matchingIngredients
        .drop(1)
        .forEach { duplicate ->

            shoppingPantryConvertQuantity(
                duplicate.initialQuantity,
                duplicate.unit,
                mainIngredient.unit
            )?.let {
                totalInitial += it
            }

            shoppingPantryConvertQuantity(
                duplicate.remainingQuantity,
                duplicate.unit,
                mainIngredient.unit
            )?.let {
                totalRemaining += it
            }

            if (
                finalImageUri.isNullOrBlank() &&
                !duplicate.imageUri.isNullOrBlank()
            ) {
                finalImageUri = duplicate.imageUri
            }
        }

    val incomingConverted =
        shoppingPantryConvertQuantity(
            item.quantity,
            item.unit,
            mainIngredient.unit
        ) ?: item.quantity

    ingredientDao.updateIngredient(
        mainIngredient.copy(
            initialQuantity =
                totalInitial + incomingConverted,
            remainingQuantity =
                totalRemaining + incomingConverted,
            imageUri = finalImageUri
        )
    )

    matchingIngredients
        .drop(1)
        .forEach { duplicate ->
            ingredientDao.deleteIngredient(
                duplicate
            )
        }

    return true
}

private fun shoppingPantryNamesMatch(
    firstName: String,
    secondName: String
): Boolean {

    return normalizeShoppingPantryName(firstName) ==
            normalizeShoppingPantryName(secondName)
}

private fun normalizeShoppingPantryName(
    name: String
): String {

    val cleaned =
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
        cleaned.length > 5 &&
                cleaned.endsWith("lar") ->
            cleaned.dropLast(3).trim()

        cleaned.length > 5 &&
                cleaned.endsWith("ler") ->
            cleaned.dropLast(3).trim()

        else ->
            cleaned
    }
}

private fun shoppingPantryExpirationDatesMatch(
    firstDate: String,
    secondDate: String
): Boolean {

    return firstDate.filter { it.isDigit() } ==
            secondDate.filter { it.isDigit() }
}

private enum class ShoppingPantryUnitFamily {
    COUNT,
    MASS,
    VOLUME,
    UNKNOWN
}

private fun normalizeShoppingPantryUnit(
    unit: String
): String {

    return unit
        .trim()
        .lowercase(Locale("tr", "TR"))
        .replace("ı", "i")
        .replace("ş", "s")
        .replace("ğ", "g")
        .replace("ü", "u")
        .replace("ö", "o")
        .replace("ç", "c")
        .replace(Regex("[^a-z0-9]"), "")
}

private fun shoppingPantryUnitFamily(
    unit: String
): ShoppingPantryUnitFamily {

    return when (normalizeShoppingPantryUnit(unit)) {

        "adet",
        "tane" ->
            ShoppingPantryUnitFamily.COUNT

        "g",
        "gr",
        "gram",
        "kg",
        "kilogram" ->
            ShoppingPantryUnitFamily.MASS

        "ml",
        "mililitre",
        "mililiter",
        "millilitre",
        "milliliter",
        "l",
        "lt",
        "litre",
        "liter" ->
            ShoppingPantryUnitFamily.VOLUME

        else ->
            ShoppingPantryUnitFamily.UNKNOWN
    }
}

private fun shoppingPantryCanConvertUnits(
    fromUnit: String,
    toUnit: String
): Boolean {

    val from =
        shoppingPantryUnitFamily(fromUnit)

    val to =
        shoppingPantryUnitFamily(toUnit)

    return if (
        normalizeShoppingPantryUnit(fromUnit) ==
        normalizeShoppingPantryUnit(toUnit)
    ) {
        true
    } else {
        from != ShoppingPantryUnitFamily.UNKNOWN &&
                from == to
    }
}

private fun shoppingPantryConvertQuantity(
    quantity: Double,
    fromUnit: String,
    toUnit: String
): Double? {

    if (
        normalizeShoppingPantryUnit(fromUnit) ==
        normalizeShoppingPantryUnit(toUnit)
    ) {
        return quantity
    }

    val fromFamily =
        shoppingPantryUnitFamily(fromUnit)

    val toFamily =
        shoppingPantryUnitFamily(toUnit)

    if (
        fromFamily == ShoppingPantryUnitFamily.UNKNOWN ||
        fromFamily != toFamily
    ) {
        return null
    }

    val base =
        when (normalizeShoppingPantryUnit(fromUnit)) {
            "kg",
            "kilogram" ->
                quantity * 1000.0

            "l",
            "lt",
            "litre",
            "liter" ->
                quantity * 1000.0

            else ->
                quantity
        }

    return when (normalizeShoppingPantryUnit(toUnit)) {
        "kg",
        "kilogram" ->
            base / 1000.0

        "l",
        "lt",
        "litre",
        "liter" ->
            base / 1000.0

        else ->
            base
    }
}

private fun formatShoppingQuantity(
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
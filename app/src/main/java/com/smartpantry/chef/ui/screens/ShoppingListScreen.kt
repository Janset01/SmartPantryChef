package com.smartpantry.chef.ui.screens

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
import com.smartpantry.chef.data.ShoppingItem
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

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

    var shoppingItems by remember {
        mutableStateOf<List<ShoppingItem>>(emptyList())
    }

    var refreshList by remember {
        mutableIntStateOf(0)
    }

    var showClearDialog by remember {
        mutableStateOf(false)
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
        }
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
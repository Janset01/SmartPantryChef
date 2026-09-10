package com.smartpantry.chef.ui.screens.pantry

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.smartpantry.chef.data.Ingredient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun IngredientCard(
    ingredient: Ingredient,
    onUseClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val stockPercentage =
        if (ingredient.initialQuantity > 0) {
            (ingredient.remainingQuantity / ingredient.initialQuantity)
                .coerceIn(0.0, 1.0)
        } else {
            0.0
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            if (!ingredient.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = Uri.parse(ingredient.imageUri),
                    contentDescription = ingredient.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = ingredient.name,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3E34)
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Başlangıç: ${
                    formatSmartIngredientQuantity(
                        ingredient.initialQuantity,
                        ingredient.unit
                    )
                }"
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Kalan: ${
                    formatSmartIngredientQuantity(
                        ingredient.remainingQuantity,
                        ingredient.unit
                    )
                }",
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { stockPercentage.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Stok: ${(stockPercentage * 100).toInt()}%",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = getIngredientStockStatus(stockPercentage),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(text = "📅 ${ingredient.expirationDate}")

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = getIngredientExpirationStatus(ingredient.expirationDate),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onUseClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = ingredient.remainingQuantity > 0
            ) {
                Text(
                    text = if (ingredient.remainingQuantity > 0) {
                        "🍴 Kullandım"
                    } else {
                        "🔴 Tükendi"
                    }
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("✏️ Düzenle")
                }

                Spacer(modifier = Modifier.weight(0.05f))

                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🗑️ Sil")
                }
            }
        }
    }
}

fun formatSmartIngredientQuantity(
    quantity: Double,
    unit: String
): String {
    val normalizedUnit = unit.trim().lowercase(Locale.getDefault())

    return when (normalizedUnit) {
        "litre" -> {
            if (quantity > 0.0 && quantity < 1.0) {
                "${formatIngredientQuantity(quantity * 1000.0)} Mililitre"
            } else {
                "${formatIngredientQuantity(quantity)} Litre"
            }
        }

        "mililitre" -> {
            if (quantity >= 1000.0) {
                "${formatIngredientQuantity(quantity / 1000.0)} Litre"
            } else {
                "${formatIngredientQuantity(quantity)} Mililitre"
            }
        }

        "kilogram" -> {
            if (quantity > 0.0 && quantity < 1.0) {
                "${formatIngredientQuantity(quantity * 1000.0)} Gram"
            } else {
                "${formatIngredientQuantity(quantity)} Kilogram"
            }
        }

        "gram" -> {
            if (quantity >= 1000.0) {
                "${formatIngredientQuantity(quantity / 1000.0)} Kilogram"
            } else {
                "${formatIngredientQuantity(quantity)} Gram"
            }
        }

        "adet" ->
            "${formatIngredientQuantity(quantity)} Adet"

        else ->
            "${formatIngredientQuantity(quantity)} $unit"
    }
}

fun formatIngredientQuantity(quantity: Double): String {
    return if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        String.format(
            Locale.getDefault(),
            "%.2f",
            quantity
        )
            .trimEnd('0')
            .trimEnd(',', '.')
    }
}

private fun getIngredientStockStatus(stockPercentage: Double): String {
    return when {
        stockPercentage <= 0.0 -> "🔴 Tükendi"
        stockPercentage <= 0.25 -> "🟠 Çok az kaldı"
        stockPercentage <= 0.50 -> "🟡 Yarıdan az kaldı"
        else -> "🟢 Stok yeterli"
    }
}

private fun getIngredientExpirationStatus(
    expirationDate: String
): String {
    return try {
        val formatter = SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        )

        formatter.isLenient = false

        val expiration =
            formatter.parse(expirationDate)
                ?: return "⚪ Tarih bilgisi okunamadı"

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val expirationCalendar =
            Calendar.getInstance().apply {
                time = expiration
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        val difference =
            expirationCalendar.timeInMillis - today.timeInMillis

        val days =
            TimeUnit.MILLISECONDS.toDays(difference)

        when {
            days < 0 -> "🔴 Süresi Doldu"
            days == 0L -> "🔴 Bugün Tüket"
            days <= 3 -> "🟠 Yakında Tüket • $days gün kaldı"
            else -> "🟢 Taze • $days gün kaldı"
        }

    } catch (_: Exception) {
        "⚪ Tarih bilgisi okunamadı"
    }
}

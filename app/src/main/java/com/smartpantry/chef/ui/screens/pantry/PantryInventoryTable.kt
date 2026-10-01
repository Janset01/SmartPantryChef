package com.smartpantry.chef.ui.screens.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpantry.chef.data.Ingredient
import com.smartpantry.chef.data.IngredientCategoryClassifier
import com.smartpantry.chef.data.PantryMainCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

private enum class PantryTableFilter {
    ALL,
    LOW_STOCK,
    EXPIRING
}

private data class PantryTableItem(
    val ingredient: Ingredient,
    val mainCategory: PantryMainCategory,
    val subCategory: String
)

@Composable
fun PantryInventoryTable(
    ingredients: List<Ingredient>,
    onUseClick: (Ingredient) -> Unit,
    onEditClick: (Ingredient) -> Unit,
    onDeleteClick: (Ingredient) -> Unit
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf(PantryTableFilter.ALL)
    }

    val expandedCategories =
        remember {
            mutableStateMapOf<PantryMainCategory, Boolean>()
        }

    val classifiedItems =
        remember(ingredients) {
            ingredients.map { ingredient ->

                val category =
                    IngredientCategoryClassifier
                        .classify(
                            ingredient.name
                        )

                PantryTableItem(
                    ingredient = ingredient,
                    mainCategory = category.mainCategory,
                    subCategory = category.subCategory
                )
            }
        }

    val filteredItems =
        classifiedItems.filter { item ->

            val matchesSearch =
                searchText.isBlank() ||
                        item.ingredient.name
                            .contains(
                                searchText,
                                ignoreCase = true
                            ) ||
                        item.subCategory
                            .contains(
                                searchText,
                                ignoreCase = true
                            ) ||
                        item.mainCategory.title
                            .contains(
                                searchText,
                                ignoreCase = true
                            )

            val matchesFilter =
                when (selectedFilter) {

                    PantryTableFilter.ALL ->
                        true

                    PantryTableFilter.LOW_STOCK ->
                        stockPercent(
                            item.ingredient
                        ) <= 35

                    PantryTableFilter.EXPIRING -> {
                        val days =
                            expirationDaysLeft(
                                item.ingredient
                                    .expirationDate
                            )

                        days != null &&
                                days in 0L..5L
                    }
                }

            matchesSearch &&
                    matchesFilter
        }

    val groupedItems =
        filteredItems
            .groupBy {
                it.mainCategory
            }

    val lowStockCount =
        ingredients.count {
            stockPercent(it) <= 35
        }

    val expiringCount =
        ingredients.count {
            val days =
                expirationDaysLeft(
                    it.expirationDate
                )

            days != null &&
                    days in 0L..5L
        }

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        // -------------------------------------------------------------
        // ÖZET
        // -------------------------------------------------------------

        Card(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(18.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color(0xFFEAF2EC)
                )
        ) {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                PantrySummaryValue(
                    title = "Toplam",
                    value = "${ingredients.size}",
                    emoji = "📦"
                )

                PantrySummaryValue(
                    title = "Stok Az",
                    value = "$lowStockCount",
                    emoji = "⚠️"
                )

                PantrySummaryValue(
                    title = "SKT Yakın",
                    value = "$expiringCount",
                    emoji = "⏰"
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // -------------------------------------------------------------
        // ARAMA
        // -------------------------------------------------------------

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier =
                Modifier.fillMaxWidth(),
            label = {
                Text(
                    "🔎 Malzeme ara"
                )
            },
            placeholder = {
                Text(
                    "Örn. sarımsak, süt, kahve..."
                )
            },
            singleLine = true
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        // -------------------------------------------------------------
        // HIZLI FİLTRELER
        // -------------------------------------------------------------

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState()
                    ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            FilterChip(
                selected =
                    selectedFilter ==
                            PantryTableFilter.ALL,
                onClick = {
                    selectedFilter =
                        PantryTableFilter.ALL
                },
                label = {
                    Text("Tümü")
                }
            )

            FilterChip(
                selected =
                    selectedFilter ==
                            PantryTableFilter.LOW_STOCK,
                onClick = {
                    selectedFilter =
                        PantryTableFilter.LOW_STOCK
                },
                label = {
                    Text("⚠️ Stok Az")
                }
            )

            FilterChip(
                selected =
                    selectedFilter ==
                            PantryTableFilter.EXPIRING,
                onClick = {
                    selectedFilter =
                        PantryTableFilter.EXPIRING
                },
                label = {
                    Text("⏰ SKT Yakın")
                }
            )
        }

        Spacer(
            modifier =
                Modifier.height(14.dp)
        )

        if (filteredItems.isEmpty()) {

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(18.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    )
            ) {

                Text(
                    text =
                        if (
                            searchText.isNotBlank() ||
                            selectedFilter !=
                            PantryTableFilter.ALL
                        ) {
                            "Bu arama/filtre için ürün bulunamadı."
                        } else {
                            "Henüz buzdolabında malzeme yok."
                        },
                    modifier =
                        Modifier.padding(18.dp),
                    color =
                        Color(0xFF5B665E)
                )
            }

            return@Column
        }

        // -------------------------------------------------------------
        // KATEGORİLER
        // -------------------------------------------------------------

        IngredientCategoryClassifier
            .groupedCategoryOrder()
            .forEach { category ->

                val categoryItems =
                    groupedItems[category]
                        .orEmpty()

                if (
                    categoryItems.isNotEmpty()
                ) {

                    val expanded =
                        expandedCategories[
                            category
                        ] ?: true

                    PantryCategoryTable(
                        category = category,
                        items = categoryItems,
                        expanded = expanded,
                        onToggleExpanded = {
                            expandedCategories[
                                category
                            ] = !expanded
                        },
                        onUseClick = onUseClick,
                        onEditClick = onEditClick,
                        onDeleteClick =
                            onDeleteClick
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )
                }
            }
    }
}

@Composable
private fun PantrySummaryValue(
    title: String,
    value: String,
    emoji: String
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "$emoji $value",
            fontSize = 18.sp,
            fontWeight =
                FontWeight.Bold,
            color =
                Color(0xFF2F3E34)
        )

        Spacer(
            modifier =
                Modifier.height(2.dp)
        )

        Text(
            text = title,
            fontSize = 12.sp,
            color =
                Color(0xFF69756D)
        )
    }
}

@Composable
private fun PantryCategoryTable(
    category: PantryMainCategory,
    items: List<PantryTableItem>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onUseClick: (Ingredient) -> Unit,
    onEditClick: (Ingredient) -> Unit,
    onDeleteClick: (Ingredient) -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),
        elevation =
            CardDefaults
                .cardElevation(
                    defaultElevation =
                        2.dp
                )
    ) {

        Column(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            TextButton(
                onClick =
                    onToggleExpanded,
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            "${category.emoji} " +
                                    category.title,
                        modifier =
                            Modifier.weight(1f),
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color(0xFF2F3E34)
                    )

                    Text(
                        text =
                            "${items.size} ürün  " +
                                    if (expanded) {
                                        "▲"
                                    } else {
                                        "▼"
                                    },
                        fontSize = 13.sp,
                        color =
                            Color(0xFF66746B)
                    )
                }
            }

            if (expanded) {

                HorizontalDivider(
                    color =
                        Color(0xFFE2E6E3)
                )

                val horizontalScrollState =
                    rememberScrollState()

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                horizontalScrollState
                            )
                ) {

                    PantryTableHeader()

                    HorizontalDivider(
                        color =
                            Color(0xFFE2E6E3)
                    )

                    items
                        .sortedWith(
                            compareBy<
                                    PantryTableItem
                                    > {
                                expirationSortValue(
                                    it.ingredient
                                        .expirationDate
                                )
                            }.thenBy {
                                it.ingredient.name
                            }
                        )
                        .forEachIndexed {
                                index,
                                item ->

                            PantryTableRow(
                                item = item,
                                rowIndex = index,
                                onUseClick =
                                    onUseClick,
                                onEditClick =
                                    onEditClick,
                                onDeleteClick =
                                    onDeleteClick
                            )

                            if (
                                index <
                                items.lastIndex
                            ) {

                                HorizontalDivider(
                                    color =
                                        Color(
                                            0xFFF0F2F0
                                        )
                                )
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun PantryTableHeader() {

    Row(
        modifier =
            Modifier
                .width(890.dp)
                .background(
                    Color(0xFFF1F5F2)
                )
                .padding(
                    vertical = 10.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        PantryHeaderCell(
            "Ürün",
            170
        )

        PantryHeaderCell(
            "Miktar",
            110
        )

        PantryHeaderCell(
            "Stok",
            115
        )

        PantryHeaderCell(
            "SKT",
            110
        )

        PantryHeaderCell(
            "Tazelik",
            125
        )

        PantryHeaderCell(
            "Alt Kategori",
            130
        )

        PantryHeaderCell(
            "İşlemler",
            130
        )
    }
}

@Composable
private fun PantryHeaderCell(
    text: String,
    width: Int
) {

    Text(
        text = text,
        modifier =
            Modifier
                .width(
                    width.dp
                )
                .padding(
                    horizontal = 8.dp
                ),
        fontSize = 12.sp,
        fontWeight =
            FontWeight.Bold,
        color =
            Color(0xFF526057)
    )
}

@Composable
private fun PantryTableRow(
    item: PantryTableItem,
    rowIndex: Int,
    onUseClick: (Ingredient) -> Unit,
    onEditClick: (Ingredient) -> Unit,
    onDeleteClick: (Ingredient) -> Unit
) {

    val ingredient =
        item.ingredient

    val stockStatus =
        pantryStockStatus(
            ingredient
        )

    val freshness =
        pantryFreshnessStatus(
            ingredient.expirationDate
        )

    Row(
        modifier =
            Modifier
                .width(890.dp)
                .background(
                    if (
                        rowIndex % 2 == 0
                    ) {
                        Color.White
                    } else {
                        Color(0xFFFAFBFA)
                    }
                )
                .padding(
                    vertical = 9.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                ingredient.name,
            modifier =
                Modifier
                    .width(170.dp)
                    .padding(
                        horizontal = 8.dp
                    ),
            fontSize = 14.sp,
            fontWeight =
                FontWeight.SemiBold,
            color =
                Color(0xFF2F3E34)
        )

        Text(
            text =
                "${
                    formatIngredientQuantity(
                        ingredient.remainingQuantity
                    )
                } ${ingredient.unit}",
            modifier =
                Modifier
                    .width(110.dp)
                    .padding(
                        horizontal = 8.dp
                    ),
            fontSize = 13.sp,
            color =
                Color(0xFF526057)
        )

        Text(
            text =
                stockStatus.first,
            modifier =
                Modifier
                    .width(115.dp)
                    .padding(
                        horizontal = 8.dp
                    ),
            fontSize = 12.sp,
            fontWeight =
                FontWeight.SemiBold,
            color =
                stockStatus.second
        )

        Text(
            text =
                ingredient.expirationDate,
            modifier =
                Modifier
                    .width(110.dp)
                    .padding(
                        horizontal = 8.dp
                    ),
            fontSize = 12.sp,
            color =
                Color(0xFF526057)
        )

        Text(
            text =
                freshness.first,
            modifier =
                Modifier
                    .width(125.dp)
                    .padding(
                        horizontal = 8.dp
                    ),
            fontSize = 12.sp,
            fontWeight =
                FontWeight.SemiBold,
            color =
                freshness.second
        )

        Text(
            text =
                item.subCategory,
            modifier =
                Modifier
                    .width(130.dp)
                    .padding(
                        horizontal = 8.dp
                    ),
            fontSize = 12.sp,
            color =
                Color(0xFF66746B)
        )

        Row(
            modifier =
                Modifier.width(130.dp),
            horizontalArrangement =
                Arrangement.spacedBy(
                    1.dp
                )
        ) {

            TextButton(
                onClick = {
                    onUseClick(
                        ingredient
                    )
                }
            ) {
                Text(
                    "🍴",
                    fontSize = 16.sp
                )
            }

            TextButton(
                onClick = {
                    onEditClick(
                        ingredient
                    )
                }
            ) {
                Text(
                    "✏️",
                    fontSize = 16.sp
                )
            }

            TextButton(
                onClick = {
                    onDeleteClick(
                        ingredient
                    )
                }
            ) {
                Text(
                    "🗑️",
                    fontSize = 16.sp
                )
            }
        }
    }
}

private fun stockPercent(
    ingredient: Ingredient
): Int {

    if (
        ingredient.initialQuantity <= 0.0
    ) {
        return 0
    }

    return (
            ingredient.remainingQuantity /
                    ingredient.initialQuantity *
                    100.0
            )
        .coerceIn(
            0.0,
            100.0
        )
        .toInt()
}

private fun pantryStockStatus(
    ingredient: Ingredient
): Pair<String, Color> {

    val percent =
        stockPercent(
            ingredient
        )

    return when {

        ingredient.remainingQuantity <=
                0.0 ->
            "⚫ Bitti" to
                    Color(0xFF616161)

        percent <= 20 ->
            "🔴 Çok Az" to
                    Color(0xFFB3261E)

        percent <= 35 ->
            "🟠 Az" to
                    Color(0xFFB85C00)

        percent <= 60 ->
            "🟡 Azalıyor" to
                    Color(0xFF8A6D00)

        else ->
            "🟢 Yeterli" to
                    Color(0xFF3F6F52)
    }
}

private fun pantryFreshnessStatus(
    expirationDate: String
): Pair<String, Color> {

    val days =
        expirationDaysLeft(
            expirationDate
        )

    return when {

        days == null ->
            "—" to
                    Color.Gray

        days < 0 ->
            "🔴 Süresi doldu" to
                    Color(0xFFB3261E)

        days == 0L ->
            "🔴 Bugün" to
                    Color(0xFFB3261E)

        days == 1L ->
            "🔴 1 gün" to
                    Color(0xFFB3261E)

        days <= 3L ->
            "🟠 $days gün" to
                    Color(0xFFB85C00)

        days <= 7L ->
            "🟡 $days gün" to
                    Color(0xFF8A6D00)

        else ->
            "🟢 $days gün" to
                    Color(0xFF3F6F52)
    }
}

private fun expirationDaysLeft(
    expirationDate: String
): Long? {

    val formatter =
        SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).apply {
            isLenient = false
        }

    val expiration =
        try {
            formatter.parse(
                expirationDate
            )
        } catch (_: Exception) {
            null
        } ?: return null

    val today =
        Calendar.getInstance().apply {
            set(
                Calendar.HOUR_OF_DAY,
                0
            )
            set(
                Calendar.MINUTE,
                0
            )
            set(
                Calendar.SECOND,
                0
            )
            set(
                Calendar.MILLISECOND,
                0
            )
        }

    val expiry =
        Calendar.getInstance().apply {
            time = expiration

            set(
                Calendar.HOUR_OF_DAY,
                0
            )
            set(
                Calendar.MINUTE,
                0
            )
            set(
                Calendar.SECOND,
                0
            )
            set(
                Calendar.MILLISECOND,
                0
            )
        }

    return TimeUnit
        .MILLISECONDS
        .toDays(
            expiry.timeInMillis -
                    today.timeInMillis
        )
}

private fun expirationSortValue(
    expirationDate: String
): Long {

    val days =
        expirationDaysLeft(
            expirationDate
        )

    return when {
        days == null ->
            Long.MAX_VALUE

        days < 0 ->
            Long.MIN_VALUE

        else ->
            days
    }
}
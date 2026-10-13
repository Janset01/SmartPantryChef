package com.smartpantry.chef.ui.screens.pantry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.smartpantry.chef.data.Ingredient
import com.smartpantry.chef.data.IngredientCategoryClassifier
import com.smartpantry.chef.data.PantryMainCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngredientDialog(
    ingredient: Ingredient,
    onDismiss: () -> Unit,
    onSave: (Ingredient) -> Unit
) {
    var name by remember(ingredient.id) {
        mutableStateOf(ingredient.name)
    }

    var remainingQuantity by remember(ingredient.id) {
        mutableStateOf(
            formatIngredientQuantity(
                ingredient.remainingQuantity
            )
        )
    }

    var quantityError by remember(ingredient.id) {
        mutableStateOf<String?>(null)
    }

    var selectedUnit by remember(ingredient.id) {
        mutableStateOf(ingredient.unit)
    }

    var expirationDate by remember(ingredient.id) {
        mutableStateOf(ingredient.expirationDate)
    }

    var unitExpanded by remember {
        mutableStateOf(false)
    }

    var mainCategoryExpanded by remember {
        mutableStateOf(false)
    }

    var subCategoryExpanded by remember {
        mutableStateOf(false)
    }

    var manualCategoryEnabled by remember(ingredient.id) {
        mutableStateOf(
            !ingredient.manualMainCategory.isNullOrBlank()
        )
    }

    val initialCategory = remember(ingredient.id) {
        IngredientCategoryClassifier.classify(ingredient)
    }

    var selectedMainCategory by remember(ingredient.id) {
        mutableStateOf(initialCategory.mainCategory)
    }

    var selectedSubCategory by remember(ingredient.id) {
        mutableStateOf(initialCategory.subCategory)
    }

    val units = listOf(
        "Adet",
        "Gram",
        "Kilogram",
        "Mililitre",
        "Litre"
    )

    val mainCategories =
        IngredientCategoryClassifier.groupedCategoryOrder()

    val subCategories =
        IngredientCategoryClassifier
            .subCategoriesFor(selectedMainCategory)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("✏️ Malzemeyi Düzenle")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName ->
                        name = newName

                        if (!manualCategoryEnabled) {
                            val autoCategory =
                                IngredientCategoryClassifier
                                    .classify(newName)

                            selectedMainCategory =
                                autoCategory.mainCategory

                            selectedSubCategory =
                                autoCategory.subCategory
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Malzeme Adı")
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = remainingQuantity,
                    onValueChange = { newValue ->
                        remainingQuantity =
                            newValue.filter {
                                it.isDigit() ||
                                        it == ',' ||
                                        it == '.'
                            }

                        quantityError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Kalan Miktar")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = quantityError != null,
                    supportingText = {
                        quantityError?.let { message ->
                            Text(message)
                        }
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = {
                        unitExpanded = !unitExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = {
                            Text("Birim")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults
                                .TrailingIcon(
                                    expanded = unitExpanded
                                )
                        },
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = {
                            unitExpanded = false
                        }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = {
                                    Text(unit)
                                },
                                onClick = {
                                    selectedUnit = unit
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = expirationDate,
                    onValueChange = {
                        expirationDate = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Son Kullanma Tarihi")
                    },
                    placeholder = {
                        Text("GG.AA.YYYY")
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text =
                        if (manualCategoryEnabled) {
                            "📌 Manuel kategori seçimi"
                        } else {
                            "✨ Otomatik kategori"
                        }
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = mainCategoryExpanded,
                    onExpandedChange = {
                        mainCategoryExpanded =
                            !mainCategoryExpanded
                    }
                ) {
                    OutlinedTextField(
                        value =
                            "${selectedMainCategory.emoji} " +
                                    selectedMainCategory.title,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = {
                            Text("Kategori")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults
                                .TrailingIcon(
                                    expanded =
                                        mainCategoryExpanded
                                )
                        },
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = mainCategoryExpanded,
                        onDismissRequest = {
                            mainCategoryExpanded = false
                        }
                    ) {
                        mainCategories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${category.emoji} " +
                                                category.title
                                    )
                                },
                                onClick = {
                                    manualCategoryEnabled = true
                                    selectedMainCategory = category

                                    selectedSubCategory =
                                        IngredientCategoryClassifier
                                            .subCategoriesFor(category)
                                            .firstOrNull()
                                            ?: "Diğer"

                                    mainCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = subCategoryExpanded,
                    onExpandedChange = {
                        subCategoryExpanded =
                            !subCategoryExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedSubCategory,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = {
                            Text("Alt Kategori")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults
                                .TrailingIcon(
                                    expanded =
                                        subCategoryExpanded
                                )
                        },
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = subCategoryExpanded,
                        onDismissRequest = {
                            subCategoryExpanded = false
                        }
                    ) {
                        subCategories.forEach { subCategory ->
                            DropdownMenuItem(
                                text = {
                                    Text(subCategory)
                                },
                                onClick = {
                                    manualCategoryEnabled = true
                                    selectedSubCategory =
                                        subCategory

                                    subCategoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                TextButton(
                    onClick = {
                        manualCategoryEnabled = false

                        val autoCategory =
                            IngredientCategoryClassifier
                                .classify(name)

                        selectedMainCategory =
                            autoCategory.mainCategory

                        selectedSubCategory =
                            autoCategory.subCategory
                    }
                ) {
                    Text("↻ Otomatik kategoriye dön")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedQuantity =
                        remainingQuantity
                            .replace(",", ".")
                            .toDoubleOrNull()

                    if (parsedQuantity == null) {
                        quantityError = "Geçerli bir miktar gir"
                        return@Button
                    }

                    if (parsedQuantity <= 0) {
                        quantityError = "Miktar 0'dan büyük olmalı"
                        return@Button
                    }

                    if (
                        name.isNotBlank() &&
                        expirationDate.isNotBlank()
                    ) {
                        val newInitialQuantity =
                            if (
                                parsedQuantity >
                                ingredient.initialQuantity
                            ) {
                                parsedQuantity
                            } else {
                                ingredient.initialQuantity
                            }

                        val updatedIngredient =
                            ingredient.copy(
                                name = name.trim(),
                                initialQuantity =
                                    newInitialQuantity,
                                remainingQuantity =
                                    parsedQuantity,
                                unit = selectedUnit,
                                expirationDate =
                                    expirationDate.trim(),
                                manualMainCategory =
                                    if (manualCategoryEnabled) {
                                        selectedMainCategory.name
                                    } else {
                                        null
                                    },
                                manualSubCategory =
                                    if (manualCategoryEnabled) {
                                        selectedSubCategory
                                    } else {
                                        null
                                    }
                            )

                        onSave(updatedIngredient)
                    }
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("İptal")
            }
        }
    )
}
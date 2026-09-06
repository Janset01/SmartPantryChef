package com.smartpantry.chef.ui.screens.pantry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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

    var selectedUnit by remember(ingredient.id) {
        mutableStateOf(ingredient.unit)
    }

    var expirationDate by remember(ingredient.id) {
        mutableStateOf(ingredient.expirationDate)
    }

    var unitExpanded by remember {
        mutableStateOf(false)
    }

    val units = listOf(
        "Adet",
        "Gram",
        "Kilogram",
        "Mililitre",
        "Litre"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("✏️ Malzemeyi Düzenle")
        },
        text = {

            Column {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
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
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Kalan Miktar")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
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
            }
        },
        confirmButton = {

            Button(
                onClick = {

                    val parsedQuantity =
                        remainingQuantity
                            .replace(",", ".")
                            .toDoubleOrNull()

                    if (
                        name.isNotBlank() &&
                        parsedQuantity != null &&
                        parsedQuantity >= 0 &&
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
                                    expirationDate.trim()
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
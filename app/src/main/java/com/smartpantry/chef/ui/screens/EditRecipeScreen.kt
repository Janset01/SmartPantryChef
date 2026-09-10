package com.smartpantry.chef.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.RecipeIngredient
import kotlinx.coroutines.launch

private data class EditRecipeIngredientInput(
    val name: String,
    val quantity: Double,
    val unit: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onRecipeUpdated: (Recipe) -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val recipeDao = database.recipeDao()
    val recipeIngredientDao =
        database.recipeIngredientDao()

    val unitList = listOf(
        "Adet",
        "Gram",
        "Kilogram",
        "Mililitre",
        "Litre"
    )

    var recipeName by remember {
        mutableStateOf(recipe.name)
    }

    var category by remember {
        mutableStateOf(recipe.category)
    }

    var preparationTime by remember {
        mutableStateOf(recipe.preparationTime)
    }

    var servings by remember {
        mutableStateOf(recipe.servings)
    }

    var instructions by remember {
        mutableStateOf(recipe.instructions)
    }

    // Yeni malzeme ekleme alanları

    var ingredientName by remember {
        mutableStateOf("")
    }

    var ingredientQuantity by remember {
        mutableStateOf("")
    }

    var ingredientUnit by remember {
        mutableStateOf("Adet")
    }

    var ingredientUnitExpanded by remember {
        mutableStateOf(false)
    }

    val recipeIngredients = remember {
        mutableStateListOf<EditRecipeIngredientInput>()
    }

    var ingredientsLoaded by remember {
        mutableStateOf(false)
    }

    // -----------------------------------------
    // TARİFİN KAYITLI MALZEMELERİNİ ROOM'DAN AL
    // -----------------------------------------

    LaunchedEffect(recipe.id) {

        val savedIngredients =
            recipeIngredientDao
                .getIngredientsForRecipe(recipe.id)

        recipeIngredients.clear()

        recipeIngredients.addAll(
            savedIngredients.map {
                EditRecipeIngredientInput(
                    name = it.name,
                    quantity = it.quantity,
                    unit = it.unit
                )
            }
        )

        ingredientsLoaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F2))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Geri")
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "✏️ Tarifi Düzenle",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                OutlinedTextField(
                    value = recipeName,
                    onValueChange = {
                        recipeName = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Tarif Adı")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Kategori")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = preparationTime,
                    onValueChange = {
                        preparationTime = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Hazırlama Süresi")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = servings,
                    onValueChange = {
                        servings = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Porsiyon")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(
                    modifier = Modifier.height(22.dp)
                )

                Text(
                    text = "🥕 Tarif Malzemeleri",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3E34)
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Tarifte kullanılan malzemeleri miktarlarıyla düzenle.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                // MALZEME ADI

                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = {
                        ingredientName = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Malzeme Adı")
                    },
                    placeholder = {
                        Text("Örn: Süt")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                // MİKTAR + BİRİM

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedTextField(
                        value = ingredientQuantity,
                        onValueChange = { value ->

                            ingredientQuantity =
                                value.filter {
                                    it.isDigit() ||
                                            it == ',' ||
                                            it == '.'
                                }
                        },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text("Miktar")
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Text
                            ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = ingredientUnitExpanded,
                        onExpandedChange = {
                            ingredientUnitExpanded =
                                !ingredientUnitExpanded
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        OutlinedTextField(
                            value = ingredientUnit,
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
                                        expanded =
                                            ingredientUnitExpanded
                                    )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = ingredientUnitExpanded,
                            onDismissRequest = {
                                ingredientUnitExpanded = false
                            }
                        ) {

                            unitList.forEach { unit ->

                                DropdownMenuItem(
                                    text = {
                                        Text(unit)
                                    },
                                    onClick = {
                                        ingredientUnit = unit
                                        ingredientUnitExpanded =
                                            false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Button(
                    onClick = {

                        val quantity =
                            ingredientQuantity
                                .replace(",", ".")
                                .toDoubleOrNull()

                        if (ingredientName.isBlank()) {

                            Toast.makeText(
                                context,
                                "Malzeme adı gir",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        if (
                            quantity == null ||
                            quantity <= 0
                        ) {

                            Toast.makeText(
                                context,
                                "Geçerli bir miktar gir",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        recipeIngredients.add(
                            EditRecipeIngredientInput(
                                name =
                                    ingredientName.trim(),
                                quantity = quantity,
                                unit = ingredientUnit
                            )
                        )

                        ingredientName = ""
                        ingredientQuantity = ""
                        ingredientUnit = "Adet"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("➕ Malzeme Ekle")
                }

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                // KAYITLI MALZEMELER

                if (!ingredientsLoaded) {

                    Text(
                        text = "Malzemeler yükleniyor...",
                        color = Color.Gray
                    )

                } else if (recipeIngredients.isEmpty()) {

                    Text(
                        text = "Henüz malzeme eklenmemiş.",
                        color = Color.Gray
                    )

                } else {

                    Text(
                        text = "Tarifteki Malzemeler",
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    recipeIngredients.forEachIndexed {
                            index,
                            ingredient ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color(0xFFF3F6F3)
                                )
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text =
                                        "${ingredient.name} • " +
                                                "${formatEditRecipeQuantity(ingredient.quantity)} " +
                                                ingredient.unit,
                                    modifier =
                                        Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        recipeIngredients
                                            .removeAt(index)
                                    }
                                ) {
                                    Text("🗑️")
                                }
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = {
                        instructions = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    label = {
                        Text("Hazırlanışı")
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = {

                if (
                    recipeName.isBlank() ||
                    category.isBlank() ||
                    preparationTime.isBlank() ||
                    servings.isBlank() ||
                    instructions.isBlank()
                ) {

                    Toast.makeText(
                        context,
                        "Lütfen tarif bilgilerini doldur",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                if (recipeIngredients.isEmpty()) {

                    Toast.makeText(
                        context,
                        "Tarifte en az bir malzeme olmalı",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                // Eski Recipe.ingredients alanını da
                // güncel tutuyoruz.
                val ingredientsText =
                    recipeIngredients.joinToString(
                        separator = "\n"
                    ) { ingredient ->

                        "${formatEditRecipeQuantity(ingredient.quantity)} " +
                                "${ingredient.unit} " +
                                ingredient.name
                    }

                val updatedRecipe =
                    recipe.copy(
                        name = recipeName.trim(),
                        category = category.trim(),
                        preparationTime =
                            preparationTime.trim(),
                        servings = servings.trim(),
                        ingredients = ingredientsText,
                        instructions =
                            instructions.trim()
                    )

                scope.launch {

                    // Tarif ana kaydını güncelle.
                    recipeDao.updateRecipe(
                        updatedRecipe
                    )

                    // Eski yapılandırılmış
                    // malzemeleri temizle.
                    recipeIngredientDao
                        .deleteIngredientsForRecipe(
                            recipe.id
                        )

                    // Ekrandaki güncel malzemeleri
                    // yeniden kaydet.
                    val updatedIngredients =
                        recipeIngredients.map {
                                ingredient ->

                            RecipeIngredient(
                                recipeId = recipe.id,
                                name = ingredient.name,
                                quantity =
                                    ingredient.quantity,
                                unit = ingredient.unit
                            )
                        }

                    recipeIngredientDao
                        .insertRecipeIngredients(
                            updatedIngredients
                        )

                    Toast.makeText(
                        context,
                        "Tarif ve malzemeler güncellendi ✅",
                        Toast.LENGTH_SHORT
                    ).show()

                    onRecipeUpdated(
                        updatedRecipe
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(18.dp)
        ) {

            Text(
                text = "💾 Değişiklikleri Kaydet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )
    }
}

private fun formatEditRecipeQuantity(
    quantity: Double
): String {

    return if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        quantity.toString()
    }
}
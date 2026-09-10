package com.smartpantry.chef.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.RecipeIngredient
import kotlinx.coroutines.launch

data class RecipeIngredientInput(
    val name: String,
    val quantity: Double,
    val unit: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val recipeDao = database.recipeDao()
    val recipeIngredientDao =
        database.recipeIngredientDao()

    val categoryList = listOf(
        "Kahvaltı",
        "Öğle Yemeği",
        "Akşam Yemeği",
        "Tatlı",
        "Fit",
        "Atıştırmalık"
    )

    val unitList = listOf(
        "Adet",
        "Gram",
        "Kilogram",
        "Mililitre",
        "Litre"
    )

    var recipeName by rememberSaveable {
        mutableStateOf("")
    }

    var category by rememberSaveable {
        mutableStateOf("Kategori Seç")
    }

    var categoryExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var preparationTime by rememberSaveable {
        mutableStateOf("")
    }

    var servings by rememberSaveable {
        mutableStateOf("")
    }

    var instructions by rememberSaveable {
        mutableStateOf("")
    }

    // Yeni malzeme giriş alanları

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

    val recipeIngredients =
        remember {
            mutableStateListOf<RecipeIngredientInput>()
        }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            selectedImageUri = uri
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF8F6F2)
            )
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 20.dp
            )
    ) {

        Spacer(
            modifier =
                Modifier.height(28.dp)
        )

        Text(
            text = "Tarif Ekle 🍳",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Text(
            text =
                "Kendi tarifini toplulukla paylaş",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Card(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(22.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color.White
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation =
                        3.dp
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(18.dp)
            ) {

                // Tarif adı

                OutlinedTextField(
                    value = recipeName,
                    onValueChange = {
                        recipeName = it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {
                        Text("Tarif Adı")
                    },
                    placeholder = {
                        Text(
                            "Örn: Kremalı Tavuklu Makarna"
                        )
                    },
                    singleLine = true,
                    shape =
                        RoundedCornerShape(16.dp)
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                // Kategori

                ExposedDropdownMenuBox(
                    expanded =
                        categoryExpanded,
                    onExpandedChange = {
                        categoryExpanded =
                            !categoryExpanded
                    }
                ) {

                    OutlinedTextField(
                        value = category,
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
                                        categoryExpanded
                                )
                        },
                        shape =
                            RoundedCornerShape(16.dp)
                    )

                    ExposedDropdownMenu(
                        expanded =
                            categoryExpanded,
                        onDismissRequest = {
                            categoryExpanded =
                                false
                        }
                    ) {

                        categoryList.forEach {
                                item ->

                            DropdownMenuItem(
                                text = {
                                    Text(item)
                                },
                                onClick = {

                                    category = item

                                    categoryExpanded =
                                        false
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    OutlinedTextField(
                        value =
                            preparationTime,
                        onValueChange = {
                            preparationTime = it
                        },
                        modifier =
                            Modifier.weight(1f),
                        label = {
                            Text("Süre")
                        },
                        placeholder = {
                            Text("30 dk")
                        },
                        singleLine = true,
                        shape =
                            RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = servings,
                        onValueChange = {
                            servings = it
                        },
                        modifier =
                            Modifier.weight(1f),
                        label = {
                            Text("Porsiyon")
                        },
                        placeholder = {
                            Text("4 kişi")
                        },
                        singleLine = true,
                        shape =
                            RoundedCornerShape(16.dp)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )

                Text(
                    text = "🥕 Tarif Malzemeleri",
                    fontSize = 18.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color(0xFF2F3E34)
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                // Malzeme adı

                OutlinedTextField(
                    value =
                        ingredientName,
                    onValueChange = {
                        ingredientName = it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {
                        Text("Malzeme Adı")
                    },
                    placeholder = {
                        Text("Örn: Süt")
                    },
                    singleLine = true,
                    shape =
                        RoundedCornerShape(16.dp)
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                // Miktar + birim

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    OutlinedTextField(
                        value =
                            ingredientQuantity,
                        onValueChange = {
                                value ->

                            ingredientQuantity =
                                value.filter {

                                    it.isDigit() ||
                                            it == ',' ||
                                            it == '.'
                                }
                        },
                        modifier =
                            Modifier.weight(1f),
                        label = {
                            Text("Miktar")
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Text
                            ),
                        singleLine = true,
                        shape =
                            RoundedCornerShape(16.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded =
                            ingredientUnitExpanded,
                        onExpandedChange = {

                            ingredientUnitExpanded =
                                !ingredientUnitExpanded
                        },
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        OutlinedTextField(
                            value =
                                ingredientUnit,
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
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded =
                                ingredientUnitExpanded,
                            onDismissRequest = {

                                ingredientUnitExpanded =
                                    false
                            }
                        ) {

                            unitList.forEach {
                                    unit ->

                                DropdownMenuItem(
                                    text = {
                                        Text(unit)
                                    },
                                    onClick = {

                                        ingredientUnit =
                                            unit

                                        ingredientUnitExpanded =
                                            false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Button(
                    onClick = {

                        val quantityValue =
                            ingredientQuantity
                                .replace(
                                    ",",
                                    "."
                                )
                                .toDoubleOrNull()

                        if (
                            ingredientName
                                .isBlank()
                        ) {

                            Toast.makeText(
                                context,
                                "Malzeme adı gir",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        if (
                            quantityValue == null ||
                            quantityValue <= 0
                        ) {

                            Toast.makeText(
                                context,
                                "Geçerli bir miktar gir",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        recipeIngredients.add(
                            RecipeIngredientInput(
                                name =
                                    ingredientName
                                        .trim(),
                                quantity =
                                    quantityValue,
                                unit =
                                    ingredientUnit
                            )
                        )

                        ingredientName = ""
                        ingredientQuantity = ""
                        ingredientUnit = "Adet"
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "➕ Malzemeyi Tarife Ekle"
                    )
                }

                // Eklenen malzemeler

                if (
                    recipeIngredients
                        .isNotEmpty()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Text(
                        text =
                            "Eklenen Malzemeler",
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    recipeIngredients
                        .forEachIndexed {
                                index,
                                ingredient ->

                            Card(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical =
                                                4.dp
                                        ),
                                colors =
                                    CardDefaults
                                        .cardColors(
                                            containerColor =
                                                Color(
                                                    0xFFF3F6F3
                                                )
                                        )
                            ) {

                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                12.dp
                                            ),
                                    horizontalArrangement =
                                        Arrangement
                                            .SpaceBetween
                                ) {

                                    Text(
                                        text =
                                            "${ingredient.name} • " +
                                                    "${formatRecipeQuantity(ingredient.quantity)} " +
                                                    ingredient.unit,
                                        modifier =
                                            Modifier
                                                .weight(
                                                    1f
                                                )
                                    )

                                    IconButton(
                                        onClick = {

                                            recipeIngredients
                                                .removeAt(
                                                    index
                                                )
                                        }
                                    ) {

                                        Text(
                                            "🗑️"
                                        )
                                    }
                                }
                            }
                        }
                }

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )

                // Hazırlanış

                OutlinedTextField(
                    value =
                        instructions,
                    onValueChange = {
                        instructions = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    label = {
                        Text("Hazırlanışı")
                    },
                    placeholder = {
                        Text(
                            "1. Malzemeleri hazırlayın.\n" +
                                    "2. Karıştırın.\n" +
                                    "3. Pişirin."
                        )
                    },
                    shape =
                        RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        // Fotoğraf

        Card(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color(0xFFDDE9DF)
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(18.dp)
            ) {

                Text(
                    text =
                        "📸 Tarif Fotoğrafı",
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color(0xFF2F3E34)
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedButton(
                    onClick = {

                        imagePickerLauncher
                            .launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts
                                        .PickVisualMedia
                                        .ImageOnly
                                )
                            )
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Galeriden Fotoğraf Seç"
                    )
                }

                selectedImageUri?.let {
                        uri ->

                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )

                    Image(
                        painter =
                            rememberAsyncImagePainter(
                                uri
                            ),
                        contentDescription =
                            "Seçilen tarif fotoğrafı",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale =
                            ContentScale.Crop
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        // ------------------------------------------------
        // TARİFİ KAYDET
        // ------------------------------------------------

        Button(
            onClick = {

                if (
                    recipeName.isBlank() ||
                    category ==
                    "Kategori Seç" ||
                    preparationTime
                        .isBlank() ||
                    servings.isBlank() ||
                    instructions
                        .isBlank()
                ) {

                    Toast.makeText(
                        context,
                        "Lütfen tarif bilgilerini doldur",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                if (
                    recipeIngredients
                        .isEmpty()
                ) {

                    Toast.makeText(
                        context,
                        "En az bir malzeme ekle",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                val ingredientsText =
                    recipeIngredients
                        .joinToString(
                            separator = "\n"
                        ) {
                                ingredient ->

                            "${formatRecipeQuantity(ingredient.quantity)} " +
                                    "${ingredient.unit} " +
                                    ingredient.name
                        }

                val recipe =
                    Recipe(
                        name =
                            recipeName.trim(),
                        category =
                            category,
                        preparationTime =
                            preparationTime.trim(),
                        servings =
                            servings.trim(),
                        ingredients =
                            ingredientsText,
                        instructions =
                            instructions.trim(),
                        imageUri =
                            selectedImageUri
                                ?.toString()
                    )

                scope.launch {

                    // Önce tarifi kaydet,
                    // oluşan ID'yi al.
                    val recipeId =
                        recipeDao
                            .insertRecipe(
                                recipe
                            )

                    // Sonra malzemeleri
                    // o recipeId ile kaydet.
                    val databaseIngredients =
                        recipeIngredients
                            .map {
                                    ingredient ->

                                RecipeIngredient(
                                    recipeId =
                                        recipeId.toInt(),
                                    name =
                                        ingredient.name,
                                    quantity =
                                        ingredient.quantity,
                                    unit =
                                        ingredient.unit
                                )
                            }

                    recipeIngredientDao
                        .insertRecipeIngredients(
                            databaseIngredients
                        )

                    Toast.makeText(
                        context,
                        "Tarif başarıyla eklendi ✅",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Formu temizle
                    recipeName = ""
                    category =
                        "Kategori Seç"
                    preparationTime = ""
                    servings = ""
                    instructions = ""

                    recipeIngredients.clear()

                    ingredientName = ""
                    ingredientQuantity = ""
                    ingredientUnit = "Adet"

                    selectedImageUri = null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape =
                RoundedCornerShape(18.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF4F7F65)
                )
        ) {

            Text(
                text = "Tarifi Paylaş",
                fontSize = 16.sp,
                fontWeight =
                    FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )
    }
}

private fun formatRecipeQuantity(
    quantity: Double
): String {

    return if (
        quantity % 1.0 == 0.0
    ) {

        quantity
            .toInt()
            .toString()

    } else {

        quantity.toString()
    }
}
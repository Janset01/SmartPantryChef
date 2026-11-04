package com.smartpantry.chef.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.room.withTransaction
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.RecipeIngredient
import com.smartpantry.chef.data.RecipeMedia
import kotlinx.coroutines.launch

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

    val recipeMediaDao =
        database.recipeMediaDao()

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

    val selectedImageUris =
        remember {
            mutableStateListOf<Uri>()
        }

    var selectedVideoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->

            val limitedUris =
                uris.take(
                    RecipeMedia.MAX_IMAGES
                )

            limitedUris.forEach { uri ->
                try {
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                } catch (_: Exception) {
                }
            }

            selectedImageUris.clear()
            selectedImageUris.addAll(
                limitedUris
            )

            if (
                uris.size >
                RecipeMedia.MAX_IMAGES
            ) {
                Toast.makeText(
                    context,
                    "En fazla 10 fotoğraf seçebilirsin.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    val videoPickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {
                try {
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                } catch (_: Exception) {
                }

                selectedVideoUri = uri
            }
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
                        onValueChange = { value ->
                            preparationTime =
                                value.filter { it.isDigit() }
                        },
                        modifier =
                            Modifier.weight(1f),
                        label = {
                            Text("Süre")
                        },
                        placeholder = {
                            Text("30")
                        },
                        suffix = {
                            Text("dk")
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            ),
                        singleLine = true,
                        shape =
                            RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = servings,
                        onValueChange = { value ->
                            servings =
                                value.filter { it.isDigit() }
                        },
                        modifier =
                            Modifier.weight(1f),
                        label = {
                            Text("Porsiyon")
                        },
                        placeholder = {
                            Text("4")
                        },
                        suffix = {
                            Text("kişi")
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            ),
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

                        val incomingIngredient =
                            RecipeIngredientInput(
                                name =
                                    ingredientName.trim(),
                                quantity =
                                    quantityValue,
                                unit =
                                    ingredientUnit
                            )

                        val mergedIngredients =
                            mergeRecipeIngredient(
                                current =
                                    recipeIngredients.toList(),
                                incoming =
                                    incomingIngredient
                            )

                        val didMerge =
                            mergedIngredients.size ==
                                    recipeIngredients.size

                        recipeIngredients.clear()
                        recipeIngredients.addAll(
                            mergedIngredients
                        )

                        if (didMerge) {
                            Toast.makeText(
                                context,
                                "Aynı malzeme birleştirildi ✅",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

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

        // Tarif medyası

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
                        "📸 Yapım Aşamaları ve Medya",
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color(0xFF2F3E34)
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    text =
                        "Malzemelerin toplu hali, karıştırma, pişirme veya servis gibi istediğin aşamalardan en fazla 10 fotoğraf ve 1 video ekleyebilirsin.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            arrayOf("image/*")
                        )
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Fotoğraf Seç (${selectedImageUris.size}/10)"
                    )
                }

                if (
                    selectedImageUris
                        .isNotEmpty()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(
                                    rememberScrollState()
                                ),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {

                        selectedImageUris
                            .forEachIndexed {
                                    index,
                                    uri ->

                                Column {

                                    Image(
                                        painter =
                                            rememberAsyncImagePainter(
                                                uri
                                            ),
                                        contentDescription =
                                            "Tarif fotoğrafı ${index + 1}",
                                        modifier = Modifier
                                            .width(150.dp)
                                            .height(120.dp),
                                        contentScale =
                                            ContentScale.Crop
                                    )

                                    if (index == 0) {
                                        Text(
                                            text =
                                                "Kapak fotoğrafı",
                                            fontSize = 12.sp,
                                            color =
                                                Color.Gray
                                        )
                                    }
                                }
                            }
                    }

                    TextButton(
                        onClick = {
                            selectedImageUris
                                .clear()
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Fotoğrafları Temizle"
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                OutlinedButton(
                    onClick = {
                        videoPickerLauncher.launch(
                            arrayOf("video/*")
                        )
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (
                            selectedVideoUri == null
                        ) {
                            "🎥 Video Seç (1 adet)"
                        } else {
                            "🎥 Video Seçildi ✓"
                        }
                    )
                }

                if (selectedVideoUri != null) {

                    TextButton(
                        onClick = {
                            selectedVideoUri = null
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Videoyu Kaldır"
                        )
                    }
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

                val preparationMinutes =
                    parsePositiveInt(
                        preparationTime
                    )

                if (
                    preparationMinutes == null ||
                    preparationMinutes <= 0
                ) {
                    Toast.makeText(
                        context,
                        "Hazırlama süresi 0'dan büyük olmalı",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                val servingCount =
                    parsePositiveInt(
                        servings
                    )

                if (
                    servingCount == null ||
                    servingCount <= 0
                ) {
                    Toast.makeText(
                        context,
                        "Porsiyon sayısı 0'dan büyük olmalı",
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
                            "$preparationMinutes dk",
                        servings =
                            "$servingCount kişi",
                        ingredients =
                            ingredientsText,
                        instructions =
                            instructions.trim(),
                        imageUri =
                            selectedImageUris
                                .firstOrNull()
                                ?.toString()
                    )

                scope.launch {

                    try {
                        database.withTransaction {

                            // Tarif ve malzemeleri tek işlemde kaydet.
                            // Malzeme kaydı başarısız olursa yarım tarif kalmaz.
                            val recipeId =
                                recipeDao
                                    .insertRecipe(
                                        recipe
                                    )

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

                            val databaseMedia =
                                buildList {

                                    selectedImageUris
                                        .forEachIndexed {
                                                index,
                                                uri ->

                                            add(
                                                RecipeMedia(
                                                    recipeId =
                                                        recipeId.toInt(),
                                                    mediaType =
                                                        RecipeMedia.TYPE_IMAGE,
                                                    uri =
                                                        uri.toString(),
                                                    sortOrder =
                                                        index
                                                )
                                            )
                                        }

                                    selectedVideoUri
                                        ?.let { uri ->

                                            add(
                                                RecipeMedia(
                                                    recipeId =
                                                        recipeId.toInt(),
                                                    mediaType =
                                                        RecipeMedia.TYPE_VIDEO,
                                                    uri =
                                                        uri.toString(),
                                                    sortOrder =
                                                        RecipeMedia.MAX_IMAGES
                                                )
                                            )
                                        }
                                }

                            if (
                                databaseMedia
                                    .isNotEmpty()
                            ) {
                                recipeMediaDao
                                    .insertMedia(
                                        databaseMedia
                                    )
                            }
                        }

                        Toast.makeText(
                            context,
                            "Tarif başarıyla eklendi ✅",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Formu yalnızca başarılı kayıttan sonra temizle.
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

                        selectedImageUris.clear()
                        selectedVideoUri = null

                    } catch (_: Exception) {

                        Toast.makeText(
                            context,
                            "Tarif kaydedilemedi. Tekrar dene.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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

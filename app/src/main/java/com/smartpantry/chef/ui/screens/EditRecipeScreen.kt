package com.smartpantry.chef.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import androidx.room.withTransaction
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe
import com.smartpantry.chef.data.RecipeIngredient
import com.smartpantry.chef.data.RecipeMedia
import kotlinx.coroutines.launch

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

    var recipeName by remember {
        mutableStateOf(recipe.name)
    }

    var category by remember {
        mutableStateOf(recipe.category)
    }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    var preparationTime by remember {
        mutableStateOf(
            extractPositiveIntText(
                recipe.preparationTime
            )
        )
    }

    var servings by remember {
        mutableStateOf(
            extractPositiveIntText(
                recipe.servings
            )
        )
    }

    var instructions by remember {
        mutableStateOf(recipe.instructions)
    }

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
        mutableStateListOf<RecipeIngredientInput>()
    }

    val selectedImageUris =
        remember {
            mutableStateListOf<String>()
        }

    var selectedVideoUri by remember {
        mutableStateOf<String?>(null)
    }

    var mediaLoaded by remember {
        mutableStateOf(false)
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->

            val incomingUris =
                uris.map { uri ->

                    try {
                        context.contentResolver
                            .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                    } catch (_: Exception) {
                    }

                    uri.toString()
                }

            val merged =
                mergeRecipeImageUris(
                    current =
                        selectedImageUris.toList(),
                    incoming =
                        incomingUris,
                    maxImages =
                        RecipeMedia.MAX_IMAGES
                )

            val rejectedCount =
                (selectedImageUris.size +
                        incomingUris
                            .filter {
                                it !in selectedImageUris
                            }
                            .distinct()
                            .size -
                        merged.size)
                    .coerceAtLeast(0)

            selectedImageUris.clear()
            selectedImageUris.addAll(
                merged
            )

            if (rejectedCount > 0) {
                Toast.makeText(
                    context,
                    "En fazla 10 fotoğraf ekleyebilirsin.",
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

                selectedVideoUri =
                    uri.toString()
            }
        }

    var ingredientsLoaded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(recipe.id) {

        val savedIngredients =
            recipeIngredientDao
                .getIngredientsForRecipe(recipe.id)

        recipeIngredients.clear()

        recipeIngredients.addAll(
            savedIngredients.map {
                RecipeIngredientInput(
                    name = it.name,
                    quantity = it.quantity,
                    unit = it.unit
                )
            }
        )

        val savedMedia =
            recipeMediaDao
                .getMediaForRecipe(
                    recipe.id
                )

        val savedImages =
            savedMedia
                .filter {
                    it.mediaType ==
                            RecipeMedia.TYPE_IMAGE
                }
                .map {
                    it.uri
                }

        selectedImageUris.clear()

        if (savedImages.isNotEmpty()) {
            selectedImageUris.addAll(
                savedImages.take(
                    RecipeMedia.MAX_IMAGES
                )
            )
        } else if (
            !recipe.imageUri.isNullOrBlank()
        ) {
            selectedImageUris.add(
                recipe.imageUri
            )
        }

        selectedVideoUri =
            savedMedia
                .firstOrNull {
                    it.mediaType ==
                            RecipeMedia.TYPE_VIDEO
                }
                ?.uri

        mediaLoaded = true
        ingredientsLoaded = true
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
            .padding(20.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("← Geri")
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(
            text = "✏️ Tarifi Düzenle",
            fontSize = 28.sp,
            fontWeight =
                FontWeight.Bold,
            color =
                Color(0xFF2F3E34)
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Card(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color.White
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(16.dp)
            ) {

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
                    singleLine = true,
                    shape =
                        RoundedCornerShape(16.dp)
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

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
                        singleLine = true,
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
                                value ->
                            preparationTime =
                                value.filter {
                                    it.isDigit()
                                }
                        },
                        modifier =
                            Modifier.weight(1f),
                        label = {
                            Text("Süre")
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
                        onValueChange = {
                                value ->
                            servings =
                                value.filter {
                                    it.isDigit()
                                }
                        },
                        modifier =
                            Modifier.weight(1f),
                        label = {
                            Text("Porsiyon")
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
                        Modifier.height(22.dp)
                )

                Text(
                    text =
                        "🥕 Tarif Malzemeleri",
                    fontSize = 18.sp,
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
                        "Tarifte kullanılan malzemeleri miktarlarıyla düzenle.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

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
                            singleLine = true,
                            shape =
                                RoundedCornerShape(16.dp)
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
                                    recipeIngredients
                                        .toList(),
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

                    Text("➕ Malzeme Ekle")
                }

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                if (!ingredientsLoaded) {

                    Text(
                        text =
                            "Malzemeler yükleniyor...",
                        color =
                            Color.Gray
                    )

                } else if (
                    recipeIngredients.isEmpty()
                ) {

                    Text(
                        text =
                            "Henüz malzeme eklenmemiş.",
                        color =
                            Color.Gray
                    )

                } else {

                    Text(
                        text =
                            "Tarifteki Malzemeler",
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
                                            .padding(12.dp),
                                    horizontalArrangement =
                                        Arrangement
                                            .SpaceBetween
                                ) {

                                    Text(
                                        text =
                                            "${ingredient.name} • " +
                                                    "${formatEditRecipeQuantity(ingredient.quantity)} " +
                                                    ingredient.unit,
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            recipeIngredients
                                                .removeAt(
                                                    index
                                                )
                                        }
                                    ) {
                                        Text("🗑️")
                                    }
                                }
                            }
                        }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

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
                    shape =
                        RoundedCornerShape(16.dp)
                )

                Spacer(
                    modifier =
                        Modifier.height(22.dp)
                )

                Text(
                    text =
                        "📸 Yapım Aşamaları ve Medya",
                    fontSize = 18.sp,
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
                        "Mevcut fotoğrafları silebilir, yenilerini ekleyebilir ve tarif videosunu değiştirebilirsin.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                if (!mediaLoaded) {

                    Text(
                        text =
                            "Medya yükleniyor...",
                        color =
                            Color.Gray
                    )

                } else {

                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher
                                .launch(
                                    arrayOf(
                                        "image/*"
                                    )
                                )
                        },
                        enabled =
                            selectedImageUris.size <
                                    RecipeMedia.MAX_IMAGES,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                "📷 Fotoğraf Ekle (" +
                                        selectedImageUris.size +
                                        "/10)"
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

                                    Card(
                                        modifier =
                                            Modifier.width(
                                                170.dp
                                            ),
                                        shape =
                                            RoundedCornerShape(
                                                16.dp
                                            )
                                    ) {

                                        Column {

                                            Image(
                                                painter =
                                                    rememberAsyncImagePainter(
                                                        uri
                                                    ),
                                                contentDescription =
                                                    "Tarif fotoğrafı " +
                                                            (index + 1),
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(
                                                            125.dp
                                                        ),
                                                contentScale =
                                                    ContentScale.Crop
                                            )

                                            Text(
                                                text =
                                                    if (
                                                        index == 0
                                                    ) {
                                                        "Kapak fotoğrafı"
                                                    } else {
                                                        (index + 1)
                                                            .toString() +
                                                                ". fotoğraf"
                                                    },
                                                modifier =
                                                    Modifier.padding(
                                                        horizontal =
                                                            10.dp,
                                                        vertical =
                                                            6.dp
                                                    ),
                                                fontSize =
                                                    12.sp,
                                                color =
                                                    Color.Gray
                                            )

                                            TextButton(
                                                onClick = {
                                                    selectedImageUris
                                                        .removeAt(
                                                            index
                                                        )
                                                },
                                                modifier =
                                                    Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    "🗑️ Fotoğrafı Sil"
                                                )
                                            }
                                        }
                                    }
                                }
                        }
                    } else {

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "Bu tarifte fotoğraf yok.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            videoPickerLauncher
                                .launch(
                                    arrayOf(
                                        "video/*"
                                    )
                                )
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text =
                                if (
                                    selectedVideoUri ==
                                    null
                                ) {
                                    "🎥 Video Ekle"
                                } else {
                                    "🎥 Videoyu Değiştir"
                                }
                        )
                    }

                    selectedVideoUri
                        ?.let {

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        6.dp
                                    )
                            )

                            Text(
                                text =
                                    "Video seçili ✓",
                                fontSize = 13.sp,
                                color =
                                    Color(
                                        0xFF4F7F65
                                    )
                            )

                            TextButton(
                                onClick = {
                                    selectedVideoUri =
                                        null
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    "🗑️ Videoyu Kaldır"
                                )
                            }
                        }
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Button(
            onClick = {

                if (
                    recipeName.isBlank() ||
                    category.isBlank() ||
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
                    preparationMinutes == null
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
                    servingCount == null
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
                        "Tarifte en az bir malzeme olmalı",
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

                            "${formatEditRecipeQuantity(ingredient.quantity)} " +
                                    "${ingredient.unit} " +
                                    ingredient.name
                        }

                val updatedRecipe =
                    recipe.copy(
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
                            recipeCoverImageUri(
                                selectedImageUris
                            )
                    )

                scope.launch {

                    try {
                        database.withTransaction {

                            recipeDao.updateRecipe(
                                updatedRecipe
                            )

                            recipeIngredientDao
                                .deleteIngredientsForRecipe(
                                    recipe.id
                                )

                            val updatedIngredients =
                                recipeIngredients
                                    .map {
                                            ingredient ->

                                        RecipeIngredient(
                                            recipeId =
                                                recipe.id,
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
                                    updatedIngredients
                                )

                            recipeMediaDao
                                .deleteMediaForRecipe(
                                    recipe.id
                                )

                            val updatedMedia =
                                buildList {

                                    selectedImageUris
                                        .forEachIndexed {
                                                index,
                                                uri ->

                                            add(
                                                RecipeMedia(
                                                    recipeId =
                                                        recipe.id,
                                                    mediaType =
                                                        RecipeMedia.TYPE_IMAGE,
                                                    uri =
                                                        uri,
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
                                                        recipe.id,
                                                    mediaType =
                                                        RecipeMedia.TYPE_VIDEO,
                                                    uri =
                                                        uri,
                                                    sortOrder =
                                                        RecipeMedia.MAX_IMAGES
                                                )
                                            )
                                        }
                                }

                            if (
                                updatedMedia
                                    .isNotEmpty()
                            ) {
                                recipeMediaDao
                                    .insertMedia(
                                        updatedMedia
                                    )
                            }
                        }

                        Toast.makeText(
                            context,
                            "Tarif, malzemeler ve medya güncellendi ✅",
                            Toast.LENGTH_SHORT
                        ).show()

                        onRecipeUpdated(
                            updatedRecipe
                        )

                    } catch (_: Exception) {

                        Toast.makeText(
                            context,
                            "Tarif güncellenemedi. Tekrar dene.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape =
                RoundedCornerShape(18.dp)
        ) {

            Text(
                text =
                    "💾 Değişiklikleri Kaydet",
                fontSize = 16.sp,
                fontWeight =
                    FontWeight.Bold
            )
        }

        Spacer(
            modifier =
                Modifier.height(30.dp)
        )
    }
}

private fun formatEditRecipeQuantity(
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

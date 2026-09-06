package com.smartpantry.chef.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Ingredient
import com.smartpantry.chef.ui.screens.pantry.DeleteIngredientDialog
import com.smartpantry.chef.ui.screens.pantry.EditIngredientDialog
import com.smartpantry.chef.ui.screens.pantry.IngredientCard
import com.smartpantry.chef.ui.screens.pantry.formatIngredientQuantity
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val ingredientDao = database.ingredientDao()

    var ingredients by remember {
        mutableStateOf<List<Ingredient>>(emptyList())
    }

    // Yeni malzeme formu
    var name by remember {
        mutableStateOf("")
    }

    var quantity by remember {
        mutableStateOf("")
    }

    var selectedUnit by remember {
        mutableStateOf("Adet")
    }

    var unitExpanded by remember {
        mutableStateOf(false)
    }

    var expirationDate by remember {
        mutableStateOf("")
    }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var pendingCameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // Dialoglarda kullanılacak malzemeler
    var ingredientToUse by remember {
        mutableStateOf<Ingredient?>(null)
    }

    var ingredientToEdit by remember {
        mutableStateOf<Ingredient?>(null)
    }

    var ingredientToDelete by remember {
        mutableStateOf<Ingredient?>(null)
    }

    val units = listOf(
        "Adet",
        "Gram",
        "Kilogram",
        "Mililitre",
        "Litre"
    )

    // Room'dan malzemeleri getir
    LaunchedEffect(Unit) {
        ingredients = ingredientDao.getAllIngredients()
    }

    // ------------------------------------------------
    // GALERİ
    // ------------------------------------------------

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
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

                selectedImageUri = uri
            }
        }

    // ------------------------------------------------
    // KAMERA
    // ------------------------------------------------

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->

            if (success) {
                selectedImageUri = pendingCameraUri
            } else {
                pendingCameraUri = null
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                val uri =
                    createIngredientImageUri(context)

                pendingCameraUri = uri

                cameraLauncher.launch(uri)

            } else {

                Toast.makeText(
                    context,
                    "Fotoğraf çekmek için kamera izni gerekli",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // ------------------------------------------------
    // TARİH
    // ------------------------------------------------

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->

            expirationDate =
                "%02d.%02d.%04d".format(
                    dayOfMonth,
                    month + 1,
                    year
                )
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // ------------------------------------------------
    // ANA EKRAN
    // ------------------------------------------------

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

        Text(
            text = "🧊 Buzdolabım",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Stoklarını ve kalan miktarlarını takip et",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // ------------------------------------------------
        // YENİ MALZEME KARTI
        // ------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "Yeni Malzeme",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // ------------------------------------------------
                // FOTOĞRAF ÖNİZLEME
                // ------------------------------------------------

                if (selectedImageUri != null) {

                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Malzeme fotoğrafı",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(
                                RoundedCornerShape(18.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedButton(
                        onClick = {

                            val hasPermission =
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {

                                val uri =
                                    createIngredientImageUri(
                                        context
                                    )

                                pendingCameraUri = uri

                                cameraLauncher.launch(uri)

                            } else {

                                cameraPermissionLauncher.launch(
                                    Manifest.permission.CAMERA
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "📷 Fotoğraf Çek"
                        )
                    }

                    OutlinedButton(
                        onClick = {

                            galleryLauncher.launch(
                                arrayOf("image/*")
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "🖼️ Galeriden Seç"
                        )
                    }
                }

                if (selectedImageUri != null) {

                    TextButton(
                        onClick = {
                            selectedImageUri = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = "Fotoğrafı Kaldır"
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                // ------------------------------------------------
                // MALZEME ADI
                // ------------------------------------------------

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

                // ------------------------------------------------
                // MİKTAR + BİRİM
                // ------------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newValue ->

                            quantity =
                                newValue.filter {

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
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = {
                            unitExpanded =
                                !unitExpanded
                        },
                        modifier = Modifier.weight(1f)
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
                                        expanded =
                                            unitExpanded
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

                                        selectedUnit =
                                            unit

                                        unitExpanded =
                                            false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                // ------------------------------------------------
                // SON KULLANMA TARİHİ
                // ------------------------------------------------

                OutlinedTextField(
                    value = expirationDate,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Son Kullanma Tarihi")
                    },
                    placeholder = {
                        Text("Tarih seç")
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                OutlinedButton(
                    onClick = {
                        datePickerDialog.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "📅 Tarih Seç"
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // ------------------------------------------------
                // BUZDOLABINA EKLE
                // ------------------------------------------------

                Button(
                    onClick = {

                        if (
                            name.isBlank() ||
                            quantity.isBlank() ||
                            expirationDate.isBlank()
                        ) {

                            Toast.makeText(
                                context,
                                "Lütfen gerekli alanları doldur",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        val parsedQuantity =
                            quantity
                                .replace(",", ".")
                                .toDoubleOrNull()

                        if (
                            parsedQuantity == null ||
                            parsedQuantity <= 0
                        ) {

                            Toast.makeText(
                                context,
                                "Geçerli bir miktar gir",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        val ingredient =
                            Ingredient(
                                name = name.trim(),
                                initialQuantity =
                                    parsedQuantity,
                                remainingQuantity =
                                    parsedQuantity,
                                unit = selectedUnit,
                                expirationDate =
                                    expirationDate,
                                imageUri =
                                    selectedImageUri
                                        ?.toString()
                            )

                        scope.launch {

                            ingredientDao
                                .insertIngredient(
                                    ingredient
                                )

                            ingredients =
                                ingredientDao
                                    .getAllIngredients()

                            name = ""
                            quantity = ""
                            selectedUnit = "Adet"
                            expirationDate = ""
                            selectedImageUri = null
                            pendingCameraUri = null

                            Toast.makeText(
                                context,
                                "Malzeme eklendi ✅",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "➕ Buzdolabına Ekle"
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // ------------------------------------------------
        // MALZEMELER
        // ------------------------------------------------

        Text(
            text = "📦 Malzemelerim",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        if (ingredients.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        Color(0xFFDDE9DF)
                )
            ) {

                Text(
                    text =
                        "Henüz buzdolabında malzeme yok 🥛",
                    modifier =
                        Modifier.padding(18.dp),
                    color = Color(0xFF55645A)
                )
            }

        } else {

            ingredients.forEach { ingredient ->

                IngredientCard(
                    ingredient = ingredient,

                    onUseClick = {
                        ingredientToUse =
                            ingredient
                    },

                    onEditClick = {
                        ingredientToEdit =
                            ingredient
                    },

                    onDeleteClick = {
                        ingredientToDelete =
                            ingredient
                    }
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )
    }

    // ------------------------------------------------
    // KULLANDIM DIALOG
    // ------------------------------------------------

    ingredientToUse?.let { ingredient ->

        UseIngredientDialog(
            ingredient = ingredient,

            onDismiss = {
                ingredientToUse = null
            },

            onConfirm = { amountUsed ->

                if (amountUsed <= 0) {

                    Toast.makeText(
                        context,
                        "Geçerli bir miktar gir",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@UseIngredientDialog
                }

                if (
                    amountUsed >
                    ingredient.remainingQuantity
                ) {

                    Toast.makeText(
                        context,
                        "Bu kadar stok yok",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@UseIngredientDialog
                }

                val updatedIngredient =
                    ingredient.copy(
                        remainingQuantity =
                            ingredient.remainingQuantity -
                                    amountUsed
                    )

                scope.launch {

                    ingredientDao
                        .updateIngredient(
                            updatedIngredient
                        )

                    ingredients =
                        ingredientDao
                            .getAllIngredients()

                    ingredientToUse = null

                    Toast.makeText(
                        context,
                        "Stok güncellendi ✅",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    // ------------------------------------------------
    // DÜZENLE DIALOG
    // ------------------------------------------------

    ingredientToEdit?.let { ingredient ->

        EditIngredientDialog(
            ingredient = ingredient,

            onDismiss = {
                ingredientToEdit = null
            },

            onSave = { updatedIngredient ->

                scope.launch {

                    ingredientDao
                        .updateIngredient(
                            updatedIngredient
                        )

                    ingredients =
                        ingredientDao
                            .getAllIngredients()

                    ingredientToEdit = null

                    Toast.makeText(
                        context,
                        "Malzeme güncellendi ✅",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    // ------------------------------------------------
    // SİL DIALOG
    // ------------------------------------------------

    ingredientToDelete?.let { ingredient ->

        DeleteIngredientDialog(
            ingredient = ingredient,

            onDismiss = {
                ingredientToDelete = null
            },

            onConfirm = {

                scope.launch {

                    ingredientDao
                        .deleteIngredient(
                            ingredient
                        )

                    ingredients =
                        ingredientDao
                            .getAllIngredients()

                    ingredientToDelete = null

                    if (
                        ingredientToUse?.id ==
                        ingredient.id
                    ) {
                        ingredientToUse = null
                    }

                    if (
                        ingredientToEdit?.id ==
                        ingredient.id
                    ) {
                        ingredientToEdit = null
                    }

                    Toast.makeText(
                        context,
                        "Malzeme silindi 🗑️",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

// ------------------------------------------------
// KULLANILAN MİKTAR DIALOG
// ------------------------------------------------

@Composable
private fun UseIngredientDialog(
    ingredient: Ingredient,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {

    var amount by remember(
        ingredient.id
    ) {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {

            Text(
                text = "🍴 ${ingredient.name}"
            )
        },

        text = {

            Column {

                Text(
                    text =
                        "Kalan stok: " +
                                "${
                                    formatIngredientQuantity(
                                        ingredient.remainingQuantity
                                    )
                                } " +
                                ingredient.unit
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                OutlinedTextField(
                    value = amount,

                    onValueChange = { newValue ->

                        amount =
                            newValue.filter {

                                it.isDigit() ||
                                        it == ',' ||
                                        it == '.'
                            }
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    label = {

                        Text(
                            "Ne kadar kullandın?"
                        )
                    },

                    suffix = {

                        Text(
                            ingredient.unit
                        )
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Text
                        ),

                    singleLine = true
                )
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    val amountValue =
                        amount
                            .replace(",", ".")
                            .toDoubleOrNull()

                    if (amountValue != null) {

                        onConfirm(
                            amountValue
                        )
                    }
                }
            ) {

                Text(
                    "Stoktan Düş"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    "İptal"
                )
            }
        }
    )
}

// ------------------------------------------------
// KAMERA FOTOĞRAFI URI OLUŞTUR
// ------------------------------------------------

private fun createIngredientImageUri(
    context: android.content.Context
): Uri {

    val imagesDirectory =
        File(
            context.filesDir,
            "images"
        )

    if (!imagesDirectory.exists()) {

        imagesDirectory.mkdirs()
    }

    val imageFile =
        File(
            imagesDirectory,
            "ingredient_${System.currentTimeMillis()}.jpg"
        )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
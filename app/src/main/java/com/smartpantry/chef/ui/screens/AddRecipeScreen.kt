package com.smartpantry.chef.ui.screens
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val recipeDao = database.recipeDao()

    val categoryList = listOf(
        "Kahvaltı",
        "Öğle Yemeği",
        "Akşam Yemeği",
        "Tatlı",
        "Fit",
        "Atıştırmalık"
    )

    var recipeName by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Kategori Seç") }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }

    var preparationTime by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableStateOf("") }
    var ingredients by rememberSaveable { mutableStateOf("") }
    var instructions by rememberSaveable { mutableStateOf("") }


    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F2))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Tarif Ekle 🍳",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Kendi tarifini toplulukla paylaş",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

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

                OutlinedTextField(
                    value = recipeName,
                    onValueChange = { recipeName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tarif Adı") },
                    placeholder = {
                        Text("Örn: Kremalı Tavuklu Makarna")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = {
                        categoryExpanded = !categoryExpanded
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
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = categoryExpanded
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = {
                            categoryExpanded = false
                        }
                    ) {

                        categoryList.forEach { item ->

                            DropdownMenuItem(
                                text = {
                                    Text(item)
                                },
                                onClick = {
                                    category = item
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedTextField(
                        value = preparationTime,
                        onValueChange = { preparationTime = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Süre") },
                        placeholder = { Text("30 dk") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = servings,
                        onValueChange = { servings = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Porsiyon") },
                        placeholder = { Text("4 kişi") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = ingredients,
                    onValueChange = { ingredients = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    label = { Text("Malzemeler") },
                    placeholder = {
                        Text(
                            "Örn:\n" +
                                    "2 adet yumurta\n" +
                                    "1 su bardağı süt\n" +
                                    "200 gr un"
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    label = { Text("Hazırlanışı") },
                    placeholder = {
                        Text(
                            "1. Malzemeleri hazırlayın.\n" +
                                    "2. Karıştırın.\n" +
                                    "3. Pişirin."
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFDDE9DF)
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "📸 Tarif Fotoğrafı",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F3E34)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Galeriden Fotoğraf Seç")
                }

                selectedImageUri?.let { uri ->

                    Spacer(modifier = Modifier.height(14.dp))

                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Seçilen tarif fotoğrafı",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val recipe = Recipe(
                    name = recipeName,
                    category = category,
                    preparationTime = preparationTime,
                    servings = servings,
                    ingredients = ingredients,
                    instructions = instructions,
                    imageUri = selectedImageUri?.toString()
                )

                scope.launch {
                    recipeDao.insertRecipe(recipe)

                    Toast.makeText(
                        context,
                        "Tarif başarıyla eklendi ✅",
                        Toast.LENGTH_SHORT
                    ).show()

                    recipeName = ""
                    category = "Kategori Seç"
                    preparationTime = ""
                    servings = ""
                    ingredients = ""
                    instructions = ""
                    selectedImageUri = null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F7F65)
            )
        ) {
            Text(
                text = "Tarifi Paylaş",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
package com.smartpantry.chef.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Recipe
import kotlinx.coroutines.launch

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

    var ingredients by remember {
        mutableStateOf(recipe.ingredients)
    }

    var instructions by remember {
        mutableStateOf(recipe.instructions)
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

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "✏️ Tarifi Düzenle",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(20.dp))

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
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Kategori")
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = preparationTime,
                    onValueChange = {
                        preparationTime = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Hazırlama Süresi")
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = servings,
                    onValueChange = {
                        servings = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Porsiyon")
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = ingredients,
                    onValueChange = {
                        ingredients = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    label = {
                        Text("Malzemeler")
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                val updatedRecipe = recipe.copy(
                    name = recipeName,
                    category = category,
                    preparationTime = preparationTime,
                    servings = servings,
                    ingredients = ingredients,
                    instructions = instructions
                )

                scope.launch {

                    recipeDao.updateRecipe(updatedRecipe)

                    Toast.makeText(
                        context,
                        "Tarif güncellendi ✅",
                        Toast.LENGTH_SHORT
                    ).show()

                    onRecipeUpdated(updatedRecipe)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "💾 Değişiklikleri Kaydet",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
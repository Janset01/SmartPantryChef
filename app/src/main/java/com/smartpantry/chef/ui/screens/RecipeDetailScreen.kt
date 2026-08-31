package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.data.Recipe

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit
) {

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

        Spacer(modifier = Modifier.height(18.dp))

        recipe.imageUri?.let { uri ->

            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = recipe.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = recipe.category,
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "⏱️ Süre: ${recipe.preparationTime}",
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🍽️ Porsiyon: ${recipe.servings}",
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "🥕 Malzemeler",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = recipe.ingredients,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "👩‍🍳 Hazırlanışı",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = recipe.instructions,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
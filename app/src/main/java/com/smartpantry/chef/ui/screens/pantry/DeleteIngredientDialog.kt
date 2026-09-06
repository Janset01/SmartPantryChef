package com.smartpantry.chef.ui.screens.pantry

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.smartpantry.chef.data.Ingredient

@Composable
fun DeleteIngredientDialog(
    ingredient: Ingredient,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("🗑️ Malzemeyi Sil")
        },

        text = {
            Text(
                "\"${ingredient.name}\" buzdolabından silinsin mi?\n\n" +
                        "Bu işlem geri alınamaz."
            )
        },

        confirmButton = {

            Button(
                onClick = onConfirm
            ) {
                Text("Evet, Sil")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Vazgeç")
            }
        }
    )
}
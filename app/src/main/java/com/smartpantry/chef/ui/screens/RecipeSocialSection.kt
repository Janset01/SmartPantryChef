package com.smartpantry.chef.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartpantry.chef.data.RecipeComment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun RecipeRatingSection(
    selectedRating: Int,
    averageRating: Double,
    ratingCount: Int,
    onRatingSelected: (Int) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = "Bu yemeği puanlayın",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F3E34)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text =
                    if (ratingCount > 0) {
                        String.format(
                            Locale("tr", "TR"),
                            "%.1f • %d değerlendirme",
                            averageRating,
                            ratingCount
                        )
                    } else {
                        "İlk değerlendirmeyi sen yap."
                    },
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceEvenly
            ) {

                (1..5).forEach { star ->

                    Text(
                        text =
                            if (star <= selectedRating) {
                                "★"
                            } else {
                                "☆"
                            },
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                onRatingSelected(star)
                            }
                            .padding(6.dp),
                        fontSize = 32.sp,
                        color = Color(0xFFE2A72E)
                    )
                }
            }
        }
    }
}

@Composable
internal fun RecipeCommentsSection(
    comments: List<RecipeComment>,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSubmit: () -> Unit
) {

    Column {

        Text(
            text =
                "Yorumlar (" +
                        comments.size +
                        ")",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2F3E34)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier.padding(14.dp)
            ) {

                OutlinedTextField(
                    value = commentText,
                    onValueChange =
                        onCommentTextChange,
                    modifier =
                        Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Tarif hakkında ne düşünüyorsun?"
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
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
                        Arrangement.End
                ) {

                    Button(
                        onClick = onSubmit,
                        enabled =
                            commentText
                                .trim()
                                .isNotEmpty(),
                        shape =
                            RoundedCornerShape(14.dp)
                    ) {
                        Text("Yorumu Gönder")
                    }
                }
            }
        }

        if (comments.isEmpty()) {

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text =
                    "Henüz yorum yok. İlk yorumu sen bırakabilirsin.",
                fontSize = 13.sp,
                color = Color.Gray
            )

        } else {

            Spacer(modifier = Modifier.height(14.dp))

            comments.forEach { comment ->

                RecipeCommentCard(
                    comment = comment
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )
            }
        }
    }
}

@Composable
private fun RecipeCommentCard(
    comment: RecipeComment
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                Color(0xFFFEFEFC)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment =
                Alignment.Top
        ) {

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Color(0xFFDDE9DF)
                    ),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "👤",
                    fontSize = 17.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            comment.authorNickname,
                        fontSize = 13.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color(0xFF2F3E34)
                    )

                    Spacer(
                        modifier =
                            Modifier.weight(1f)
                    )

                    Text(
                        text =
                            formatRecipeCommentTime(
                                comment.createdAt
                            ),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(5.dp)
                )

                Text(
                    text = comment.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color =
                        Color(0xFF465149)
                )
            }
        }
    }
}

private fun formatRecipeCommentTime(
    timestamp: Long
): String {

    val formatter =
        SimpleDateFormat(
            "dd MMM • HH:mm",
            Locale("tr", "TR")
        )

    return formatter.format(
        Date(timestamp)
    )
}

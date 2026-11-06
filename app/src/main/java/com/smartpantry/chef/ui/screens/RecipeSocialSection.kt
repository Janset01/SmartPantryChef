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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    onSubmit: () -> Unit,
    onReply: (Int, String) -> Unit,
    onReact: (RecipeComment, Int) -> Unit
) {
    var newestFirst by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Yorumlar (" + comments.size + ")", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2F3E34))
        Spacer(Modifier.height(10.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = commentText, onValueChange = onCommentTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tarif hakkında ne düşünüyorsun?") },
                    minLines = 2, maxLines = 4, shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onSubmit, enabled = commentText.isNotBlank()) { Text("Yorumu Gönder") }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.FilterChip(selected = newestFirst, onClick = { newestFirst = true }, label = { Text("En yeni") })
            androidx.compose.material3.FilterChip(selected = !newestFirst, onClick = { newestFirst = false }, label = { Text("En eski") })
        }
        val parents = comments.filter { it.parentCommentId == null }
            .let { if (newestFirst) it.sortedWith(compareByDescending<RecipeComment> { c -> c.createdAt }.thenByDescending { c -> c.id }) else it.sortedWith(compareBy<RecipeComment> { c -> c.createdAt }.thenBy { c -> c.id }) }
        parents.forEach { parent ->
            RecipeCommentCard(parent, onReply, onReact)
            comments.filter { it.parentCommentId == parent.id }.sortedWith(compareBy<RecipeComment> { it.createdAt }.thenBy { it.id }).forEach { reply ->
                Box(Modifier.fillMaxWidth().padding(start = 22.dp, top = 6.dp)) {
                    RecipeCommentCard(reply, onReply, onReact)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        if (comments.isEmpty()) Text("Henüz yorum yok. İlk yorumu sen bırakabilirsin.", fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
private fun RecipeCommentCard(
    comment: RecipeComment,
    onReply: (Int, String) -> Unit,
    onReact: (RecipeComment, Int) -> Unit
) {
    var replyOpen by androidx.compose.runtime.remember(comment.id) { androidx.compose.runtime.mutableStateOf(false) }
    var replyText by androidx.compose.runtime.remember(comment.id) { androidx.compose.runtime.mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFDDE9DF)), contentAlignment = Alignment.Center) {
                Text("👤", fontSize = 16.sp)
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(comment.authorNickname, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2F3E34), maxLines = 2)
                Text(formatRecipeCommentTime(comment.createdAt), fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                Text(comment.text, modifier = Modifier.fillMaxWidth(), fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF465149), softWrap = true, overflow = androidx.compose.ui.text.style.TextOverflow.Clip)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (comment.myReaction == 1) "👍 " + comment.likeCount else "👍 " + comment.likeCount,
                        modifier = Modifier.clickable { onReact(comment, 1) }, fontSize = 13.sp,
                        color = if (comment.myReaction == 1) Color(0xFF287A4B) else Color.Gray)
                    Text("👎 " + comment.dislikeCount, modifier = Modifier.clickable { onReact(comment, -1) }, fontSize = 13.sp,
                        color = if (comment.myReaction == -1) Color(0xFFB3261E) else Color.Gray)
                    Text("Yanıtla", modifier = Modifier.clickable { replyOpen = !replyOpen }, fontSize = 12.sp, color = Color(0xFF287A4B), fontWeight = FontWeight.SemiBold)
                }
                if (replyOpen) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = replyText, onValueChange = { replyText = it },
                        modifier = Modifier.fillMaxWidth(), placeholder = { Text("Yanıtını yaz...") },
                        maxLines = 4, shape = RoundedCornerShape(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { replyOpen = false }) { Text("Vazgeç") }
                        androidx.compose.material3.TextButton(onClick = {
                            if (replyText.isNotBlank()) {
                                onReply(comment.parentCommentId ?: comment.id, replyText.trim())
                                replyText = ""
                                replyOpen = false
                            }
                        }, enabled = replyText.isNotBlank()) { Text("Gönder") }
                    }
                }
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

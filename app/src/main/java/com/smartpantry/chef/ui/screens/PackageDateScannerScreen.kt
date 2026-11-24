package com.smartpantry.chef.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ScannedPantryProduct(
    val name: String,
    val quantity: String,
    val unit: String,
    val expirationDate: String
)

private val scannerOrange = Color(0xFFEA681F)
private val scannerInk = Color(0xFF38231B)

private data class DateCandidate(val value: String, val type: String)

private fun parseLabelledDate(raw: String): DateCandidate? {
    val normalized = raw.uppercase(Locale("tr", "TR"))
        .replace('İ', 'I').replace('Ş', 'S').replace('Ü', 'U')
    val pattern = Regex(
        """(SKT|S\.K\.T\.?|STT|SON\s*KULLANMA\s*TARIHI|TETT|T\.E\.T\.T\.?|TAVSIYE\s*EDILEN\s*TUKETIM\s*TARIHI)\s*[:\-]?\s*(\d{1,2})[./\-](\d{1,2})[./\-](\d{2,4})"""
    )
    for (match in pattern.findAll(normalized)) {
        val label = match.groupValues[1]
        val day = match.groupValues[2].toIntOrNull() ?: continue
        val month = match.groupValues[3].toIntOrNull() ?: continue
        val yearPart = match.groupValues[4]
        val year = if (yearPart.length == 2) 2000 + (yearPart.toIntOrNull() ?: continue)
            else yearPart.toIntOrNull() ?: continue
        val parsed = "${"%02d".format(Locale.US, day)}.${"%02d".format(Locale.US, month)}.$year"
        val df = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).apply { isLenient = false }
        if (runCatching { df.parse(parsed) }.getOrNull() == null) continue
        val type = if (label.startsWith("TETT") || label.startsWith("T.E") || label.startsWith("TAVSIYE")) "TETT" else "SKT"
        return DateCandidate(parsed, type)
    }
    return null
}

private fun parsePackageAmount(text: String): Pair<String, String>? {
    val m = Regex("""(?i)(\d+(?:[.,]\d+)?)\s*(ML|L|LT|LITRE|LİTRE|G|GR|GRAM|KG)\b""").find(text)
        ?: return null
    val original = m.groupValues[1].replace(",", ".")
    val qty = original.toDoubleOrNull() ?: return null
    return when (m.groupValues[2].uppercase(Locale("tr", "TR"))) {
        "ML" -> original to "Mililitre"
        "L", "LT", "LITRE", "LİTRE" -> original to "Litre"
        "G", "GR", "GRAM" -> original to "Gram"
        "KG" -> original to "Kilogram"
        else -> null
    }.takeIf { qty > 0 }
}

@Composable
fun PackageDateScannerScreen(
    onBack: () -> Unit,
    onApplyToPantry: (ScannedPantryProduct) -> Unit
) {
    val context = LocalContext.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    DisposableEffect(recognizer) { onDispose { recognizer.close() } }

    var name by remember { mutableStateOf("Süt") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Litre") }
    var date by remember { mutableStateOf("") }
    var dateType by remember { mutableStateOf("Belirlenemedi") }
    var lastText by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun recognize(uri: Uri) {
        busy = true
        error = ""
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    lastText = result.text
                    if (Regex("""(?i)\bS[ÜU]T\b""").containsMatchIn(result.text)) name = "Süt"
                    parsePackageAmount(result.text)?.let { (q, u) -> quantity = q; unit = u }
                    parseLabelledDate(result.text)?.let { match -> date = match.value; dateType = match.type }
                    if (result.text.isBlank()) error = "Yazı okunamadı. Daha net bir fotoğraf çek veya bilgileri elle doldur."
                    else if (date.isBlank()) error = "SKT/TETT etiketiyle birlikte tarih bulunamadı. Tarihi elle kontrol et."
                    busy = false
                }
                .addOnFailureListener {
                    error = "Fotoğraf okunamadı: ${it.localizedMessage ?: "Tekrar dene."}"
                    busy = false
                }
        } catch (e: Exception) {
            error = "Görüntü açılamadı: ${e.localizedMessage ?: "Tekrar dene."}"
            busy = false
        }
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let(::recognize) else error = "Fotoğraf çekimi iptal edildi."
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) recognize(uri)
    }

    fun launchCamera() {
        try {
            val dir = File(context.cacheDir, "scanner_photos").apply { mkdirs() }
            val file = File.createTempFile("package_", ".jpg", dir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.scannerprovider", file)
            cameraUri = uri
            takePicture.launch(uri)
        } catch (e: Exception) {
            error = "Kamera açılamadı: ${e.localizedMessage ?: "Galeriden fotoğraf seçebilirsin."}"
        }
    }

    val verifiedDate = remember(date) {
        runCatching {
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).apply { isLenient = false }
            date.isNotBlank() && formatter.parse(date) != null
        }.getOrDefault(false)
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFFFF8F2))
        .verticalScroll(rememberScrollState()).padding(18.dp)) {
        TextButton(onClick = onBack) { Text("← Buzdolabıma dön", color = scannerOrange) }
        Text("📷 Ambalajı Tara", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = scannerInk)
        Spacer(Modifier.height(8.dp))
        Text("Önce ürünün ön yüzünü, sonra SKT/TETT yazan yüzünü fotoğraflayabilirsin. Her taramada bulunan bilgiler aşağıdaki alanlara aktarılır.",
            color = scannerInk)
        Spacer(Modifier.height(16.dp))
        Button(onClick = ::launchCamera, enabled = !busy,
            modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = scannerOrange)) {
            Text("📷 Kamerayla fotoğraf çek")
        }
        OutlinedButton(onClick = { gallery.launch("image/*") }, enabled = !busy,
            modifier = Modifier.fillMaxWidth()) { Text("Galeriden fotoğraf seç") }
        if (busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = scannerOrange)
            Text("Ambalaj yazıları okunuyor…")
        }
        if (error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color(0xFF9E3F22))
        }
        Spacer(Modifier.height(16.dp))
        Text("Okunan bilgileri kontrol et", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = scannerInk)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it },
            label = { Text("Ürün adı") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = quantity, onValueChange = { quantity = it },
            label = { Text("Miktar") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Adet", "Gram", "Kilogram", "Mililitre", "Litre").forEach { item ->
                // Choice is editable without discarding recognised quantity.
                if (item == unit) {
                    TextButton(onClick = {}) { Text(item, color = scannerOrange, fontWeight = FontWeight.Bold) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Adet", "Gram", "Kilogram", "Mililitre", "Litre").forEach { item ->
                if (item != unit) TextButton(onClick = { unit = item }) { Text(item, fontSize = 10.sp) }
            }
        }
        OutlinedTextField(value = date, onValueChange = { date = it.filter { c -> c.isDigit() || c == '.' }.take(10) },
            label = { Text("Ambalajdaki tarih (GG.AA.YYYY)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text("Okunan tarih türü: $dateType", color = scannerInk)
        if (dateType == "TETT") {
            Text("TETT, son kullanma tarihiyle aynı değildir. Mevcut bildirim sistemi SKT mantığıyla çalıştığı için bu tarihi SKT alanına otomatik aktarmıyoruz.",
                color = Color(0xFFAA4B21), fontSize = 12.sp)
        } else if (dateType == "Belirlenemedi") {
            Text("Etikette SKT veya TETT okunamadı. Bir tarihi son kullanma tarihi olarak kullanmadan önce ambalajdan doğrula.",
                color = Color(0xFFAA4B21), fontSize = 12.sp)
        }
        if (lastText.isNotBlank()) {
            Text("Okunan metin (kontrol için):", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            Text(lastText.take(500), color = Color.DarkGray, fontSize = 12.sp)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            onApplyToPantry(ScannedPantryProduct(name.trim(), quantity.trim(), unit, date))
        }, enabled = !busy && name.isNotBlank() && (quantity.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0 &&
            verifiedDate && dateType == "SKT",
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = scannerOrange)) {
            Text("Bilgileri stok formuna aktar")
        }
        Text("Bu buton stoka kaydetmez. Buzdolabım ekranında bilgileri tekrar onaylayıp ‘Buzdolabına Ekle’ demen gerekir.",
            modifier = Modifier.padding(top = 10.dp), fontSize = 12.sp, color = Color.Gray)
    }
}

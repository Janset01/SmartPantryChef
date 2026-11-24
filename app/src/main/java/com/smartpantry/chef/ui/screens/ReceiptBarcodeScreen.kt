package com.smartpantry.chef.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Ingredient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private val scanOrange = Color(0xFFEA681F)
private val scanCream = Color(0xFFFFF8F2)
private val scanInk = Color(0xFF38231B)

private data class ReceiptRow(
    val id: Int, val name: String, val amount: String = "1",
    val unit: String = "Adet", val date: String = "", val selected: Boolean = true
)

private fun validCalendarDate(date: String): Boolean = runCatching {
    val parser = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).apply { isLenient = false }
    parser.parse(date)?.let { parser.format(it) == date } ?: false
}.getOrDefault(false)

// Receipt OCR is unstructured; NEVER silently interpret price lines as verified stock.
private fun receiptCandidates(raw: String): List<ReceiptRow> {
    val ignored = Regex("""(?i)\b(KDV|TOPLAM|TUTAR|NAKIT|NAKİT|POS|KART|VERGI|VERGİ|FIS|FİŞ|TARIH|TARİH|SAAT|PARA|ODEME|ÖDEME|INDIRIM|İNDİRİM|KASİYER|KASIYER|BARKOD|MARKET|TESEKKUR|TEŞEKKÜR)\b""")
    return raw.lineSequence().map { it.trim() }
        .filter { line -> line.length in 3..65 && line.any(Char::isLetter) && !ignored.containsMatchIn(line) }
        .filter { it.count(Char::isDigit) < it.length / 2 }
        .distinct().take(30).mapIndexed { i, line ->
            val name = line.replace(Regex("""\s+\d+[.,]\d{2}\s*(?:TL|₺)?\s*$"""), "")
                .replace(Regex("""\s+"""), " ").trim()
            ReceiptRow(i, name)
        }.filter { it.name.isNotEmpty() }
}

@Composable
fun ReceiptBarcodeScreen(
    barcodeMode: Boolean,
    onBack: () -> Unit,
    onBarcodeProduct: (ScannedPantryProduct) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember(context) { AppDatabase.getDatabase(context) }
    val textReader = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val barcodeReader = remember { BarcodeScanning.getClient() }
    DisposableEffect(Unit) {
        onDispose { textReader.close(); barcodeReader.close() }
    }
    var isReading by remember { mutableStateOf(false) }
    var showLiveCamera by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var scannedText by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf<List<ReceiptRow>>(emptyList()) }
    var barcode by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("Adet") }
    var expiration by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf("") }

    fun scan(uri: Uri) {
        isReading = true
        error = ""
        try {
            val image = InputImage.fromFilePath(context, uri)
            if (barcodeMode) {
                barcodeReader.process(image).addOnSuccessListener { codes ->
                    barcode = codes.firstOrNull()?.rawValue.orEmpty()
                    if (barcode.isEmpty()) error = "Barkod bulunamadı. Net bir fotoğraf seç veya tekrar dene."
                    isReading = false
                }.addOnFailureListener {
                    error = "Barkod okunamadı: ${it.localizedMessage ?: "Tekrar dene."}"
                    isReading = false
                }
            } else {
                textReader.process(image).addOnSuccessListener { result ->
                    scannedText = result.text
                    rows = receiptCandidates(result.text)
                    if (rows.isEmpty()) error = "Ürün adları ayırt edilemedi. Fotoğrafı yeniden çekebilirsin."
                    isReading = false
                }.addOnFailureListener {
                    error = "Fiş okunamadı: ${it.localizedMessage ?: "Tekrar dene."}"
                    isReading = false
                }
            }
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Görüntü açılamadı."
            isReading = false
        }
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) showLiveCamera = true
        else error = "Kamera izni verilmedi. Galeriden fotoğraf seçebilirsin."
    }
    fun openLiveCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            showLiveCamera = true
        else cameraPermission.launch(Manifest.permission.CAMERA)
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scan(uri)
    }
    // The Android camera app captures at full resolution using the same private FileProvider.
    var pictureUri by remember { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) pictureUri?.let(::scan) else error = "Fotoğraf çekimi iptal edildi."
    }
    fun takePhoto() {
        runCatching {
            val dir = java.io.File(context.cacheDir, "scanner_photos").apply { mkdirs() }
            val photo = java.io.File.createTempFile("scan_", ".jpg", dir)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.scannerprovider", photo)
            pictureUri = uri
            camera.launch(uri)
        }.onFailure { error = "Kamera açılamadı: ${it.localizedMessage ?: "Tekrar dene."}" }
    }
    if (showLiveCamera) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            PantryLiveCamera(Modifier.fillMaxSize(),
                onPhoto = { uri -> showLiveCamera = false; scan(uri) },
                onError = { error = it; showLiveCamera = false })
            TextButton(onClick = { showLiveCamera = false }, modifier = Modifier.align(Alignment.TopStart)) {
                Text("✕ Kapat", color = Color.White)
            }
            Text(if (barcodeMode) "Barkodu çerçeve içine yerleştir" else "Fişi net biçimde kadraja al",
                color = Color.White, modifier = Modifier.align(Alignment.TopCenter).padding(top = 65.dp))
        }
    } else Column(Modifier.fillMaxSize().background(scanCream).padding(horizontal = 16.dp)) {
        TextButton(onClick = onBack) { Text("‹ Buzdolabıma dön", color = scanOrange) }
        Text(if (barcodeMode) "Barkod Okut" else "Fiş Tara",
            fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = scanInk)
        Text(if (barcodeMode)
            "Barkodu fotoğraflayarak numarasını oku. Ürün adı, miktar ve tarih bilgilerini doğrula."
        else "Fişindeki satırları okuyalım. Ürünleri tek tek kontrol edip stoklara topluca ekle.",
            color = Color(0xFF80695C), fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = ::openLiveCamera, enabled = !isReading, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = scanOrange)) { Text("📷 Fotoğraf çek") }
            OutlinedButton(onClick = { gallery.launch("image/*") }, enabled = !isReading,
                modifier = Modifier.weight(1f)) { Text("Galeriden seç") }
        }
        if (isReading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = scanOrange)
        if (error.isNotEmpty()) Text(error, color = Color(0xFFB74726), fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        if (barcodeMode) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (barcode.isNotBlank()) {
                    Text("Okunan barkod: $barcode", fontWeight = FontWeight.Bold)
                    Text("Barkod numarası tek başına ürün adını veya SKT'yi doğrulamaz.",
                        fontSize = 12.sp, color = Color(0xFF80695C))
                }
                OutlinedTextField(productName, { productName = it }, label = { Text("Ürün adı") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amount, { amount = it }, label = { Text("Miktar") }, modifier = Modifier.fillMaxWidth())
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("Adet", "Litre", "Mililitre", "Gram", "Kilogram").forEach { u ->
                        FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u) })
                        Spacer(Modifier.width(5.dp))
                    }
                }
                OutlinedTextField(expiration, { expiration = it }, label = { Text("Ambalajdaki SKT (GG.AA.YYYY)") },
                    modifier = Modifier.fillMaxWidth())
                Text("TETT ile SKT farklıdır. Tarihi ambalajdan doğrulayın.", fontSize = 12.sp)
                Button(onClick = { onBarcodeProduct(ScannedPantryProduct(productName.trim(), amount, unit, expiration)) },
                    enabled = barcode.isNotEmpty() && productName.isNotBlank() &&
                        (amount.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0 && validCalendarDate(expiration),
                    modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = scanOrange)) {
                    Text("Bilgileri stok formuna aktar")
                }
            }
        } else {
            if (scannedText.isNotBlank()) {
                Text("${rows.size} olası ürün satırı bulundu. Fişteki fiyatlar ve ürün adları yanlış okunabilir.",
                    fontSize = 12.sp, color = scanInk)
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(rows, key = { _, row -> row.id }) { index, row ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(15.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Row {
                                Checkbox(row.selected, onCheckedChange = { v ->
                                    rows = rows.map { if (it.id == row.id) it.copy(selected = v) else it }
                                })
                                OutlinedTextField(row.name, onValueChange = { v ->
                                    rows = rows.map { if (it.id == row.id) it.copy(name = v) else it }
                                }, label = { Text("Ürün adı") }, modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(row.amount, onValueChange = { v ->
                                    rows = rows.map { if (it.id == row.id) it.copy(amount = v) else it }
                                }, label = { Text("Miktar") }, modifier = Modifier.weight(1f))
                                var expanded by remember { mutableStateOf(false) }
                                Box(Modifier.weight(1f)) {
                                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(row.unit) }
                                    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                                        listOf("Adet","Litre","Mililitre","Gram","Kilogram").forEach { u ->
                                            DropdownMenuItem(text = { Text(u) }, onClick = {
                                                rows = rows.map { if (it.id == row.id) it.copy(unit = u) else it }
                                                expanded = false
                                            })
                                        }
                                    }
                                }
                            }
                            OutlinedTextField(row.date, onValueChange = { v ->
                                rows = rows.map { if (it.id == row.id) it.copy(date = v) else it }
                            }, label = { Text("Ambalaj SKT'si • GG.AA.YYYY") },
                                modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                item {
                    if (rows.isNotEmpty()) {
                        Text("Fiş son kullanma tarihlerini içermez. Tarihi doğrulamadığın ürünleri kaydetme.",
                            fontSize = 12.sp, color = Color(0xFF916332))
                        if (success.isNotBlank()) Text(success, color = Color(0xFF32804A))
                    }
                }
            }
            val chosen = rows.filter { it.selected }
            val allValid = chosen.isNotEmpty() && chosen.all {
                it.name.isNotBlank() && (it.amount.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0 &&
                    validCalendarDate(it.date)
            }
            Button(onClick = {
                saving = true
                scope.launch {
                    try {
                        chosen.forEach { row ->
                            val amountValue = row.amount.replace(",", ".").toDouble()
                            db.ingredientDao().insertIngredient(
                                Ingredient(name = row.name.trim(), initialQuantity = amountValue,
                                    remainingQuantity = amountValue, unit = row.unit,
                                    expirationDate = row.date)
                            )
                        }
                        success = "${chosen.size} ürün kaydedildi. Buzdolabım'a dönerek kontrol edebilirsin."
                        rows = rows.filterNot { it.selected }
                    } catch (e: Exception) { error = "Kayıt hatası: ${e.localizedMessage}" }
                    saving = false
                }
            }, enabled = !saving && allValid, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = scanOrange)) {
                Text("Onaylanan ürünleri stoka ekle")
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

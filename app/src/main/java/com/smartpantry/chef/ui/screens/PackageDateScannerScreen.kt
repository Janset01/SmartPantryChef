package com.smartpantry.chef.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.smartpantry.chef.data.AppDatabase
import com.smartpantry.chef.data.Ingredient
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
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
    val scope = rememberCoroutineScope()
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    DisposableEffect(recognizer) { onDispose { recognizer.close() } }
    val db = remember(context) { AppDatabase.getDatabase(context) }
    var stage by remember { mutableIntStateOf(0) }
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Litre") }
    var date by remember { mutableStateOf("") }
    var dateType by remember { mutableStateOf("Belirlenemedi") }
    var readText by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    fun recognize(photo: Uri) {
        working = true
        error = ""
        if (imageUri == null) imageUri = photo
        try {
            recognizer.process(InputImage.fromFilePath(context, photo))
                .addOnSuccessListener { value ->
                    readText = value.text
                    if (Regex("""(?i)\bS[ÜU]T\b""").containsMatchIn(value.text)) productName = "Süt"
                    parsePackageAmount(value.text)?.let { (q, u) -> quantity = q; unit = u }
                    parseLabelledDate(value.text)?.let { (value, type) -> date = value; dateType = type }
                    stage = 2
                    if (value.text.isBlank()) error = "Yazılar net okunamadı; bilgileri elle tamamlayabilirsin."
                    working = false
                }
                .addOnFailureListener { e ->
                    error = "Fotoğraf okunamadı: ${e.localizedMessage ?: "Tekrar dene."}"
                    working = false
                    stage = 2
                }
        } catch (e: Exception) {
            error = "Fotoğraf açılamadı: ${e.localizedMessage ?: "Tekrar dene."}"
            working = false
            stage = 2
        }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) recognize(it)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) stage = 1 else error = "Kamera izni verilmedi. Galeriden fotoğraf seçebilirsin."
    }
    fun openCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            stage = 1
        else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val parsedDate = remember(date) {
        runCatching {
            val df = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).apply { isLenient = false }
            df.parse(date)?.let { df.format(it) == date } ?: false
        }.getOrDefault(false)
    }
    val category = if (Regex("""(?i)s[üu]t|yoğurt|peynir|ayran|tereyağ|krema""").containsMatchIn(productName)) "Süt Ürünleri" else "Otomatik kategori"
    val complete = productName.isNotBlank() &&
        (quantity.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0 && parsedDate && dateType == "SKT"

    Column(Modifier.fillMaxSize().background(Color(0xFFFFF8F2))) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { if (stage == 0) onBack() else stage = 0 }) { Text("‹", fontSize = 25.sp, color = scannerInk) }
            Text(when(stage) { 0 -> "Ürün Tara"; 1 -> "Ambalajdaki Tarihi Çek"; else -> "Taranan Bilgiler" },
                fontSize = 20.sp, color = scannerInk, fontWeight = FontWeight.ExtraBold)
        }
        when (stage) {
            0 -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(36.dp))
                Text("📷", fontSize = 54.sp)
                Spacer(Modifier.height(8.dp))
                Text("Ambalajı Tara", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = scannerInk)
                Text("Ürünün adını, miktarını ve SKT/TETT bilgisini okuyalım.",
                    color = Color(0xFF80695C), modifier = Modifier.padding(12.dp))
                Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFE1CD)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🥛", fontSize = 90.sp)
                        Text("Ön yüz + tarih etiketi", fontWeight = FontWeight.Bold, color = scannerInk)
                    }
                }
                Spacer(Modifier.height(20.dp))
                listOf("📅 Tarihin bulunduğu alanı net çek.", "☀️ Yeterli ışık kullan.", "▣ Yazıların bulanık olmamasını sağla.")
                    .forEach { Text(it, Modifier.fillMaxWidth().padding(vertical = 7.dp), color = scannerInk) }
                Spacer(Modifier.height(14.dp))
                Button(onClick = ::openCamera, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = scannerOrange)) {
                    Text("📷 Kamerayı Aç")
                }
                OutlinedButton(onClick = { gallery.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Galeriden Fotoğraf Seç")
                }
                if (error.isNotBlank()) Text(error, color = Color(0xFFA23A19))
            }
            1 -> Box(Modifier.fillMaxSize().background(Color.Black)) {
                PantryLiveCamera(
                    modifier = Modifier.fillMaxSize(),
                    onPhoto = { recognize(it) },
                    onError = { error = it; stage = 0 }
                )
                Text("SKT / TETT yazan alanı çerçevenin ortasına getir",
                    modifier = Modifier.align(Alignment.TopCenter).padding(20.dp),
                    color = Color.White, fontSize = 15.sp)
            }
            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)) {
                if (working) LinearProgressIndicator(Modifier.fillMaxWidth(), color = scannerOrange)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(102.dp).clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFE1CD)), contentAlignment = Alignment.Center) {
                        if (imageUri != null) Image(rememberAsyncImagePainter(imageUri), contentDescription = "Çekilen ambalaj",
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Text("🥛", fontSize = 48.sp)
                    }
                    Spacer(Modifier.width(13.dp))
                    Column {
                        Text(if (readText.isNotBlank()) "✓ Yazılar okundu" else "Bilgileri kontrol et",
                            fontWeight = FontWeight.Bold, color = scannerInk)
                        Text("Önerilen bilgiler kesin değildir. Kaydetmeden önce doğrula.",
                            fontSize = 12.sp, color = Color(0xFF80695C))
                    }
                }
                if (error.isNotBlank()) Text(error, color = Color(0xFFAC3E1F))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(productName, { productName = it }, label = { Text("Ürün adı") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(quantity, { quantity = it }, label = { Text("Miktar") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    listOf("Litre","Mililitre","Adet","Gram","Kilogram").forEach { u ->
                        FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u) })
                        Spacer(Modifier.width(5.dp))
                    }
                }
                OutlinedTextField(date, { date = it }, label = { Text("Ambalajdaki tarih • GG.AA.YYYY") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Tarih türü: $dateType", fontSize = 13.sp, color = scannerInk)
                if (dateType != "SKT") Text("SKT okunamadı veya tarih TETT. Yanlış son kullanma bildirimi oluşturmamak için kaydetmeden önce SKT'yi okut.",
                    color = Color(0xFFAC3E1F), fontSize = 12.sp)
                Text("Kategori: $category", fontWeight = FontWeight.SemiBold, color = scannerInk)
                OutlinedButton(onClick = ::openCamera, modifier = Modifier.fillMaxWidth()) { Text("📷 Diğer yüzü / SKT'yi yeniden tara") }
                OutlinedButton(onClick = { gallery.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Galeriden başka fotoğraf seç") }
                if (readText.isNotBlank()) {
                    Text("Okunan metin: ${readText.take(250)}", fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    working = true
                    scope.launch {
                        try {
                            val stableImage = imageUri?.let { photo ->
                                runCatching {
                                    val source = context.contentResolver.openInputStream(photo)
                                    val storage = File(context.filesDir, "pantry_photos").apply { mkdirs() }
                                    val destination = File(storage, "product_${System.currentTimeMillis()}.jpg")
                                    source?.use { input -> destination.outputStream().use { input.copyTo(it) } }
                                    if (destination.length() > 0) Uri.fromFile(destination).toString() else null
                                }.getOrNull()
                            }
                            val amount = quantity.replace(",", ".").toDouble()
                            db.ingredientDao().insertIngredient(Ingredient(
                                name = productName.trim(), initialQuantity = amount,
                                remainingQuantity = amount, unit = unit,
                                expirationDate = date, imageUri = stableImage
                            ))
                            saved = true
                            onBack()
                        } catch (e: Exception) {
                            error = "Stok kaydedilemedi: ${e.localizedMessage ?: "Tekrar dene."}"
                        }
                        working = false
                    }
                }, enabled = !working && complete, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = scannerOrange)) {
                    Text("✓ Stoka Ekle")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
internal fun PantryLiveCamera(
    modifier: Modifier,
    onPhoto: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val preview = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    DisposableEffect(context, lifecycle) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            runCatching {
                val provider = providerFuture.get()
                val useCase = Preview.Builder().build().apply {
                    setSurfaceProvider(preview.surfaceProvider)
                }
                provider.unbindAll()
                provider.bindToLifecycle(lifecycle, CameraSelector.DEFAULT_BACK_CAMERA,
                    useCase, imageCapture)
            }.onFailure { onError("Kamera önizlemesi açılamadı: ${it.localizedMessage ?: "Tekrar dene."}") }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            if (providerFuture.isDone) runCatching { providerFuture.get().unbindAll() }
        }
    }
    Box(modifier) {
        AndroidView(factory = { preview }, modifier = Modifier.fillMaxSize())
        Box(Modifier.align(Alignment.Center).fillMaxWidth(.78f).height(115.dp)
            .background(Color.Transparent).border(2.dp, Color.White, RoundedCornerShape(16.dp)))
        Button(onClick = {
            val outputDir = File(context.cacheDir, "scanner_photos").apply { mkdirs() }
            val photoFile = File.createTempFile("package_", ".jpg", outputDir)
            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.scannerprovider", photoFile)
            imageCapture.takePicture(ImageCapture.OutputFileOptions.Builder(photoFile).build(),
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                        onPhoto(fileUri)
                    }
                    override fun onError(exception: ImageCaptureException) {
                        onError("Fotoğraf çekilemedi: ${exception.localizedMessage}")
                    }
                })
        }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp)
            .size(84.dp), shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
            Text("●", fontSize = 42.sp, color = scannerOrange)
        }
    }
}

package com.smartpantry.chef.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.smartpantry.chef.MainActivity
import com.smartpantry.chef.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val EXPIRATION_CHANNEL_ID = "ingredient_expiration_alerts"
private const val PERIODIC_WORK_NAME = "ingredient_expiration_periodic_check"
private const val IMMEDIATE_WORK_NAME = "ingredient_expiration_immediate_check"
private const val PREFS_NAME = "ingredient_expiration_notifications"

class ExpirationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            processIngredientExpirations(applicationContext)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

fun scheduleExpirationChecks(context: Context) {
    val periodicRequest =
        PeriodicWorkRequestBuilder<ExpirationWorker>(
            24,
            TimeUnit.HOURS
        ).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        PERIODIC_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicRequest
    )

    val immediateRequest =
        OneTimeWorkRequestBuilder<ExpirationWorker>().build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        IMMEDIATE_WORK_NAME,
        androidx.work.ExistingWorkPolicy.REPLACE,
        immediateRequest
    )
}

suspend fun processIngredientExpirations(
    context: Context
): List<String> {

    createExpirationNotificationChannel(context)

    val ingredientDao =
        AppDatabase.getDatabase(context).ingredientDao()

    val ingredients =
        ingredientDao.getAllIngredients()

    val removedNames =
        mutableListOf<String>()

    val formatter =
        SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).apply {
            isLenient = false
        }

    val today =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    ingredients.forEach { ingredient ->

        val expiration =
            try {
                formatter.parse(ingredient.expirationDate)
            } catch (_: Exception) {
                null
            } ?: return@forEach

        val expirationCalendar =
            Calendar.getInstance().apply {
                time = expiration
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        val daysLeft =
            TimeUnit.MILLISECONDS.toDays(
                expirationCalendar.timeInMillis -
                        today.timeInMillis
            )

        when {
            daysLeft < 0 -> {
                ingredientDao.deleteIngredient(ingredient)

                removedNames.add(ingredient.name)

                showExpirationNotificationOnce(
                    context = context,
                    ingredient = ingredient,
                    eventKey = "expired",
                    title = "🗑️ Süresi doldu",
                    message =
                        "${ingredient.name} süresi dolmuştur ve buzdolabından çıkarılmıştır."
                )
            }

            daysLeft == 5L -> {
                showExpirationNotificationOnce(
                    context = context,
                    ingredient = ingredient,
                    eventKey = "5days",
                    title = "⚠️ Son kullanma tarihi yaklaşıyor",
                    message =
                        "${ingredient.name} son kullanma tarihine 5 gün kaldı."
                )
            }

            daysLeft == 2L -> {
                showExpirationNotificationOnce(
                    context = context,
                    ingredient = ingredient,
                    eventKey = "2days",
                    title = "🟠 Yakında tüket",
                    message =
                        "${ingredient.name} son kullanma tarihine 2 gün kaldı."
                )
            }

            daysLeft == 1L -> {
                showExpirationNotificationOnce(
                    context = context,
                    ingredient = ingredient,
                    eventKey = "1day",
                    title = "🔴 Yarın süresi doluyor",
                    message =
                        "${ingredient.name} süresi yarın doluyor."
                )
            }

            daysLeft == 0L -> {
                showExpirationNotificationOnce(
                    context = context,
                    ingredient = ingredient,
                    eventKey = "today",
                    title = "🔴 Bugün tüket",
                    message =
                        "${ingredient.name} bugün tüketilmeli."
                )
            }
        }
    }

    return removedNames
}

private fun showExpirationNotificationOnce(
    context: Context,
    ingredient: Ingredient,
    eventKey: String,
    title: String,
    message: String
) {

    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val prefs =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    val uniqueKey =
        "${ingredient.id}_${ingredient.expirationDate}_$eventKey"

    if (prefs.getBoolean(uniqueKey, false)) {
        return
    }

    val openAppIntent =
        Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    val pendingIntent =
        PendingIntent.getActivity(
            context,
            ingredient.id,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

    val notification =
        NotificationCompat.Builder(
            context,
            EXPIRATION_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

    NotificationManagerCompat
        .from(context)
        .notify(
            ingredient.id + eventKey.hashCode(),
            notification
        )

    prefs.edit()
        .putBoolean(uniqueKey, true)
        .apply()
}

private fun createExpirationNotificationChannel(
    context: Context
) {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }

    val channel =
        NotificationChannel(
            EXPIRATION_CHANNEL_ID,
            "Son Kullanma Tarihi Uyarıları",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description =
                "Buzdolabındaki malzemelerin son kullanma tarihi uyarıları"
        }

    val manager =
        context.getSystemService(
            NotificationManager::class.java
        )

    manager.createNotificationChannel(channel)
}

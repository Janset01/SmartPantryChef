package com.smartpantry.chef.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ScannedFoodValidatorTest {
    @Test fun acceptsKnownFoodsAndNuts() {
        listOf("Süt", "1 L Tam Yağlı Süt", "Yumurta", "Makarna 500 g", "Domates",
            "Ceviz", "Kuruyemiş", "Çekirdek", "Zeytinyağı", "Kırmızı Mercimek", "Tuz")
            .forEach { assertEquals(it, ScannedFoodStatus.FOOD, ScannedFoodValidator.classify(it)) }
    }

    @Test fun rejectsCleaningAndPersonalCareEvenIfTheyMentionFood() {
        listOf("Deterjan", "Çamaşır Suyu", "Bulaşık Tableti", "Şampuan",
            "Süt Kokulu Duş Jeli", "Limonlu Yüzey Temizleyici", "Kağıt Havlu",
            "Peçete", "Kedi Maması", "Diş Macunu", "Sabun")
            .forEach { assertEquals(it, ScannedFoodStatus.NOT_FOOD, ScannedFoodValidator.classify(it)) }
    }

    @Test fun unknownReceiptsRequireConfirmation() {
        listOf("MARKA XYZ", "KAMPANYALI URUN", "BILINMEYEN AROMALI", "ABC123")
            .forEach { assertEquals(it, ScannedFoodStatus.REVIEW, ScannedFoodValidator.classify(it)) }
    }
}

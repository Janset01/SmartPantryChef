package com.smartpantry.chef.data

import java.util.Locale

/**
 * Conservative validation for scanned supermarket product names.
 * Never mistake the pantry classifier's OTHER fallback for food evidence.
 * A user may explicitly confirm an unknown product, but known non-food terms
 * cannot be added through either scanning flow.
 */
enum class ScannedFoodStatus { FOOD, NOT_FOOD, REVIEW }

object ScannedFoodValidator {
    private fun normalize(value: String): String =
        value.lowercase(Locale.forLanguageTag("tr-TR"))
            .replace("ı", "i").replace("ş", "s").replace("ğ", "g")
            .replace("ü", "u").replace("ö", "o").replace("ç", "c")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ").trim()

    private val nonFoodTerms = setOf(
        "deterjan", "camasir deterjani", "bulasik deterjani", "bulasik tableti",
        "bulasik makinesi tableti", "parlatici", "yumusatici", "camasir suyu",
        "temizlik", "yuzey temizleyici", "cam sil", "kirec sokucu",
        "sabun", "sivi sabun", "sampuan", "shampoo", "sac kremi",
        "dus jeli", "dis macunu", "dis fircasi", "deodorant", "parfum",
        "kozmetik", "makyaj", "krem temizleyici", "el kremi",
        "tuvalet kagidi", "kagit havlu", "pecete", "islak mendil",
        "bebek bezi", "bebek mendili", "hijyenik ped", "tampon",
        "cop torbasi", "buzdolabi poseti", "strech film", "strec film",
        "aliminyum folyo", "aluminyum folyo", "bulasik sungeri",
        "sunger", "firca", "pas pas", "mop", "pil", "batarya",
        "ampul", "camasir", "pet mamasi", "kedi mamasi", "kopek mamasi",
        "kopek odulu", "kedi kumu", "cicek topragi", "gubre",
        "yag cozucu", "lavabo acici", "bleach", "detergent", "cleaner",
        "soap", "toothpaste", "paper towel", "tissue", "shower gel",
        "sac boyasi", "tirnak cilasi", "oje", "antibakteriyel jel"
    ).map(::normalize)

    private val recognizedExtraFoods = setOf(
        "cekirdek", "ay cekirdegi", "kabak cekirdegi", "kuruyemis",
        "kuruyemis karisimi", "findik ici", "ceviz ici", "leblebi",
        "yer fistigi", "tuzlu fistık", "cig badem", "kavrulmus badem"
    ).map(::normalize)

    private fun containsPhrase(text: String, phrase: String): Boolean =
        (" $text ").contains(" $phrase ")

    fun classify(name: String): ScannedFoodStatus {
        val normalized = normalize(name)
        if (normalized.isBlank()) return ScannedFoodStatus.REVIEW
        if (nonFoodTerms.any { containsPhrase(normalized, it) }) return ScannedFoodStatus.NOT_FOOD
        if (recognizedExtraFoods.any { containsPhrase(normalized, it) } ||
            IngredientCategoryClassifier.containsKnownFoodName(name)) return ScannedFoodStatus.FOOD
        return ScannedFoodStatus.REVIEW
    }
}

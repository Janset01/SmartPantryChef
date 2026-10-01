package com.smartpantry.chef.data

import java.util.Locale

enum class PantryMainCategory(val title: String, val emoji: String, val order: Int) {
    VEGETABLES("Sebze & Yeşillik", "🥬", 1),
    FRUITS("Meyveler", "🍎", 2),
    DAIRY_BREAKFAST("Süt & Kahvaltılık", "🥛", 3),
    RED_MEAT("Kırmızı Et", "🥩", 4),
    POULTRY("Tavuk & Hindi", "🍗", 5),
    FISH_SEAFOOD("Balık & Deniz Ürünleri", "🐟", 6),
    DELI("Şarküteri", "🥓", 7),
    GRAINS_LEGUMES("Tahıl & Bakliyat", "🌾", 8),
    BAKERY_FLOUR("Ekmek & Unlu Mamuller", "🥖", 9),
    CANNED_DRY("Kuru & Konserve", "🥫", 10),
    SPICES_SAUCES_OILS("Baharat / Sos / Yağ", "🧂", 11),
    BEVERAGES("İçecekler", "🥤", 12),
    FROZEN("Dondurulmuş", "❄️", 13),
    SNACKS_DESSERTS("Atıştırmalık & Tatlı", "🍫", 14),
    READY_MEALS("Hazır Gıda", "🍱", 15),
    OTHER("Diğer", "📦", 99);
}

data class PantryCategoryResult(val mainCategory: PantryMainCategory, val subCategory: String)

object IngredientCategoryClassifier {
    private data class Entry(val mainCategory: PantryMainCategory, val subCategory: String)
    private val categoryMap: Map<String, Entry> by lazy {
        buildMap {
            fun add(main: PantryMainCategory, sub: String, vararg names: String) {
                names.forEach { put(normalize(it), Entry(main, sub)) }
            }
            add(PantryMainCategory.VEGETABLES, "Soğangiller", "sarımsak", "kuru soğan", "kırmızı soğan", "arpacık soğan", "taze soğan", "pırasa")
            add(PantryMainCategory.VEGETABLES, "Kök Sebze", "patates", "tatlı patates", "havuç", "pancar", "kırmızı turp", "beyaz turp", "kereviz kökü", "yer elması", "şalgam")
            add(PantryMainCategory.VEGETABLES, "Meyve Sebze", "domates", "çeri domates", "salatalık", "patlıcan", "kabak", "sakız kabak", "bamya", "dolmalık biber", "sivri biber", "kapya biber", "çarliston biber", "acı biber", "jalapeno")
            add(PantryMainCategory.VEGETABLES, "Yapraklı", "marul", "kıvırcık", "göbek marul", "roka", "ıspanak", "pazı", "semizotu", "kara lahana", "maydanoz", "dereotu", "nane", "fesleğen", "tere", "kişniş")
            add(PantryMainCategory.VEGETABLES, "Lahana Grubu", "brokoli", "karnabahar", "beyaz lahana", "kırmızı lahana", "brüksel lahanası")
            add(PantryMainCategory.VEGETABLES, "Diğer Sebze", "enginar", "kuşkonmaz", "rezene", "taze fasulye", "bezelye", "mısır", "mısır koçanı", "mantar", "kestane mantarı", "istiridye mantarı")
            add(PantryMainCategory.FRUITS, "Çekirdekli", "elma", "yeşil elma", "kırmızı elma", "armut", "ayva")
            add(PantryMainCategory.FRUITS, "Narenciye", "portakal", "mandalina", "limon", "lime", "misket limonu", "greyfurt")
            add(PantryMainCategory.FRUITS, "Tropikal", "muz", "ananas", "mango", "kivi", "papaya", "hindistan cevizi", "avokado", "passion fruit", "çarkıfelek meyvesi")
            add(PantryMainCategory.FRUITS, "Yumuşak Meyve", "çilek", "ahududu", "böğürtlen", "yaban mersini", "frenk üzümü")
            add(PantryMainCategory.FRUITS, "Sert Çekirdekli", "şeftali", "nektarin", "kayısı", "erik", "kiraz", "vişne")
            add(PantryMainCategory.FRUITS, "Diğer Meyve", "üzüm", "nar", "incir", "kavun", "karpuz", "hurma", "trabzon hurması")
            add(PantryMainCategory.DAIRY_BREAKFAST, "Süt", "süt", "tam yağlı süt", "yarım yağlı süt", "laktozsuz süt", "keçi sütü", "manda sütü")
            add(PantryMainCategory.DAIRY_BREAKFAST, "Yoğurt & Fermente", "yoğurt", "süzme yoğurt", "ayran", "kefir")
            add(PantryMainCategory.DAIRY_BREAKFAST, "Peynir", "beyaz peynir", "kaşar peyniri", "tost peyniri", "lor peyniri", "labne", "krem peynir", "tulum peyniri", "ezine peyniri", "mozzarella", "parmesan", "cheddar", "hellim", "rokfor", "gouda")
            add(PantryMainCategory.DAIRY_BREAKFAST, "Yumurta", "yumurta", "tavuk yumurtası", "bıldırcın yumurtası")
            add(PantryMainCategory.DAIRY_BREAKFAST, "Yağ & Krema", "tereyağı", "kaymak", "krema", "çırpma kreması")
            add(PantryMainCategory.DAIRY_BREAKFAST, "Bitkisel Alternatif", "badem sütü", "yulaf sütü", "soya sütü", "hindistan cevizi sütü")
            // KIRMIZI ET
            add(
                PantryMainCategory.RED_MEAT,
                "Dana",
                "et", "kırmızı et", "dana", "dana eti", "dana kıyma", "dana kuşbaşı",
                "dana bonfile", "dana antrikot", "dana biftek", "dana kontrfile",
                "dana rosto", "dana nuar", "dana döş", "dana incik", "dana kaburga",
                "dana ciğer", "dana yürek"
            )
            add(
                PantryMainCategory.RED_MEAT,
                "Kuzu & Koyun",
                "kuzu", "kuzu eti", "kuzu kıyma", "kuzu kuşbaşı", "kuzu pirzola",
                "kuzu incik", "kuzu but", "kuzu kol", "kuzu gerdan", "kuzu ciğer",
                "koyun eti", "koyun kıyma", "keçi eti"
            )

            // TAVUK & HİNDİ
            add(
                PantryMainCategory.POULTRY,
                "Tavuk",
                "tavuk", "tavuk eti", "tavuk göğsü", "tavuk göğüs", "tavuk fileto",
                "tavuk bonfile", "tavuk but", "tavuk kalça", "tavuk baget",
                "tavuk kanat", "bütün tavuk", "tavuk kıyma", "tavuk ciğer", "tavuk taşlık"
            )
            add(
                PantryMainCategory.POULTRY,
                "Hindi",
                "hindi", "hindi eti", "hindi göğsü", "hindi göğüs", "hindi fileto",
                "hindi but", "hindi kıyma"
            )
            add(
                PantryMainCategory.POULTRY,
                "Diğer Kümes",
                "ördek", "ördek eti", "kaz", "kaz eti", "bıldırcın", "bıldırcın eti"
            )

            // BALIK & DENİZ ÜRÜNLERİ
            add(
                PantryMainCategory.FISH_SEAFOOD,
                "Balık",
                "balık", "somon", "somon fileto", "levrek", "levrek fileto",
                "çipura", "çipura fileto", "hamsi", "sardalya", "uskumru",
                "palamut", "alabalık", "ton balığı", "ton balığı taze",
                "kalkan", "mezgit", "barbun", "barbunya balığı", "tekir",
                "lüfer", "istavrit", "kefal", "sazan", "yayın balığı",
                "dil balığı", "orfoz", "mercan"
            )
            add(
                PantryMainCategory.FISH_SEAFOOD,
                "Deniz Ürünleri",
                "karides", "kalamar", "midye", "ahtapot", "yengeç",
                "ıstakoz", "deniz tarağı", "kerevit"
            )

            // ŞARKÜTERİ
            add(
                PantryMainCategory.DELI,
                "Şarküteri",
                "sucuk", "sosis", "salam", "pastırma", "jambon",
                "füme et", "hindi füme", "tavuk füme", "roast beef", "kavurma"
            )
            add(PantryMainCategory.GRAINS_LEGUMES, "Pirinç", "pirinç", "baldo pirinç", "osmancık pirinç", "basmati pirinç", "jasmin pirinç", "esmer pirinç")
            add(PantryMainCategory.GRAINS_LEGUMES, "Tahıl", "bulgur", "ince bulgur", "pilavlık bulgur", "buğday", "aşurelik buğday", "arpa", "yulaf", "yulaf ezmesi", "kinoa", "karabuğday", "kuskus", "irmik")
            add(PantryMainCategory.GRAINS_LEGUMES, "Bakliyat", "kırmızı mercimek", "yeşil mercimek", "sarı mercimek", "mercimek", "nohut", "kuru fasulye", "barbunya", "börülce", "maş fasulyesi", "soya fasulyesi", "kuru bezelye")
            add(PantryMainCategory.GRAINS_LEGUMES, "Makarna", "makarna", "spagetti", "penne", "fusilli", "fiyonk makarna", "erişte", "lazanya")
            add(PantryMainCategory.BAKERY_FLOUR, "Ekmek", "ekmek", "beyaz ekmek", "tam buğday ekmeği", "çavdar ekmeği", "ekşi mayalı ekmek", "köy ekmeği", "baget ekmek", "tost ekmeği")
            add(PantryMainCategory.BAKERY_FLOUR, "Hamur Ürünü", "lavaş", "yufka", "tortilla", "pide", "bazlama", "simit", "poğaça", "kruvasan")
            add(PantryMainCategory.BAKERY_FLOUR, "Un", "un", "buğday unu", "tam buğday unu", "çavdar unu", "mısır unu", "pirinç unu", "badem unu", "yulaf unu", "glutensiz un")
            add(PantryMainCategory.BAKERY_FLOUR, "Pişirme Malzemesi", "galeta unu", "mısır nişastası", "buğday nişastası", "kabartma tozu", "karbonat", "kuru maya", "yaş maya", "vanilin", "vanilya")
            add(PantryMainCategory.CANNED_DRY, "Konserve", "konserve domates", "domates püresi", "konserve mısır", "konserve bezelye", "konserve nohut", "konserve fasulye", "konserve barbunya", "ton balığı konservesi")
            add(PantryMainCategory.CANNED_DRY, "Salça", "domates salçası", "biber salçası")
            add(PantryMainCategory.CANNED_DRY, "Turşu & Zeytin", "kornişon turşu", "karışık turşu", "lahana turşusu", "siyah zeytin", "yeşil zeytin")
            add(PantryMainCategory.CANNED_DRY, "Kuruyemiş", "badem", "fındık", "ceviz", "antep fıstığı", "kaju", "yer fıstığı", "pekan cevizi")
            add(PantryMainCategory.CANNED_DRY, "Tohum", "chia tohumu", "keten tohumu", "susam", "ay çekirdeği", "kabak çekirdeği")
            add(PantryMainCategory.CANNED_DRY, "Kuru Meyve", "kuru üzüm", "kuru kayısı", "kuru incir", "kuru erik", "kuru hurma")
            add(PantryMainCategory.SPICES_SAUCES_OILS, "Baharat", "karabiber", "kırmızı toz biber", "pul biber", "kimyon", "kekik", "kuru nane", "zerdeçal", "köri", "tarçın", "zencefil toz", "muskat", "sumak", "yenibahar", "karanfil", "safran")
            add(PantryMainCategory.SPICES_SAUCES_OILS, "Tuz & Şeker", "tuz", "deniz tuzu", "kaya tuzu", "toz şeker", "esmer şeker", "pudra şekeri")
            add(PantryMainCategory.SPICES_SAUCES_OILS, "Yağ", "zeytinyağı", "ayçiçek yağı", "mısırözü yağı", "kanola yağı", "susam yağı", "hindistan cevizi yağı")
            add(PantryMainCategory.SPICES_SAUCES_OILS, "Sirke", "üzüm sirkesi", "elma sirkesi", "balsamik sirke", "pirinç sirkesi")
            add(PantryMainCategory.SPICES_SAUCES_OILS, "Sos", "ketçap", "mayonez", "hardal", "soya sosu", "acı sos", "barbekü sos", "pesto", "nar ekşisi", "worcestershire sos", "teriyaki sos")
            add(PantryMainCategory.SPICES_SAUCES_OILS, "Tatlandırıcı", "bal", "pekmez", "akçaağaç şurubu", "agave şurubu")
            add(PantryMainCategory.BEVERAGES, "Soğuk", "su", "maden suyu", "soda", "kola", "gazoz", "limonata", "portakal suyu", "elma suyu", "vişne suyu", "şeftali suyu", "soğuk çay", "enerji içeceği", "sporcu içeceği", "kombucha", "cold brew", "soğuk kahve", "iced latte")
            add(PantryMainCategory.BEVERAGES, "Sıcak", "siyah çay", "yeşil çay", "beyaz çay", "türk kahvesi", "filtre kahve", "espresso kahvesi", "çekirdek kahve", "kakao", "salep", "sıcak çikolata")
            add(PantryMainCategory.BEVERAGES, "Bitki Çayı", "ıhlamur", "papatya çayı", "adaçayı", "rezene çayı", "kuşburnu çayı", "nane limon", "hibiskus çayı")
            add(PantryMainCategory.BEVERAGES, "Hazırlık", "kahve kreması", "kahve şurubu", "vanilya şurubu", "karamel şurubu", "çikolata şurubu", "tonik", "konsantre meyve şurubu")
            add(PantryMainCategory.FROZEN, "Sebze", "dondurulmuş bezelye", "dondurulmuş mısır", "dondurulmuş ıspanak", "dondurulmuş brokoli", "dondurulmuş sebze karışımı")
            add(PantryMainCategory.FROZEN, "Meyve", "dondurulmuş çilek", "dondurulmuş yaban mersini", "dondurulmuş orman meyvesi", "dondurulmuş mango")
            add(PantryMainCategory.FROZEN, "Hazır", "dondurulmuş patates", "dondurulmuş pizza", "dondurulmuş mantı", "dondurulmuş börek", "dondurulmuş köfte")
            add(PantryMainCategory.FROZEN, "Tatlı", "dondurma", "sorbe")
            add(PantryMainCategory.SNACKS_DESSERTS, "Çikolata", "sütlü çikolata", "bitter çikolata", "beyaz çikolata", "damla çikolata")
            add(PantryMainCategory.SNACKS_DESSERTS, "Bisküvi & Kraker", "bisküvi", "yulaflı bisküvi", "kraker", "galeta", "gofret")
            add(PantryMainCategory.SNACKS_DESSERTS, "Cips", "patates cipsi", "mısır cipsi")
            add(PantryMainCategory.SNACKS_DESSERTS, "Şekerleme", "şeker", "jelibon", "lokum", "marshmallow")
            add(PantryMainCategory.SNACKS_DESSERTS, "Tatlı Malzemesi", "reçel", "fıstık ezmesi", "fındık kreması", "tahin", "helva", "puding", "jöle")
            add(PantryMainCategory.READY_MEALS, "Hazır Yemek", "hazır çorba", "hazır makarna", "hazır noodle", "hazır pilav", "hazır sandviç", "hazır salata")
            add(PantryMainCategory.READY_MEALS, "Soslu / Marine", "marine tavuk", "marine et", "hazır köfte")
            add(PantryMainCategory.READY_MEALS, "Özel", "bebek maması", "protein bar", "granola", "müsli")
        }
    }

    fun classify(ingredientName: String): PantryCategoryResult {
        val normalized = normalize(ingredientName)

        if (normalized.isBlank()) {
            return PantryCategoryResult(
                PantryMainCategory.OTHER,
                "Diğer"
            )
        }

        // 1) Önce tam eşleşme
        categoryMap[normalized]?.let { entry ->
            return PantryCategoryResult(
                entry.mainCategory,
                entry.subCategory
            )
        }

        // 2) Sonra güvenli yazım hatası toleransı
        val fuzzyEntry = findBestFuzzyMatch(normalized)

        return if (fuzzyEntry != null) {
            PantryCategoryResult(
                fuzzyEntry.mainCategory,
                fuzzyEntry.subCategory
            )
        } else {
            PantryCategoryResult(
                PantryMainCategory.OTHER,
                "Diğer"
            )
        }
    }

    private fun findBestFuzzyMatch(
        normalizedInput: String
    ): Entry? {

        // Çok kısa kelimelerde yanlış eşleşme riski yüksek.
        if (normalizedInput.length < 4) {
            return null
        }

        val allowedDistance =
            when (normalizedInput.length) {
                in 4..5 -> 1
                in 6..8 -> 2
                in 9..13 -> 3
                else -> 4
            }

        var bestEntry: Entry? = null
        var bestDistance = Int.MAX_VALUE
        var bestSimilarity = 0.0
        var ambiguous = false

        categoryMap.forEach { (candidateName, entry) ->

            // Çok farklı uzunluktaki kelimeleri karşılaştırma.
            if (
                kotlin.math.abs(
                    candidateName.length -
                            normalizedInput.length
                ) > allowedDistance
            ) {
                return@forEach
            }

            val distance =
                damerauLevenshteinDistance(
                    normalizedInput,
                    candidateName
                )

            if (distance > allowedDistance) {
                return@forEach
            }

            val maxLength =
                maxOf(
                    normalizedInput.length,
                    candidateName.length
                )
                    .coerceAtLeast(1)

            val similarity =
                1.0 -
                        (
                                distance.toDouble() /
                                        maxLength.toDouble()
                                )

            // Rastgele metinlerin yanlış kategoriye düşmesini engeller.
            val minimumSimilarity =
                when {
                    normalizedInput.length <= 5 ->
                        0.80
                    normalizedInput.length <= 8 ->
                        0.75
                    else ->
                        0.72
                }

            if (similarity < minimumSimilarity) {
                return@forEach
            }

            when {
                distance < bestDistance -> {
                    bestDistance =
                        distance
                    bestSimilarity =
                        similarity
                    bestEntry =
                        entry
                    ambiguous =
                        false
                }

                distance == bestDistance &&
                        similarity >
                        bestSimilarity -> {
                    bestSimilarity =
                        similarity
                    bestEntry =
                        entry
                    ambiguous =
                        false
                }

                distance == bestDistance &&
                        kotlin.math.abs(
                            similarity -
                                    bestSimilarity
                        ) < 0.0001 &&
                        bestEntry != null &&
                        (
                                bestEntry!!.mainCategory !=
                                        entry.mainCategory ||
                                        bestEntry!!.subCategory !=
                                        entry.subCategory
                                ) -> {
                    // Aynı güven düzeyinde iki farklı kategori varsa
                    // otomatik tahmin yapmayalım.
                    ambiguous =
                        true
                }
            }
        }

        return if (ambiguous) {
            null
        } else {
            bestEntry
        }
    }

    private fun damerauLevenshteinDistance(
        first: String,
        second: String
    ): Int {

        if (first == second) {
            return 0
        }

        if (first.isEmpty()) {
            return second.length
        }

        if (second.isEmpty()) {
            return first.length
        }

        val matrix =
            Array(
                first.length + 1
            ) {
                IntArray(
                    second.length + 1
                )
            }

        for (i in 0..first.length) {
            matrix[i][0] = i
        }

        for (j in 0..second.length) {
            matrix[0][j] = j
        }

        for (i in 1..first.length) {
            for (j in 1..second.length) {

                val cost =
                    if (
                        first[i - 1] ==
                        second[j - 1]
                    ) {
                        0
                    } else {
                        1
                    }

                matrix[i][j] =
                    minOf(
                        matrix[i - 1][j] + 1,
                        matrix[i][j - 1] + 1,
                        matrix[i - 1][j - 1] +
                                cost
                    )

                if (
                    i > 1 &&
                    j > 1 &&
                    first[i - 1] ==
                    second[j - 2] &&
                    first[i - 2] ==
                    second[j - 1]
                ) {
                    matrix[i][j] =
                        minOf(
                            matrix[i][j],
                            matrix[i - 2][j - 2] +
                                    1
                        )
                }
            }
        }

        return matrix[
            first.length
        ][
            second.length
        ]
    }

    fun groupedCategoryOrder(): List<PantryMainCategory> = PantryMainCategory.entries.sortedBy { it.order }

    private fun normalize(value: String): String = value.trim()
        .lowercase(Locale("tr", "TR"))
        .replace("ı","i").replace("ş","s").replace("ğ","g").replace("ü","u").replace("ö","o").replace("ç","c")
        .replace(Regex("[^a-z0-9\\s]"), " ").replace(Regex("\\s+"), " ").trim()
}
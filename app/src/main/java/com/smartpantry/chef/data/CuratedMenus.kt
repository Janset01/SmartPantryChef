package com.smartpantry.chef.data

/**
 * Curated menu suggestions reference existing recipe names.
 * A recipe remains a single record in Room even when used in many menus.
 * These are starter examples, not a new recipe category or saved user plan.
 */
data class MenuDish(val role: String, val recipeName: String)

data class ReadyMenu(
    val title: String,
    val detail: String,
    val dishes: List<MenuDish>
)

data class MenuCollection(
    val id: String,
    val title: String,
    val shortDescription: String,
    val emoji: String,
    val menus: List<ReadyMenu>
)

object CuratedMenus {
    val collections = listOf(
        MenuCollection(
            "misafir", "Misafir Menüleri", "Birbiriyle uyumlu sofralar", "🍽️",
            listOf(
                ReadyMenu("Klasik Akşam Daveti", "5 yemek • dengeli sofra", listOf(
                    MenuDish("Çorba", "Mercimek Çorbası"),
                    MenuDish("Ana yemek", "Fırında Sebzeli Köfte"),
                    MenuDish("Yan yemek", "Sebzeli Bulgur Pilavı"),
                    MenuDish("Salata", "Çoban Salatası"),
                    MenuDish("Tatlı", "Sütlaç")
                )),
                ReadyMenu("Pratik Misafir Sofrası", "5 yemek • kolay seçenekler", listOf(
                    MenuDish("Çorba", "Domates Çorbası"),
                    MenuDish("Ana yemek", "Tavuk Sote"),
                    MenuDish("Yan yemek", "Nohutlu Pirinç Pilavı"),
                    MenuDish("Salata", "Mevsim Salatası"),
                    MenuDish("Tatlı", "Muzlu Puding")
                ))
            )
        ),
        MenuCollection(
            "cay", "Çay Saati", "Börek, atıştırmalık ve tatlı", "🫖",
            listOf(
                ReadyMenu("Klasik Çay Saati", "4 ikram • birlikte sunulabilir", listOf(
                    MenuDish("Börek", "Pırasalı Börek"),
                    MenuDish("Krep", "Peynirli Krep"),
                    MenuDish("Salata", "Yoğurtlu Havuç Salatası"),
                    MenuDish("Tatlı", "Yoğurtlu Çilek Kup")
                )),
                ReadyMenu("Hafif Çay İkramları", "4 ikram • pratik seçenekler", listOf(
                    MenuDish("Fırın", "Sebzeli Fırın Mücver"),
                    MenuDish("Sandviç", "Peynirli Mini Sandviç"),
                    MenuDish("Atıştırmalık", "Muzlu Yulaf Topları"),
                    MenuDish("Tatlı", "Fırında Elma Tatlısı")
                ))
            )
        ),
        MenuCollection(
            "iftar", "İftar & Sahur", "Öğüne uygun hazır menüler", "🌙",
            listOf(
                ReadyMenu("İftar Sofrası", "5 yemek • başlangıçtan tatlıya", listOf(
                    MenuDish("Çorba", "Ezogelin Çorbası"),
                    MenuDish("Ana yemek", "Etli Patates Yemeği"),
                    MenuDish("Yan yemek", "Nohutlu Pirinç Pilavı"),
                    MenuDish("Salata", "Mevsim Salatası"),
                    MenuDish("Tatlı", "İrmik Helvası")
                )),
                ReadyMenu("Sahur Menüsü", "4 yemek • kolay hazırlanır", listOf(
                    MenuDish("Yumurta", "Menemen"),
                    MenuDish("Tost", "Peynirli Tost"),
                    MenuDish("Tahıl", "Yulaf Lapası"),
                    MenuDish("Meyve", "Yoğurtlu Meyve Kasesi")
                ))
            )
        ),
        MenuCollection(
            "ozel", "Özel Günler", "Kutlama ve aile sofraları", "🎉",
            listOf(
                ReadyMenu("Doğum Günü İkramları", "4 yemek • paylaşmalık", listOf(
                    MenuDish("Ana yemek", "Fırında Tavuk Kanat"),
                    MenuDish("Fırın", "Fırın Makarna"),
                    MenuDish("Salata", "Çoban Salatası"),
                    MenuDish("Tatlı", "Muzlu Puding")
                )),
                ReadyMenu("Bayram Sofrası", "5 yemek • klasik seçenekler", listOf(
                    MenuDish("Çorba", "Yayla Çorbası"),
                    MenuDish("Ana yemek", "İzmir Köfte"),
                    MenuDish("Yan yemek", "Sebzeli Bulgur Pilavı"),
                    MenuDish("Salata", "Roka Salatası"),
                    MenuDish("Tatlı", "Sütlaç")
                )),
                ReadyMenu("Yılbaşı Sofrası", "4 yemek • özel sunum", listOf(
                    MenuDish("Ana yemek", "Fırında Somon"),
                    MenuDish("Yan yemek", "Fırında Patates"),
                    MenuDish("Salata", "Nohutlu Yeşil Salata"),
                    MenuDish("Tatlı", "Fırında Elma Tatlısı")
                ))
            )
        )
    )
}

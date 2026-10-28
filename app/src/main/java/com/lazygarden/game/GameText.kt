package com.lazygarden.game

/** Central text catalog for native LazyGarden UI. Keeps gameplay screens free of duplicated literals. */
object GameText {
    private val supported = setOf("sq", "de", "it", "mk", "tr")
    fun language(value: String?) = value?.takeIf { it in supported } ?: "sq"

    private val text = mapOf(
        "sq" to mapOf(
            "garden" to "Kopshti", "play" to "Luaj", "continue" to "Vazhdo", "levels" to "Nivelet",
            "story" to "Historia", "new" to "E re", "build" to "Ndërto", "decorate" to "Dekoro",
            "settings" to "Cilësimet", "close" to "Mbyll", "save" to "Ruaj", "profile" to "Profili",
            "boosters" to "Përforcues", "inventory" to "Inventari", "buy" to "Bli", "stock" to "në inventar",
            "no_stock" to "pa stok", "coins" to "Game Coins", "stars" to "yje", "materials" to "Materialet",
            "restoration" to "Restaurimi", "ready" to "Gati", "missing" to "Mungojnë", "reward" to "Shpërblimi",
            "level_complete" to "Niveli i përfunduar!", "try_again" to "Provo përsëri", "moves" to "Lëvizje"
        ),
        "de" to mapOf(
            "garden" to "Garten", "play" to "Spielen", "continue" to "Fortsetzen", "levels" to "Level",
            "story" to "Geschichte", "new" to "Neu", "build" to "Bauen", "decorate" to "Dekorieren",
            "settings" to "Einstellungen", "close" to "Schließen", "save" to "Speichern", "profile" to "Profil",
            "boosters" to "Booster", "inventory" to "Inventar", "buy" to "Kaufen", "stock" to "im Inventar",
            "no_stock" to "nicht vorrätig", "coins" to "Game Coins", "stars" to "Sterne", "materials" to "Materialien",
            "restoration" to "Restaurierung", "ready" to "Bereit", "missing" to "Fehlt", "reward" to "Belohnung",
            "level_complete" to "Level geschafft!", "try_again" to "Nochmal versuchen", "moves" to "Züge"
        ),
        "it" to mapOf(
            "garden" to "Giardino", "play" to "Gioca", "continue" to "Continua", "levels" to "Livelli",
            "story" to "Storia", "new" to "Nuovo", "build" to "Costruisci", "decorate" to "Decora",
            "settings" to "Impostazioni", "close" to "Chiudi", "save" to "Salva", "profile" to "Profilo",
            "boosters" to "Potenziamenti", "inventory" to "Inventario", "buy" to "Compra", "stock" to "in inventario",
            "no_stock" to "esaurito", "coins" to "Game Coins", "stars" to "stelle", "materials" to "Materiali",
            "restoration" to "Restauro", "ready" to "Pronto", "missing" to "Mancano", "reward" to "Ricompensa",
            "level_complete" to "Livello completato!", "try_again" to "Riprova", "moves" to "Mosse"
        ),
        "mk" to mapOf(
            "garden" to "Градина", "play" to "Играј", "continue" to "Продолжи", "levels" to "Нивоа",
            "story" to "Приказна", "new" to "Ново", "build" to "Изгради", "decorate" to "Украси",
            "settings" to "Поставки", "close" to "Затвори", "save" to "Зачувај", "profile" to "Профил",
            "boosters" to "Засилувачи", "inventory" to "Инвентар", "buy" to "Купи", "stock" to "во инвентар",
            "no_stock" to "нема залиха", "coins" to "Game Coins", "stars" to "ѕвезди", "materials" to "Материјали",
            "restoration" to "Реставрација", "ready" to "Подготвено", "missing" to "Недостига", "reward" to "Награда",
            "level_complete" to "Нивото е завршено!", "try_again" to "Обиди се повторно", "moves" to "Потези"
        ),
        "tr" to mapOf(
            "garden" to "Bahçe", "play" to "Oyna", "continue" to "Devam et", "levels" to "Seviyeler",
            "story" to "Hikâye", "new" to "Yeni", "build" to "İnşa et", "decorate" to "Süsle",
            "settings" to "Ayarlar", "close" to "Kapat", "save" to "Kaydet", "profile" to "Profil",
            "boosters" to "Güçlendiriciler", "inventory" to "Envanter", "buy" to "Satın al", "stock" to "envanterde",
            "no_stock" to "stok yok", "coins" to "Game Coins", "stars" to "yıldız", "materials" to "Malzemeler",
            "restoration" to "Restorasyon", "ready" to "Hazır", "missing" to "Eksik", "reward" to "Ödül",
            "level_complete" to "Seviye tamamlandı!", "try_again" to "Tekrar dene", "moves" to "Hamle"
        )
    )

    fun get(language: String?, key: String): String {
        val lang = language(language)
        return text[lang]?.get(key) ?: text.getValue("sq")[key] ?: key
    }
}

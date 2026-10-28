package com.lazygarden.game

/** Central text catalog for native LazyGarden UI. Keeps gameplay screens free of duplicated literals. */
object GameText {
    private val supported = setOf("sq", "de", "it", "mk", "tr")
    fun language(value: String?) = value?.takeIf { it in supported } ?: "sq"

    private val text = mapOf(
        "sq" to mapOf(
            "recorded_voices" to "Zërat (TTS i përkohshëm)",
            "locked" to "Përforcuesit përdoren në nivelin aktual", "insufficient_coins" to "Nuk ke mjaft Coins", "invalid_target" to "Zgjidh një figurë të vlefshme", "booster_HAMMER" to "Çekiçi", "booster_ROW" to "Rreshti", "booster_COLUMN" to "Kolona", "booster_CROSS" to "Kryqi", "booster_BOMB" to "Bomba", "booster_COLOR" to "Ngjyra", "booster_SHUFFLE" to "Përzieje",
            "garden" to "Kopshti", "play" to "Luaj", "continue" to "Vazhdo", "levels" to "Nivelet",
            "story" to "Historia", "new" to "E re", "build" to "Ndërto", "decorate" to "Dekoro",
            "settings" to "Cilësimet", "close" to "Mbyll", "save" to "Ruaj", "profile" to "Profili",
            "boosters" to "Përforcues", "inventory" to "Inventari", "buy" to "Bli", "stock" to "në inventar",
            "no_stock" to "pa stok", "coins" to "Game Coins", "stars" to "yje", "materials" to "Materialet",
            "restoration" to "Restaurimi", "ready" to "Gati", "missing" to "Mungojnë", "reward" to "Shpërblimi",
            "level_complete" to "Niveli i përfunduar!", "try_again" to "Provo përsëri", "moves" to "Lëvizje"
        ),
        "de" to mapOf(
            "recorded_voices" to "Stimmen (vorübergehend TTS)",
            "locked" to "Booster sind nur im aktuellen Level verfügbar", "insufficient_coins" to "Nicht genügend Münzen", "invalid_target" to "Wähle ein gültiges Spielfeld", "booster_HAMMER" to "Hammer", "booster_ROW" to "Reihe", "booster_COLUMN" to "Spalte", "booster_CROSS" to "Kreuz", "booster_BOMB" to "Bombe", "booster_COLOR" to "Farbe", "booster_SHUFFLE" to "Mischen",
            "garden" to "Garten", "play" to "Spielen", "continue" to "Fortsetzen", "levels" to "Level",
            "story" to "Geschichte", "new" to "Neu", "build" to "Bauen", "decorate" to "Dekorieren",
            "settings" to "Einstellungen", "close" to "Schließen", "save" to "Speichern", "profile" to "Profil",
            "boosters" to "Booster", "inventory" to "Inventar", "buy" to "Kaufen", "stock" to "im Inventar",
            "no_stock" to "nicht vorrätig", "coins" to "Game Coins", "stars" to "Sterne", "materials" to "Materialien",
            "restoration" to "Restaurierung", "ready" to "Bereit", "missing" to "Fehlt", "reward" to "Belohnung",
            "level_complete" to "Level geschafft!", "try_again" to "Nochmal versuchen", "moves" to "Züge"
        ),
        "it" to mapOf(
            "recorded_voices" to "Voci (TTS temporaneo)",
            "locked" to "I potenziamenti sono disponibili nel livello attuale", "insufficient_coins" to "Monete insufficienti", "invalid_target" to "Scegli una casella valida", "booster_HAMMER" to "Martello", "booster_ROW" to "Riga", "booster_COLUMN" to "Colonna", "booster_CROSS" to "Croce", "booster_BOMB" to "Bomba", "booster_COLOR" to "Colore", "booster_SHUFFLE" to "Mescola",
            "garden" to "Giardino", "play" to "Gioca", "continue" to "Continua", "levels" to "Livelli",
            "story" to "Storia", "new" to "Nuovo", "build" to "Costruisci", "decorate" to "Decora",
            "settings" to "Impostazioni", "close" to "Chiudi", "save" to "Salva", "profile" to "Profilo",
            "boosters" to "Potenziamenti", "inventory" to "Inventario", "buy" to "Compra", "stock" to "in inventario",
            "no_stock" to "esaurito", "coins" to "Game Coins", "stars" to "stelle", "materials" to "Materiali",
            "restoration" to "Restauro", "ready" to "Pronto", "missing" to "Mancano", "reward" to "Ricompensa",
            "level_complete" to "Livello completato!", "try_again" to "Riprova", "moves" to "Mosse"
        ),
        "mk" to mapOf(
            "recorded_voices" to "Гласови (привремено TTS)",
            "locked" to "Засилувачите се достапни во тековното ниво", "insufficient_coins" to "Нема доволно монети", "invalid_target" to "Избери важечко поле", "booster_HAMMER" to "Чекан", "booster_ROW" to "Ред", "booster_COLUMN" to "Колона", "booster_CROSS" to "Крст", "booster_BOMB" to "Бомба", "booster_COLOR" to "Боја", "booster_SHUFFLE" to "Измешај",
            "garden" to "Градина", "play" to "Играј", "continue" to "Продолжи", "levels" to "Нивоа",
            "story" to "Приказна", "new" to "Ново", "build" to "Изгради", "decorate" to "Украси",
            "settings" to "Поставки", "close" to "Затвори", "save" to "Зачувај", "profile" to "Профил",
            "boosters" to "Засилувачи", "inventory" to "Инвентар", "buy" to "Купи", "stock" to "во инвентар",
            "no_stock" to "нема залиха", "coins" to "Game Coins", "stars" to "ѕвезди", "materials" to "Материјали",
            "restoration" to "Реставрација", "ready" to "Подготвено", "missing" to "Недостига", "reward" to "Награда",
            "level_complete" to "Нивото е завршено!", "try_again" to "Обиди се повторно", "moves" to "Потези"
        ),
        "tr" to mapOf(
            "recorded_voices" to "Sesler (geçici TTS)",
            "locked" to "Güçlendiriciler mevcut seviyede kullanılabilir", "insufficient_coins" to "Yeterli jeton yok", "invalid_target" to "Geçerli bir kare seç", "booster_HAMMER" to "Çekiç", "booster_ROW" to "Satır", "booster_COLUMN" to "Sütun", "booster_CROSS" to "Çapraz", "booster_BOMB" to "Bomba", "booster_COLOR" to "Renk", "booster_SHUFFLE" to "Karıştır",
            "garden" to "Bahçe", "play" to "Oyna", "continue" to "Devam et", "levels" to "Seviyeler",
            "story" to "Hikâye", "new" to "Yeni", "build" to "İnşa et", "decorate" to "Süsle",
            "settings" to "Ayarlar", "close" to "Kapat", "save" to "Kaydet", "profile" to "Profil",
            "boosters" to "Güçlendiriciler", "inventory" to "Envanter", "buy" to "Satın al", "stock" to "envanterde",
            "no_stock" to "stok yok", "coins" to "Game Coins", "stars" to "yıldız", "materials" to "Malzemeler",
            "restoration" to "Restorasyon", "ready" to "Hazır", "missing" to "Eksik", "reward" to "Ödül",
            "level_complete" to "Seviye tamamlandı!", "try_again" to "Tekrar dene", "moves" to "Hamle"
        )
    )

    fun booster(language:String?,kind:Match3Engine.Booster)=get(language,"booster_${kind.name}")

    fun get(language: String?, key: String): String {
        val lang = language(language)
        return text[lang]?.get(key) ?: text.getValue("sq")[key] ?: key
    }
}

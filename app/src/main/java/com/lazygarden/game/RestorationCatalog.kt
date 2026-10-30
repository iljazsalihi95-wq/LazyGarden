package com.lazygarden.game

/**
 * Canonical restoration chapter catalogue.
 * Keeps the 20-job expansion aligned across story, build UI and scene replay.
 */
object RestorationCatalog {
    data class Entry(
        val sq: String,
        val de: String,
        val it: String,
        val mk: String,
        val tr: String
    ) {
        fun title(language: String): String = when (language) {
            "de" -> de
            "it" -> it
            "mk" -> mk
            "tr" -> tr
            else -> sq
        }
    }

    val entries = listOf(
        Entry("Rikthe ujin e shatërvanit", "Brunnen wieder in Betrieb nehmen", "Riattiva la fontana", "Обнови ја фонтаната", "Çeşmeyi yeniden çalıştır"),
        Entry("Mbille kopshtin me lule", "Blumengarten anlegen", "Pianta il giardino fiorito", "Засади цветна градина", "Çiçek bahçesini dik"),
        Entry("Rregullo shtegun", "Gartenweg reparieren", "Ripara il sentiero", "Поправи ја патеката", "Bahçe yolunu onar"),
        Entry("Riparo stolin", "Gartenbank reparieren", "Ripara la panchina", "Поправи ја клупата", "Bahçe bankını onar"),
        Entry("Rikthe gjelbërimin", "Grünfläche erneuern", "Ripristina il verde", "Обнови го зеленилото", "Yeşil alanı yenile"),
        Entry("Restauro hyrjen", "Eingang restaurieren", "Restaura l'ingresso", "Обнови го влезот", "Girişi restore et"),
        Entry("Restauro fasadën", "Fassade restaurieren", "Restaura la facciata", "Обнови ја фасадата", "Cepheyi restore et"),
        Entry("Rregullo brendësinë", "Innenraum reparieren", "Sistema gli interni", "Поправи ја внатрешноста", "İç mekânı onar"),
        Entry("Ndërro dritaret", "Fenster erneuern", "Rinnova le finestre", "Обнови ги прозорците", "Pencereleri yenile"),
        Entry("Riparo çatinë", "Dach reparieren", "Ripara il tetto", "Поправи го покривот", "Çatıyı onar"),
        Entry("Rregullo dhomën e fëmijëve", "Kinderzimmer einrichten", "Sistema la camera dei bambini", "Уреди ја детската соба", "Çocuk odasını düzenle"),
        Entry("Rregullo kuzhinën", "Küche reparieren", "Sistema la cucina", "Поправи ја кујната", "Mutfağı onar"),
        Entry("Përfundo instalimet e kuzhinës", "Kücheninstallationen fertigstellen", "Completa gli impianti della cucina", "Заврши ги кујнските инсталации", "Mutfak tesisatını tamamla"),
        Entry("Rregullo sallonin", "Wohnzimmer renovieren", "Rinnova il soggiorno", "Обнови ја дневната соба", "Salonu yenile"),
        Entry("Rregullo ballkonin", "Balkon gestalten", "Sistema il balcone", "Уреди го балконот", "Balkonu düzenle"),
        Entry("Ndërto punishten", "Werkstatt einrichten", "Allestisci l'officina", "Уреди ја работилницата", "Atölyeyi kur"),
        Entry("Krijo këndin e biçikletave", "Fahrradecke bauen", "Crea l'angolo biciclette", "Направи катче за велосипеди", "Bisiklet köşesi yap"),
        Entry("Vendos ndriçimin e oborrit", "Hofbeleuchtung installieren", "Installa le luci del cortile", "Постави дворско осветлување", "Avlu aydınlatmasını kur"),
        Entry("Ndërto zonën e lojës", "Spielbereich bauen", "Costruisci l'area giochi", "Изгради простор за игра", "Oyun alanını kur"),
        Entry("Përfundo kopshtin për festë", "Garten für das Fest vollenden", "Completa il giardino per la festa", "Заврши ја градината за прославата", "Bahçeyi kutlama için tamamla")
    )

    init {
        check(entries.size == RestorationInventory.TASK_COUNT) {
            "Restoration catalogue must match RestorationInventory.TASK_COUNT"
        }
    }

    fun title(task: Int, language: String): String =
        entries.getOrNull(task)?.title(language) ?: entries.last().title(language)
}

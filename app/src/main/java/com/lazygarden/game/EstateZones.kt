package com.lazygarden.game

/** Groups the existing work orders geographically without changing their save indices. */
internal object EstateZones {
    data class Zone(val tasks: IntRange, val titles: List<String>) {
        fun title(language: String): String = titles[when (language) {
            "de" -> 1
            "it" -> 2
            "mk" -> 3
            "tr" -> 4
            else -> 0
        }]

        fun completed(progress: Int): Int = (progress - tasks.first).coerceIn(0, tasks.count())
    }

    val all = listOf(
        Zone(0..4, listOf("Kopshti i burimit", "Brunnengarten", "Giardino della fontana", "Градина со фонтана", "Çeşme bahçesi")),
        Zone(5..9, listOf("Shtëpia dhe dritaret", "Herrenhaus und Fenster", "Villa e finestre", "Куќа и прозорци", "Konak ve pencereler")),
        Zone(10..16, listOf("Dhomat dhe punishtja", "Wohnräume und Werkstatt", "Stanze e officina", "Соби и работилница", "Odalar ve atölye")),
        Zone(17..19, listOf("Kopshti i festës", "Festgarten", "Giardino della festa", "Празнична градина", "Şenlik bahçesi"))
    )

    fun index(task: Int): Int = all.indexOfFirst { task in it.tasks }.coerceAtLeast(0)
}

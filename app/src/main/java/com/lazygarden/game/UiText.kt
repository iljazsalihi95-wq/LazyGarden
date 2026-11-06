package com.lazygarden.game

/** Remaining navigation and construction dialogs share one five-language dictionary. */
object UiText {
 private val languages=listOf("sq","de","it","mk","tr")
 private val rows="""
 hint_delivery|Çliro pakon dhe zbrite te shigjeta ↓|Paket befreien und nach unten bringen ↓|Libera il pacco e portalo alla freccia ↓|Ослободи го пакетот и спушти го долу ↓|Paketi kurtar ve oka kadar indir ↓
 hear_again|Dëgjo përsëri|Erneut anhören|Ascolta di nuovo|Слушај повторно|Tekrar dinle
 audio_loading|Po ngarkohet zëri…|Audio wird geladen…|Caricamento della voce…|Се вчитува гласот…|Ses yükleniyor…
 audio_playing|Personazhi po flet|Die Figur spricht|Il personaggio sta parlando|Ликот зборува|Karakter konuşuyor
 audio_unavailable|Audio nuk u mor. Mund ta provosh përsëri.|Audio nicht verfügbar. Versuche es erneut.|Audio non disponibile. Puoi riprovare.|Аудиото не е достапно. Обиди се повторно.|Ses alınamadı. Tekrar deneyebilirsin.
 audio_offline|Pa internet: kërkohet audio e ruajtur.|Offline: gespeicherte Aufnahme erforderlich.|Offline: serve una registrazione salvata.|Без интернет: потребна е зачувана снимка.|İnternet yok: kayıtlı ses gerekli.
 audio_disabled|Zërat janë çaktivizuar te cilësimet.|Stimmen sind in den Einstellungen ausgeschaltet.|Le voci sono disattivate nelle impostazioni.|Гласовите се исклучени во поставките.|Sesler ayarlardan kapatılmış.
 local_profile|Profili lokal|Lokales Profil|Profilo locale|Локален профил|Yerel profil
 saved_device|Progresi ruhet në këtë pajisje.|Der Fortschritt wird auf diesem Gerät gespeichert.|I progressi sono salvati su questo dispositivo.|Напредокот се чува на овој уред.|İlerleme bu cihazda saklanır.
 restorations|Restaurime|Restaurierungen|Restauri|Обнови|Restorasyonlar
 work_complete|Detyra e përfunduar!|Auftrag abgeschlossen!|Lavoro completato!|Задачата е завршена!|İş tamamlandı!
 continue_story|Vazhdo tregimin|Geschichte fortsetzen|Continua la storia|Продолжи ја приказната|Hikâyeye devam et
 estate_story|Historia e pronës|Geschichte des Anwesens|Storia della tenuta|Приказна за имотот|Mülkün hikâyesi
 replay_fountain|Riluaj shatërvanin|Brunnen erneut ansehen|Rivedi la fontana|Повтори ја фонтаната|Çeşmeyi tekrar izle
 replay|Riluaj|Erneut ansehen|Rivedi|Повтори|Tekrar izle
 reread|Rilexo|Erneut lesen|Rileggi|Прочитај повторно|Tekrar oku
 chapter|Kapitulli|Kapitel|Capitolo|Поглавје|Bölüm
 complete_restoration|Përfundo restaurimin|Restaurierung abschließen|Completa il restauro|Заврши ја обновата|Restorasyonu tamamla
 chapter_locked|Ky kapitull hapet pas restaurimit|Dieses Kapitel öffnet sich nach der Restaurierung|Questo capitolo si apre dopo il restauro|Поглавјето се отвора по обновата|Bu bölüm restorasyondan sonra açılır
 choose_build|Zgjidh si ta ndërtosh|Bauweise wählen|Scegli come costruire|Избери како да градиш|Nasıl inşa edeceğini seç
 build_materials|Restauro me materialet e mbledhura|Mit gesammeltem Material bauen|Restaura con i materiali raccolti|Обнови со собраните материјали|Toplanan malzemeyle restore et
 use_key|Përdor 1 çelës — pa materiale|1 Schlüssel verwenden — ohne Material|Usa 1 chiave — senza materiali|Користи 1 клуч — без материјали|1 anahtar kullan — malzeme gerekmez
 buy_missing|Bli materialet që mungojnë|Fehlendes Material kaufen|Compra i materiali mancanti|Купи ги материјалите што недостигаат|Eksik malzemeyi satın al
 materials_bought|Materialet u blenë. Tani mund të restaurosh.|Material gekauft. Du kannst jetzt restaurieren.|Materiali acquistati. Ora puoi restaurare.|Материјалите се купени. Сега можеш да обновуваш.|Malzemeler alındı. Şimdi restore edebilirsin.
 base_cost|Kostoja bazë: 120 Game Coins dhe 1 yll, përveç restaurimit të parë. Çelësi zëvendëson materialet.|Grundpreis: 120 Game Coins und 1 Stern, außer beim ersten Auftrag. Ein Schlüssel ersetzt das Material.|Costo base: 120 Game Coins e 1 stella, tranne il primo restauro. La chiave sostituisce i materiali.|Основна цена: 120 Game Coins и 1 ѕвезда, освен за првата обнова. Клучот ги заменува материјалите.|Temel ücret: ilk restorasyon dışında 120 Game Coins ve 1 yıldız. Anahtar malzemenin yerine geçer.
 video_loading|Po kërkohet video…|Video wird geladen…|Caricamento del video…|Се вчитува видео…|Video yükleniyor…
 cancel|Anulo|Abbrechen|Annulla|Откажи|İptal
 voluntary_video|Shiko një video të plotë për bonus. Mund të vazhdosh edhe pa reklamë.|Sieh ein vollständiges Video für einen Bonus. Du kannst auch ohne Werbung weiterspielen.|Guarda un video completo per un bonus. Puoi continuare anche senza pubblicità.|Гледај цело видео за бонус. Можеш да продолжиш и без реклама.|Bonus için videoyu tamamla. Reklam olmadan da devam edebilirsin.
 life|jetë|Leben|vita|живот|can
 cannot_build|Ende nuk mund të ndërtohet|Bau noch nicht möglich|Non puoi ancora costruire|Сè уште не може да се гради|Henüz inşa edilemiyor
 fountain_alive|Shatërvani është përsëri gjallë!|Der Brunnen lebt wieder!|La fontana è tornata a vivere!|Фонтаната повторно оживеа!|Çeşme yeniden canlandı!
 before|Para|Vorher|Prima|Пред|Önce
 after|Pas|Nachher|Dopo|Потоа|Sonra
 flowers_complete|Mbjellja dhe ujitja u përfunduan.|Pflanzung und Bewässerung abgeschlossen.|Piantagione e annaffiatura completate.|Садењето и наводнувањето завршија.|Dikim ve sulama tamamlandı.
 garden_alive|Kopshti mori jetë!|Der Garten ist lebendig!|Il giardino ha preso vita!|Градината оживеа!|Bahçe canlandı!
 decoration|Dekorime|Dekorationen|Decorazioni|Украси|Dekorasyonlar
 decorate_estate|Dekoro pronën|Anwesen gestalten|Decora la tenuta|Украси го имотот|Mülkü dekore et
 preview|Pamje para vendosjes|Vorschau vor Platzierung|Anteprima del posizionamento|Преглед пред поставување|Yerleştirme önizlemesi
 placed|Vendosur|Platziert|Posizionato|Поставено|Yerleştirildi
 place|Vendos|Platzieren|Posiziona|Постави|Yerleştir
 owned|Në koleksion|In der Sammlung|Nella collezione|Во колекцијата|Koleksiyonda
 choose_style|Zgjidh stilin e kopshtit tënd|Wähle deinen Gartenstil|Scegli lo stile del giardino|Избери стил на градината|Bahçenin tarzını seç
 remove_decoration|Hiq dekorimin e kësaj kategorie|Dekoration dieser Kategorie entfernen|Rimuovi la decorazione di questa categoria|Отстрани го украсот од категоријата|Bu kategorinin dekorasyonunu kaldır
 not_saved|Zgjedhja nuk u ruajt|Auswahl wurde nicht gespeichert|La scelta non è stata salvata|Изборот не е зачуван|Seçim kaydedilmedi
 BUILDINGS|Ndërtesa|Gebäude|Edifici|Згради|Binalar
 DECOR|Dekorime|Dekorationen|Decorazioni|Украси|Dekorasyonlar
 TREES|Pemë|Bäume|Alberi|Дрвја|Ağaçlar
 FOUNTAINS|Shatërvanë|Brunnen|Fontane|Фонтани|Çeşmeler
 gazebo_blue|Belveder i kaltër|Blauer Pavillon|Gazebo blu|Син белведер|Mavi çardak
 gazebo_rustic|Belveder rustik|Rustikaler Pavillon|Gazebo rustico|Рустичен белведер|Rustik çardak
 pavilion|Pavijon prej xhami|Glaspavillon|Padiglione di vetro|Стаклен павилјон|Cam köşk
 bench|Stoli me lule|Blumenbank|Panchina fiorita|Клупа со цвеќиња|Çiçekli bank
 rose_arch|Harku i trëndafilave|Rosenbogen|Arco di rose|Лак од рози|Gül kemeri
 lantern|Feneri i kopshtit|Gartenlaterne|Lanterna del giardino|Градинарски фенер|Bahçe feneri
 cherry|Qershia në lulëzim|Blühender Kirschbaum|Ciliegio in fiore|Расцветана цреша|Çiçek açan kiraz
 maple|Panja e artë|Goldener Ahorn|Acero dorato|Златен јавор|Altın akçaağaç
 citrus|Pema e agrumeve|Zitrusbaum|Albero di agrumi|Цитрусно дрво|Narenciye ağacı
 classic_fountain|Shatërvani klasik|Klassischer Brunnen|Fontana classica|Класична фонтана|Klasik çeşme
 lotus_fountain|Shatërvani i lotusit|Lotusbrunnen|Fontana di loto|Лотосова фонтана|Lotus çeşmesi
 stone_fountain|Ujëvara e gurit|Steinwasserfall|Cascata di pietra|Камен водопад|Taş şelale
 insufficient_materials|Nuk ka materiale të mjaftueshme|Nicht genug Material|Materiali insufficienti|Нема доволно материјали|Yeterli malzeme yok
 """.trimIndent().lines().map{it.trim().split('|')}.associate{row->check(row.size==6);row.first() to row.drop(1)}
 fun get(language:String,key:String):String=rows[key]?.get(languages.indexOf(GameText.language(language)))?:GameText.get(language,key)
}

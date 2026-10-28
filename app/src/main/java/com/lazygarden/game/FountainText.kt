package com.lazygarden.game

object FountainText {
 private val languages=listOf("sq","de","it","mk","tr")
 private val rows=mapOf(
  "FLOWER" to listOf("Lule","Blumen","Fiori","Цвеќиња","Çiçekler"),
  "SOIL" to listOf("Dhe","Erde","Terra","Земја","Toprak"),
  "STONE" to listOf("Gurë","Steine","Pietre","Камења","Taşlar"),
  "SAND" to listOf("Rërë","Sand","Sabbia","Песок","Kum"),
  "BOARD" to listOf("Dërrasa","Bretter","Assi","Даски","Tahtalar"),
  "SCREW" to listOf("Vida","Schrauben","Viti","Завртки","Vidalar"),
  "PAINT" to listOf("Bojë","Farbe","Vernice","Боја","Boya"),
  "SEEDLING" to listOf("Fidanë","Setzlinge","Piantine","Садници","Fidanlar"),
  "FERTILIZER" to listOf("Pleh","Dünger","Fertilizzante","Ѓубриво","Gübre"),
  "HINGE" to listOf("Mentesha","Scharniere","Cardini","Шарки","Menteşeler"),
  "LAMP" to listOf("Llamba","Lampen","Lampade","Светилки","Lambalar"),
  "PLASTER" to listOf("Suva","Putz","Intonaco","Малтер","Sıva"),
  "GLASS" to listOf("Xham","Glas","Vetro","Стакло","Cam"),
  "TILE" to listOf("Tjegulla","Dachziegel","Tegole","Ќерамиди","Kiremitler"),
  "CABLE" to listOf("Kabllo","Kabel","Cavi","Кабли","Kablolar"),
  "keys" to listOf("Çelësa","Schlüssel","Chiavi","Клучеви","Anahtarlar"),
  "tickets" to listOf("Bileta","Tickets","Biglietti","Билети","Biletler"),
  "inspect" to listOf("Gjej shkakun pse shatërvani nuk punon","Finde heraus, warum der Brunnen nicht läuft","Scopri perché la fontana non funziona","Откриј зошто фонтаната не работи","Çeşmenin neden çalışmadığını bul"),
  "test_water" to listOf("Provo ujin · prek shatërvanin","Wasser prüfen · Brunnen antippen","Prova l’acqua · tocca la fontana","Провери ја водата · допри ја фонтаната","Suyu dene · çeşmeye dokun"),
  "call_lazi" to listOf("Meli kërkon ndihmë · prek Melin","Meli braucht Hilfe · Meli antippen","Meli chiede aiuto · tocca Meli","Мели бара помош · допри ја Мели","Meli yardım istiyor · Meli’ye dokun"),
  "call_rexha" to listOf("Telefono Rexhën · prek telefonin","Rexha anrufen · Telefon antippen","Chiama Rexha · tocca il telefono","Јави му се на Реџа · допри го телефонот","Rexha’yı ara · telefona dokun"),
  "rexha_inspect" to listOf("Rexha kontrollon pompën · prek pompën","Rexha prüft die Pumpe · Pumpe antippen","Rexha controlla la pompa · tocca la pompa","Реџа ја проверува пумпата · допри ја пумпата","Rexha pompayı inceliyor · pompaya dokun"),
  "collect" to listOf("Fito materialet në Match-3","Materialien in Match-3 verdienen","Ottieni materiali nel Match-3","Освои материјали во Match-3","Match-3 oynayarak malzeme kazan"),
  "repair" to listOf("Riparo pompën","Pumpe reparieren","Ripara la pompa","Поправи ја пумпата","Pompayı onar"),
  "clean" to listOf("Pastro shatërvanin · mblidh 3 grumbuj gjethesh","Brunnen reinigen · 3 Laubhaufen sammeln","Pulisci la fontana · raccogli 3 mucchi di foglie","Исчисти ја фонтаната · собери 3 купчиња лисја","Çeşmeyi temizle · 3 yaprak yığını topla"),
  "start_water" to listOf("Hap valvulën · prek rrotën","Ventil öffnen · Rad antippen","Apri la valvola · tocca la ruota","Отвори го вентилот · допри го тркалото","Vanayı aç · çarka dokun"),
  "claim" to listOf("Merr shpërblimin","Belohnung abholen","Ritira il premio","Земи ја наградата","Ödülü al"),
  "ready" to listOf("GATI PËR RESTAURIM","BEREIT ZUM RESTAURIEREN","PRONTO PER IL RESTAURO","ПОДГОТВЕНО ЗА ОБНОВА","ONARIMA HAZIR"),
  "meli_line" to listOf("Nuk del ujë. Lazi, na ndihmon?","Es kommt kein Wasser. Lazi, hilfst du uns?","Non esce acqua. Lazi, ci aiuti?","Нема вода. Лази, ќе ни помогнеш?","Su gelmiyor. Lazi, yardım eder misin?"),
  "lazi_line" to listOf("Pompa është bllokuar. Po telefonoj Rexhën.","Die Pumpe ist verstopft. Ich rufe Rexha an.","La pompa è bloccata. Chiamo Rexha.","Пумпата е затната. Ќе му се јавам на Реџа.","Pompa tıkanmış. Rexha’yı arıyorum."),
  "rexha_line" to listOf("Më duhen veglat dhe pjesët. Fitojini në Match-3!","Ich brauche Werkzeug und Teile. Verdient sie in Match-3!","Mi servono attrezzi e ricambi. Guadagnateli nel Match-3!","Ми требаат алат и делови. Освојте ги во Match-3!","Alet ve parçalar gerekiyor. Match-3 oynayarak kazanın!"),
  "happy_line" to listOf("Ura! Shatërvani po punon!","Hurra! Der Brunnen läuft!","Evviva! La fontana funziona!","Ура! Фонтаната работи!","Yaşasın! Çeşme çalışıyor!"),
  "arriving" to listOf("Rexha po vjen në pronë…","Rexha kommt zum Anwesen…","Rexha sta arrivando…","Реџа доаѓа на имотот…","Rexha mülke geliyor…"),
  "phone_call" to listOf("Lazi po telefonon Rexhën…","Lazi ruft Rexha an…","Lazi chiama Rexha…","Лази му се јавува на Реџа…","Lazi, Rexha’yı arıyor…"),
  "tools" to listOf("Veglat","Werkzeug","Attrezzi","Алат","Aletler"),
  "special_items" to listOf("Objekte të veçanta","Besondere Gegenstände","Oggetti speciali","Посебни предмети","Özel eşyalar"),
  "PIPE" to listOf("Tuba","Rohre","Tubi","Цевки","Borular"),
  "VALVE" to listOf("Valvul","Ventil","Valvola","Вентил","Vana"),
  "TOOL" to listOf("Vegla","Werkzeug","Attrezzi","Алат","Aletler"),
  "PUMP_PART" to listOf("Pjesë pompe","Pumpenteile","Ricambi pompa","Делови за пумпа","Pompa parçaları")
 )
 fun get(language:String,key:String)=rows[key]?.get(languages.indexOf(language).coerceAtLeast(0))?:GameText.get(language,key)
 fun objective(language:String,stage:FountainQuest.Stage):String=get(language,when(stage){
  FountainQuest.Stage.TEST_WATER->"test_water";FountainQuest.Stage.CALL_LAZI->"call_lazi"
  FountainQuest.Stage.CALL_REXHA->"call_rexha";FountainQuest.Stage.PHONE_CALL->"phone_call"
  FountainQuest.Stage.REXHA_ARRIVAL->"arriving";FountainQuest.Stage.REXHA_INSPECT->"rexha_inspect"
  FountainQuest.Stage.COLLECT->"collect";FountainQuest.Stage.REPAIR->"repair"
  FountainQuest.Stage.CLEAN->"clean";FountainQuest.Stage.START_WATER->"start_water"
  FountainQuest.Stage.CLAIM->"claim";else->"inspect"
 })
}

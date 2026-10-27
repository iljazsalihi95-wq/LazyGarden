package com.lazygarden.game

object SceneText {
 fun name(id:Int)=listOf("Meti","Bejdi","Merjem","Meli").getOrElse(id){"LazyGarden"}
 private val lines=mapOf(
  "Pse nuk punon shatërvani?" to listOf("Pse nuk punon shatërvani?","Warum funktioniert der Brunnen nicht?","Perché la fontana non funziona?","Зошто не работи фонтаната?","Çeşme neden çalışmıyor?"),
  "Pompa duket e bllokuar." to listOf("Pompa duket e bllokuar.","Die Pumpe scheint verstopft zu sein.","La pompa sembra bloccata.","Пумпата изгледа затната.","Pompa tıkanmış görünüyor."),
  "Shikoni! Po vjen uji!" to listOf("Shikoni! Po vjen uji!","Schaut! Das Wasser kommt!","Guardate! Arriva l’acqua!","Погледнете! Доаѓа водата!","Bakın! Su geliyor!"),
  "Ura! Erdhi uji!" to listOf("Ura! Erdhi uji!","Hurra! Das Wasser ist da!","Evviva! È arrivata l’acqua!","Ура! Водата дојде!","Yaşasın! Su geldi!"),
  "Gurët janë çarë. Ta hapim shtegun!" to listOf("Gurët janë çarë. Ta hapim shtegun!","Die Steine sind gerissen. Machen wir den Weg frei!","Le pietre sono rotte. Liberiamo il sentiero!","Камењата се напукнати. Да ја отвориме патеката!","Taşlar çatlamış. Yolu açalım!"),
  "Një nga një, gurët marrin vend." to listOf("Një nga një, gurët marrin vend.","Ein Stein nach dem anderen findet seinen Platz.","Una dopo l’altra, le pietre trovano posto.","Еден по еден, камењата доаѓаат на место.","Taşlar birer birer yerini buluyor."),
  "Tani mund të ecim mes luleve!" to listOf("Tani mund të ecim mes luleve!","Jetzt können wir zwischen den Blumen spazieren!","Ora possiamo camminare tra i fiori!","Сега можеме да шетаме меѓу цвеќињата!","Artık çiçeklerin arasında yürüyebiliriz!"),
  "Sa shteg i bukur!" to listOf("Sa shteg i bukur!","Was für ein schöner Weg!","Che bel sentiero!","Колку убава патека!","Ne güzel bir yol!"),
  "Stoli lëkundet. Dërrasat janë kalbur." to listOf("Stoli lëkundet. Dërrasat janë kalbur.","Die Bank wackelt. Die Bretter sind morsch.","La panchina traballa. Le assi sono marce.","Клупата се ниша. Даските се скапани.","Bank sallanıyor. Tahtalar çürümüş."),
  "Po shtrëngoj lidhjet e reja." to listOf("Po shtrëngoj lidhjet e reja.","Ich ziehe die neuen Verbindungen fest.","Sto stringendo i nuovi giunti.","Ги затегнувам новите спојки.","Yeni bağlantıları sıkıyorum."),
  "Një shtresë bojë do ta mbrojë." to listOf("Një shtresë bojë do ta mbrojë.","Eine Schicht Farbe schützt sie.","Una mano di vernice la proteggerà.","Слој боја ќе ја заштити.","Bir kat boya onu koruyacak."),
  "E provova! Tani është i sigurt." to listOf("E provova! Tani është i sigurt.","Ich habe sie ausprobiert! Jetzt ist sie sicher.","L’ho provata! Ora è sicura.","Ја пробав! Сега е безбедна.","Denedim! Artık güvenli."),
  "Një vend për të pushuar së bashku!" to listOf("Një vend për të pushuar së bashku!","Ein Platz, um gemeinsam auszuruhen!","Un posto per riposare insieme!","Место за заеднички одмор!","Birlikte dinlenecek bir yer!"),
  "Toka ka nevojë për kujdes." to listOf("Toka ka nevojë për kujdes.","Der Boden braucht Pflege.","Il terreno ha bisogno di cure.","На почвата ѝ треба нега.","Toprağın bakıma ihtiyacı var."),
  "Po heq rrënjët e barërave." to listOf("Po heq rrënjët e barërave.","Ich entferne die Wurzeln des Unkrauts.","Sto togliendo le radici delle erbacce.","Ги отстранувам корењата на плевелот.","Yabani otların köklerini çıkarıyorum."),
  "Pemët e reja do të rriten këtu." to listOf("Pemët e reja do të rriten këtu.","Hier werden die neuen Bäume wachsen.","Qui cresceranno i nuovi alberi.","Тука ќе растат новите дрвја.","Yeni ağaçlar burada büyüyecek."),
  "Shikoni sa gjelbërim!" to listOf("Shikoni sa gjelbërim!","Seht nur, wie grün alles ist!","Guardate quanto verde!","Погледнете колку зеленило!","Ne kadar yeşil olduğuna bakın!"),
  "Kopshti merr frymë përsëri!" to listOf("Kopshti merr frymë përsëri!","Der Garten atmet wieder auf!","Il giardino respira di nuovo!","Градината повторно дише!","Bahçe yeniden nefes alıyor!"),
  "Porta ngec. Menteshat duhen ndrequr." to listOf("Porta ngec. Menteshat duhen ndrequr.","Das Tor klemmt. Die Scharniere müssen repariert werden.","Il cancello si blocca. Bisogna riparare i cardini.","Портата заглавува. Треба да ги поправиме шарките.","Kapı sıkışıyor. Menteşeler onarılmalı."),
  "Po rregulloj menteshat dhe shtyllat." to listOf("Po rregulloj menteshat dhe shtyllat.","Ich repariere Scharniere und Pfosten.","Sto riparando cardini e pilastri.","Ги поправам шарките и столбовите.","Menteşeleri ve sütunları onarıyorum."),
  "Lulet do t’i mirëpresin miqtë." to listOf("Lulet do t’i mirëpresin miqtë.","Die Blumen werden unsere Freunde begrüßen.","I fiori accoglieranno gli amici.","Цвеќињата ќе ги пречекаат пријателите.","Çiçekler dostlarımızı karşılayacak."),
  "Porta hapet dhe fenerët ndriçojnë!" to listOf("Porta hapet dhe fenerët ndriçojnë!","Das Tor öffnet sich und die Laternen leuchten!","Il cancello si apre e le lanterne brillano!","Портата се отвора и фенерите светат!","Kapı açılıyor ve fenerler parlıyor!"),
  "Mirë se vini në kopshtin tonë!" to listOf("Mirë se vini në kopshtin tonë!","Willkommen in unserem Garten!","Benvenuti nel nostro giardino!","Добредојдовте во нашата градина!","Bahçemize hoş geldiniz!"),
  "Tani është radha e shtëpisë." to listOf("Tani është radha e shtëpisë.","Jetzt ist das Haus an der Reihe.","Ora tocca alla casa.","Сега е ред на куќата.","Şimdi sıra evde."),
  "Çarjet po mbyllen një nga një." to listOf("Çarjet po mbyllen një nga një.","Die Risse werden einer nach dem anderen geschlossen.","Le crepe si chiudono una dopo l’altra.","Пукнатините се затвораат една по една.","Çatlaklar birer birer kapanıyor."),
  "Ngjyrat e ngrohta i japin jetë." to listOf("Ngjyrat e ngrohta i japin jetë.","Die warmen Farben bringen Leben ins Haus.","I colori caldi le danno vita.","Топлите бои ѝ даваат живот.","Sıcak renkler ona hayat veriyor."),
  "Edhe vazot e dritareve janë gati!" to listOf("Edhe vazot e dritareve janë gati!","Auch die Blumenkästen sind fertig!","Anche le fioriere sono pronte!","И саксиите на прозорците се готови!","Pencere saksıları da hazır!"),
  "E ndërtuam së bashku!" to listOf("E ndërtuam së bashku!","Wir haben es gemeinsam geschafft!","L’abbiamo costruita insieme!","Ја изградивме заедно!","Birlikte yaptık!"),
  "Kjo pronë është përsëri shtëpi!" to listOf("Kjo pronë është përsëri shtëpi!","Dieses Anwesen ist wieder ein Zuhause!","Questa proprietà è di nuovo una casa!","Овој имот повторно е дом!","Bu mülk yeniden bir yuva oldu!")
 )
 fun line(sq:String,language:String):String=lines[sq]?.getOrNull(listOf("sq","de","it","mk","tr").indexOf(language).coerceAtLeast(0))?:sq
}

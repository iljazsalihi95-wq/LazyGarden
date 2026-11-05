package com.lazygarden.game

/** Lines used by the actual construction timelines, including indoor jobs 8–20. */
object WorkTranslations {
 private val rows="""
 E përfunduam së bashku!|Wir haben es zusammen geschafft!|L’abbiamo completato insieme!|Го завршивме заедно!|Birlikte tamamladık!
 Brenda ka shumë punë. Ta pastrojmë dhe ta rregullojmë.|Drinnen gibt es viel zu tun. Räumen wir auf und reparieren alles.|Dentro c’è molto da fare. Puliamo e ripariamo.|Внатре има многу работа. Да исчистиме и поправиме.|İçeride çok iş var. Temizleyip onaralım.
 Muret po bëhen të forta përsëri.|Die Wände werden wieder stabil.|Le pareti tornano solide.|Ѕидовите повторно стануваат цврсти.|Duvarlar yeniden sağlamlaşıyor.
 Tani shtëpia ndihet e ngrohtë.|Jetzt fühlt sich das Haus gemütlich an.|Ora la casa è accogliente.|Сега куќата е пријатна.|Ev artık sıcak ve huzurlu.
 Dritaret e vjetra nuk mbyllen mirë.|Die alten Fenster schließen nicht richtig.|Le vecchie finestre non chiudono bene.|Старите прозорци не се затвораат добро.|Eski pencereler iyi kapanmıyor.
 Besi po vendos kornizat dhe xhamat e rinj.|Besi baut die neuen Rahmen und Scheiben ein.|Besi monta i nuovi telai e vetri.|Беси ги поставува новите рамки и стакла.|Besi yeni çerçeveleri ve camları takıyor.
 Sa shumë dritë hyri brenda!|Wie viel Licht hereinkommt!|Quanta luce è entrata!|Колку светлина влезе!|İçeri ne kadar çok ışık girdi!
 Çatia duhet rregulluar para shiut.|Das Dach muss vor dem Regen repariert werden.|Il tetto va riparato prima della pioggia.|Покривот треба да се поправи пред дождот.|Yağmurdan önce çatı onarılmalı.
 Nermiri po vendos tjegullat me radhë.|Nermiri verlegt die Dachziegel der Reihe nach.|Nermiri posa le tegole in ordine.|Нермири ги поставува ќерамидите по ред.|Nermiri kiremitleri sırayla yerleştiriyor.
 Tani jemi të mbrojtur nga shiu.|Jetzt sind wir vor Regen geschützt.|Ora siamo protetti dalla pioggia.|Сега сме заштитени од дождот.|Artık yağmurdan korunuyoruz.
 Fëmijët kanë nevojë për një dhomë të sigurt dhe të bukur.|Die Kinder brauchen ein sicheres, schönes Zimmer.|I bambini hanno bisogno di una stanza sicura e bella.|На децата им треба безбедна и убава соба.|Çocukların güvenli ve güzel bir odaya ihtiyacı var.
 Po montojmë mobiliet që të jenë të forta.|Wir bauen die Möbel stabil auf.|Montiamo i mobili perché siano solidi.|Го составуваме мебелот за да биде цврст.|Mobilyaları sağlam olacak şekilde kuruyoruz.
 Këtu mund të lexojmë dhe të luajmë!|Hier können wir lesen und spielen!|Qui possiamo leggere e giocare!|Тука можеме да читаме и да играме!|Burada okuyup oynayabiliriz!
 Kuzhina duhet pastruar dhe riparuar.|Die Küche muss gereinigt und repariert werden.|La cucina va pulita e riparata.|Кујната треба да се исчисти и поправи.|Mutfak temizlenip onarılmalı.
 Po rregullojmë dollapët dhe sipërfaqet.|Wir reparieren Schränke und Arbeitsflächen.|Ripariamo gli armadietti e le superfici.|Ги поправаме шкафовите и површините.|Dolapları ve yüzeyleri onarıyoruz.
 Tani mund të gatuajmë së bashku.|Jetzt können wir zusammen kochen.|Ora possiamo cucinare insieme.|Сега можеме да готвиме заедно.|Artık birlikte yemek yapabiliriz.
 Uji dhe rryma duhet të jenë gati para përdorimit.|Wasser und Strom müssen vor der Nutzung bereit sein.|Acqua ed elettricità devono essere pronte prima dell’uso.|Водата и струјата мора да бидат подготвени пред употреба.|Kullanmadan önce su ve elektrik hazır olmalı.
 Po kontrollojmë lidhjet një nga një.|Wir prüfen die Anschlüsse einzeln.|Controlliamo i collegamenti uno per uno.|Ги проверуваме приклучоците еден по еден.|Bağlantıları tek tek kontrol ediyoruz.
 Tani kuzhina është gati për familjen.|Jetzt ist die Küche für die Familie bereit.|Ora la cucina è pronta per la famiglia.|Сега кујната е подготвена за семејството.|Mutfak artık aile için hazır.
 Rexha po kontrollon tubat dhe lidhjet.|Rexha prüft Rohre und Anschlüsse.|Rexha controlla tubi e collegamenti.|Реџа ги проверува цевките и приклучоците.|Rexha boruları ve bağlantıları kontrol ediyor.
 Mevludini po kontrollon instalimin elektrik.|Mevludini prüft die Elektrik.|Mevludini controlla l’impianto elettrico.|Мевлудини ја проверува електричната инсталација.|Mevludini elektrik tesisatını kontrol ediyor.
 Punon! Gjithçka është në rregull.|Es funktioniert! Alles ist in Ordnung.|Funziona! È tutto a posto.|Работи! Сè е во ред.|Çalışıyor! Her şey yolunda.
 Salloni duhet të bëhet vendi ku mblidhemi të gjithë.|Das Wohnzimmer soll unser gemeinsamer Treffpunkt werden.|Il soggiorno deve diventare il posto dove riunirci tutti.|Дневната соба треба да биде местото каде што се собираме сите.|Salon hepimizin buluştuğu yer olmalı.
 Po riparojmë dhe montojmë mobiliet.|Wir reparieren die Möbel und bauen sie auf.|Ripariamo e montiamo i mobili.|Го поправаме и составуваме мебелот.|Mobilyaları onarıp kuruyoruz.
 Këtu do të kalojmë shumë mbrëmje bashkë.|Hier werden wir viele Abende zusammen verbringen.|Qui passeremo tante serate insieme.|Тука ќе поминеме многу вечери заедно.|Burada birlikte birçok akşam geçireceğiz.
 Ballkoni ka nevojë për siguri dhe pak gjelbërim.|Der Balkon braucht Sicherheit und etwas Grün.|Il balcone ha bisogno di sicurezza e un po’ di verde.|На балконот му треба безбедност и малку зеленило.|Balkonun güvenliğe ve biraz yeşilliğe ihtiyacı var.
 Po forcojmë pjesët e dobëta.|Wir verstärken die schwachen Stellen.|Rinforziamo le parti deboli.|Ги зацврстуваме слабите делови.|Zayıf kısımları güçlendiriyoruz.
 Nga këtu kopshti duket bukur.|Von hier sieht der Garten schön aus.|Da qui il giardino è bellissimo.|Оттука градината изгледа убаво.|Bahçe buradan çok güzel görünüyor.
 Le t’i vendosim lulet këtu.|Stellen wir die Blumen hier auf.|Mettiamo i fiori qui.|Да ги ставиме цвеќињата тука.|Çiçekleri buraya yerleştirelim.
 Na duhet një vend ku mjetet të jenë në rregull.|Wir brauchen einen ordentlichen Platz für die Werkzeuge.|Ci serve un posto per tenere in ordine gli attrezzi.|Ни треба место за уредно чување на алатите.|Aletleri düzenli tutacak bir yere ihtiyacımız var.
 Po montojmë tavolinën dhe raftet e punës.|Wir bauen Werkbank und Regale auf.|Montiamo il banco e gli scaffali da lavoro.|Ги составуваме работната маса и полиците.|Çalışma tezgâhını ve rafları kuruyoruz.
 Tani çdo mjet e ka vendin e vet.|Jetzt hat jedes Werkzeug seinen Platz.|Ora ogni attrezzo ha il suo posto.|Сега секој алат има свое место.|Artık her aletin kendi yeri var.
 Biçikletat duhen mbajtur të sigurta dhe të rregulluara.|Die Fahrräder brauchen einen sicheren, ordentlichen Platz.|Le biciclette vanno tenute al sicuro e in ordine.|Велосипедите треба да се чуваат безбедно и уредно.|Bisikletler güvenli ve düzenli tutulmalı.
 Po ndërtojmë mbajtëset prej druri.|Wir bauen die Holzhalterungen.|Costruiamo i supporti di legno.|Ги градиме дрвените држачи.|Ahşap tutucuları yapıyoruz.
 Tani biçikletat janë gati për xhiro!|Jetzt sind die Fahrräder bereit für eine Fahrt!|Ora le biciclette sono pronte per un giro!|Сега велосипедите се подготвени за возење!|Bisikletler artık geziye hazır!
 E vendosa biçikletën. Qëndron fort!|Ich habe das Fahrrad abgestellt. Es steht fest!|Ho messo la bicicletta. È stabile!|Го поставив велосипедот. Стои цврсто!|Bisikleti yerleştirdim. Sağlam duruyor!
 Oborri duhet të jetë i ndriçuar edhe në mbrëmje.|Der Hof soll auch abends beleuchtet sein.|Il cortile deve essere illuminato anche la sera.|Дворот треба да биде осветлен и навечер.|Avlu akşamları da aydınlık olmalı.
 Mevludini po lidh dritat me kujdes.|Mevludini schließt die Lichter sorgfältig an.|Mevludini collega le luci con cura.|Мевлудини внимателно ги поврзува светлата.|Mevludini ışıkları dikkatle bağlıyor.
 Dritat e bëjnë kopshtin magjik.|Die Lichter machen den Garten magisch.|Le luci rendono magico il giardino.|Светлата ја прават градината волшебна.|Işıklar bahçeyi büyülü kılıyor.
 U ndezën të gjitha!|Alle sind angegangen!|Si sono accese tutte!|Сите се вклучија!|Hepsi yandı!
 Fëmijët duan një vend ku të luajnë të sigurt.|Die Kinder wünschen sich einen sicheren Spielplatz.|I bambini vogliono un posto sicuro dove giocare.|Децата сакаат безбедно место за игра.|Çocuklar güvenle oynayacak bir yer istiyor.
 Po montojmë lojërat dhe po kontrollojmë çdo vidë.|Wir bauen die Spielgeräte auf und prüfen jede Schraube.|Montiamo i giochi e controlliamo ogni vite.|Ги монтираме справите и ја проверуваме секоја завртка.|Oyun donanımlarını kurup her vidayı kontrol ediyoruz.
 Hajde të luajmë!|Kommt, wir spielen!|Andiamo a giocare!|Ајде да играме!|Haydi oynayalım!
 Është e fortë dhe gati për lojë!|Es ist stabil und bereit zum Spielen!|È solido e pronto per giocare!|Цврсто е и подготвено за игра!|Sağlam ve oyun için hazır!
 Ka mbetur vetëm prekja e fundit para festës.|Vor dem Fest fehlen nur die letzten Handgriffe.|Manca solo l’ultimo ritocco prima della festa.|Останува само последниот детаљ пред прославата.|Kutlamadan önce son dokunuş kaldı.
 Po rregullojmë çdo detaj të fundit.|Wir kümmern uns um die letzten Einzelheiten.|Sistemiamo gli ultimi dettagli.|Ги средуваме последните детали.|Son ayrıntıları düzenliyoruz.
 Prona është gati. Ta festojmë së bashku!|Das Anwesen ist fertig. Feiern wir gemeinsam!|La tenuta è pronta. Festeggiamo insieme!|Имотот е готов. Да прославиме заедно!|Mülk hazır. Birlikte kutlayalım!
 Lulet dhe dekorimet po e mbushin oborrin me ngjyra.|Blumen und Dekorationen bringen Farbe in den Hof.|Fiori e decorazioni riempiono il cortile di colori.|Цвеќињата и украсите го исполнуваат дворот со бои.|Çiçekler ve dekorasyonlar avluyu renklendiriyor.
 Dritat e festës janë gati.|Die Festbeleuchtung ist bereit.|Le luci della festa sono pronte.|Светлата за прославата се подготвени.|Kutlama ışıkları hazır.
 Gjithçka është në vend!|Alles ist an seinem Platz!|È tutto al suo posto!|Сè е на свое место!|Her şey yerli yerinde!
 """.trimIndent().lines().map{it.trim().split('|')}.associate{row->check(row.size==5);row.first() to row}
 fun line(sq:String,language:String):String?=rows[sq]?.get(listOf("sq","de","it","mk","tr").indexOf(GameText.language(language)))
}

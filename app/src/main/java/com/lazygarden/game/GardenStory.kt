package com.lazygarden.game

enum class GardenCharacter(val displayName:String,val portrait:Int){
 LAZI("Lazi",R.drawable.character_meti),MELI("Meli",R.drawable.character_meli),
 BEJDI("Bejdi",R.drawable.character_bejdi),METI("Meti",R.drawable.character_meti),CUFI("Cufi",R.drawable.character_bejdi),RONA("Rona",R.drawable.character_lina),
 REJANI("Rejani",R.drawable.character_bejdi),MEDINA("Medina",R.drawable.character_lina),HAFSA("Hafsa",R.drawable.character_meli),UNEJSI("Unejsi",R.drawable.character_bejdi),
 GREKU("Greku",R.drawable.character_meti),REXHA("Rexha",R.drawable.character_meti),NERMIRI("Nermiri",R.drawable.character_meti),BEKA("Beka",R.drawable.character_meti),
 GEZIMI("Gëzimi",R.drawable.character_meti),BESI("Besi",R.drawable.character_meti),MEVLUDINI("Mevludini",R.drawable.character_meti),LULI("Luli",R.drawable.character_meti)
}
data class StoryLine(val character:GardenCharacter,val text:String)
data class StoryChapter(val title:String,val lines:List<StoryLine>)
object GardenStory {
 val chapters=listOf(
  StoryChapter("Shatërvani i vjetër",listOf(
   StoryLine(GardenCharacter.MELI,"Dëgjojeni shatërvanin! Rona dhe Cufi pastruan gjethet, dhe oborri ynë mori përsëri jetë."),
   StoryLine(GardenCharacter.LAZI,"Rexha e provoi pompën dhe tubat e rinj. Tani mund të ulemi këtu së bashku. Por pranë shatërvanit ende mungojnë lulet."),
   StoryLine(GardenCharacter.REXHA,"Uji rrjedh si duhet. Te kapaku gjeta një shenjë të vjetër në formë ylli. Ruajeni; ndoshta dikush nga familja e njeh."))),
  StoryChapter("Lulet e oborrit",listOf(
   StoryLine(GardenCharacter.RONA,"Mami, lulet u mbollën! Unë dhe Medina do t’i ujitim me kujdes."),
   StoryLine(GardenCharacter.MELI,"Gëzimi e përgatiti dheun dhe ne zgjodhëm lulet. Tani duhet të rregullojmë shtegun që të arrijmë te to pa u penguar."),
   StoryLine(GardenCharacter.GEZIMI,"Rrënjët janë mbuluar dhe lulet janë ujitur. Kujdesuni për to çdo ditë; një kopsht i bukur kërkon vazhdimisht kujdes."))),
  StoryChapter("Shtegu i sigurt",listOf(
   StoryLine(GardenCharacter.CUFI,"Babi, tani biçikleta nuk ngec më te gurët! Po e provoj shtegun e ri."),
   StoryLine(GardenCharacter.LAZI,"Ec ngadalë, Cufi. Greku e niveloi shtegun; tani kemi një kalim të sigurt mes luleve."),
   StoryLine(GardenCharacter.GREKU,"Gurët janë shtruar dhe bashkimet janë pastruar. Më tej shoh një stol të dëmtuar; atë do ta shikojë Luli."))),
  StoryChapter("Stoli për familjen",listOf(
   StoryLine(GardenCharacter.MELI,"Stoli nuk lëkundet më. Tani mund të ulemi të gjithë pranë kopshtit."),
   StoryLine(GardenCharacter.LAZI,"Luli ndërroi dërrasat, shtrëngoi vidat dhe e mbrojti drurin. Një punë e bërë me kujdes ndihet menjëherë."),
   StoryLine(GardenCharacter.LULI,"E provuam bashkë dhe qëndron fort. Tani kemi ku të pushojmë pas punës me gjelbërimin."))),
  StoryChapter("Kopshti bëhet i gjallë",listOf(
   StoryLine(GardenCharacter.BEJDI,"Unë dhe Meti mblodhëm gjethet. Sa shumë ndryshoi kjo pjesë e oborrit!"),
   StoryLine(GardenCharacter.MELI,"Puna juaj ndihmoi. Pemët dhe fidanët e rinj e lidhin kopshtin me shtegun e restauruar."),
   StoryLine(GardenCharacter.GEZIMI,"Dheu është përgatitur, fidanët janë mbjellë dhe gjelbërimi ka marrë jetë. Tani shohim edhe hyrjen që na pret përpara."))),
  StoryChapter("Hyrja e pronës",listOf(
   StoryLine(GardenCharacter.LAZI,"Porta hapet lehtë dhe fenerët ndriçojnë. Miqtë mund të hyjnë pa u penguar."),
   StoryLine(GardenCharacter.MELI,"Greku rregulloi portën, Beka e leu dhe Mevludini përfundoi ndriçimin. Secili bëri punën e vet."),
   StoryLine(GardenCharacter.MEVLUDINI,"Lidhjet dhe fenerët janë kontrolluar. Hyrja është gati; nga këtu vërehet edhe fasada që do të rregullojmë më pas."))),
  StoryChapter("Fasada e shtëpisë",listOf(
   StoryLine(GardenCharacter.MELI,"Shtëpia më në fund shkon me kopshtin. Dritaret dhe ngjyrat i dhanë një pamje të re."),
   StoryLine(GardenCharacter.LAZI,"Nermiri u kujdes për çatinë, Beka për fasadën dhe Besi për dritaret. Rezultati duket sepse punuam me radhë."),
   StoryLine(GardenCharacter.BEKA,"Çarjet u riparuan, suvaja dhe boja përfunduan. Nga jashtë shtëpia është gati; tani mund të vazhdojmë brenda."))),
  StoryChapter("Kapitulli 2 · Brenda shtëpisë",listOf(
   StoryLine(GardenCharacter.HAFSA,"Brenda është pastër dhe muret janë rregulluar. Këtu do të gjejmë një vend për librat."),
   StoryLine(GardenCharacter.UNEJSI,"Edhe lodrat do t’i mbajmë në rregull, që të kemi vend për të luajtur!"),
   StoryLine(GardenCharacter.LAZI,"Brendësia u riparua. Do t’i marrim dhomat një nga një, duke filluar me dritaret që ende kërkojnë kujdes."))),
  StoryChapter("Dritaret që sjellin dritë",listOf(
   StoryLine(GardenCharacter.MEDINA,"Tani dritarja hapet lehtë dhe dhoma ka shumë më tepër dritë."),
   StoryLine(GardenCharacter.MELI,"Besi i kontrolloi dorezat dhe izolimin. Dritaret e reja janë një ndryshim që e ndiejmë çdo ditë."),
   StoryLine(GardenCharacter.BESI,"Kornizat dhe xhamat u vendosën dhe u pastruan. Hapi tjetër është çatia, që dhomat të mbeten të mbrojtura kur bie shi."))),
  StoryChapter("Çatia pas shiut",listOf(
   StoryLine(GardenCharacter.REJANI,"Babi, vendi ku pikonte është i thatë. Tjegullat e reja duken nga kopshti."),
   StoryLine(GardenCharacter.LAZI,"Nermiri e kontrolloi çatinë dhe e përfundoi punën lart. Ne qëndruam poshtë dhe i sollëm materialet."),
   StoryLine(GardenCharacter.NERMIRI,"Pjesët e dëmtuara u zëvendësuan dhe lidhjet u forcuan. Tani mund të vazhdoni me dhomën e fëmijëve."))),
  StoryChapter("Dhoma e fëmijëve",listOf(
   StoryLine(GardenCharacter.HAFSA,"Rafti dhe tavolina janë gati! Këtu mund të lexoj dhe të vizatoj."),
   StoryLine(GardenCharacter.UNEJSI,"Lodrat kanë kutitë e tyre. Tani dhoma është më e rregullt dhe kemi vend për lojë."),
   StoryLine(GardenCharacter.LULI,"Mobiliet u montuan dhe u kontrolluan. Më pëlqen që ju zgjodhët si do ta përdorni dhomën; tani na pret kuzhina."))),
  StoryChapter("Kuzhina e vjetër",listOf(
   StoryLine(GardenCharacter.MELI,"Dollapët dhe sipërfaqet e kuzhinës u rregulluan. Shtëpia po bëhet më praktike."),
   StoryLine(GardenCharacter.LAZI,"Drita dhe uji kërkojnë kontrollin e tyre. Nuk do t’i ngatërrojmë mobiliet e përfunduara me instalimet që kemi ende për të provuar."),
   StoryLine(GardenCharacter.REXHA,"Pjesa e mobilieve është gati. Te detyra tjetër do të përfundojmë tubat dhe lidhjet, para se ta përdorim kuzhinën plotësisht."))),
  StoryChapter("Drita në kuzhinë",listOf(
   StoryLine(GardenCharacter.METI,"Drita e re e ndriçon gjithë kuzhinën. Tani shohim edhe këndet që ishin të errëta."),
   StoryLine(GardenCharacter.MEVLUDINI,"Instalimi elektrik u kontrollua, ndërsa Rexha përfundoi lidhjet e ujit. Provat e fundit dolën si duhet."),
   StoryLine(GardenCharacter.LAZI,"Uji dhe ndriçimi janë gati. Tani kuzhina mund t’i shërbejë familjes, ndërsa ne vazhdojmë me sallonin."))),
  StoryChapter("Salloni i familjes",listOf(
   StoryLine(GardenCharacter.MELI,"Salloni është bërë vendi ynë i përbashkët. Raftet dhe mobiliet më në fund kanë rend."),
   StoryLine(GardenCharacter.BEJDI,"Këndi i leximit doli bukur. Këtu do t’i sjellim librat kur të mblidhemi në mbrëmje."),
   StoryLine(GardenCharacter.LULI,"Raftet dhe pjesët e montuara janë kontrolluar. Tani mund të ulemi rehat; më tej na pret ballkoni."))),
  StoryChapter("Ballkoni i harruar",listOf(
   StoryLine(GardenCharacter.RONA,"Nga ballkoni shihet shatërvani, lulet dhe shtegu që rregulluam. Sa shumë kemi bërë!"),
   StoryLine(GardenCharacter.MELI,"Greku forcoi pjesët e dobëta dhe Gëzimi solli gjelbërimin. Tani kemi edhe një kënd të qetë jashtë."),
   StoryLine(GardenCharacter.GEZIMI,"Lulet dhe vazot janë vendosur. Kujdesuni për ujitjen; ndërkohë veglat e punëve tona kanë nevojë për një punishte."))),
  StoryChapter("Punishtja e vogël",listOf(
   StoryLine(GardenCharacter.CUFI,"Tani veglat nuk janë më të shpërndara. Në punishte e gjejmë çdo gjë në vendin e vet."),
   StoryLine(GardenCharacter.LAZI,"Tavolina dhe raftet e Lulit na ndihmojnë të punojmë me rregull. Do t’i mbajmë mjetet e rrezikshme larg fëmijëve."),
   StoryLine(GardenCharacter.GREKU,"Punishtja u rregullua dhe ndriçimi u përfundua. Më pas do t’u japim edhe biçikletave një vend të veçantë."))),
  StoryChapter("Këndi i biçikletave",listOf(
   StoryLine(GardenCharacter.CUFI,"Biçikleta ime qëndron në mbajtëse dhe nuk bie më në tokë."),
   StoryLine(GardenCharacter.BEJDI,"Edhe helmetat i varëm pranë tyre. Tani mund të përgatitemi shpejt për xhiro."),
   StoryLine(GardenCharacter.GREKU,"Mbajtëset dhe grepat u fiksuan e u provuan. Këndi është gati; në mbrëmje do të na ndihmojë edhe ndriçimi i oborrit."))),
  StoryChapter("Oborri i mbrëmjes",listOf(
   StoryLine(GardenCharacter.MELI,"Tani kopshti duket bukur edhe kur errësohet. Dritat na tregojnë shtegun."),
   StoryLine(GardenCharacter.LAZI,"Mevludini i kontrolloi lidhjet dhe Gëzimi rregulloi bimët përreth. Mund të shijojmë edhe mbrëmjet këtu."),
   StoryLine(GardenCharacter.MEVLUDINI,"Fenerët u ndezën dhe u provuan. Mbajini lidhjet të mbrojtura; hapësira pranë tyre mund të bëhet zona e lojës."))),
  StoryChapter("Zona e lojës",listOf(
   StoryLine(GardenCharacter.UNEJSI,"Zona jonë e lojës është gati! Lojërat qëndrojnë fort dhe kemi vend për të lëvizur."),
   StoryLine(GardenCharacter.RONA,"Edhe tavolina për vizatime është këtu. Do ta mbajmë hapësirën të pastër pas lojës."),
   StoryLine(GardenCharacter.LAZI,"Montimi dhe provat përfunduan. Tani na mbeten prekjet e fundit që ta festojmë gjithë këtë punë së bashku."))),
  StoryChapter("Festa e kopshtit",listOf(
   StoryLine(GardenCharacter.MELI,"Dritat, lulet dhe këndet e familjes u bënë bashkë. Prona ka ndryshuar nga puna e të gjithëve."),
   StoryLine(GardenCharacter.LAZI,"Shatërvani ishte fillimi; më pas çdo detyrë na mësoi të përgatitemi, të ndihmojmë dhe ta përfundojmë punën me kujdes."),
   StoryLine(GardenCharacter.BEJDI,"Le ta festojmë kopshtin e restauruar! Tani kemi një vend ku mund të luajmë, të pushojmë dhe të kalojmë kohë së bashku.")))
 )
 const val LINES_PER_CHAPTER=3
 init {
  check(chapters.size==RestorationInventory.TASK_COUNT){"Story chapters must match the 20 restoration campaign"}
  check(chapters.size==RestorationCatalog.entries.size){"Story and restoration catalog must stay aligned"}
  check(chapters.all{it.lines.size==LINES_PER_CHAPTER}){"Every story chapter must keep a complete 3-line scene"}
 }
}
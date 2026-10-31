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
   StoryLine(GardenCharacter.MELI,"Lazi, shatërvani s'po jep ujë. Fëmijët po presin ta shohin duke punuar."),
   StoryLine(GardenCharacter.LAZI,"Po e shoh. Këtu duhet Rexha. Po e thërras hidraulikun."),
   StoryLine(GardenCharacter.REXHA,"Pompa është bllokuar. Më duhen pjesët dhe veglat; mblidhini në nivel dhe e rregullojmë."))),
  StoryChapter("Lulet e oborrit",listOf(
   StoryLine(GardenCharacter.RONA,"Mami, a mund të kemi lule këtu? Unë dhe Medina i ujitim."),
   StoryLine(GardenCharacter.MELI,"Po. Lazi, për tokën më mirë ta thërrasim Gëzimin."),
   StoryLine(GardenCharacter.GEZIMI,"Mblidhni fidanët, dheun dhe ujitësen. Pastaj i mbjellim bashkë."))),
  StoryChapter("Shtegu i sigurt",listOf(
   StoryLine(GardenCharacter.CUFI,"Babi, biçikleta ngec te gurët e thyer!"),
   StoryLine(GardenCharacter.LAZI,"Mos kaloni aty derisa ta rregullojmë. Meli, po e thërras Grekun ta shohë."),
   StoryLine(GardenCharacter.GREKU,"Duhet pastruar dhe niveluar. Na duhen gurë, rërë dhe vegla para se të fillojmë."))),
  StoryChapter("Stoli për familjen",listOf(
   StoryLine(GardenCharacter.MELI,"Ky stol mezi mbahet. Fëmijët s'duhet të ulen ende."),
   StoryLine(GardenCharacter.LAZI,"Për dru e thërrasim Lulin. Ai e bën si duhet."),
   StoryLine(GardenCharacter.LULI,"Më sillni dërrasat, vidat, letrën e zmeriles dhe bojën. Pastaj e riparojmë e e provojmë."))),
  StoryChapter("Kopshti bëhet i gjallë",listOf(
   StoryLine(GardenCharacter.BEJDI,"Unë e Meti mbledhim gjethet. Cufi e Rona mund t'i sjellin veglat."),
   StoryLine(GardenCharacter.MELI,"Shumë mirë. Gëzimi na tregon çfarë duhet mbjellë."),
   StoryLine(GardenCharacter.GEZIMI,"Na duhen fara, fidanë dhe pleh. Kur t'i kemi, fillojmë gjelbërimin."))),
  StoryChapter("Hyrja e pronës",listOf(
   StoryLine(GardenCharacter.LAZI,"Porta ngec dhe drita jashtë s'punon. Janë dy punë të ndryshme."),
   StoryLine(GardenCharacter.MELI,"Thirre Grekun për portën dhe Mevludinin për rrymën."),
   StoryLine(GardenCharacter.MEVLUDINI,"Unë kontrolloj kabllot e fenerët. Pa pjesët e duhura nuk e ndezim rrymën."))),
  StoryChapter("Fasada e shtëpisë",listOf(
   StoryLine(GardenCharacter.MELI,"Kopshti u bë bukur. Tani shihet sa e lodhur është shtëpia."),
   StoryLine(GardenCharacter.LAZI,"Beka merret me fasadën, Besi me dritaret dhe Nermiri me çatinë. Po i thërrasim me radhë."),
   StoryLine(GardenCharacter.BEKA,"Së pari materialet dhe skela. Pastaj riparojmë, suvatojmë dhe lyejmë; jo punë sa për sy e faqe."))),
  StoryChapter("Kapitulli 2 · Brenda shtëpisë",listOf(
   StoryLine(GardenCharacter.HAFSA,"Mami, a mund ta bëjmë një kënd me libra në dhomë?"),
   StoryLine(GardenCharacter.UNEJSI,"Edhe unë dua një vend për lodrat!"),
   StoryLine(GardenCharacter.LAZI,"Po. Kopshti ishte vetëm fillimi. Tani hyjmë brenda dhe secila dhomë ka tregimin e vet."))),
  StoryChapter("Dritaret që sjellin dritë",listOf(
   StoryLine(GardenCharacter.MEDINA,"Mami, kjo dritare mezi hapet dhe dhoma është e errët."),
   StoryLine(GardenCharacter.MELI,"Lazi, thirre Besin. Duam dritë, por edhe dritare të sigurta për fëmijët."),
   StoryLine(GardenCharacter.BESI,"Më duhen xhami, doreza dhe izolim. Kur t'i fitoni, i ndërrojmë një nga një."))),
  StoryChapter("Çatia pas shiut",listOf(
   StoryLine(GardenCharacter.REJANI,"Babi, këtu ra një pikë uji kur binte shi."),
   StoryLine(GardenCharacter.LAZI,"Askush nuk ngjitet lart. Nermiri do ta kontrollojë çatinë."),
   StoryLine(GardenCharacter.NERMIRI,"Ka tjegulla të dëmtuara. Mblidhni materialet; punën lart e bëj unë."))),
  StoryChapter("Dhoma e fëmijëve",listOf(
   StoryLine(GardenCharacter.HAFSA,"Dua raft për libra dhe një tavolinë ku mund të vizatojmë."),
   StoryLine(GardenCharacter.UNEJSI,"Dhe kuti për lodrat, që të mos jenë gjithandej!"),
   StoryLine(GardenCharacter.LULI,"Do të ndërtojmë raftin, tavolinën dhe kutitë. Na duhen dru, vida dhe bojë."))),
  StoryChapter("Kuzhina e vjetër",listOf(
   StoryLine(GardenCharacter.MELI,"Kuzhina ka nevojë për rregullim para se ta përdorim si duhet."),
   StoryLine(GardenCharacter.LAZI,"Rexha kontrollon ujin, Mevludini rrymën. Pastaj zgjedhim mobiliet."),
   StoryLine(GardenCharacter.REXHA,"Fillojmë me tubat dhe lavamanin. Materialet duhet t'i kemi gati para montimit."))),
  StoryChapter("Drita në kuzhinë",listOf(
   StoryLine(GardenCharacter.METI,"Këtu është errët edhe kur jemi të gjithë brenda."),
   StoryLine(GardenCharacter.MEVLUDINI,"Do të vendosim ndriçim të ri dhe priza të sigurta."),
   StoryLine(GardenCharacter.LAZI,"Mirë. Pjesët i fitojmë me nivelet dhe pastaj fillon puna."))),
  StoryChapter("Salloni i familjes",listOf(
   StoryLine(GardenCharacter.MELI,"Këtu dua një vend ku të mblidhemi të gjithë, jo vetëm mobilie të shpërndara."),
   StoryLine(GardenCharacter.BEJDI,"Mund të ndihmojmë të zgjedhim këndin e leximit."),
   StoryLine(GardenCharacter.LULI,"Së pari dyshemeja dhe raftet; pastaj vendosim pjesët dekorative."))),
  StoryChapter("Ballkoni i harruar",listOf(
   StoryLine(GardenCharacter.RONA,"Nga këtu shihet gjithë kopshti, por ballkoni është bosh."),
   StoryLine(GardenCharacter.MELI,"Do ta bëjmë një kënd të qetë me lule dhe ulëse."),
   StoryLine(GardenCharacter.GEZIMI,"Më sillni vazot, dheun dhe fidanët. Pastaj e gjelbërojmë."))),
  StoryChapter("Punishtja e vogël",listOf(
   StoryLine(GardenCharacter.CUFI,"Babi, ku do t'i mbajmë biçikletat dhe veglat?"),
   StoryLine(GardenCharacter.LAZI,"Garazhi i vogël do të bëhet punishte dhe depo."),
   StoryLine(GardenCharacter.GREKU,"Duhet pastrim, rafte dhe një derë që mbyllet mirë. Fillojmë pasi të kemi materialet."))),
  StoryChapter("Këndi i biçikletave",listOf(
   StoryLine(GardenCharacter.CUFI,"Tani dua një vend ku biçikleta të mos bjerë në tokë."),
   StoryLine(GardenCharacter.BEJDI,"Edhe helmetat mund t'i varim pranë tyre."),
   StoryLine(GardenCharacter.GREKU,"Mbajtëset, grepat dhe dërrasat fitohen në nivelet e ardhshme."))),
  StoryChapter("Oborri i mbrëmjes",listOf(
   StoryLine(GardenCharacter.MELI,"Kopshti është bukur ditën. Tani duhet të jetë i këndshëm edhe në mbrëmje."),
   StoryLine(GardenCharacter.LAZI,"Mevludini vendos ndriçimin e jashtëm dhe Gëzimi rregullon bimët rreth tij."),
   StoryLine(GardenCharacter.MEVLUDINI,"Fenerët dhe kabllot i vendosim vetëm pasi të kontrollojmë çdo lidhje."))),
  StoryChapter("Zona e lojës",listOf(
   StoryLine(GardenCharacter.UNEJSI,"A mund të kemi një vend vetëm për lojë?"),
   StoryLine(GardenCharacter.RONA,"Dhe një tavolinë të vogël për vizatime jashtë."),
   StoryLine(GardenCharacter.LAZI,"Po. Do ta ndërtojmë pasi të fitojmë materialet dhe yjet e nevojshëm."))),
  StoryChapter("Festa e kopshtit",listOf(
   StoryLine(GardenCharacter.MELI,"Shiko sa shumë ndryshoi shtëpia. Por kemi ende punë përpara."),
   StoryLine(GardenCharacter.LAZI,"Sot familja e provon kopshtin e restauruar; nesër hapim zonën tjetër të pronës."),
   StoryLine(GardenCharacter.BEJDI,"Atëherë le ta festojmë këtë pjesë dhe pastaj vazhdojmë me aventurën tjetër!")))
 )
 const val LINES_PER_CHAPTER=3
 init {
  check(chapters.size==RestorationInventory.TASK_COUNT){"Story chapters must match the 20 restoration campaign"}
  check(chapters.size==RestorationCatalog.entries.size){"Story and restoration catalog must stay aligned"}
  check(chapters.all{it.lines.size==LINES_PER_CHAPTER}){"Every story chapter must keep a complete 3-line scene"}
 }
}
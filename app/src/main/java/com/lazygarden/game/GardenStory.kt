package com.lazygarden.game

enum class GardenCharacter(val displayName:String,val portrait:Int){
 METI("Meti",R.drawable.character_meti),MELI("Meli",R.drawable.character_meli),BEJDI("Bejdi",R.drawable.character_bejdi),LINA("Lina",R.drawable.character_lina),
 CUFI("Cufi",R.drawable.character_bejdi),RONA("Rona",R.drawable.character_lina),MEDINA("Medina",R.drawable.character_lina),HAFSA("Hafsa",R.drawable.character_meli),
 GEZIMI("Gëzimi",R.drawable.character_meti),HURIJA("Hurija",R.drawable.character_meli),MEKANIKU("Mekaniku",R.drawable.character_meti)
}
data class StoryLine(val character:GardenCharacter,val text:String)
data class StoryChapter(val title:String,val lines:List<StoryLine>)
object GardenStory {
 val chapters=listOf(
  StoryChapter("Zëri i ujit",listOf(
   StoryLine(GardenCharacter.MELI,"Dëgjo! Uji u kthye në shatërvan. Kur isha e vogël, ky tingull na mblidhte çdo mbrëmje në kopsht."),
   StoryLine(GardenCharacter.BEJDI,"Gjeta disa gurë nën barin e egër. Ndoshta shtegu nuk është zhdukur; vetëm po pret të zbulohet!"),
   StoryLine(GardenCharacter.METI,"Atëherë ky është hapi ynë i radhës. Të fitojmë yje dhe t'i hapim përsëri rrugë shtëpisë."))),
  StoryChapter("Shtegu i kujtimeve",listOf(
   StoryLine(GardenCharacter.BEJDI,"Shtegu shkon deri te porta e vjetër. Në njërin gur është gdhendur një lule."),
   StoryLine(GardenCharacter.MELI,"E kishte bërë gjyshi. Ai thoshte se çdo rrugë e bukur fillon me një hap të vogël."),
   StoryLine(GardenCharacter.LINA,"Unë dua që përgjatë këtij shtegu të ketë lule të vërteta. Mund të fillojmë me një kënd të vogël?"))),
  StoryChapter("Fëmijët e kopshtit",listOf(
   StoryLine(GardenCharacter.CUFI,"Unë jam më i vogli, por mund t'i mbledh gjethet pranë shatërvanit!"),
   StoryLine(GardenCharacter.RONA,"Unë dhe Medina do t'i ndajmë farat sipas ngjyrave. Hafsa mund të na ndihmojë me vazot."),
   StoryLine(GardenCharacter.MEDINA,"Kur të mbarojmë, dua të mbjellim një kënd që të ndryshojë ngjyrë çdo stinë."))),
  StoryChapter("Punë për të gjithë",listOf(
   StoryLine(GardenCharacter.HAFSA,"Vazot janë gati. Tani secili ka një punë të vogël dhe kopshti po merr jetë."),
   StoryLine(GardenCharacter.GEZIMI,"Unë do të kontrolloj gardhin dhe ndriçimin. Disa pjesë duhen rregulluar para mbrëmjes."),
   StoryLine(GardenCharacter.HURIJA,"Ndërsa ju punoni jashtë, unë do të përgatis verandën që të kemi ku të pushojmë."))),
  StoryChapter("Punishtja e vjetër",listOf(
   StoryLine(GardenCharacter.MEKANIKU,"Pompa e vjetër nuk ka mbaruar punë. Më sillni pjesët dhe do ta bëjmë shatërvanin të punojë si dikur."),
   StoryLine(GardenCharacter.BEJDI,"Unë mund t'i kërkoj pjesët pranë depos. Meti, eja me mua!"),
   StoryLine(GardenCharacter.METI,"Dakord. Pasi të fitojmë nivelin tjetër, hapim depon dhe fillojmë riparimin."))),
  StoryChapter("Ngjyrat e para",listOf(
   StoryLine(GardenCharacter.LINA,"Shiko sa bukur! Nuk e dija që kjo tokë e harruar mund të mbante kaq shumë ngjyra."),
   StoryLine(GardenCharacter.METI,"Tani kemi edhe një vend për dekorime. Zgjedhja është e jotja: një stol, një hark me lule apo një fener."),
   StoryLine(GardenCharacter.MELI,"Nuk duhet t'i blejmë të gjitha. Le të luajmë, të fitojmë Game Coins dhe ta ndërtojmë kopshtin me durim."))),
  StoryChapter("Një vend për të gjithë",listOf(
   StoryLine(GardenCharacter.METI,"Stoli është gati. Gjyshi pushonte këtu pas punës dhe dëgjonte zogjtë."),
   StoryLine(GardenCharacter.BEJDI,"Unë do të ruaj këtu farat që gjetëm në shtëpi. Në kuti shkruan: për pranverën e ardhshme."),
   StoryLine(GardenCharacter.LINA,"Atëherë duhet të përgatisim vendin për pemët. Do të rriten bashkë me historinë tonë."))),
  StoryChapter("Rrënjët e së ardhmes",listOf(
   StoryLine(GardenCharacter.LINA,"Pemët e reja po ia ndryshojnë krejt pamjen kopshtit. Mund të zgjedhim edhe një pemë dekorative!"),
   StoryLine(GardenCharacter.MELI,"Çdo pemë ka kohën e vet. Ne do t'i japim ujë dhe kujdes; pjesën tjetër do ta bëjë pranvera."),
   StoryLine(GardenCharacter.BEJDI,"Nga këtu shihet hyrja. Kur ta rregullojmë, fqinjët më në fund do të mund të hyjnë pa pengesa."))),
  StoryChapter("Porta e hapur",listOf(
   StoryLine(GardenCharacter.BEJDI,"Porta u hap pa kërcitur! Kjo është hera e parë që nga dita kur erdhëm."),
   StoryLine(GardenCharacter.GEZIMI,"Ndriçimi i hyrjes është gati. Tani mund të punojmë edhe kur bie muzgu."),
   StoryLine(GardenCharacter.HURIJA,"Le ta bëjmë këtë pjesë mikpritëse. Kopshti duhet të duket i gjallë, jo vetëm i rregulluar."))),
  StoryChapter("Shtëpia merr jetë",listOf(
   StoryLine(GardenCharacter.MELI,"Shtëpia jonë ndriçon përsëri. Kemi ruajtur kujtimet e vjetra dhe kemi krijuar vend për kujtime të reja."),
   StoryLine(GardenCharacter.RONA,"Ne ende kemi shumë qoshe për të zbuluar. Shtegu pas shtëpisë vazhdon drejt serrës së vjetër."),
   StoryLine(GardenCharacter.METI,"Atëherë ky nuk është fundi. Çdo nivel i ri do të hapë një punë, një vend dhe një pjesë tjetër të historisë.")))
 )
 const val LINES_PER_CHAPTER=3
}

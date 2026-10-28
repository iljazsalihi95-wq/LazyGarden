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
   StoryLine(GardenCharacter.LAZI,"Po. Kopshti ishte vetëm fillimi. Tani hyjmë brenda dhe secila dhomë ka tregimin e vet.")))
 )
 const val LINES_PER_CHAPTER=3
}

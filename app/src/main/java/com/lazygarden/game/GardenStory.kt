package com.lazygarden.game

enum class GardenCharacter(val displayName:String,val portrait:Int){
 METI("Meti",R.drawable.character_meti),MELI("Meli",R.drawable.character_meli),BEJDI("Bejdi",R.drawable.character_bejdi),LINA("Lina",R.drawable.character_lina),
 CUFI("Cufi",R.drawable.character_bejdi),RONA("Rona",R.drawable.character_lina),MEDINA("Medina",R.drawable.character_lina),HAFSA("Hafsa",R.drawable.character_meli),
 GEZIMI("Gëzimi",R.drawable.character_meti),HURIJA("Hurija",R.drawable.character_meli),MEKANIKU("Mekaniku",R.drawable.character_meti),MERJEM("Merjem",R.drawable.character_meli)
}
data class StoryLine(val character:GardenCharacter,val text:String)
data class StoryChapter(val title:String,val lines:List<StoryLine>)
object GardenStory {
 val chapters=listOf(
  StoryChapter("Zëri i ujit",listOf(StoryLine(GardenCharacter.METI,"Uji u kthye. Tani le t’i japim ngjyra kopshtit."),StoryLine(GardenCharacter.MERJEM,"Unë do të zgjedh lulet. Meli, do të më ndihmosh t’i mbjellim?"),StoryLine(GardenCharacter.MELI,"Me kënaqësi! Fitojmë yllin tjetër dhe fillojmë mbjelljen."))),
  StoryChapter("Lulet e Merjemit",listOf(StoryLine(GardenCharacter.MERJEM,"Lulet e zgjedhura do të qëndrojnë këtu. Do t’i ujitim çdo ditë."),StoryLine(GardenCharacter.BEJDI,"Por gurët e shtegut janë çarë. Duhet t’i rregullojmë që të kalojmë pranë tyre."),StoryLine(GardenCharacter.METI,"Fitojmë Coins dhe një yll në Match-3, pastaj fillojmë me gurët."))),
  StoryChapter("Shtegu i kujtimeve",listOf(StoryLine(GardenCharacter.BEJDI,"Shtegu është përsëri i sigurt. Çdo gur u vendos me kujdes."),StoryLine(GardenCharacter.MELI,"Pas gjithë kësaj pune dua të ulem. Por stoli është dëmtuar!"),StoryLine(GardenCharacter.METI,"Atëherë puna tjetër është stoli. Do t’i zëvendësojmë dërrasat."))),
  StoryChapter("Pushim në kopsht",listOf(StoryLine(GardenCharacter.MELI,"Stoli është i qëndrueshëm dhe boja u tha. Më në fund mund të pushojmë."),StoryLine(GardenCharacter.MERJEM,"Nga këtu shihet zona e tharë. Le t’i kthejmë gjelbërimin."),StoryLine(GardenCharacter.BEJDI,"Unë përgatit tokën. Ti merr pemët e reja pas nivelit tjetër."))),
  StoryChapter("Rrënjët e së ardhmes",listOf(StoryLine(GardenCharacter.MERJEM,"Pemët janë mbjellë dhe shkurret po çelin. Kopshti ka nevojë për kujdes çdo ditë."),StoryLine(GardenCharacter.METI,"Miqtë do të vijnë ta shohin, por porta ende ngec."),StoryLine(GardenCharacter.BEJDI,"Puna tjetër: menteshat, shtyllat, boja dhe fenerët e hyrjes."))),
  StoryChapter("Porta e hapur",listOf(StoryLine(GardenCharacter.BEJDI,"Porta hapet lirshëm dhe fenerët ndriçojnë hyrjen."),StoryLine(GardenCharacter.MELI,"Tani mbetet shtëpia. Dritaret dhe fasada duan shumë punë."),StoryLine(GardenCharacter.METI,"Do ta bëjmë së bashku: skela, riparim, suva, bojë dhe lulet e fundit."))),
  StoryChapter("Shtëpia merr jetë",listOf(StoryLine(GardenCharacter.MERJEM,"Shtëpia ndriçon përsëri. Lulet, shtegu dhe pemët e bëjnë këtë vend tonin."),StoryLine(GardenCharacter.MELI,"E mbaj mend si dukej ditën e parë. Sa shumë kemi ndryshuar!"),StoryLine(GardenCharacter.METI,"Shtatë punë të përfunduara. Nivelet e tjera dhe dekorimet na presin.")))
 )
 const val LINES_PER_CHAPTER=3
}

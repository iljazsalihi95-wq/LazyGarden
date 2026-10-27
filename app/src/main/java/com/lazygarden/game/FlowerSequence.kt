package com.lazygarden.game

/** Second restoration: Merjem leads the flower choice and planting. Pure timeline logic is testable. */
object FlowerSequence {
 const val DURATION=31_000L
 enum class Stage { GATHER, TALK, CHOOSE, PREPARE, PLANT, WATER, BLOOM, CELEBRATE }
 data class FlowerChoice(val id:String,val nameSq:String,val nameDe:String,val nameIt:String,val nameMk:String,val nameTr:String,val color:Int)
 val choices=listOf(
  FlowerChoice("tulip","Tulipanë","Tulpen","Tulipani","Лалиња","Laleler",0xffff5b73.toInt()),
  FlowerChoice("lavender","Livando","Lavendel","Lavanda","Лаванда","Lavanta",0xff9a78df.toInt()),
  FlowerChoice("daisy","Margarita","Gänseblümchen","Margherite","Маргаритки","Papatyalar",0xfffff4c4.toInt())
 )
 fun stage(ms:Long)=when(ms.coerceIn(0,DURATION)){
  in 0..3999->Stage.GATHER; in 4000..7499->Stage.TALK; in 7500..11999->Stage.CHOOSE
  in 12000..15999->Stage.PREPARE; in 16000..21999->Stage.PLANT; in 22000..25499->Stage.WATER
  in 25500..28499->Stage.BLOOM; else->Stage.CELEBRATE
 }
 fun approach(ms:Long)= (ms/3600f).coerceIn(0f,1f)
 fun prepare(ms:Long)=((ms-12000)/4000f).coerceIn(0f,1f)
 fun planting(ms:Long)=((ms-16000)/6000f).coerceIn(0f,1f)
 fun watering(ms:Long)=((ms-22000)/3500f).coerceIn(0f,1f)
 fun bloom(ms:Long)=((ms-25500)/3000f).coerceIn(0f,1f)
 fun speaker(ms:Long)=when(ms){in 4000..5799->"meti";in 5800..7499->"merjem";in 28500..DURATION->"merjem";else->null}
 fun dialogue(ms:Long,lang:String="sq")=when(stage(ms)){
  Stage.TALK->if(ms<5800)tr(lang,"Merjem, Meli, zgjidhni lulet!","Merjem, Meli, sucht die Blumen aus!","Merjem, Meli, scegliete i fiori!","Мерјем, Мели, изберете ги цвеќињата!","Merjem, Meli, çiçekleri seçin!") else tr(lang,"Meli, do t’i mbjellim bashkë!","Meli, wir pflanzen sie zusammen!","Meli, li pianteremo insieme!","Мели, ќе ги засадиме заедно!","Meli, birlikte dikeceğiz!")
  Stage.CELEBRATE->tr(lang,"Sa bukur! Kopshti po merr jetë.","Wie schön! Der Garten wird lebendig.","Che bello! Il giardino prende vita.","Колку убаво! Градината оживува.","Ne güzel! Bahçe canlanıyor.")
  else->""
 }
 private fun tr(lang:String,sq:String,de:String,it:String,mk:String,tr:String)=when(lang){"de"->de;"it"->it;"mk"->mk;"tr"->tr;else->sq}
}

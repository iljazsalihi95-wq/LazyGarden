package com.lazygarden.game

/** Second restoration: a playable flower-bed arc with family + gardener beats. Pure timeline logic stays testable. */
object FlowerSequence {
 const val DURATION=38_000L
 enum class Stage { GATHER, TALK, CALL_GEZIMI, GEZIMI_ARRIVAL, CHOOSE, PREPARE, PLANT, WATER, BLOOM, CELEBRATE }
 data class FlowerChoice(val id:String,val nameSq:String,val nameDe:String,val nameIt:String,val nameMk:String,val nameTr:String,val color:Int)
 val choices=listOf(
  FlowerChoice("tulip","Tulipanë","Tulpen","Tulipani","Лалиња","Laleler",0xffff5b73.toInt()),
  FlowerChoice("lavender","Livando","Lavendel","Lavanda","Лаванда","Lavanta",0xff9a78df.toInt()),
  FlowerChoice("daisy","Margarita","Gänseblümchen","Margherite","Маргаритки","Papatyalar",0xfffff4c4.toInt())
 )
 fun stage(ms:Long)=when(ms.coerceIn(0,DURATION)){
  in 0..3499->Stage.GATHER
  in 3500..6999->Stage.TALK
  in 7000..9499->Stage.CALL_GEZIMI
  in 9500..12999->Stage.GEZIMI_ARRIVAL
  in 13000..16999->Stage.CHOOSE
  in 17000..20999->Stage.PREPARE
  in 21000..26999->Stage.PLANT
  in 27000..30499->Stage.WATER
  in 30500..34499->Stage.BLOOM
  else->Stage.CELEBRATE
 }
 fun approach(ms:Long)=((ms-9500)/3500f).coerceIn(0f,1f)
 fun prepare(ms:Long)=((ms-17000)/4000f).coerceIn(0f,1f)
 fun planting(ms:Long)=((ms-21000)/6000f).coerceIn(0f,1f)
 fun watering(ms:Long)=((ms-27000)/3500f).coerceIn(0f,1f)
 fun bloom(ms:Long)=((ms-30500)/4000f).coerceIn(0f,1f)
 fun speaker(ms:Long)=when(stage(ms)){
  Stage.TALK->if(ms<5200)"rona" else "meli"
  Stage.CALL_GEZIMI->"lazi"
  Stage.GEZIMI_ARRIVAL,Stage.PREPARE->"gezimi"
  Stage.CELEBRATE->"meli"
  else->null
 }
 fun actorState(ms:Long,id:String):CharacterAnimation.State=when{
  stage(ms)==Stage.GEZIMI_ARRIVAL&&id=="gezimi"->CharacterAnimation.State.WALK
  stage(ms)==Stage.PREPARE&&id=="gezimi"->CharacterAnimation.State.WORK
  stage(ms)==Stage.PLANT&&id in setOf("meli","rona","gezimi")->CharacterAnimation.State.WORK
  stage(ms)==Stage.WATER&&id in setOf("rona","meli")->CharacterAnimation.State.WORK
  stage(ms)==Stage.CELEBRATE&&id in setOf("lazi","meli","rona","gezimi")->CharacterAnimation.State.HAPPY
  speaker(ms)==id->CharacterAnimation.State.TALK
  else->CharacterAnimation.State.IDLE
 }
 fun dialogue(ms:Long,lang:String="sq")=when(stage(ms)){
  Stage.TALK->if(ms<5200)tr(lang,
   "Mami, a mund të kemi lule këtu? Unë i ujit.","Mama, können wir hier Blumen pflanzen? Ich gieße sie.","Mamma, possiamo mettere dei fiori qui? Li annaffio io.","Мамо, може ли тука да засадиме цвеќиња? Јас ќе ги полевам.","Anne, buraya çiçek ekebilir miyiz? Ben sularım.") else tr(lang,
   "Po. Për tokën dhe mbjelljen na duhet Gëzimi.","Ja. Für Boden und Pflanzung brauchen wir Gëzimi.","Sì. Per il terreno e la piantagione ci serve Gëzimi.","Да. За земјата и садењето ни треба Gëzimi.","Evet. Toprak ve dikim için Gëzimi'ye ihtiyacımız var.")
  Stage.CALL_GEZIMI->tr(lang,"Po e thërras Gëzimin. Ai na tregon çfarë duhet mbledhur.","Ich rufe Gëzimi. Er sagt uns, was wir sammeln müssen.","Chiamo Gëzimi. Ci dirà cosa dobbiamo raccogliere.","Ќе му се јавам на Gëzimi. Тој ќе ни каже што треба да собереме.","Gëzimi'yi arıyorum. Neleri toplamamız gerektiğini söyleyecek.")
  Stage.GEZIMI_ARRIVAL->tr(lang,"Erdha. Së pari kontrollojmë tokën, pastaj zgjedhim lulet.","Da bin ich. Zuerst prüfen wir den Boden, dann wählen wir die Blumen.","Eccomi. Prima controlliamo il terreno, poi scegliamo i fiori.","Еве ме. Прво ја проверуваме земјата, потоа ги избираме цвеќињата.","Geldim. Önce toprağı kontrol ederiz, sonra çiçekleri seçeriz.")
  Stage.PREPARE->tr(lang,"Toka duhet liruar dhe përgatitur para mbjelljes.","Der Boden muss vor dem Pflanzen gelockert und vorbereitet werden.","Il terreno va smosso e preparato prima di piantare.","Земјата мора да се разрохка и подготви пред садењето.","Dikimden önce toprak gevşetilip hazırlanmalı.")
  Stage.CELEBRATE->tr(lang,"Sa bukur! Kopshti po merr jetë.","Wie schön! Der Garten wird lebendig.","Che bello! Il giardino prende vita.","Колку убаво! Градината оживува.","Ne güzel! Bahçe canlanıyor.")
  else->""
 }
 private fun tr(lang:String,sq:String,de:String,it:String,mk:String,tr:String)=when(lang){"de"->de;"it"->it;"mk"->mk;"tr"->tr;else->sq}
}

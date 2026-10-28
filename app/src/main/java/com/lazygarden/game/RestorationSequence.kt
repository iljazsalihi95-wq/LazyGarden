package com.lazygarden.game

/** Independent work plans; milliseconds advance only while their native scene is visible. */
object RestorationSequence {
 data class Step(val key:String,val duration:Long,val speaker:Int=-1,val line:String="",val sound:String="")
 data class Plan(val task:Int,val title:String,val steps:List<Step>){
  val duration get()=steps.sumOf{it.duration}
  fun index(ms:Long):Int {var end=0L;steps.forEachIndexed{i,s->end+=s.duration;if(ms<end)return i};return steps.lastIndex}
  fun fraction(ms:Long):Float {val i=index(ms);return ((ms-steps.take(i).sumOf{it.duration})/steps[i].duration.toFloat()).coerceIn(0f,1f)}
  fun progress(ms:Long,key:String):Float {val i=steps.indexOfFirst{it.key==key};if(i<0)return 0f;return ((ms-steps.take(i).sumOf{it.duration})/steps[i].duration.toFloat()).coerceIn(0f,1f)}
 }
 fun plan(task:Int)=when(task){
  2->Plan(2,"Shtegu i kujtimeve",listOf(Step("ARRIVAL",3500),Step("INSPECT",3500,0,"Gurët janë çarë. Ta hapim shtegun!"),Step("CLEAR",4000,sound="brush"),Step("REMOVE",4000,sound="stone"),Step("LEVEL",4000,sound="shovel"),Step("PLACE",8000,1,"Një nga një, gurët marrin vend.","stone"),Step("CLEAN",3000,sound="brush"),Step("REVEAL",2500,2,"Tani mund të ecim mes luleve!","bloom"),Step("CELEBRATE",3000,3,"Sa shteg i bukur!","reward")))
  3->Plan(3,"Një vend për pushim",listOf(Step("ARRIVAL",3500),Step("INSPECT",3000,0,"Stoli lëkundet. Dërrasat janë kalbur."),Step("REMOVE",4000,sound="hammer"),Step("REPAIR",4500,1,"Po shtrëngoj lidhjet e reja.","hammer"),Step("SAND",4000,sound="brush"),Step("PAINT",5000,2,"Një shtresë bojë do ta mbrojë.","brush"),Step("ASSEMBLE",3500,sound="hammer"),Step("CLEAN",2000,sound="brush"),Step("TRY",3500,3,"E provova! Tani është i sigurt."),Step("CELEBRATE",3000,0,"Një vend për të pushuar së bashku!","reward")))
  4->Plan(4,"Rrënjë të reja",listOf(Step("ARRIVAL",3500),Step("INSPECT",3000,2,"Toka ka nevojë për kujdes."),Step("CLEAR",3500,sound="brush"),Step("WEED",4000,1,"Po heq rrënjët e barërave.","shovel"),Step("SOIL",4000,sound="shovel"),Step("PLANT",6000,2,"Pemët e reja do të rriten këtu.","plant"),Step("WATER",4000,sound="water"),Step("GROW",4000,3,"Shikoni sa gjelbërim!","bloom"),Step("CELEBRATE",3000,0,"Kopshti merr frymë përsëri!","reward")))
  5->Plan(5,"Porta e hapur",listOf(Step("ARRIVAL",3500),Step("INSPECT",3000,0,"Porta ngec. Menteshat duhen ndrequr."),Step("CLEAR",3500,sound="brush"),Step("REPAIR",6000,1,"Po rregulloj menteshat dhe shtyllat.","hammer"),Step("PAINT",5000,sound="brush"),Step("DECORATE",4000,2,"Lulet do t’i mirëpresin miqtë.","plant"),Step("LIGHT",3000,sound="star"),Step("REVEAL",3000,3,"Porta hapet dhe fenerët ndriçojnë!"),Step("CELEBRATE",3000,0,"Mirë se vini në kopshtin tonë!","reward")))
  6->Plan(6,"Shtëpia merr jetë",listOf(Step("ARRIVAL",4000),Step("INSPECT",3500,0,"Tani është radha e shtëpisë."),Step("SCAFFOLD",3500,sound="stone"),Step("CLEAN",4000,sound="brush"),Step("REPAIR",5000,1,"Çarjet po mbyllen një nga një.","hammer"),Step("ROOF",4500,sound="hammer"),Step("PLASTER",4500,sound="shovel"),Step("PAINT",6000,2,"Ngjyrat e ngrohta i japin jetë.","brush"),Step("WINDOWS",4000,sound="hammer"),Step("DECORATE",4000,3,"Edhe vazot e dritareve janë gati!","plant"),Step("LIGHT",3000,sound="star"),Step("REVEAL",4000,0,"E ndërtuam së bashku!","bloom"),Step("CELEBRATE",5000,2,"Kjo pronë është përsëri shtëpi!","reward")))
  else->error("No work plan for task $task")
 }
}

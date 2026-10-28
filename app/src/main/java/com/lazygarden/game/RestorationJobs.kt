package com.lazygarden.game

/** The same job definition drives the crew, tool picker and Match-3 material palette. */
object RestorationJobs {
 data class Worker(val id:String,val name:String,val roleSq:String,val roleDe:String)
 val crew=listOf(
  Worker("greku","Greku","Hausmajstër","Hausmeister"),
  Worker("rexha","Rexha","Hidraulik","Installateur"),
  Worker("nermir","Nermir","Kulmaxhi","Dachdecker"),
  Worker("beka","Beka","Fasader / moler","Maler / Verputzer"),
  Worker("gezimi","Gëzimi","Bahçexhi","Gärtner"),
  Worker("besi","Besi","Dritarendreqës","Fensterbauer"),
  Worker("mevludin","Mevludin","Elektricist","Elektriker")
 )
 fun worker(task:Int,step:String):Worker=crew.first{it.id==when {
  step=="LIGHT"->"mevludin"
  step=="WINDOWS"->"besi"
  step=="ROOF"->"nermir"
  step in setOf("PAINT","PLASTER")->"beka"
  task==0->"rexha"
  task in 1..2||task==4||step=="DECORATE"->"gezimi"
  else->"greku"
 }}
 fun tool(task:Int,step:String):Material=when(step){
  "PAINT"->Material.PAINT;"PLASTER"->Material.PLASTER;"WINDOWS"->Material.GLASS
  "ROOF"->Material.TILE;"LIGHT"->Material.CABLE
  "PLANT","DECORATE","GROW"->if(task==4)Material.SEEDLING else Material.FLOWER
  "SOIL","WEED","LEVEL"->Material.SOIL
  "PLACE"->Material.STONE
  "ASSEMBLE"->Material.SCREW
  "REPAIR"->when(task){0->Material.PIPE;3->Material.BOARD;5->Material.HINGE;else->Material.TOOL}
  else->Material.TOOL
 }
 fun choices(task:Int,step:String):List<Material>{val correct=tool(task,step);return (listOf(correct)+palette(task)).distinct().take(3).sortedBy{it.ordinal}}
 fun palette(task:Int):List<Material> =(RestorationInventory.forTask(task).map{it.material}+when(task){
  0->listOf(Material.SCREW,Material.CABLE)
  1,4->listOf(Material.SEEDLING,Material.SOIL,Material.FERTILIZER,Material.FLOWER,Material.TOOL,Material.STONE)
  2->listOf(Material.STONE,Material.SAND,Material.TOOL,Material.BOARD,Material.SCREW,Material.SOIL)
  3->listOf(Material.TOOL,Material.HINGE,Material.SAND)
  5->listOf(Material.TOOL,Material.SCREW)
  else->listOf(Material.CABLE,Material.TOOL)
 }).distinct().let{(it+Material.entries).distinct().take(6)}
 fun title(task:Int,step:String,language:String):String {val w=worker(task,step);return "${w.name} · ${if(language=="de")w.roleDe else w.roleSq}"}
}

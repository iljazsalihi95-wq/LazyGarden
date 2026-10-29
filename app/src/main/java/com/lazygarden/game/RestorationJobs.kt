package com.lazygarden.game

/** The same job definition drives the crew, tool picker and Match-3 material palette. */
object RestorationJobs {
 data class Worker(val id:String,val name:String,val roleSq:String,val roleDe:String,val roleIt:String,val roleMk:String,val roleTr:String)
 val crew=listOf(
  Worker("greku","Greku","Hausmajstër","Hausmeister","Tuttofare","Хаусмајстор","Kapıcı / tamirci"),
  Worker("rexha","Rexha","Hidraulik","Installateur","Idraulico","Водоводџија","Tesisatçı"),
  Worker("nermir","Nermir","Kulmaxhi","Dachdecker","Tettiere","Кровопокривач","Çatıcı"),
  Worker("beka","Beka","Fasader / moler","Maler / Verputzer","Imbianchino / intonacatore","Молер / фасадер","Boyacı / sıvacı"),
  Worker("gezimi","Gëzimi","Bahçexhi","Gärtner","Giardiniere","Градинар","Bahçıvan"),
  Worker("besi","Besi","Dritarendreqës","Fensterbauer","Serramentista","Мајстор за прозорци","Pencere ustası"),
  Worker("mevludin","Mevludin","Elektricist","Elektriker","Elettricista","Електричар","Elektrikçi"),
  Worker("luli","Luli","Mjeshtër druri / mobilieri","Holz- / Möbelhandwerker","Falegname / mobiliere","Столар / мајстор за мебел","Marangoz / mobilya ustası")
 )
 fun worker(task:Int,step:String):Worker=crew.first{it.id==when {
  // The bench is Luli's complete restoration job, including sanding, paint and assembly.
  task==3->"luli"
  step=="LIGHT"->"mevludin"
  step=="WINDOWS"->"besi"
  step=="ROOF"->"nermir"
  step in setOf("PAINT","PLASTER")->"beka"
  task==0->"rexha"
  task==2->"greku"
  task==1||task==4||step=="DECORATE"->"gezimi"
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
  3->listOf(Material.BOARD,Material.SCREW,Material.PAINT,Material.TOOL,Material.SAND,Material.HINGE)
  5->listOf(Material.TOOL,Material.SCREW)
  else->listOf(Material.CABLE,Material.TOOL)
 }).distinct().let{(it+Material.entries).distinct().take(6)}
 fun title(task:Int,step:String,language:String):String {val w=worker(task,step);val role=when(language){"de"->w.roleDe;"it"->w.roleIt;"mk"->w.roleMk;"tr"->w.roleTr;else->w.roleSq};return "${w.name} · $role"}
}

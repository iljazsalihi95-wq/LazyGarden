package com.lazygarden.game

/** Materials earned in Match-3 and consumed by real restoration jobs. */
enum class Material(val label:String){
 PIPE("Tuba"),VALVE("Valvul"),TOOL("Vegla"),PUMP_PART("Pjesë pompe"),FLOWER("Lule"),SOIL("Dhe"),STONE("Gurë"),SAND("Rërë"),
 BOARD("Dërrasa"),SCREW("Vida"),PAINT("Bojë"),SEEDLING("Fidanë"),FERTILIZER("Pleh"),
 HINGE("Mentesha"),LAMP("Llamba"),PLASTER("Suva"),GLASS("Xham"),TILE("Tjegulla"),CABLE("Kabllo")
}

data class MaterialNeed(val material:Material,val count:Int)

object RestorationInventory {
 val requirements=listOf(
  listOf(MaterialNeed(Material.PIPE,4),MaterialNeed(Material.VALVE,1),MaterialNeed(Material.TOOL,1),MaterialNeed(Material.PUMP_PART,2)),
  listOf(MaterialNeed(Material.FLOWER,8),MaterialNeed(Material.SOIL,4)),
  listOf(MaterialNeed(Material.STONE,10),MaterialNeed(Material.SAND,5)),
  listOf(MaterialNeed(Material.BOARD,6),MaterialNeed(Material.SCREW,8),MaterialNeed(Material.PAINT,3)),
  listOf(MaterialNeed(Material.SEEDLING,8),MaterialNeed(Material.FERTILIZER,4)),
  listOf(MaterialNeed(Material.HINGE,4),MaterialNeed(Material.PAINT,3),MaterialNeed(Material.LAMP,2),MaterialNeed(Material.CABLE,2)),
  listOf(MaterialNeed(Material.PLASTER,8),MaterialNeed(Material.PAINT,6),MaterialNeed(Material.GLASS,4),MaterialNeed(Material.TILE,6)),
  // House interior: repair the room before furniture can be placed.
  listOf(MaterialNeed(Material.PLASTER,6),MaterialNeed(Material.PAINT,5),MaterialNeed(Material.BOARD,5),MaterialNeed(Material.SCREW,6)),
  // Children's room: safe floor/furniture plus working light.
  listOf(MaterialNeed(Material.BOARD,8),MaterialNeed(Material.PAINT,4),MaterialNeed(Material.LAMP,2),MaterialNeed(Material.CABLE,3)),
  // Kitchen: cabinets, hinges and wall finish.
  listOf(MaterialNeed(Material.BOARD,8),MaterialNeed(Material.HINGE,6),MaterialNeed(Material.TILE,5),MaterialNeed(Material.PAINT,3)),
  // Living room: windows, finish and lighting.
  listOf(MaterialNeed(Material.GLASS,4),MaterialNeed(Material.PAINT,5),MaterialNeed(Material.LAMP,3),MaterialNeed(Material.CABLE,3)),
  // Balcony/workshop: structure and weather-proof finish.
  listOf(MaterialNeed(Material.BOARD,8),MaterialNeed(Material.SCREW,10),MaterialNeed(Material.PAINT,4),MaterialNeed(Material.TILE,4)),
  // Bikes/play yard: path surface and garden planting.
  listOf(MaterialNeed(Material.STONE,8),MaterialNeed(Material.SAND,5),MaterialNeed(Material.SEEDLING,6),MaterialNeed(Material.FLOWER,6))
 )
 const val TASK_COUNT=13
 fun forTask(task:Int)=requirements.getOrElse(task){emptyList()}
 /** Levels around the current restoration intentionally award materials needed by that story job. */
 fun rewardForLevel(level:Int,task:Int):Pair<Material,Int>? {
  val needs=forTask(task);if(needs.isEmpty())return null
  val need=needs[(level-1).coerceAtLeast(0)%needs.size]
  return need.material to if(level%5==0)3 else 2
 }
}

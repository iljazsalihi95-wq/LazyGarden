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
  listOf(MaterialNeed(Material.PLASTER,6),MaterialNeed(Material.PAINT,5),MaterialNeed(Material.BOARD,5),MaterialNeed(Material.SCREW,6)),
  listOf(MaterialNeed(Material.GLASS,6),MaterialNeed(Material.HINGE,4),MaterialNeed(Material.PAINT,3),MaterialNeed(Material.TOOL,2)),
  listOf(MaterialNeed(Material.TILE,10),MaterialNeed(Material.BOARD,5),MaterialNeed(Material.SCREW,8),MaterialNeed(Material.TOOL,2)),
  listOf(MaterialNeed(Material.BOARD,8),MaterialNeed(Material.PAINT,4),MaterialNeed(Material.LAMP,2),MaterialNeed(Material.CABLE,3)),
  listOf(MaterialNeed(Material.PIPE,4),MaterialNeed(Material.VALVE,2),MaterialNeed(Material.BOARD,8),MaterialNeed(Material.HINGE,6)),
  listOf(MaterialNeed(Material.LAMP,3),MaterialNeed(Material.CABLE,5),MaterialNeed(Material.TILE,6),MaterialNeed(Material.PLASTER,4)),
  listOf(MaterialNeed(Material.BOARD,8),MaterialNeed(Material.PAINT,5),MaterialNeed(Material.GLASS,3),MaterialNeed(Material.LAMP,3)),
  listOf(MaterialNeed(Material.BOARD,6),MaterialNeed(Material.FLOWER,6),MaterialNeed(Material.SOIL,4),MaterialNeed(Material.SEEDLING,4)),
  listOf(MaterialNeed(Material.BOARD,10),MaterialNeed(Material.SCREW,12),MaterialNeed(Material.HINGE,4),MaterialNeed(Material.TOOL,3)),
  listOf(MaterialNeed(Material.BOARD,5),MaterialNeed(Material.SCREW,8),MaterialNeed(Material.HINGE,4),MaterialNeed(Material.PAINT,3)),
  listOf(MaterialNeed(Material.LAMP,6),MaterialNeed(Material.CABLE,8),MaterialNeed(Material.STONE,5),MaterialNeed(Material.TOOL,2)),
  listOf(MaterialNeed(Material.SAND,8),MaterialNeed(Material.BOARD,8),MaterialNeed(Material.SCREW,10),MaterialNeed(Material.PAINT,4)),
  listOf(MaterialNeed(Material.FLOWER,10),MaterialNeed(Material.SEEDLING,6),MaterialNeed(Material.FERTILIZER,4),MaterialNeed(Material.PAINT,4))
 )
 const val TASK_COUNT=20
 fun forTask(task:Int)=requirements.getOrElse(task){emptyList()}

 /**
  * A completed Match-3 level awards restoration stock, not a cosmetic counter.
  * The amount is intentionally large enough to make every win visible in inventory;
  * every fifth level gives an extra material. The selected material rotates through
  * the current restoration requirements so flowers/soil/tools cannot get starved.
  */
 fun rewardForLevel(level:Int,task:Int):Pair<Material,Int>? {
  val needs=forTask(task);if(needs.isEmpty())return null
  val need=needs[(level-1).coerceAtLeast(0)%needs.size]
  val base=when {
   need.count>=10 -> 4
   need.count>=6 -> 3
   else -> 2
  }
  return need.material to (base + if(level%5==0)1 else 0)
 }
}

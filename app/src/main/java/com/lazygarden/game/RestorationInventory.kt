package com.lazygarden.game

/** Materials earned in Match-3 and consumed by real restoration jobs. */
enum class Material(val label:String){
 PIPE("Tuba"),VALVE("Valvul"),FLOWER("Lule"),SOIL("Dhe"),STONE("Gurë"),SAND("Rërë"),
 BOARD("Dërrasa"),SCREW("Vida"),PAINT("Bojë"),SEEDLING("Fidanë"),FERTILIZER("Pleh"),
 HINGE("Mentesha"),LAMP("Llamba"),PLASTER("Suva"),GLASS("Xham"),TILE("Tjegulla"),CABLE("Kabllo")
}

data class MaterialNeed(val material:Material,val count:Int)

object RestorationInventory {
 val requirements=listOf(
  listOf(MaterialNeed(Material.PIPE,4),MaterialNeed(Material.VALVE,1)),
  listOf(MaterialNeed(Material.FLOWER,8),MaterialNeed(Material.SOIL,4)),
  listOf(MaterialNeed(Material.STONE,10),MaterialNeed(Material.SAND,5)),
  listOf(MaterialNeed(Material.BOARD,6),MaterialNeed(Material.SCREW,8),MaterialNeed(Material.PAINT,3)),
  listOf(MaterialNeed(Material.SEEDLING,8),MaterialNeed(Material.FERTILIZER,4)),
  listOf(MaterialNeed(Material.HINGE,4),MaterialNeed(Material.PAINT,3),MaterialNeed(Material.LAMP,2),MaterialNeed(Material.CABLE,2)),
  listOf(MaterialNeed(Material.PLASTER,8),MaterialNeed(Material.PAINT,6),MaterialNeed(Material.GLASS,4),MaterialNeed(Material.TILE,6))
 )
 fun forTask(task:Int)=requirements.getOrElse(task){emptyList()}
 /** Levels around the current restoration intentionally award materials needed by that story job. */
 fun rewardForLevel(level:Int,task:Int):Pair<Material,Int>? {
  val needs=forTask(task);if(needs.isEmpty())return null
  val need=needs[(level-1).coerceAtLeast(0)%needs.size]
  return need.material to if(level%5==0)3 else 2
 }
}

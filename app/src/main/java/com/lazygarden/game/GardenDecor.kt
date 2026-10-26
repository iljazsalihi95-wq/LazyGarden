package com.lazygarden.game

/** Fixed shop catalog: gameplay currency only, one saved placement per category. */
enum class DecorCategory(val title:String){BUILDINGS("Ndërtesa"),DECOR("Dekorime"),TREES("Pemë"),FOUNTAINS("Shatërvanë")}
data class DecorItem(val id:String,val category:DecorCategory,val title:String,val cost:Int,val requiredTasks:Int,val art:Int,val x:Float,val y:Float,val width:Float)
object GardenDecor {
 val items=listOf(
  DecorItem("gazebo_blue",DecorCategory.BUILDINGS,"Belveder i kaltër",180,6,R.drawable.decor_gazebo_blue,.82f,.49f,.29f),
  DecorItem("gazebo_rustic",DecorCategory.BUILDINGS,"Belveder rustik",200,6,R.drawable.decor_gazebo_rustic,.82f,.49f,.29f),
  DecorItem("pavilion",DecorCategory.BUILDINGS,"Pavijon prej xhami",240,6,R.drawable.decor_pavilion,.82f,.49f,.29f),
  DecorItem("bench",DecorCategory.DECOR,"Stoli me lule",90,3,R.drawable.decor_bench,.20f,.76f,.29f),
  DecorItem("rose_arch",DecorCategory.DECOR,"Harku i trëndafilave",130,3,R.drawable.decor_rose_arch,.20f,.76f,.29f),
  DecorItem("lantern",DecorCategory.DECOR,"Feneri i kopshtit",75,3,R.drawable.decor_lantern,.20f,.76f,.18f),
  DecorItem("cherry",DecorCategory.TREES,"Qershia në lulëzim",110,5,R.drawable.decor_cherry,.87f,.66f,.27f),
  DecorItem("maple",DecorCategory.TREES,"Panja e artë",120,5,R.drawable.decor_maple,.87f,.66f,.27f),
  DecorItem("citrus",DecorCategory.TREES,"Pema e agrumeve",100,5,R.drawable.decor_citrus,.87f,.66f,.23f),
  DecorItem("classic_fountain",DecorCategory.FOUNTAINS,"Shatërvani klasik",150,1,R.drawable.decor_fountain_classic,.62f,.81f,.26f),
  DecorItem("lotus_fountain",DecorCategory.FOUNTAINS,"Shatërvani i lotusit",165,1,R.drawable.decor_fountain_lotus,.62f,.81f,.26f),
  DecorItem("stone_fountain",DecorCategory.FOUNTAINS,"Ujëvara e gurit",180,1,R.drawable.decor_fountain_stone,.62f,.81f,.26f)
 )
 fun find(id:String)=items.firstOrNull{it.id==id}
}

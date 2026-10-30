package com.lazygarden.game

/**
 * Canonical world-space work targets for restoration scenes.
 * Coordinates are normalized against the estate canvas so actors can walk to the
 * actual object instead of performing work in the middle of the path.
 */
object RestorationWorksites {
    data class Site(
        val task: Int,
        val worker: String,
        val objectId: String,
        val approachX: Float,
        val approachY: Float,
        val workX: Float,
        val workY: Float,
        val faceRight: Boolean
    )

    private val sites = listOf(
        Site(0,"rexha","fountain_pump",.55f,.70f,.58f,.64f,true),
        Site(1,"gezimi","flower_bed",.40f,.73f,.38f,.68f,true),
        Site(2,"greku","garden_path",.48f,.78f,.50f,.72f,true),
        Site(3,"luli","garden_bench",.72f,.70f,.75f,.66f,false),
        Site(4,"gezimi","green_lawn",.30f,.72f,.27f,.67f,true),
        Site(5,"greku","main_entrance",.50f,.59f,.50f,.53f,true),
        Site(6,"beka","facade",.55f,.54f,.60f,.47f,false),
        Site(7,"greku","interior",.50f,.55f,.50f,.49f,true),
        Site(8,"besi","windows",.63f,.55f,.66f,.48f,false),
        Site(9,"nermir","roof",.54f,.47f,.58f,.37f,false),
        Site(10,"luli","kids_room",.42f,.55f,.40f,.49f,true),
        Site(11,"greku","kitchen",.57f,.55f,.59f,.49f,false),
        Site(12,"rexha","kitchen_plumbing",.59f,.56f,.62f,.50f,false),
        Site(13,"luli","living_room",.48f,.56f,.46f,.50f,true),
        Site(14,"greku","balcony",.70f,.54f,.73f,.47f,false),
        Site(15,"luli","workshop",.79f,.69f,.82f,.64f,false),
        Site(16,"greku","bike_corner",.18f,.72f,.16f,.67f,true),
        Site(17,"mevludin","yard_lighting",.66f,.70f,.69f,.61f,false),
        Site(18,"greku","play_area",.25f,.72f,.22f,.66f,true),
        Site(19,"gezimi","celebration_garden",.50f,.73f,.50f,.66f,true)
    )

    init {
        check(sites.size == RestorationInventory.TASK_COUNT)
        check(sites.map { it.task }.toSet().size == RestorationInventory.TASK_COUNT)
        check(sites.all { it.approachX in 0f..1f && it.approachY in 0f..1f && it.workX in 0f..1f && it.workY in 0f..1f })
    }

    fun forTask(task: Int): Site = sites[task.coerceIn(0, sites.lastIndex)]
}

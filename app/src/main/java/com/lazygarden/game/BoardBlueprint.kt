package com.lazygarden.game

/** Authored obstacle geometry: lanes, gates, rings, pockets and stepped retaining walls.
 * Board colours remain random; construction puzzles no longer scatter every obstacle randomly.
 */
object BoardBlueprint {
    fun cells(level:Int,count:Int):List<Int> {
        val pattern=((level-1)%6)
        fun priority(index:Int):Int {
            val r=index/8;val c=index%8
            return when(pattern){
                0->kotlin.math.abs(r-4)*20+kotlin.math.abs(c-3)*2
                1->kotlin.math.abs(c-3)*20+kotlin.math.abs(r-4)*2
                2->kotlin.math.abs(kotlin.math.abs(r-3)+kotlin.math.abs(c-3)-3)*20+r
                3->minOf(kotlin.math.abs(r-2)+kotlin.math.abs(c-2),kotlin.math.abs(r-5)+kotlin.math.abs(c-5))*20+r
                4->kotlin.math.abs(r-c)*20+(7-r)
                else->kotlin.math.abs(r-5)*20+(if(c%2==0)0 else 8)+c
            }
        }
        return (0..63).sortedWith(compareBy<Int>{priority(it)}.thenBy{it}).take(count.coerceIn(0,64))
    }
    /** Crates guard mature sites; the chapter's main obstacle remains the majority. */
    fun obstacle(kind:Int,index:Int,level:Int)=if(kind>1&&index%4==0&&level>=26)1 else kind
}

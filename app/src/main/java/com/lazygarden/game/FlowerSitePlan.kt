package com.lazygarden.game

/** Survey of garden_before/garden_estate, 1024×1536: left soil bed, stone path to its right.
 * All values are in background coordinates, never percentages of the phone screen.
 * The same plant sockets are used during construction and after saving the restoration.
 */
object FlowerSitePlan {
    data class Point(val x:Float,val y:Float)
    val boundary=listOf(Point(.055f,.655f),Point(.235f,.65f),Point(.285f,.70f),Point(.22f,.765f),Point(.055f,.78f))
    val plants=(0 until 12).map { i->Point(.083f+(i%6)*.026f,.691f+(i/6)*.044f) }
    val gardenerRoute=listOf(Point(.63f,.90f),Point(.46f,.82f),Point(.35f,.77f),Point(.30f,.715f))
    val waiting=linkedMapOf("rona" to Point(.345f,.77f),"meli" to Point(.39f,.705f),"lazi" to Point(.415f,.79f))
    val gardener get()=gardenerRoute.last()
    fun contains(point:Point):Boolean {
        var inside=false;var j=boundary.lastIndex
        for(i in boundary.indices){val a=boundary[i];val b=boundary[j]
            if((a.y>point.y)!=(b.y>point.y)&&point.x<(b.x-a.x)*(point.y-a.y)/(b.y-a.y)+a.x)inside=!inside
            j=i
        };return inside
    }
    fun arrival(progress:Float):Point {
        if(progress>=1f)return gardener
        if(progress<=0f)return gardenerRoute.first()
        val lengths=gardenerRoute.zipWithNext{a,b->kotlin.math.hypot(b.x-a.x,b.y-a.y)}
        var distance=progress.coerceIn(0f,1f)*lengths.sum()
        for(i in lengths.indices){if(distance<=lengths[i]||i==lengths.lastIndex){val a=gardenerRoute[i];val b=gardenerRoute[i+1];val t=(distance/lengths[i]).coerceIn(0f,1f);return Point(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t)};distance-=lengths[i]}
        return gardener
    }
}

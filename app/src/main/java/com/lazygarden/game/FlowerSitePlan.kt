package com.lazygarden.game

/** Survey of garden_before/garden_estate, 1024×1536: left soil bed, stone path to its right.
 * All values are in background coordinates, never percentages of the phone screen.
 * The same plant sockets are used during construction and after saving the restoration.
 */
object FlowerSitePlan {
    data class Point(val x:Float,val y:Float)
    val boundary=listOf(Point(.055f,.655f),Point(.235f,.65f),Point(.285f,.70f),Point(.22f,.765f),Point(.055f,.78f))
    val plants=(0 until 12).map { i->Point(.105f+(i%6)*.023f,.713f+(i/6)*.028f) }
    val gardenerRoute=listOf(Point(.63f,.90f),Point(.46f,.82f),Point(.35f,.77f),Point(.30f,.715f))
    val waiting=linkedMapOf("rona" to Point(.345f,.77f),"meli" to Point(.39f,.705f),"lazi" to Point(.415f,.79f))
    val gardener get()=gardenerRoute.last()
    private val wateringRoots=plants.take(6)+plants.drop(6).reversed()
    fun wateringTarget(progress:Float):Point {
        val t=((progress-.2f)/.6f).coerceIn(0f,1f)*(wateringRoots.size-1)
        val i=t.toInt().coerceAtMost(wateringRoots.lastIndex)
        val a=wateringRoots[i];val b=wateringRoots[(i+1).coerceAtMost(wateringRoots.lastIndex)]
        return Point(a.x+(b.x-a.x)*(t-i),a.y+(b.y-a.y)*(t-i))
    }
    fun wateringPosition(progress:Float):Point {
        val t=progress.coerceIn(0f,1f);val home=waiting.getValue("rona")
        if(t==0f||t==1f)return home
        val target=wateringTarget(t);val station=Point(target.x+.035f,.81f)
        fun between(a:Point,b:Point,u:Float)=Point(a.x+(b.x-a.x)*u,a.y+(b.y-a.y)*u)
        return when {
            t<.2f->between(home,station,t/.2f)
            t>.8f->between(station,home,(t-.8f)/.2f)
            else->station
        }
    }
    fun isPouring(progress:Float)=progress>=.2f&&progress<=.8f
    /** Constant horizontal velocity and downward acceleration; exact nozzle/root endpoints. */
    fun waterDrop(source:Point,root:Point,progress:Float):Point {
        val t=progress.coerceIn(0f,1f)
        return Point(source.x+(root.x-source.x)*t,source.y+(root.y-source.y)*t-.025f*t*(1-t))
    }
    /** Width-led framing keeps the rightmost family member visible on tall Samsung displays. */
    fun cameraWidth(width:Int,height:Int):Float=maxOf(width*2.1f,height*(2f/3f))
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

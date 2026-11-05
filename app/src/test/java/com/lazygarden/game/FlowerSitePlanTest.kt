package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test

class FlowerSitePlanTest {
    @Test fun everyPlantedRootIsInTheSurveyedSideBed(){
        assertEquals(12,FlowerSitePlan.plants.size)
        FlowerSitePlan.plants.forEach{assertTrue("Plant outside soil: $it",FlowerSitePlan.contains(it));assertTrue(it.x<.25f)}
        assertFalse(FlowerSitePlan.contains(FlowerSitePlan.Point(.5f,.72f)))
        assertFalse(FlowerSitePlan.contains(FlowerSitePlan.Point(.5f,.9f)))
    }
    @Test fun crewNeverCrossesThePlantedRootsAndStopsAtTheWorkEdge(){
        FlowerSitePlan.waiting.values.forEach{assertFalse(FlowerSitePlan.contains(it))}
        for(i in 0..100){val p=FlowerSitePlan.arrival(i/100f);assertFalse("Route crosses flower bed: $p",FlowerSitePlan.contains(p))}
        assertEquals(FlowerSitePlan.gardener,FlowerSitePlan.arrival(1f))
        assertEquals(FlowerSitePlan.gardener,FlowerSitePlan.arrival(2f))
    }
    @Test fun tallPhoneFramingDoesNotCutOffTheFamily(){
        for((width,height)in listOf(1080 to 1920,1080 to 2340,720 to 1600,1080 to 2520)){
            val worldWidth=FlowerSitePlan.cameraWidth(width,height)
            val worldHeight=worldWidth*1.5f
            val left=(width*.44f-worldWidth*.20f).coerceIn(width-worldWidth,0f)
            val top=(height*.67f-worldHeight*.715f).coerceIn(height-worldHeight,0f)
            for((id,position)in FlowerSitePlan.waiting){
                val actorWidth=worldWidth*if(id=="rona").075f else .095f
                val x=left+worldWidth*position.x
                val foot=top+worldHeight*position.y
                assertTrue("Left crop: $width/$height/$id",x-actorWidth/2>=0)
                assertTrue("Right crop: $width/$height/$id",x+actorWidth/2<=width)
                assertTrue("Ground crop: $width/$height/$id",foot in 0f..height.toFloat())
            }
        }
    }

}

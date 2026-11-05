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
}

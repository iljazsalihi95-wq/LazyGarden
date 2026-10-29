package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
class RestorationJobsTest {
 @Test fun specialistsOwnTheirActualWork(){
  assertEquals("rexha",RestorationJobs.worker(0,"REPAIR").id)
  assertEquals("gezimi",RestorationJobs.worker(4,"PLANT").id)
  assertEquals("beka",RestorationJobs.worker(6,"PLASTER").id)
  assertEquals("nermir",RestorationJobs.worker(6,"ROOF").id)
  assertEquals("besi",RestorationJobs.worker(6,"WINDOWS").id)
  assertEquals("mevludin",RestorationJobs.worker(5,"LIGHT").id)
  assertEquals("luli",RestorationJobs.worker(3,"REPAIR").id)
 }
 @Test fun boardContainsEveryRequiredMaterialWithoutDuplicateMatchSymbols(){
  for(task in 0..6){val palette=RestorationJobs.palette(task)
   assertEquals(6,palette.size);assertEquals(6,palette.toSet().size)
   assertTrue(palette.containsAll(RestorationInventory.forTask(task).map{it.material}))
   for(step in RestorationSequence.plan(if(task<2)2 else task).steps){val choices=RestorationJobs.choices(task,step.key)
    assertEquals(3,choices.size);assertEquals(3,choices.toSet().size);assertTrue(RestorationJobs.tool(task,step.key) in choices)
   }
  }
 }
 @Test fun roofIsRepairedBeforeFacadeAndWindows(){val keys=RestorationSequence.plan(6).steps.map{it.key};assertTrue(keys.indexOf("ROOF")<keys.indexOf("PLASTER"));assertTrue(keys.indexOf("PLASTER")<keys.indexOf("WINDOWS"))}
}

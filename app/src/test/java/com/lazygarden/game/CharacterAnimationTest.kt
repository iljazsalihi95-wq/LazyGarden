package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
class CharacterAnimationTest {
 @Test fun movementClampsToEndpointsAndFacingFollowsTarget(){val a=CharacterAnimation.Position(1f,2f);val b=CharacterAnimation.Position(9f,8f);assertEquals(a,CharacterAnimation.move(a,b,-1f));assertEquals(b,CharacterAnimation.move(a,b,2f));assertEquals(CharacterAnimation.Position(5f,5f),CharacterAnimation.move(a,b,.5f));assertTrue(CharacterAnimation.facesRight(1f,9f));assertFalse(CharacterAnimation.facesRight(9f,1f))}
 @Test fun allEightStatesSampleValidFramesOnBothSheets(){for(sheet in listOf(true,false))for(state in CharacterAnimation.State.entries)for(t in 0L..3000L step 90)assertTrue(CharacterAnimation.frame(state,t,sheet) in 0..7)}
}

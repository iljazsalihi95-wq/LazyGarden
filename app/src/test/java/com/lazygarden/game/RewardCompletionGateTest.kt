package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test
class RewardCompletionGateTest {
 @Test fun completedExpectedPlacementRewardsExactlyOnce(){val g=RewardCompletionGate("Rewarded_Android");assertFalse(g.complete("Interstitial_Android",true));assertTrue(g.complete("Rewarded_Android",true));assertFalse(g.complete("Rewarded_Android",true))}
 @Test fun skippedOrCancelledNeverRewards(){val skipped=RewardCompletionGate("r");assertFalse(skipped.complete("r",false));assertFalse(skipped.complete("r",true));val cancelled=RewardCompletionGate("r");cancelled.cancel();assertFalse(cancelled.complete("r",true))}
}

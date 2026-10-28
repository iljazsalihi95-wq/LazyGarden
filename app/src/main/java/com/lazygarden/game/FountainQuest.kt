package com.lazygarden.game

/** Timed movement alternates with player actions. No timer can grant materials or skip a repair gate. */
object FountainQuest {
 enum class Stage(val duration:Long=0){
  MELI_APPROACH(2800),TEST_WATER,TRY_WATER(1600),CHILDREN_APPROACH(2000),CALL_LAZI,
  LAZI_APPROACH(3000),INSPECT,LAZI_INSPECT(2200),CALL_REXHA,PHONE_CALL(3000),PHONE_REPLY(2200),REXHA_ARRIVAL(4500),
  REXHA_INSPECT,PUMP_DIAGNOSIS(2600),COLLECT,REPAIR(6500),CLEAN,START_WATER,WATER_FLOW(3200),CHILDREN_WATER_APPROACH(1800),CELEBRATE(3000),CLAIM,COMPLETE
 }
 enum class Action { TEST_WATER,CALL_LAZI,INSPECT,CALL_REXHA,INSPECT_PUMP,RESTORE,CLEAN_LEAF,START_WATER,CLAIM }
 fun timedNext(stage:Stage):Stage?=when(stage){
  Stage.MELI_APPROACH->Stage.TEST_WATER
  Stage.TRY_WATER->Stage.CHILDREN_APPROACH
  Stage.CHILDREN_APPROACH->Stage.CALL_LAZI
  Stage.LAZI_APPROACH->Stage.INSPECT
  Stage.LAZI_INSPECT->Stage.CALL_REXHA
  Stage.PHONE_CALL->Stage.PHONE_REPLY
  Stage.PHONE_REPLY->Stage.REXHA_ARRIVAL
  Stage.REXHA_ARRIVAL->Stage.REXHA_INSPECT
  Stage.PUMP_DIAGNOSIS->Stage.COLLECT
  Stage.REPAIR->Stage.CLEAN
  Stage.WATER_FLOW->Stage.CHILDREN_WATER_APPROACH
  Stage.CHILDREN_WATER_APPROACH->Stage.CELEBRATE
  Stage.CELEBRATE->Stage.CLAIM
  else->null
 }
 fun actionNext(stage:Stage,action:Action):Stage?=when(stage to action){
  Stage.TEST_WATER to Action.TEST_WATER->Stage.TRY_WATER
  Stage.CALL_LAZI to Action.CALL_LAZI->Stage.LAZI_APPROACH
  Stage.INSPECT to Action.INSPECT->Stage.LAZI_INSPECT
  Stage.CALL_REXHA to Action.CALL_REXHA->Stage.PHONE_CALL
  Stage.REXHA_INSPECT to Action.INSPECT_PUMP->Stage.PUMP_DIAGNOSIS
  Stage.START_WATER to Action.START_WATER->Stage.WATER_FLOW
  else->null
 }
 fun fraction(stage:Stage,elapsed:Long)=if(stage.duration==0L)0f else (elapsed/stage.duration.toFloat()).coerceIn(0f,1f)
 /** A timed spoken beat may finish its line, but a broken audio service cannot stall gameplay. */
 fun canAdvance(stage:Stage,elapsed:Long,talking:Boolean)=stage.duration>0&&elapsed>=stage.duration&&(!talking||elapsed>=stage.duration+10000)
}

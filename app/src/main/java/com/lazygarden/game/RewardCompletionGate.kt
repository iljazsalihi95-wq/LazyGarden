package com.lazygarden.game

/** Open/start/click never settle. Only the expected SDK completion can consume this show. */
class RewardCompletionGate(private val placement:String){
 private var terminal=false
 fun complete(id:String,completed:Boolean):Boolean {if(terminal||id!=placement)return false;terminal=true;return completed}
 fun cancel(){terminal=true}
}

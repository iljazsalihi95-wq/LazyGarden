package com.lazygarden.game

import android.content.Context
import android.graphics.*

/** Keeps a resident's body, face and foot anchor stable between idle, talk and work. */
object SceneActorArt {
    fun draw(context:Context,c:Canvas,p:Paint,id:String,player:CharacterAnimation.Player,now:Long,bounds:RectF,right:Boolean){
        val actor=SceneCast.actor(id)
        val spec=CharacterSprites.definition(actor.sprite)!!
        val frame=player.frame(now,spec.arrivalAtlas)
        CharacterSprites.draw(context,c,p,actor.sprite,frame,bounds,right,player.motion(now),actor.coat)
    }
}

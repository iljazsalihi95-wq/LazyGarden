package com.lazygarden.game
import android.content.Context
import android.graphics.*
import android.view.View

/** Live restoration objective bound directly to the board being played. */
class MaterialObjectiveHud(context:Context,private val save:GardenSave,private val board:Match3Engine):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val reward=RestorationInventory.rewardForLevel(board.level,save.tasks)
 private val art=MaterialArt(context)
 private fun label():String{
  val (material,amount)=reward?:return ""
  val found=when{
   board.rescueTarget>0->board.rescued
   board.spec.collectCount>0->board.collected[board.spec.collectColor]
   else->0
  }
  val target=when{
   board.rescueTarget>0->board.rescueTarget
   board.spec.collectCount>0->board.spec.collectCount
   else->1
  }
  val inventory=save.materialCount(material)
  return "${FountainText.get(save.language,material.name)}  $found/$target  ·  ${GameText.get(save.language,"reward")} +$amount  ·  Inv. $inventory"
 }
 init{contentDescription=label()}
 fun boardChanged(){contentDescription=label();invalidate()}
 override fun onDraw(c:Canvas){
  super.onDraw(c);val d=resources.displayMetrics.density
  GameSkin.nine(c,GameSkin.bitmap(context,R.drawable.hd_hud),RectF(8*d,2*d,width-8*d,height-2*d),12*d,p)
  reward?.let{art.draw(c,p,it.first,RectF(14*d,3*d,48*d,height-3*d))}
  val text=label();contentDescription=text
  p.color=0xff446538.toInt();p.typeface=Typeface.DEFAULT_BOLD;p.textSize=11.5f*d;p.textAlign=Paint.Align.LEFT
  c.drawText(text,54*d,height*.5f+4*d,p)
 }
}

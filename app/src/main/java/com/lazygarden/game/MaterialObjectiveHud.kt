package com.lazygarden.game
import android.content.Context
import android.graphics.*
import android.view.View

/** Restoration objective HUD bound to the live Match-3 board when available. */
class MaterialObjectiveHud:View{
 private val save:GardenSave
 private val level:Int
 private var liveBoard:Match3Engine?=null
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val reward:Pair<Material,Int>?
 private val art:MaterialArt
 constructor(context:Context,save:GardenSave,level:Int):super(context){this.save=save;this.level=level;reward=RestorationInventory.rewardForLevel(level,save.tasks);art=MaterialArt(context);contentDescription=label()}
 constructor(context:Context,save:GardenSave,board:Match3Engine):super(context){this.save=save;this.level=board.level;this.liveBoard=board;reward=RestorationInventory.rewardForLevel(level,save.tasks);art=MaterialArt(context);contentDescription=label()}
 private fun board():Match3Engine?{
  liveBoard?.let{return it}
  if(level!=save.level)return null
  val raw=save.activeBoard?:return null
  return Match3Engine(level).takeIf{it.restore(raw)}
 }
 private fun label():String{
  val (material,amount)=reward?:return ""
  val b=board()
  val found=when{b==null->0;b.rescueTarget>0->b.rescued;b.spec.collectCount>0->b.collected[b.spec.collectColor];else->0}
  val target=when{b==null->1;b.rescueTarget>0->b.rescueTarget;b.spec.collectCount>0->b.spec.collectCount;else->1}
  val inventory=save.materialCount(material)
  return "${FountainText.get(save.language,material.name)}  $found/$target  ·  ${GameText.get(save.language,"reward")} +$amount  ·  Inv. $inventory"
 }
 fun bind(board:Match3Engine){if(board.level==level){liveBoard=board;boardChanged()}}
 fun boardChanged(){contentDescription=label();invalidate()}
 override fun onDraw(c:Canvas){
  super.onDraw(c);val d=resources.displayMetrics.density
  GameSkin.nine(c,GameSkin.bitmap(context,R.drawable.hd_hud),RectF(8*d,2*d,width-8*d,height-2*d),12*d,p)
  reward?.let{art.draw(c,p,it.first,RectF(14*d,3*d,48*d,height-3*d))}
  val text=label();contentDescription=text
  p.color=0xff446538.toInt();p.typeface=Typeface.DEFAULT_BOLD;p.textSize=11.5f*d;p.textAlign=Paint.Align.LEFT
  c.drawText(text,54*d,height*.5f+4*d,p)
  // Keep objective progress visibly synchronized while cascades, cargo and booster waves mutate the live engine.
  postInvalidateOnAnimation()
 }
}

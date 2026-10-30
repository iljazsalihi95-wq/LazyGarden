package com.lazygarden.game
import android.content.Context
import android.graphics.*
import android.view.View

/** Restoration reward HUD: shows the stock that the current restoration actually still needs. */
class MaterialObjectiveHud:View{
 private val save:GardenSave
 private val level:Int
 private var liveBoard:Match3Engine?=null
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val art:MaterialArt
 constructor(context:Context,save:GardenSave,level:Int):super(context){this.save=save;this.level=level;art=MaterialArt(context);contentDescription=label()}
 constructor(context:Context,save:GardenSave,board:Match3Engine):super(context){this.save=save;this.level=board.level;this.liveBoard=board;art=MaterialArt(context);contentDescription=label()}
 private fun board():Match3Engine?{
  liveBoard?.let{return it}
  if(level!=save.level)return null
  val raw=save.activeBoard?:return null
  return Match3Engine(level).takeIf{it.restore(raw)}
 }
 private fun reward():Pair<Material,Int>?{
  val missing=save.missingRestorationMaterials()
  if(missing.isEmpty())return null
  val need=missing.maxByOrNull{it.count-save.materialCount(it.material)}?:return null
  val left=(need.count-save.materialCount(need.material)).coerceAtLeast(0)
  val base=when{need.count>=10->4;need.count>=6->3;else->2}+if(level%5==0)1 else 0
  return need.material to minOf(left,base)
 }
 private fun label():String{
  val (material,amount)=reward()?:return "✓ ${GameText.get(save.language,"materials")} · ${GameText.get(save.language,"ready")}"
  val need=save.restorationNeeds().firstOrNull{it.material==material}
  val owned=save.materialCount(material);val required=need?.count?:owned
  val b=board()
  val levelProgress=when{
   b==null->""
   b.rescueTarget>0->" · ${b.rescued.coerceAtMost(b.rescueTarget)}/${b.rescueTarget}"
   b.spec.collectCount>0->" · ${b.collected[b.spec.collectColor].coerceAtMost(b.spec.collectCount)}/${b.spec.collectCount}"
   else->""
  }
  return "${FountainText.get(save.language,material.name)}  $owned/$required  ·  ${GameText.get(save.language,"reward")} +$amount$levelProgress"
 }
 fun bind(board:Match3Engine){if(board.level==level){liveBoard=board;boardChanged()}}
 fun boardChanged(){contentDescription=label();invalidate()}
 override fun onDraw(c:Canvas){
  super.onDraw(c);val d=resources.displayMetrics.density
  GameSkin.nine(c,GameSkin.bitmap(context,R.drawable.hd_hud),RectF(8*d,2*d,width-8*d,height-2*d),12*d,p)
  reward()?.let{art.draw(c,p,it.first,RectF(14*d,3*d,48*d,height-3*d))}
  val text=label();contentDescription=text
  p.color=0xff446538.toInt();p.typeface=Typeface.DEFAULT_BOLD;p.textSize=11.5f*d;p.textAlign=Paint.Align.LEFT
  c.drawText(text,54*d,height*.5f+4*d,p)
  postInvalidateOnAnimation()
 }
}

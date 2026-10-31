package com.lazygarden.game
import android.content.Context
import android.graphics.*
import android.view.View

/** Restoration reward HUD: stock reward plus the real live board objectives from the engine. */
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
 private fun goal(done:Boolean,icon:String,current:Int,target:Int)=
  "${if(done)"✓" else icon} ${current.coerceAtMost(target)}/$target"
 private fun liveGoals(b:Match3Engine?):String{
  if(b==null)return ""
  val goals=mutableListOf<String>()
  if(b.spec.collectCount>0){val n=b.collected[b.spec.collectColor];goals+=goal(n>=b.spec.collectCount,"◆",n,b.spec.collectCount)}
  if(b.spec.ice>0)goals+=goal(b.clearedIce>=b.spec.ice,"❄",b.clearedIce,b.spec.ice)
  if(b.rescueTarget>0)goals+=goal(b.rescued>=b.rescueTarget,"♥",b.rescued,b.rescueTarget)
  if(b.terrainRemaining>0)goals+="▣ ${b.terrainRemaining}"
  else if(b.spec.terrain>0)goals+="✓ ▣"
  return if(goals.isEmpty())"" else " · "+goals.joinToString("  ")
 }
 private fun label():String{
  val b=board();val goals=liveGoals(b)
  val pair=reward()?:return "✓ ${GameText.get(save.language,"materials")} · ${GameText.get(save.language,"ready")}$goals"
  val (material,amount)=pair
  val need=save.restorationNeeds().firstOrNull{it.material==material}
  val owned=save.materialCount(material);val required=need?.count?:owned
  return "${FountainText.get(save.language,material.name)} ${owned.coerceAtMost(required)}/$required · ${GameText.get(save.language,"reward")} +$amount$goals"
 }
 fun bind(board:Match3Engine){if(board.level==level){liveBoard=board;boardChanged()}}
 fun boardChanged(){contentDescription=label();invalidate()}
 override fun onDraw(c:Canvas){
  super.onDraw(c);val d=resources.displayMetrics.density
  GameSkin.nine(c,GameSkin.bitmap(context,R.drawable.hd_hud),RectF(8*d,2*d,width-8*d,height-2*d),12*d,p)
  reward()?.let{art.draw(c,p,it.first,RectF(14*d,3*d,48*d,height-3*d))}
  val text=label();contentDescription=text
  p.color=0xff446538.toInt();p.typeface=Typeface.DEFAULT_BOLD;p.textSize=10.5f*d;p.textAlign=Paint.Align.LEFT
  c.drawText(text,54*d,height*.5f+4*d,p)
 }
}

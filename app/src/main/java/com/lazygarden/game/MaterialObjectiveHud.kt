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
  if(b.spec.terrainCount>0){
   val cleared=(b.spec.terrainCount-b.terrainRemaining).coerceIn(0,b.spec.terrainCount)
   goals+=goal(b.terrainRemaining==0,"▣",cleared,b.spec.terrainCount)
  }
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
 private fun fitText(text:String,maxWidth:Float,d:Float):Float{
  var size=11.5f*d
  p.textSize=size
  while(size>8f*d&&p.measureText(text)>maxWidth){size-=.5f*d;p.textSize=size}
  return size
 }
 override fun onDraw(c:Canvas){
  super.onDraw(c);val d=resources.displayMetrics.density
  val bounds=RectF(8*d,2*d,width-8*d,height-2*d)
  GameSkin.nine(c,GameSkin.bitmap(context,R.drawable.hd_hud),bounds,12*d,p)
  val pair=reward()
  pair?.let{
   p.style=Paint.Style.FILL;p.color=0x22ffffff
   c.drawCircle(31*d,height*.5f,20*d,p)
   art.draw(c,p,it.first,RectF(14*d,3*d,48*d,height-3*d))
   p.color=0xffffc84a.toInt();p.style=Paint.Style.FILL
   c.drawCircle(46*d,12*d,8*d,p)
   p.color=0xff6b451d.toInt();p.typeface=Typeface.DEFAULT_BOLD;p.textAlign=Paint.Align.CENTER;p.textSize=8.5f*d
   c.drawText("+${it.second}",46*d,15*d,p)
  }
  val text=label();contentDescription=text
  val left=if(pair==null)18*d else 56*d
  p.color=if(text.startsWith("✓"))0xff2f7a42.toInt() else 0xff446538.toInt()
  p.typeface=Typeface.DEFAULT_BOLD;p.textAlign=Paint.Align.LEFT
  fitText(text,(width-left-14*d).coerceAtLeast(20*d),d)
  c.drawText(text,left,height*.5f-(p.ascent()+p.descent())*.5f,p)
 }
}

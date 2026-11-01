package com.lazygarden.game
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.*

/** Restoration reward HUD: every required restoration material plus live board objectives. */
class MaterialObjectiveHud:View{
 private val save:GardenSave
 private val level:Int
 private var liveBoard:Match3Engine?=null
 private var lastLabel:String?=null
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val art:MaterialArt
 private var shimmer=0f
 private var ambient:ValueAnimator?=null
 constructor(context:Context,save:GardenSave,level:Int):super(context){this.save=save;this.level=level;art=MaterialArt(context);contentDescription=label();lastLabel=contentDescription?.toString()}
 constructor(context:Context,save:GardenSave,board:Match3Engine):super(context){this.save=save;this.level=board.level;this.liveBoard=board;art=MaterialArt(context);contentDescription=label();lastLabel=contentDescription?.toString()}
 private fun board():Match3Engine?{liveBoard?.let{return it};if(level!=save.level)return null;val raw=save.activeBoard?:return null;return Match3Engine(level).takeIf{it.restore(raw)}}
 private fun reward():Pair<Material,Int>?{val missing=save.missingRestorationMaterials();if(missing.isEmpty())return null;val need=missing.minByOrNull{save.materialCount(it.material).toFloat()/it.count.coerceAtLeast(1)}?:return null;val left=(need.count-save.materialCount(need.material)).coerceAtLeast(0);val base=when{need.count>=10->4;need.count>=6->3;else->2}+if(level%5==0)1 else 0;return need.material to minOf(left,base)}
 private fun materialProgress():String{val needs=save.restorationNeeds();if(needs.isEmpty())return "✓ ${GameText.get(save.language,"materials")}";return needs.joinToString("   "){need->val owned=save.materialCount(need.material).coerceAtMost(need.count);val missing=(need.count-owned).coerceAtLeast(0);"${if(missing==0)"✓" else "•"} ${FountainText.get(save.language,need.material.name)} $owned/${need.count}${if(missing==0)"" else " (−$missing)"}"}}
 private fun materialFraction():Float{val needs=save.restorationNeeds();if(needs.isEmpty())return 1f;val required=needs.sumOf{it.count}.coerceAtLeast(1);val owned=needs.sumOf{minOf(save.materialCount(it.material),it.count)};return (owned.toFloat()/required).coerceIn(0f,1f)}
 private fun goal(done:Boolean,icon:String,current:Int,target:Int)="${if(done)"✓" else icon} ${current.coerceAtMost(target)}/$target"
 private fun liveGoals(b:Match3Engine?):String{if(b==null)return "";val goals=mutableListOf<String>();if(b.spec.collectCount>0){val n=b.collected[b.spec.collectColor];goals+=goal(n>=b.spec.collectCount,"◆",n,b.spec.collectCount)};if(b.spec.ice>0)goals+=goal(b.clearedIce>=b.spec.ice,"❄",b.clearedIce,b.spec.ice);if(b.rescueTarget>0)goals+=goal(b.rescued>=b.rescueTarget,"♥",b.rescued,b.rescueTarget);if(b.spec.terrainCount>0){val cleared=(b.spec.terrainCount-b.terrainRemaining).coerceIn(0,b.spec.terrainCount);goals+=goal(b.terrainRemaining==0,"▣",cleared,b.spec.terrainCount)};return goals.joinToString("   ")}
 private fun rewardText():String{val pair=reward();return if(pair==null)"✓ ${GameText.get(save.language,"ready")}" else "${GameText.get(save.language,"reward")} +${pair.second} ${FountainText.get(save.language,pair.first.name)}"}
 private fun label():String=listOf(materialProgress(),rewardText(),liveGoals(board())).filter{it.isNotBlank()}.joinToString(" · ")
 fun bind(board:Match3Engine){if(board.level==level){liveBoard=board;boardChanged()}}
 fun boardChanged(){val next=label();contentDescription=next;if(lastLabel!=null&&lastLabel!=next&&isAttachedToWindow){animate().cancel();scaleX=.97f;scaleY=.97f;alpha=.82f;animate().scaleX(1.04f).scaleY(1.04f).alpha(1f).setDuration(120).withEndAction{animate().scaleX(1f).scaleY(1f).setDuration(130).start()}.start()};lastLabel=next;invalidate()}
 override fun onAttachedToWindow(){super.onAttachedToWindow();ambient?.cancel();ambient=ValueAnimator.ofFloat(0f,1f).apply{duration=2200;repeatCount=ValueAnimator.INFINITE;addUpdateListener{shimmer=it.animatedValue as Float;invalidate()};start()}}
 override fun onDetachedFromWindow(){ambient?.removeAllUpdateListeners();ambient?.cancel();ambient=null;super.onDetachedFromWindow()}
 private fun fitText(text:String,maxWidth:Float,start:Float,min:Float):Float{var size=start;p.textSize=size;while(size>min&&p.measureText(text)>maxWidth){size-=.4f;p.textSize=size};return size}
 override fun onDraw(c:Canvas){super.onDraw(c);val d=resources.displayMetrics.density;val bounds=RectF(8*d,2*d,width-8*d,height-2*d);GameSkin.nine(c,GameSkin.bitmap(context,R.drawable.hd_hud),bounds,12*d,p);val fraction=materialFraction();val shineX=bounds.left+(bounds.width()+60*d)*shimmer-30*d;p.shader=LinearGradient(shineX-26*d,0f,shineX+26*d,0f,intArrayOf(0x00ffffff,0x35ffffff,0x00ffffff),null,Shader.TileMode.CLAMP);c.drawRoundRect(bounds,12*d,12*d,p);p.shader=null
  val pair=reward();pair?.let{val pulse=1f+.06f*sin(shimmer*PI.toFloat()*2);p.style=Paint.Style.FILL;p.color=0x22ffffff;c.drawCircle(31*d,height*.45f,20*d*pulse,p);p.style=Paint.Style.STROKE;p.strokeWidth=2*d;p.color=if(fraction>=1f)0x884f9a58.toInt() else 0x77ffc84a;c.drawCircle(31*d,height*.45f,22*d*pulse,p);p.style=Paint.Style.FILL;art.draw(c,p,it.first,RectF(14*d,6*d,48*d,40*d));p.color=0xffffc84a.toInt();c.drawCircle(46*d,12*d,8*d,p);p.color=0xff6b451d.toInt();p.typeface=Typeface.DEFAULT_BOLD;p.textAlign=Paint.Align.CENTER;p.textSize=8.5f*d;c.drawText("+${it.second}",46*d,15*d,p)}
  val left=if(pair==null)16*d else 57*d;val maxWidth=(width-left-14*d).coerceAtLeast(20*d);p.typeface=Typeface.DEFAULT_BOLD;p.textAlign=Paint.Align.LEFT;p.color=0xff294f31.toInt();val materials=materialProgress();fitText(materials,maxWidth,13.5f*d,9f*d);c.drawText(materials,left,22*d,p)
  val second=listOf(rewardText(),liveGoals(board())).filter{it.isNotBlank()}.joinToString("   ·   ");p.color=0xff60401f.toInt();fitText(second,maxWidth,12.5f*d,9f*d);c.drawText(second,left,43*d,p)
  val barLeft=left;val barRight=width-16*d;val barTop=height-9*d;val barBottom=height-5*d;if(barRight>barLeft){p.style=Paint.Style.FILL;p.color=0x22654323;c.drawRoundRect(RectF(barLeft,barTop,barRight,barBottom),2*d,2*d,p);val fillRight=barLeft+(barRight-barLeft)*fraction;if(fillRight>barLeft){p.shader=LinearGradient(barLeft,barTop,fillRight,barBottom,intArrayOf(if(fraction>=1f)0xff4f9a58.toInt() else 0xffffc84a.toInt(),if(fraction>=1f)0xff79c982.toInt() else 0xffffe58a.toInt()),null,Shader.TileMode.CLAMP);c.drawRoundRect(RectF(barLeft,barTop,fillRight,barBottom),2*d,2*d,p);p.shader=null;val sparkX=barLeft+(fillRight-barLeft)*(.15f+.85f*shimmer);p.color=0xddffffff.toInt();c.drawCircle(sparkX,(barTop+barBottom)/2,1.7f*d,p)}}
 }
}

package com.lazygarden.game
import android.content.Context
import android.graphics.*
import android.view.View

/** The active puzzle advertises the real material paid only by its first win. */
class MaterialObjectiveHud(context:Context,save:GardenSave,level:Int):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val reward=RestorationInventory.rewardForLevel(level,save.tasks)
 private val art=MaterialArt(context)
 private val label=reward?.let{(m,n)->"${GameText.get(save.language,"reward")}: ${FountainText.get(save.language,m.name)} ${save.materialCount(m)}/${save.restorationNeeds().first{it.material==m}.count} · +$n"}?:""
 init{contentDescription=label}
 override fun onDraw(c:Canvas){super.onDraw(c);val d=resources.displayMetrics.density;GameSkin.nine(c,GameSkin.bitmap(context,R.drawable.hd_hud),RectF(8*d,2*d,width-8*d,height-2*d),12*d,p)
  reward?.let{art.draw(c,p,it.first,RectF(14*d,3*d,48*d,height-3*d))}
  p.color=0xff446538.toInt();p.typeface=Typeface.DEFAULT_BOLD;p.textSize=12*d;p.textAlign=Paint.Align.LEFT;c.drawText(label,54*d,height*.5f+4*d,p)
 }
}

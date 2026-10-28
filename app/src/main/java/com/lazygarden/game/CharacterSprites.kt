package com.lazygarden.game

import android.content.Context
import android.graphics.*

/** One identity registry and atlas sampler for playable scenes and the persistent estate. */
object CharacterSprites {
 data class Definition(val resource:Int,val row:Int,val arrivalAtlas:Boolean)
 private val characters=linkedMapOf(
  "lazi" to Definition(R.drawable.resident_atlas,0,false),
  "meli" to Definition(R.drawable.resident_atlas,2,false),
  "cufi" to Definition(R.drawable.fountain_family_atlas,1,true),
  "rona" to Definition(R.drawable.fountain_family_atlas,2,true),
  "rexha" to Definition(R.drawable.fountain_family_atlas,0,true)
 )
 val identities get()=characters.keys.toList()
 private val familyRows=floatArrayOf(0f,280/768f,536/768f,1f)
 private val originalRows=floatArrayOf(0f,.25f,.5f,.75f,1f)
 fun definition(id:String)=characters[id]
 fun aspect(context:Context,id:String):Float {
  val spec=characters.getValue(id);val b=GameSkin.bitmap(context,spec.resource)
  val rows=if(spec.arrivalAtlas)familyRows else originalRows
  return (rows[spec.row+1]-rows[spec.row])*b.height/(b.width/8f)
 }
 fun draw(context:Context,c:Canvas,p:Paint,id:String,frame:Int,bounds:RectF,facingRight:Boolean):Boolean {
  val spec=characters[id]?:return false;val b=GameSkin.bitmap(context,spec.resource)
  val rows=if(spec.arrivalAtlas)familyRows else originalRows;val width=b.width/8
  val pose=frame.coerceIn(0,7)
  val source=Rect(pose*width,(rows[spec.row]*b.height).toInt(),(pose+1)*width,(rows[spec.row+1]*b.height).toInt())
  p.shader=null;p.colorFilter=null;p.style=Paint.Style.FILL;p.alpha=255
  val layer=c.save();c.scale(if(facingRight)1f else -1f,1f,bounds.centerX(),bounds.bottom)
  c.drawBitmap(b,source,bounds,p);c.restoreToCount(layer)
  return true
 }
}

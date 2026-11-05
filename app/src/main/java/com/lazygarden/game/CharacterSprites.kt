package com.lazygarden.game

import android.content.Context
import android.graphics.*
import kotlin.math.abs
import kotlin.math.sin

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
 private val groundedSources=mutableMapOf<String,Rect>()
 private val familyRows=floatArrayOf(0f,280/768f,536/768f,1f)
 private val originalRows=floatArrayOf(0f,.25f,.5f,.75f,1f)
 fun definition(id:String)=characters[id]?:when(id){
  "bejdi"->Definition(R.drawable.resident_atlas,1,false)
  "meti"->Definition(R.drawable.resident_atlas,1,false)
  else->null
 }
 fun aspect(context:Context,id:String):Float {
  val spec=definition(id)?:error("Unknown sprite $id");val b=GameSkin.bitmap(context,spec.resource)
  val rows=if(spec.arrivalAtlas)familyRows else originalRows
  return (rows[spec.row+1]-rows[spec.row])*b.height/(b.width/8f)
 }
 fun draw(context:Context,c:Canvas,p:Paint,id:String,frame:Int,bounds:RectF,facingRight:Boolean,motion:CharacterAnimation.PoseMotion=CharacterAnimation.PoseMotion(),coat:Int=0):Boolean {
  val spec=definition(id)?:return false;val b=GameSkin.bitmap(context,spec.resource)
  val rows=if(spec.arrivalAtlas)familyRows else originalRows;val width=b.width/8
  val pose=frame.coerceIn(0,7)
  val source=Rect(pose*width,(rows[spec.row]*b.height).toInt(),(pose+1)*width,(rows[spec.row+1]*b.height).toInt())
  val grounded=groundedSources.getOrPut("$id/$pose"){
   val pixels=IntArray(source.width()*source.height())
   b.getPixels(pixels,0,source.width(),source.left,source.top,source.width(),source.height())
   val first=(0 until source.height()).firstOrNull{y->(0 until source.width()).any{x->(pixels[y*source.width()+x] ushr 24)>24}}?:0
   val last=(source.height()-1 downTo first).firstOrNull{y->(0 until source.width()).any{x->(pixels[y*source.width()+x] ushr 24)>24}}?:source.height()-1
   Rect(source.left,source.top+first,source.right,source.top+last+1)
  }
  val groundedBounds=RectF(bounds.left,bounds.bottom-bounds.height()*grounded.height()/source.height(),bounds.right,bounds.bottom)
  p.shader=null;p.colorFilter=null;p.style=Paint.Style.FILL;p.alpha=255
  // Both atlas-pose polish and live state motion pivot at the feet. This lets scene actors
  // breathe, talk, work and celebrate without sliding away from the world ground plane.
  val poseWave=sin((pose+1)*1.73f)
  val action=if(pose==0)0f else 1f
  val atlasSx=1f+abs(poseWave)*.008f*action
  val atlasSy=1f-abs(poseWave)*.006f*action
  val atlasLean=poseWave*.45f*action
  val liftPx=0f // Live pose envelopes deform around the feet, never translate the root.
  val liveSx=motion.scaleX.coerceIn(.94f,1.06f)
  val liveSy=motion.scaleY.coerceIn(.94f,1.06f)
  val liveLean=motion.rotation.coerceIn(-4f,4f)
  val layer=c.save()
  c.translate(0f,liftPx)
  c.rotate(atlasLean+liveLean,bounds.centerX(),bounds.bottom)
  c.scale((if(facingRight)1f else -1f)*atlasSx*liveSx,atlasSy*liveSy,bounds.centerX(),bounds.bottom)
  c.drawBitmap(b,grounded,groundedBounds,p)
  if(coat!=0){
   // Uniform colour changes only the torso; the face, skin, trousers and shoes retain the original art.
   val uniform=c.save();c.clipRect(groundedBounds.left,groundedBounds.top+groundedBounds.height()*.37f,groundedBounds.right,groundedBounds.top+groundedBounds.height()*.69f)
   val red=Color.red(coat)/255f;val green=Color.green(coat)/255f;val blue=Color.blue(coat)/255f
   p.colorFilter=ColorMatrixColorFilter(ColorMatrix(floatArrayOf(
    .30f*red,.59f*red,.11f*red,0f,22f,.30f*green,.59f*green,.11f*green,0f,22f,
    .30f*blue,.59f*blue,.11f*blue,0f,22f,0f,0f,0f,1f,0f)))
   c.drawBitmap(b,grounded,groundedBounds,p);p.colorFilter=null;c.restoreToCount(uniform)
  }
  c.restoreToCount(layer)
  return true
 }
}

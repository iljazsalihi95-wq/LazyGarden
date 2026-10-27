package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.LruCache
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import kotlin.math.min

/** Production raster chrome, sliced rather than stretching carved edges or corner ornaments. */
object GameSkin {
 private val cache=object:LruCache<Int,Bitmap>((Runtime.getRuntime().maxMemory()/4096).toInt().coerceIn(16*1024,64*1024)){override fun sizeOf(key:Int,value:Bitmap)=value.byteCount/1024}
 fun bitmap(context:Context,id:Int):Bitmap=cache.get(id) ?: BitmapFactory.decodeResource(context.resources,id,BitmapFactory.Options().apply{inScaled=false}).also{cache.put(id,it)}
 fun nine(c:Canvas,b:Bitmap,dst:RectF,corner:Float,p:Paint,fraction:Float=.22f,center:Boolean=true){
  val sx=(min(b.width,b.height)*fraction).toInt().coerceAtLeast(1)
  val dx=min(corner,dst.width()/2);val dy=min(corner,dst.height()/2)
  val xs=intArrayOf(0,sx,b.width-sx,b.width);val ys=intArrayOf(0,sx,b.height-sx,b.height)
  val xd=floatArrayOf(dst.left,dst.left+dx,dst.right-dx,dst.right);val yd=floatArrayOf(dst.top,dst.top+dy,dst.bottom-dy,dst.bottom)
  for(y in 0..2)for(x in 0..2)if(center || x!=1 || y!=1)c.drawBitmap(b,Rect(xs[x],ys[y],xs[x+1],ys[y+1]),RectF(xd[x],yd[y],xd[x+1],yd[y+1]),p)
 }
 fun background(context:Context,id:Int,cornerDp:Float=24f,fraction:Float=.22f)=object:Drawable(){
  private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
  override fun draw(c:Canvas){nine(c,bitmap(context,id),RectF(bounds),cornerDp*context.resources.displayMetrics.density,p,fraction)}
  override fun setAlpha(alpha:Int){p.alpha=alpha;invalidateSelf()}
  override fun setColorFilter(filter:ColorFilter?){p.colorFilter=filter;invalidateSelf()}
  @Deprecated("Legacy Drawable API") override fun getOpacity()=PixelFormat.TRANSLUCENT
 }
}
class GameButton(context:Context,primary:Boolean=false):AppCompatButton(context){
 init {
  background=GameSkin.background(context,if(primary)R.drawable.hd_button_green else R.drawable.hd_button_blue,26f,.38f)
  stateListAnimator=null;isAllCaps=false;includeFontPadding=false
  setTextColor(Color.WHITE);typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD)
  setShadowLayer(2*resources.displayMetrics.density,0f,resources.displayMetrics.density,0xff183c22.toInt())
 }
 override fun onTouchEvent(event:MotionEvent):Boolean{
  when(event.actionMasked){
   MotionEvent.ACTION_DOWN->animate().scaleX(.95f).scaleY(.95f).setDuration(80).start()
   MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->animate().scaleX(1f).scaleY(1f).setDuration(140).setInterpolator(android.view.animation.OvershootInterpolator()).start()
  }
  return super.onTouchEvent(event)
 }
}

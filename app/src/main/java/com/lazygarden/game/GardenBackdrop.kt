package com.lazygarden.game

import android.content.Context
import android.graphics.*

/** Previous restorations only; a cinematic must not expose the already-finished entire estate. */
class GardenBackdrop(context:Context,tasks:Int){
 private val hasRestorations=tasks>0
 private val before=GameSkin.bitmap(context,R.drawable.garden_before)
 private val after=GameSkin.bitmap(context,R.drawable.garden_estate)
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val mask=Bitmap.createBitmap(256,384,Bitmap.Config.ARGB_8888)
 init{val c=Canvas(mask);val brush=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.WHITE;maskFilter=BlurMaskFilter(3f,BlurMaskFilter.Blur.NORMAL)};val regions=arrayOf(RectF(.32f,.425f,.80f,.635f),RectF(.75f,.60f,1.04f,.99f),RectF(.12f,.63f,.91f,1.04f),RectF(-.04f,.46f,.32f,.65f),RectF(-.04f,.28f,.36f,.48f),RectF(.34f,.36f,.79f,.46f),RectF(.37f,.14f,1.04f,.415f));for(i in 0 until tasks.coerceIn(0,7)){val r=regions[i];c.drawRoundRect(RectF(r.left*256,r.top*384,r.right*256,r.bottom*384),5f,5f,brush)}}
 fun draw(c:Canvas,dst:RectF){c.drawBitmap(before,null,dst,p);if(!hasRestorations)return;val s=c.saveLayer(dst,null);c.drawBitmap(after,null,dst,p);p.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN);c.drawBitmap(mask,null,dst,p);p.xfermode=null;c.restoreToCount(s)}
}

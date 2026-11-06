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
 init { paintRestorationMask(mask, tasks) }
 companion object {
  private val regions = listOf(
   RectF(.32f,.425f,.80f,.635f), RectF(.04f,.64f,.29f,.79f), RectF(),
   RectF(-.04f,.46f,.32f,.65f), RectF(-.04f,.28f,.36f,.48f),
   RectF(.34f,.36f,.79f,.46f), RectF(.37f,.14f,1.04f,.415f)
  )
  private val maskBounds = RectF(0f,0f,256f,384f)
  private val paving = EstatePathPlan.outline(maskBounds)

  /** Reveal the same surveyed paving in the work scene, replays and the saved estate. */
  fun paintRestorationMask(mask: Bitmap, tasks: Int, currentAlpha: Int = 0) {
   mask.eraseColor(Color.TRANSPARENT)
   val canvas = Canvas(mask)
   val brush = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.WHITE
    maskFilter = BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL)
   }
   for (task in regions.indices) {
    brush.alpha = if (task < tasks) 255 else if (task == tasks) currentAlpha else 0
    if (brush.alpha == 0) continue
    if (task == 2) {
     canvas.drawPath(paving, brush)
    } else {
     val region = regions[task]
     canvas.drawRoundRect(RectF(region.left*256,region.top*384,region.right*256,region.bottom*384),5f,5f,brush)
    }
   }
  }
 }

 fun draw(c:Canvas,dst:RectF){c.drawBitmap(before,null,dst,p);if(!hasRestorations)return;val s=c.saveLayer(dst,null);c.drawBitmap(after,null,dst,p);p.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN);c.drawBitmap(mask,null,dst,p);p.xfermode=null;c.restoreToCount(s)}
}

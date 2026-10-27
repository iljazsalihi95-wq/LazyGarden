package com.lazygarden.game

import android.graphics.*

/** The work sheet has uneven row heights. Explicit cuts keep the preceding actor's feet out of a pose. */
object WorkerArt {
 private val rows=floatArrayOf(0f,251/887f,468/887f,689/887f,1f)
 fun draw(c:Canvas,p:Paint,sheet:Bitmap,id:Int,frame:Int,dst:RectF){p.shader=null;p.colorFilter=null;p.alpha=255;p.style=Paint.Style.FILL
  c.drawBitmap(sheet,Rect((frame*sheet.width/8f).toInt(),(rows[id]*sheet.height).toInt(),((frame+1)*sheet.width/8f).toInt(),(rows[id+1]*sheet.height).toInt()),dst,p)
 }
}

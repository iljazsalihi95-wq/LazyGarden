package com.lazygarden.game
import android.content.Context
import android.graphics.*

/** Physical inventory uses the same original hardware and gardening assets as the world. */
class MaterialArt(private val context:Context){
 fun draw(c:Canvas,p:Paint,m:Material,r:RectF){
  when(m){
   Material.TOOL->SpriteArt.draw(c,p,GameSkin.bitmap(context,R.drawable.fountain_worksite),2,2,2,r)
   Material.PIPE,Material.VALVE,Material.PUMP_PART->SpriteArt.draw(c,p,GameSkin.bitmap(context,R.drawable.fountain_worksite),2,2,if(m==Material.PUMP_PART)1 else 0,r)
   else->{val i=when(m){Material.FLOWER,Material.SEEDLING->0;Material.SOIL,Material.SAND,Material.FERTILIZER->3;Material.STONE,Material.TILE->5;Material.BOARD,Material.SCREW,Material.PAINT,Material.HINGE,Material.CABLE->9;else->10};SpriteArt.draw(c,p,GameSkin.bitmap(context,R.drawable.garden_work_props),4,3,i,r)}
  }
 }
}

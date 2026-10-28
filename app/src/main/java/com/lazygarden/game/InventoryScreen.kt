package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.view.Gravity
import android.widget.*

/** Illustrated shelves with persistent counters; booster icons open an explicit currency purchase. */
class InventoryScreen(context:Context,save:GardenSave,category:Int,close:()->Unit,select:(Int)->Unit,buy:(Match3Engine.Booster)->Unit):LinearLayout(context){
 private val d=resources.displayMetrics.density
 private fun dp(i:Int)=(i*d).toInt()
 private fun text(value:String,size:Float)=TextView(context).apply{text=value;textSize=size;gravity=Gravity.CENTER;setTextColor(0xff573c22.toInt());typeface=Typeface.DEFAULT_BOLD}
 init {
  orientation=VERTICAL;setPadding(dp(10),dp(8),dp(10),dp(8));setBackgroundColor(0xff183f34.toInt())
  val header=LinearLayout(context).apply{gravity=Gravity.CENTER_VERTICAL;background=GameSkin.background(context,R.drawable.hd_hud,18f)}
  header.addView(GameButton(context).apply{text="‹";setOnClickListener{close()}},LayoutParams(dp(48),dp(46)))
  header.addView(text(GameText.get(save.language,"inventory"),23f),LayoutParams(0,dp(48),1f));addView(header)
  addView(text("${save.coins} ${GameText.get(save.language,"coins")} · ★ ${save.stars}",16f).apply{setTextColor(Color.WHITE)},LayoutParams(-1,dp(40)))
  val tabs=LinearLayout(context)
  listOf("materials","tools","boosters","special_items").forEachIndexed{i,key->tabs.addView(GameButton(context).apply{
   text=FountainText.get(save.language,key);textSize=11f;setPadding(0,0,0,0);alpha=if(i==category)1f else .65f;contentDescription="inventory_$key";setOnClickListener{select(i)}
  },LayoutParams(0,dp(50),1f))};addView(tabs)
  val scroll=ScrollView(context);val grid=GridLayout(context).apply{columnCount=3;setPadding(dp(2),dp(12),dp(2),dp(12))}
  fun item(label:String,count:String,draw:(Canvas,Paint,RectF)->Unit,action:(()->Unit)?=null){
   val shelf=LinearLayout(context).apply{orientation=VERTICAL;gravity=Gravity.CENTER;background=GameSkin.background(context,R.drawable.hd_hud,15f);setPadding(dp(4),dp(8),dp(4),dp(8))}
   val art=object:android.view.View(context){private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG);override fun onDraw(c:Canvas){draw(c,p,RectF(dp(8).toFloat(),0f,width-dp(8).toFloat(),height.toFloat()))}}
   shelf.addView(art,LayoutParams(-1,dp(74)));shelf.addView(text(label,12f),LayoutParams(-1,dp(38)));shelf.addView(text(count,18f),LayoutParams(-1,dp(30)))
   shelf.contentDescription="$label $count"
   if(action!=null)shelf.setOnClickListener{shelf.animate().scaleX(.94f).scaleY(.94f).setDuration(80).withEndAction{action()}.start()}
   grid.addView(shelf,GridLayout.LayoutParams().apply{width=0;height=dp(162);columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);setMargins(dp(3),dp(3),dp(3),dp(3))})
  }
  val materialArt=MaterialArt(context)
  when(category){
   0,1->Material.entries.filter{(it==Material.TOOL)==(category==1)}.forEach{m->
    val need=save.restorationNeeds().firstOrNull{it.material==m}?.count
    item(FountainText.get(save.language,m.name),"${save.materialCount(m)}"+(need?.let{" / $it"}?:""),{c,p,r->materialArt.draw(c,p,m,r)})
   }
   2->{val icons=intArrayOf(R.drawable.boost_hammer,R.drawable.boost_row,R.drawable.boost_column,R.drawable.boost_cross,R.drawable.boost_bomb,R.drawable.boost_color,R.drawable.boost_shuffle)
    Match3Engine.Booster.entries.forEachIndexed{i,kind->item(GameText.booster(save.language,kind),"×${save.boosterCount(kind)}",{c,p,r->c.drawBitmap(GameSkin.bitmap(context,icons[i]),null,r,p)},{buy(kind)})}
   }
   else->{item(FountainText.get(save.language,"keys"),"×${save.keys}",{c,p,r->c.drawBitmap(GameSkin.bitmap(context,R.drawable.hd_lock),null,r,p)})
    item(FountainText.get(save.language,"tickets"),"×${save.tickets}",{c,p,r->c.drawBitmap(GameSkin.bitmap(context,R.drawable.hd_icon_map),null,r,p)})}
  }
  scroll.addView(grid);addView(scroll,LayoutParams(-1,0,1f))
 }
}

package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*

/** Illustrated shelves with persistent counters and explicit restoration readiness. */
class InventoryScreen(context:Context,save:GardenSave,category:Int,close:()->Unit,select:(Int)->Unit,buy:(Match3Engine.Booster)->Unit):LinearLayout(context){
 private val d=resources.displayMetrics.density
 private fun dp(i:Int)=(i*d).toInt()
 private fun text(value:String,size:Float)=TextView(context).apply{text=value;textSize=size;gravity=Gravity.CENTER;setTextColor(0xff573c22.toInt());typeface=Typeface.DEFAULT_BOLD}
 private fun statusBg(ready:Boolean)=GradientDrawable().apply{cornerRadius=dp(14).toFloat();setColor(if(ready)0xffdff1d4.toInt() else 0xffffe2cf.toInt());setStroke(dp(2),if(ready)0xff4f8b52.toInt() else 0xffb65b3d.toInt())}
 init {
  orientation=VERTICAL;setPadding(dp(10),dp(8),dp(10),dp(8));setBackgroundColor(0xff183f34.toInt())
  val header=LinearLayout(context).apply{gravity=Gravity.CENTER_VERTICAL;background=GameSkin.background(context,R.drawable.hd_hud,18f)}
  header.addView(GameButton(context).apply{text="‹";setOnClickListener{close()}},LayoutParams(dp(48),dp(46)))
  header.addView(text(GameText.get(save.language,"inventory"),23f),LayoutParams(0,dp(48),1f));addView(header)
  addView(text("${save.coins} ${GameText.get(save.language,"coins")} · ★ ${save.stars}",16f).apply{setTextColor(Color.WHITE)},LayoutParams(-1,dp(40)))

  if(category==0 && save.tasks<RestorationInventory.TASK_COUNT){
   val needs=save.restorationNeeds();val missing=save.missingRestorationMaterials();val materialReady=missing.isEmpty()&&needs.isNotEmpty()
   val coinReady=save.coins>=120;val starReady=save.tasks==0||save.stars>=1
   val ready=materialReady&&coinReady&&starReady
   val summary=LinearLayout(context).apply{orientation=VERTICAL;setPadding(dp(12),dp(8),dp(12),dp(8));background=statusBg(ready)}
   summary.addView(text(FountainText.get(save.language,if(ready)"restoration_ready" else "restoration_missing"),15f))
   summary.addView(text("${if(coinReady)"✓" else "✕"} ${save.coins}/120 Game Coins   •   ${if(starReady)"✓" else "✕"} ${if(save.tasks==0)"0" else "${save.stars}/1"} ★",12f).apply{setTextColor(if(coinReady&&starReady)0xff35653b.toInt() else 0xff8a3f2c.toInt())})
   val rows=needs.joinToString("   •   "){n->val have=save.materialCount(n.material);val mark=if(have>=n.count)"✓" else "−${n.count-have}";"$mark ${FountainText.get(save.language,n.material.name)} $have/${n.count}"}
   summary.addView(text(rows,12f).apply{setTextColor(if(materialReady)0xff35653b.toInt() else 0xff8a3f2c.toInt())})
   if(!ready)summary.addView(text(FountainText.get(save.language,"play_for_materials"),11f).apply{setTextColor(0xff8a3f2c.toInt())})
   addView(summary,LayoutParams(-1,dp(if(ready)90 else 108)).apply{setMargins(dp(4),0,dp(4),dp(6))})
  }

  val tabs=LinearLayout(context)
  listOf("materials","tools","boosters","special_items").forEachIndexed{i,key->tabs.addView(GameButton(context).apply{text=FountainText.get(save.language,key);textSize=11f;setPadding(0,0,0,0);alpha=if(i==category)1f else .65f;contentDescription="inventory_$key";setOnClickListener{select(i)}},LayoutParams(0,dp(50),1f))};addView(tabs)
  val scroll=ScrollView(context);val grid=GridLayout(context).apply{columnCount=3;setPadding(dp(2),dp(12),dp(2),dp(12))}
  fun item(label:String,count:String,ready:Boolean?=null,draw:(Canvas,Paint,RectF)->Unit,action:(()->Unit)?=null){
   val shelf=LinearLayout(context).apply{orientation=VERTICAL;gravity=Gravity.CENTER;background=ready?.let{statusBg(it)}?:GameSkin.background(context,R.drawable.hd_hud,15f);setPadding(dp(4),dp(8),dp(4),dp(8))}
   val art=object:android.view.View(context){private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG);override fun onDraw(c:Canvas){draw(c,p,RectF(dp(8).toFloat(),0f,width-dp(8).toFloat(),height.toFloat()))}}
   shelf.addView(art,LayoutParams(-1,dp(74)));shelf.addView(text(label,12f),LayoutParams(-1,dp(38)));shelf.addView(text(count,18f).apply{ready?.let{setTextColor(if(it)0xff35653b.toInt() else 0xffa34831.toInt())}},LayoutParams(-1,dp(30)))
   shelf.contentDescription="$label $count"
   if(action!=null)shelf.setOnClickListener{shelf.animate().scaleX(.94f).scaleY(.94f).setDuration(80).withEndAction{action()}.start()}
   grid.addView(shelf,GridLayout.LayoutParams().apply{width=0;height=dp(162);columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);setMargins(dp(3),dp(3),dp(3),dp(3))})
  }
  val materialArt=MaterialArt(context)
  when(category){
   0,1->Material.entries.filter{(it==Material.TOOL)==(category==1)}.forEach{m->
    val need=save.restorationNeeds().firstOrNull{it.material==m}?.count;val have=save.materialCount(m);val required=need?.let{have>=it}
    val count=if(need==null)"×$have" else "$have / $need"
    item(FountainText.get(save.language,m.name),count,required,{c,p,r->materialArt.draw(c,p,m,r)})
   }
   2->{val icons=intArrayOf(R.drawable.boost_hammer,R.drawable.boost_row,R.drawable.boost_column,R.drawable.boost_cross,R.drawable.boost_bomb,R.drawable.boost_color,R.drawable.boost_shuffle)
    Match3Engine.Booster.entries.forEachIndexed{i,kind->item(GameText.booster(save.language,kind),"×${save.boosterCount(kind)}",null,{c,p,r->c.drawBitmap(GameSkin.bitmap(context,icons[i]),null,r,p)},{buy(kind)})}
   }
   else->{item(FountainText.get(save.language,"keys"),"×${save.keys}",null,{c,p,r->c.drawBitmap(GameSkin.bitmap(context,R.drawable.hd_lock),null,r,p)});item(FountainText.get(save.language,"tickets"),"×${save.tickets}",null,{c,p,r->c.drawBitmap(GameSkin.bitmap(context,R.drawable.hd_icon_map),null,r,p)})}
  }
  scroll.addView(grid);addView(scroll,LayoutParams(-1,0,1f))
 }
}

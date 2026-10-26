package com.lazygarden.game

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*

/** Catalog preview is transient; only GardenSave transactions can purchase or place a prop. */
class DecorationScreen(
 context:Context,save:GardenSave,category:DecorCategory,preview:DecorItem?,
 close:()->Unit,changeCategory:(DecorCategory)->Unit,showPreview:(DecorItem)->Unit,
 purchase:(DecorItem)->Unit,remove:()->Unit
):FrameLayout(context){
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun text(value:String,size:Float,color:Int=0xff57391f.toInt())=TextView(context).apply{text=value;textSize=size;setTextColor(color);gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD}
 private fun button(value:String,action:()->Unit)=GameButton(context).apply{
  text=value;isAllCaps=false;textSize=12f;setTextColor(Color.WHITE);minHeight=dp(43);setPadding(dp(3),dp(4),dp(3),dp(4))

  setOnClickListener{action()}
 }
 init {
  val placed=if(preview==null)save.decorations else save.decorations.filter{it.category!=category}+preview
  addView(GardenWorldView(context,save.tasks,decorations=placed,focus=preview ?: save.selectedDecoration(category)),LayoutParams(-1,-1))
  val top=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(10),dp(12),0)}
  val bar=LinearLayout(context)
  bar.addView(button("‹ Kopshti",close),LinearLayout.LayoutParams(dp(110),dp(44)))
  bar.addView(text("${save.coins} Game Coins",17f,Color.WHITE),LinearLayout.LayoutParams(0,dp(44),1f));top.addView(bar)
  top.addView(text("Dekoro pronën",26f,0xffffdf83.toInt()).apply{setPadding(0,dp(8),0,0)})
  if(preview!=null)top.addView(text("Pamje para vendosjes",14f,Color.WHITE))
  addView(top,LayoutParams(-1,-2,Gravity.TOP))
  val panel=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(9),dp(11),dp(9),dp(12));background=GameSkin.background(context,R.drawable.hd_hud,22f)}
  val tabs=LinearLayout(context)
  DecorCategory.entries.forEach{cat->
   val tab=button(cat.title){changeCategory(cat)}.apply{isSelected=cat==category;alpha=if(cat==category)1f else .65f;contentDescription="Kategoria ${cat.name}"}
   tabs.addView(tab,LinearLayout.LayoutParams(0,dp(48),1f).apply{setMargins(dp(2),0,dp(2),0)})
  };panel.addView(tabs)
  val selected=save.selectedDecoration(category)
  panel.addView(text(if(preview!=null)preview.title else selected?.let{"Vendosur: ${it.title}"} ?: "Zgjidh stilin e kopshtit tënd",14f).apply{setPadding(0,dp(8),0,dp(7))})
  val cards=LinearLayout(context)
  GardenDecor.items.filter{it.category==category}.forEach{item->
   val owned=save.ownsDecoration(item.id);val locked=save.tasks<item.requiredTasks
   val card=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(5),dp(6),dp(5),dp(6));background=GradientDrawable().apply{setColor(0xfffff6df.toInt());cornerRadius=dp(13).toFloat();setStroke(dp(if(selected?.id==item.id)3 else 1),if(selected?.id==item.id)0xff449d49.toInt() else 0xffc18c47.toInt())}}
   card.addView(ImageView(context).apply{setImageResource(item.art);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Shiko ${item.id}";setOnClickListener{showPreview(item)}},LinearLayout.LayoutParams(-1,dp(86)))
   card.addView(text(item.title,12f).apply{minLines=2},LinearLayout.LayoutParams(-1,dp(39)))
   card.addView(text(if(locked)"${item.requiredTasks} ${if(item.requiredTasks==1)"restaurim" else "restaurime"}" else if(owned)"Në koleksion" else "${item.cost} Coins",11f).apply{setPadding(0,0,0,dp(5))})
   card.addView(button(if(locked)"🔒" else if(selected?.id==item.id)"Vendosur" else if(owned)"Vendos" else "Bli"){purchase(item)}.apply{contentDescription="Vendos ${item.id}"},LinearLayout.LayoutParams(-1,dp(43)))
   cards.addView(card,LinearLayout.LayoutParams(0,-2,1f).apply{setMargins(dp(3),0,dp(3),0)})
  };panel.addView(cards)
  panel.addView(button("Hiq dekorimin e kësaj kategorie",remove).apply{contentDescription="Hiq kategorinë"},LinearLayout.LayoutParams(-1,dp(43)).apply{topMargin=dp(8)})
  val scroll=ScrollView(context).apply{isFillViewport=false;addView(panel)}
  addView(scroll,LayoutParams(-1,-2,Gravity.BOTTOM).apply{setMargins(dp(9),0,dp(9),dp(10))})
 }
}

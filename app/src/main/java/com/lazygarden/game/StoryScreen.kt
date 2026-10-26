package com.lazygarden.game

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*

class StoryScreen(context:Context,save:GardenSave,chapter:Int,line:Int,replay:Boolean,next:()->Unit,later:()->Unit):FrameLayout(context){
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun text(value:String,size:Float,color:Int=0xff5d3c24.toInt())=TextView(context).apply{text=value;textSize=size;setTextColor(color);typeface=Typeface.DEFAULT_BOLD}
 private fun button(value:String,primary:Boolean,action:()->Unit)=GameButton(context,primary).apply{
  text=value;isAllCaps=false;textSize=if(primary)20f else 14f;setTextColor(Color.WHITE);minHeight=dp(48)

  setOnClickListener{isEnabled=false;action()}
 }
 init {
  val story=GardenStory.chapters[chapter];val entry=story.lines[line]
  addView(GardenWorldView(context,save.tasks,decorations=save.decorations),LayoutParams(-1,-1))
  addView(android.view.View(context).apply{setBackgroundColor(0x66304a36)},LayoutParams(-1,-1))
  val header=text("${if(replay)"Rilexim" else "Kapitulli ${chapter+1}"} · ${story.title}",20f,0xffffe5a1.toInt()).apply{gravity=Gravity.CENTER;setPadding(dp(18),dp(15),dp(18),dp(8));setShadowLayer(3f,0f,2f,Color.BLACK)}
  val column=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL}
  column.addView(header,LinearLayout.LayoutParams(-1,dp(76)))
  column.addView(ImageView(context).apply{setImageResource(entry.character.portrait);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Personazhi ${entry.character.displayName}"},LinearLayout.LayoutParams(-1,0,1f))
  val card=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(19),dp(14),dp(19),dp(14));background=GameSkin.background(context,R.drawable.hd_hud,24f)}
  card.addView(text(entry.character.displayName,24f))
  val body=text(entry.text,17f).apply{setPadding(0,dp(8),0,dp(12));setLineSpacing(dp(2).toFloat(),1f)}
  card.addView(ScrollView(context).apply{addView(body)},LinearLayout.LayoutParams(-1,dp(140)))
  card.addView(text("${line+1}/3 · ${if(replay)"Historia e lexuar" else "Progresi ruhet automatikisht"}",12f))
  card.addView(button("Vazhdo tregimin",true,next),LinearLayout.LayoutParams(-1,dp(53)).apply{topMargin=dp(10)})
  card.addView(button(if(replay)"Kthehu te kapitujt" else "Lexo më vonë",false,later),LinearLayout.LayoutParams(-1,dp(44)).apply{topMargin=dp(7)})
  column.addView(card,LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(14),0,dp(14),dp(12))})
  addView(column,LayoutParams(-1,-1))
 }
}

package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import kotlin.math.sin

/** Story scene layered over the real estate: characters enter, settle and speak instead of showing a static portrait. */
class StoryScreen(context:Context,save:GardenSave,chapter:Int,line:Int,replay:Boolean,next:()->Unit,later:()->Unit):FrameLayout(context),SceneLifecycle{
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun text(value:String,size:Float,color:Int=0xff5d3c24.toInt())=TextView(context).apply{text=value;textSize=size;setTextColor(color);typeface=Typeface.DEFAULT_BOLD}
 private fun button(value:String,primary:Boolean,action:()->Unit)=GameButton(context,primary).apply{
  text=value;isAllCaps=false;textSize=if(primary)20f else 14f;setTextColor(Color.WHITE);minHeight=dp(48)
  setOnClickListener{isEnabled=false;action()}
 }
 private val sfx=GardenSfx(context)
 private var motion:ValueAnimator?=null
 private var paused=false
 private fun ui(key:String)=GameText.get(save.language,key)
 private fun localized(replayValue:String,sq:String,de:String,it:String,mk:String,tr:String):String=when(save.language){"de"->de;"it"->it;"mk"->mk;"tr"->tr;else->if(replay)replayValue else sq}
 init {
  val story=GardenStory.chapters[chapter];val entry=story.lines[line]
  val phoneLine=entry.text.lowercase().let{it.contains("thërr")||it.contains("telefon")}
  val world=GardenWorldView(context,save.tasks,decorations=save.decorations)
  addView(world,LayoutParams(-1,-1))
  addView(View(context).apply{setBackgroundColor(0x55304a36)},LayoutParams(-1,-1))

  val column=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL}
  val replayWord=localized("Rilexim","Kapitulli","Kapitel","Capitolo","Поглавје","Bölüm")
  val header=text("${if(replay)replayWord else replayWord+" ${chapter+1}"} · ${story.title}",20f,0xffffe5a1.toInt()).apply{gravity=Gravity.CENTER;setPadding(dp(18),dp(15),dp(18),dp(8));setShadowLayer(3f,0f,2f,Color.BLACK)}
  column.addView(header,LinearLayout.LayoutParams(-1,dp(76)))

  val stage=FrameLayout(context)
  val portrait=ImageView(context).apply{setImageResource(entry.character.portrait);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="${entry.character.displayName}";alpha=0f;translationX=if(line%2==0)-dp(90).toFloat() else dp(90).toFloat();scaleX=.9f;scaleY=.9f}
  stage.addView(portrait,FrameLayout.LayoutParams(-1,-1,Gravity.CENTER))
  if(phoneLine){
   val phone=text("☎",32f,0xffffe5a1.toInt()).apply{gravity=Gravity.CENTER;alpha=0f;contentDescription="Phone call"}
   stage.addView(phone,FrameLayout.LayoutParams(dp(56),dp(56),Gravity.END or Gravity.CENTER_VERTICAL).apply{marginEnd=dp(24)})
   phone.animate().alpha(1f).rotationBy(12f).setStartDelay(300).setDuration(220).withEndAction{phone.animate().rotationBy(-24f).setDuration(180).withEndAction{phone.animate().rotationBy(12f).setDuration(180).start()}.start()}.start()
   if(save.sfxEnabled)postDelayed({if(isAttachedToWindow&&!paused)sfx.play("phone")},260)
  }
  column.addView(stage,LinearLayout.LayoutParams(-1,0,1f))

  val card=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(19),dp(14),dp(19),dp(14));background=GameSkin.background(context,R.drawable.hd_hud,24f);alpha=0f;translationY=dp(28).toFloat()}
  card.addView(text(entry.character.displayName,24f))
  val body=text(entry.text,17f).apply{setPadding(0,dp(8),0,dp(12));setLineSpacing(dp(2).toFloat(),1f)}
  card.addView(ScrollView(context).apply{addView(body)},LinearLayout.LayoutParams(-1,dp(140)))
  val progress=if(replay)ui("story") else ui("progress_saved")
  card.addView(text("${line+1}/${GardenStory.LINES_PER_CHAPTER} · $progress",12f))
  card.addView(button("▶ ${ui("continue")}",true,next),LinearLayout.LayoutParams(-1,dp(53)).apply{topMargin=dp(10)})
  card.addView(button(if(replay)ui("story") else ui("close"),false,later),LinearLayout.LayoutParams(-1,dp(44)).apply{topMargin=dp(7)})
  column.addView(card,LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(14),0,dp(14),dp(12))})
  addView(column,LayoutParams(-1,-1))

  portrait.animate().alpha(1f).translationX(0f).scaleX(1f).scaleY(1f).setDuration(520).setInterpolator(AccelerateDecelerateInterpolator()).start()
  card.animate().alpha(1f).translationY(0f).setStartDelay(180).setDuration(360).start()
  motion=ValueAnimator.ofFloat(0f,1f).apply{duration=2400;repeatCount=ValueAnimator.INFINITE;repeatMode=ValueAnimator.RESTART;addUpdateListener{v->val f=v.animatedValue as Float;portrait.translationY=sin(f*Math.PI*2).toFloat()*dp(3);portrait.rotation=sin(f*Math.PI*2).toFloat()*.45f};start()}
 }
 override fun pauseScene(){paused=true;motion?.pause();sfx.pause()}
 override fun resumeScene(){paused=false;motion?.resume();sfx.resume()}
 override fun onDetachedFromWindow(){motion?.removeAllUpdateListeners();motion?.cancel();motion=null;sfx.release();super.onDetachedFromWindow()}
}

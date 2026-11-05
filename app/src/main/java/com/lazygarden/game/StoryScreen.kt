package com.lazygarden.game

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*

/** Live story scene: family talks on the estate, calls the specialist and stages arrival/work. */
class StoryScreen(context:Context,private val save:GardenSave,chapter:Int,line:Int,private val replay:Boolean,next:()->Unit,later:()->Unit):FrameLayout(context),SceneLifecycle{
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun text(value:String,size:Float,color:Int=0xff5d3c24.toInt())=TextView(context).apply{text=value;textSize=size;setTextColor(color);typeface=Typeface.DEFAULT_BOLD}
 private fun button(value:String,primary:Boolean,action:()->Unit)=GameButton(context,primary).apply{text=value;isAllCaps=false;textSize=if(primary)20f else 14f;setTextColor(Color.WHITE);minHeight=dp(48);setOnClickListener{isEnabled=false;action()}}
 private val sfx=GardenSfx(context);private val voice=GardenVoice(context){save.voiceEnabled};private var paused=false
 private var scene:StorySceneView?=null
 private var speakCurrent:(()->Unit)?=null
 private fun ui(key:String)=GameText.get(save.language,key)
 private fun localized(replayValue:String,sq:String,de:String,it:String,mk:String,tr:String)=when(save.language){"de"->de;"it"->it;"mk"->mk;"tr"->tr;else->if(replay)replayValue else sq}
 private fun continueStoryText()=when(save.language){"de"->"Geschichte fortsetzen";"it"->"Continua la storia";"mk"->"Продолжи ја приказната";"tr"->"Hikâyeye devam et";else->"Vazhdo tregimin"}
 private fun laterText()=when(save.language){"de"->"Später lesen";"it"->"Leggi più tardi";"mk"->"Прочитај подоцна";"tr"->"Daha sonra oku";else->"Lexo më vonë"}

 init{
  val story=GardenStory.chapters[chapter];val authored=story.lines[line];val entry=authored.copy(text=GardenContent.line(chapter,line,save.language,authored.text))
  val partner=if(line>0)story.lines[line-1].character else story.lines.getOrElse(1){entry}.character
  val columnBackground=View(context).apply{setBackgroundColor(0xff102e29.toInt())};addView(columnBackground,LayoutParams(-1,-1))
  speakCurrent={if(isAttachedToWindow&&!paused)voice.speakLine("story_${chapter+1}","line_${line+1}",entry.character.name.lowercase(java.util.Locale.ROOT),save.language,entry.text)}
  val column=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL};val replayWord=localized("Rilexim","Kapitulli","Kapitel","Capitolo","Поглавје","Bölüm")
  column.addView(text("${if(replay)replayWord else "$replayWord ${chapter+1}"} · ${story.title}",20f,0xffffe5a1.toInt()).apply{gravity=Gravity.CENTER;setPadding(dp(18),dp(15),dp(18),dp(8));setShadowLayer(3f,0f,2f,Color.BLACK)},LinearLayout.LayoutParams(-1,dp(76)))
  val stage=StorySceneView(context,save,chapter,line,entry.character.name.lowercase(java.util.Locale.ROOT),partner.name.lowercase(java.util.Locale.ROOT),{voice.isTalking},{speakCurrent?.invoke()},continueConversation=line>0,chapterCast=(story.lines.map{it.character.name.lowercase(java.util.Locale.ROOT)}+RestorationWorksites.castForTask(chapter)).distinct().take(4))
  scene=stage
  column.addView(stage,LinearLayout.LayoutParams(-1,0,1f))
  val card=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(19),dp(14),dp(19),dp(14));background=GameSkin.background(context,R.drawable.hd_hud,24f)}
  card.addView(text(entry.character.displayName,24f));card.addView(ScrollView(context).apply{addView(text(entry.text,17f).apply{setPadding(0,dp(8),0,dp(12));setLineSpacing(dp(2).toFloat(),1f)})},LinearLayout.LayoutParams(-1,dp(140)))
  card.addView(text("${line+1}/${GardenStory.LINES_PER_CHAPTER} · ${if(replay)ui("story") else ui("progress_saved")}",12f));card.addView(button(continueStoryText(),true,next),LinearLayout.LayoutParams(-1,dp(53)).apply{topMargin=dp(10)});card.addView(button(if(replay)ui("story") else laterText(),false,later),LinearLayout.LayoutParams(-1,dp(44)).apply{topMargin=dp(7)})
  column.addView(card,LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(14),0,dp(14),dp(12))});addView(column,LayoutParams(-1,-1))

 }
 override fun pauseScene(){paused=true;sfx.pause();voice.stop()};override fun resumeScene(){paused=false;sfx.resume();if(scene?.dialogueReady==true)speakCurrent?.invoke()}
 override fun onDetachedFromWindow(){speakCurrent=null;sfx.release();voice.release();super.onDetachedFromWindow()}
}

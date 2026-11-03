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

/** Live story scene: family talks on the estate, calls the specialist and stages arrival/work. */
class StoryScreen(context:Context,private val save:GardenSave,chapter:Int,line:Int,private val replay:Boolean,next:()->Unit,later:()->Unit):FrameLayout(context),SceneLifecycle{
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun text(value:String,size:Float,color:Int=0xff5d3c24.toInt())=TextView(context).apply{text=value;textSize=size;setTextColor(color);typeface=Typeface.DEFAULT_BOLD}
 private fun button(value:String,primary:Boolean,action:()->Unit)=GameButton(context,primary).apply{text=value;isAllCaps=false;textSize=if(primary)20f else 14f;setTextColor(Color.WHITE);minHeight=dp(48);setOnClickListener{isEnabled=false;action()}}
 private val sfx=GardenSfx(context);private val voice=GardenVoice(context){save.voiceEnabled};private var motion:ValueAnimator?=null;private var paused=false
 private fun ui(key:String)=GameText.get(save.language,key)
 private fun localized(replayValue:String,sq:String,de:String,it:String,mk:String,tr:String)=when(save.language){"de"->de;"it"->it;"mk"->mk;"tr"->tr;else->if(replay)replayValue else sq}
 private fun continueStoryText()=when(save.language){"de"->"Geschichte fortsetzen";"it"->"Continua la storia";"mk"->"Продолжи ја приказната";"tr"->"Hikâyeye devam et";else->"Vazhdo tregimin"}
 private fun laterText()=when(save.language){"de"->"Später lesen";"it"->"Leggi più tardi";"mk"->"Прочитај подоцна";"tr"->"Daha sonra oku";else->"Lexo më vonë"}
 private fun phaseText(phone:Boolean,work:Boolean,worker:String)=when(save.language){
  "de"->if(phone)"☎ Der Fachmann wird angerufen" else if(work)"🔨 $worker kommt an und beginnt mit der Arbeit" else "Die Familie erkundet den nächsten Bereich"
  "it"->if(phone)"☎ Chiamata allo specialista" else if(work)"🔨 $worker arriva e inizia il lavoro" else "La famiglia esplora la prossima area"
  "mk"->if(phone)"☎ Повик до мајсторот" else if(work)"🔨 $worker пристигнува и почнува со работа" else "Семејството ја истражува следната област"
  "tr"->if(phone)"☎ Usta aranıyor" else if(work)"🔨 $worker gelir ve işe başlar" else "Aile yeni bölgeyi keşfediyor"
  else->if(phone)"☎ Po telefonohet mjeshtri" else if(work)"🔨 $worker mbërrin dhe nis punën" else "Familja po zbulon zonën e radhës"
 }
 init{
  val story=GardenStory.chapters[chapter];val entry=story.lines[line];val site=RestorationWorksites.forTask(chapter)
  // Each story line is voiced by its actual speaker, never by a shared narrator.
  post { if(isAttachedToWindow && !paused) voice.speakLine("story_${chapter+1}", "line_${line+1}", entry.character.name.lowercase(), save.language, entry.text) }
  val lower=entry.text.lowercase();val phoneLine=lower.contains("thërr")||lower.contains("telefon")||lower.contains("anruf")||lower.contains("call")
  val workLine=line>=GardenStory.LINES_PER_CHAPTER-2
  val partner=if(line>0)story.lines[line-1].character else story.lines.getOrElse(1){entry}.character
  val worker=GardenCharacter.entries.firstOrNull{it.name.lowercase()==site.worker.lowercase()}
  val workerName=worker?.displayName?:site.worker.replaceFirstChar{it.uppercase()}
  val world=GardenWorldView(context,save.tasks,decorations=save.decorations);addView(world,LayoutParams(-1,-1));addView(View(context).apply{setBackgroundColor(0x55304a36)},LayoutParams(-1,-1))
  val column=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL};val replayWord=localized("Rilexim","Kapitulli","Kapitel","Capitolo","Поглавје","Bölüm")
  column.addView(text("${if(replay)replayWord else "$replayWord ${chapter+1}"} · ${story.title}",20f,0xffffe5a1.toInt()).apply{gravity=Gravity.CENTER;setPadding(dp(18),dp(15),dp(18),dp(8));setShadowLayer(3f,0f,2f,Color.BLACK)},LinearLayout.LayoutParams(-1,dp(76)))
  val stage=FrameLayout(context)
  val partnerView=ImageView(context).apply{setImageResource(partner.portrait);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Personazhi ${partner.displayName}";alpha=0f;translationX=dp(120).toFloat();scaleX=.72f;scaleY=.72f}
  stage.addView(partnerView,FrameLayout.LayoutParams(dp(180),-1,Gravity.END or Gravity.CENTER_VERTICAL).apply{marginEnd=dp(5)})
  val portrait=ImageView(context).apply{setImageResource(entry.character.portrait);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Personazhi ${entry.character.displayName}";alpha=0f;translationX=-dp(110).toFloat();scaleX=.86f;scaleY=.86f}
  stage.addView(portrait,FrameLayout.LayoutParams(dp(220),-1,Gravity.START or Gravity.CENTER_VERTICAL).apply{marginStart=dp(5)})
  val workerView=worker?.let{actor->ImageView(context).apply{setImageResource(actor.portrait);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Personazhi ${actor.displayName}";alpha=0f;translationX=dp(260).toFloat();scaleX=.66f;scaleY=.66f}.also{stage.addView(it,FrameLayout.LayoutParams(dp(190),-1,Gravity.END or Gravity.CENTER_VERTICAL).apply{marginEnd=dp(34)})}}
  val phase=text(phaseText(phoneLine,workLine,workerName),13f,0xffffe5a1.toInt()).apply{gravity=Gravity.CENTER;setShadowLayer(3f,0f,2f,Color.BLACK);alpha=.92f}
  stage.addView(phase,FrameLayout.LayoutParams(-1,dp(42),Gravity.BOTTOM).apply{setMargins(dp(12),0,dp(12),dp(4))})
  if(phoneLine){
   val phone=text("☎",34f,0xffffe5a1.toInt()).apply{gravity=Gravity.CENTER;alpha=0f;contentDescription=phaseText(true,false,workerName)}
   stage.addView(phone,FrameLayout.LayoutParams(dp(60),dp(60),Gravity.CENTER));phone.animate().alpha(1f).rotationBy(12f).setStartDelay(250).setDuration(220).withEndAction{phone.animate().rotationBy(-24f).setDuration(180).withEndAction{phone.animate().rotationBy(12f).setDuration(180).start()}.start()}.start();if(save.sfxEnabled)postDelayed({if(isAttachedToWindow&&!paused)sfx.play("phone")},220)
  }
  if(workLine&&workerView!=null){
   // The craftsman no longer appears during the phone call. He enters from the map edge,
   // walks to the worksite and only then starts the matching work sound/action.
   workerView.animate().alpha(1f).translationX(0f).scaleX(.82f).scaleY(.82f).setStartDelay(320).setDuration(1050).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction{
    workerView.animate().rotationBy(-2f).setDuration(120).withEndAction{workerView.animate().rotationBy(4f).setDuration(160).withEndAction{workerView.animate().rotation(0f).setDuration(140).start()}.start()}.start()
    if(save.sfxEnabled)sfx.play(when(site.worker){"rexha"->"water";"gezimi"->"plant";"beka"->"brush";else->"hammer"})
   }.start()
  }
  column.addView(stage,LinearLayout.LayoutParams(-1,0,1f))
  val card=LinearLayout(context).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(19),dp(14),dp(19),dp(14));background=GameSkin.background(context,R.drawable.hd_hud,24f);alpha=0f;translationY=dp(28).toFloat()}
  card.addView(text(entry.character.displayName,24f));card.addView(ScrollView(context).apply{addView(text(entry.text,17f).apply{setPadding(0,dp(8),0,dp(12));setLineSpacing(dp(2).toFloat(),1f)})},LinearLayout.LayoutParams(-1,dp(140)))
  card.addView(text("${line+1}/${GardenStory.LINES_PER_CHAPTER} · ${if(replay)ui("story") else ui("progress_saved")}",12f));card.addView(button(continueStoryText(),true,next),LinearLayout.LayoutParams(-1,dp(53)).apply{topMargin=dp(10)});card.addView(button(if(replay)ui("story") else laterText(),false,later),LinearLayout.LayoutParams(-1,dp(44)).apply{topMargin=dp(7)})
  column.addView(card,LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(14),0,dp(14),dp(12))});addView(column,LayoutParams(-1,-1))
  portrait.animate().alpha(1f).translationX(0f).scaleX(1f).scaleY(1f).setDuration(520).setInterpolator(AccelerateDecelerateInterpolator()).start();partnerView.animate().alpha(.82f).translationX(0f).scaleX(.78f).scaleY(.78f).setStartDelay(140).setDuration(600).start();card.animate().alpha(1f).translationY(0f).setStartDelay(180).setDuration(360).start()
  motion=ValueAnimator.ofFloat(0f,1f).apply{duration=2200;repeatCount=ValueAnimator.INFINITE;addUpdateListener{v->val f=v.animatedValue as Float;portrait.translationY=sin(f*Math.PI*2).toFloat()*dp(3);partnerView.translationY=sin(f*Math.PI*2+1.4).toFloat()*dp(2);workerView?.let{if(it.alpha>.5f){it.translationY=sin(f*Math.PI*4+.7).toFloat()*dp(if(workLine)5 else 2);if(workLine)it.rotation=sin(f*Math.PI*4).toFloat()*1.1f}}};start()}
 }
 override fun pauseScene(){paused=true;motion?.pause();sfx.pause();voice.stop()};override fun resumeScene(){paused=false;motion?.resume();sfx.resume()}
 override fun onDetachedFromWindow(){motion?.removeAllUpdateListeners();motion?.cancel();motion=null;sfx.release();voice.release();super.onDetachedFromWindow()}
}

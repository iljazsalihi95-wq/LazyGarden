package com.lazygarden.game

import android.animation.ValueAnimator
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.graphics.drawable.BitmapDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
 private lateinit var save:GardenSave
 private var engine:Match3Engine?=null
 private var world:SceneLifecycle?=null
 private lateinit var ads:RewardedVideos
 private lateinit var sfx:GardenSfx
 private var screen="garden"
 private var inventoryReturnToFountain=false
 private var balanceText:TextView?=null
 private var starText:TextView?=null
 private var rewardAnimator:ValueAnimator?=null
 private var completionDialog:AlertDialog?=null
 private fun t(key:String)=UiText.get(save.language,key)
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 override fun onCreate(state:Bundle?){super.onCreate(state);WindowCompat.setDecorFitsSystemWindows(window,false);GardenContent.initialize(this);save=GardenSave(this);ads=RewardedVideos(this,save);sfx=GardenSfx(this);showGarden();if(save.hasPendingStory&&!save.constructing)showStory();onBackPressedDispatcher.addCallback(this,object:OnBackPressedCallback(true){override fun handleOnBackPressed(){if(screen=="garden")finish() else {engine?.let{save.saveBoard(it)};if(screen=="inventory"&&inventoryReturnToFountain)showFountainQuest() else showGarden(screen=="fountain_quest")}}})}
 override fun onResume(){super.onResume();world?.resumeScene();rewardAnimator?.resume();sfx.resume()}
 override fun onPause(){world?.pauseScene();rewardAnimator?.pause();sfx.pause();engine?.let{save.saveBoard(it)};super.onPause()}
 override fun onDestroy(){completionDialog?.dismiss();world?.pauseScene();rewardAnimator?.cancel();ads.release();sfx.release();super.onDestroy()}
 private fun visitScenes(view:View,action:(SceneLifecycle)->Unit){
  if(view is SceneLifecycle)action(view)
  if(view is android.view.ViewGroup)for(i in 0 until view.childCount)visitScenes(view.getChildAt(i),action)
 }
 // Traverse at lifecycle time: result particles are added after the world is attached.
 private fun findWorld(view:View)=object:SceneLifecycle {
  override fun pauseScene(){visitScenes(view){it.pauseScene()}}
  override fun resumeScene(){visitScenes(view){it.resumeScene()}}
 }
 private fun attach(view:View){world?.pauseScene();rewardAnimator?.cancel();val host=FrameLayout(this).apply{setBackgroundColor(0xff142f28.toInt());addView(view,FrameLayout.LayoutParams(-1,-1))};ViewCompat.setOnApplyWindowInsetsListener(host){v,insets->val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);insets};setContentView(host);world=findWorld(view);ViewCompat.requestApplyInsets(host);WindowInsetsControllerCompat(window,host).apply{systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;hide(WindowInsetsCompat.Type.systemBars())}}
 private fun label(value:String,size:Float=18f,color:Int=Color.WHITE)=TextView(this).apply{text=value;textSize=size;setTextColor(color);gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD;setShadowLayer(2f,0f,1f,0x77253020)}
 private fun premiumButton(value:String,primary:Boolean=false,action:()->Unit)=GameButton(this,primary).apply{text=value;isAllCaps=false;textSize=if(primary)23f else 15f;setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD;minHeight=dp(if(primary)66 else 48);setPadding(dp(10),dp(6),dp(10),dp(6));layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(4),dp(5),dp(4),dp(5))};setOnClickListener{performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);action()}}
 private fun showGarden(forceHome:Boolean=false){
  if(save.fountainQuestActive&&save.constructing&&!forceHome){showFountainQuest();return}
  screen="garden";engine=null;val root=FrameLayout(this)
  val scene:View=if(save.constructing&&save.tasks==1){
   FlowerGardenView(this,language=save.language,initialChoice=save.flowerChoice,initialElapsed=save.restorationElapsed,checkpoint={save.checkpointRestoration(it)},onChoice={save.chooseFlower(it.id)},onComplete={if(save.constructing){save.finishRestoration();showGarden();showFlowerComplete()}}).apply{contentDescription=RestorationCatalog.title(1,save.language)}
  }else if(save.constructing&&save.tasks>=2){
   if(save.tasks==2)PathGardenView(this,save.restorationElapsed,{save.checkpointRestoration(it)},{completeWork()}) else RestorationGardenView(this,save.tasks,save.restorationElapsed,{save.checkpointRestoration(it)},{completeWork()})
  }else{
   GardenWorldView(this,save.tasks,save.constructing&&!save.fountainQuestActive,save.decorations,onRestorationComplete={if(save.constructing){val fountain=save.tasks==0;save.finishRestoration();showGarden();if(fountain)showFountainComplete()}},initialElapsed=save.restorationElapsed,checkpoint={save.checkpointRestoration(it)}).apply{contentDescription=t("view_estate");setOnClickListener{if(!save.constructing){if(save.tasks==0)showFountainQuest() else showEstate()}}}
  }
  root.addView(scene,FrameLayout.LayoutParams(-1,-1))
  val top=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(8),dp(12),0)}
  val balances=LinearLayout(this).apply{gravity=Gravity.CENTER;background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,18f);setPadding(dp(12),0,dp(12),0)}
  balances.addView(label("${save.coins} ${GameText.get(save.language,"coins")}",14f,0xff64401b.toInt()).apply{balanceText=this},LinearLayout.LayoutParams(0,dp(44),1f));balances.addView(ImageView(this).apply{setImageResource(R.drawable.reward_star);scaleType=ImageView.ScaleType.FIT_CENTER},LinearLayout.LayoutParams(dp(28),dp(30)));balances.addView(label("${save.stars}",19f,0xff64401b.toInt()).apply{starText=this},LinearLayout.LayoutParams(dp(48),dp(44)));balances.addView(label("${FountainText.get(save.language,"keys")} ${save.keys}",12f,0xff64401b.toInt()).apply{contentDescription="home_keys";setOnClickListener{showInventory(3)}},LinearLayout.LayoutParams(dp(80),dp(44)));top.addView(balances)
  top.addView(ImageView(this).apply{setImageResource(R.drawable.hd_logo);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Logo LazyGarden"},LinearLayout.LayoutParams(-1,dp(84)));top.addView(label(GardenContent.event(save.language)?:t("home_tagline"),15f,0xff633a19.toInt()).apply{background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,18f);setPadding(dp(14),dp(4),dp(14),dp(4))},LinearLayout.LayoutParams(-1,dp(36)).apply{setMargins(dp(48),0,dp(48),0)});root.addView(top,FrameLayout.LayoutParams(-1,-2,Gravity.TOP))
  fun side(value:String,icon:Int,action:()->Unit)=premiumButton(value,false,action).apply{textSize=if(value.length>11)10f else 13f;setPadding(dp(6),dp(9),dp(6),dp(8));val drawable=BitmapDrawable(resources,GameSkin.bitmap(this@MainActivity,icon)).apply{setBounds(0,0,dp(32),dp(32))};setCompoundDrawables(null,drawable,null,null);compoundDrawablePadding=dp(1)}
  val leftMenu=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};leftMenu.addView(side(t("rewards"),R.drawable.reward_star){showEarnedRewards()},LinearLayout.LayoutParams(dp(66),dp(68)));leftMenu.addView(side(t("settings"),R.drawable.hd_icon_gear){settings()},LinearLayout.LayoutParams(dp(66),dp(68)).apply{topMargin=dp(8)});root.addView(leftMenu,FrameLayout.LayoutParams(-2,-2,Gravity.START or Gravity.TOP).apply{setMargins(dp(9),dp(184),0,0)})
  val rightMenu=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};rightMenu.addView(side(t("build"),R.drawable.boost_hammer){tasks()},LinearLayout.LayoutParams(dp(66),dp(68)));rightMenu.addView(side(t("decorate"),R.drawable.tile_flower){showDecor()},LinearLayout.LayoutParams(dp(66),dp(68)).apply{topMargin=dp(8)});root.addView(rightMenu,FrameLayout.LayoutParams(-2,-2,Gravity.END or Gravity.TOP).apply{setMargins(0,dp(184),dp(9),0)})
  val bottom=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(10),0,dp(10),dp(10))};bottom.addView(label(t("estate_progress").replace("{count}",save.tasks.toString()),14f));bottom.addView(premiumButton(if(save.activeBoard!=null)"▶ ${t("continue")} · ${save.level}" else "▶ ${t("play")} · ${save.level}",true){showMatch3()},LinearLayout.LayoutParams(dp(242),dp(64)).apply{setMargins(0,dp(5),0,dp(5))})
  val nav=LinearLayout(this);nav.addView(side(t("levels"),R.drawable.hd_icon_map){showMap()},LinearLayout.LayoutParams(0,dp(57),1f).apply{marginEnd=dp(8)});nav.addView(side(if(save.hasPendingStory)"${t("story")} · ${t("new")}" else t("story"),R.drawable.hd_icon_book){if(save.hasPendingStory)showStory() else showStoryArchive()},LinearLayout.LayoutParams(0,dp(57),1f));nav.addView(side(GameText.get(save.language,"inventory"),R.drawable.boost_hammer){showInventory()},LinearLayout.LayoutParams(0,dp(57),1f).apply{marginStart=dp(8)});for(i in 0 until nav.childCount)(nav.getChildAt(i) as Button).setCompoundDrawables(null,null,null,null);bottom.addView(nav);root.addView(bottom,FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));attach(root)
  if(save.constructing&&!save.fountainQuestActive){bottom.visibility=View.INVISIBLE;leftMenu.visibility=View.INVISIBLE;rightMenu.visibility=View.INVISIBLE;for(i in 1 until top.childCount)top.getChildAt(i).visibility=View.GONE}
 }
 private fun showFountainQuest(){
  if(!save.startFountainQuest()){showGarden();return}
  screen="fountain_quest";engine=null
  attach(FountainQuestView(this,save,{showMatch3()},{showInventory()},{showGarden(true)},{showGarden()},{restorationOptions()}))
 }
 private fun showInventory(category:Int=0){
  if(screen!="inventory")inventoryReturnToFountain=screen=="fountain_quest"
  screen="inventory";engine=null
  attach(InventoryScreen(this,save,category,{if(inventoryReturnToFountain)showFountainQuest() else showGarden(true)},{showInventory(it)},{kind->
   AlertDialog.Builder(this).setTitle(GameText.booster(save.language,kind))
    .setMessage("${GameText.get(save.language,"buy")} ×1 · ${kind.cost} ${GameText.get(save.language,"coins")}")
    .setPositiveButton(GameText.get(save.language,"buy")){_,_->
     if(!save.buyBoosterStock(kind))Toast.makeText(this,GameText.get(save.language,"insufficient_coins"),Toast.LENGTH_SHORT).show()
     showInventory(category)
    }.setNegativeButton(GameText.get(save.language,"close"),null).show()
  }))
 }
 private fun completeWork(){if(save.constructing){save.finishRestoration();showGarden();showRestorationComplete()}}
 private fun profile(){AlertDialog.Builder(this).setTitle(t("local_profile")).setMessage("${t("level")} ${save.level}/250\n${GameText.get(save.language,"coins")}: ${save.coins}\n${t("stars")}: ${save.stars}\n${t("restorations")}: ${RestorationUiModel.progress(save.tasks)}\n\n${t("saved_device")}").setPositiveButton(t("close"),null).show()}
 private fun settings(){val langs=arrayOf("Shqip","Deutsch","Italiano","Македонски","Türkçe");val ids=listOf("sq","de","it","mk","tr");val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(14),dp(18),dp(14));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,22f)};card.addView(label(t("language_audio"),24f,0xff654123.toInt()));val picker=Spinner(this);picker.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,langs);picker.setSelection(ids.indexOf(save.language).coerceAtLeast(0));card.addView(picker);card.addView(premiumButton(t("profile")){profile()});val voice=Switch(this).apply{text=GameText.get(save.language,"recorded_voices");isChecked=save.voiceEnabled};val sfx=Switch(this).apply{text=t("sfx");isChecked=save.sfxEnabled};card.addView(voice);card.addView(sfx);AlertDialog.Builder(this).setView(card).setPositiveButton(t("save")){_,_->save.setLanguage(ids[picker.selectedItemPosition]);save.setAudio(voice.isChecked,sfx.isChecked);showGarden(true)}.setNegativeButton(t("close"),null).show()}
 private fun showRestorationComplete(){val dialog=AlertDialog.Builder(this).create();completionDialog=dialog;val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(22),dp(22),dp(22),dp(18));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,24f)};card.addView(label(t("work_complete"),25f,0xff654123.toInt()));card.addView(label((RestorationUiModel.completedTitle(save.tasks,save.language) ?: t("work_complete")),18f,0xff427344.toInt()));card.addView(premiumButton("${t("continue_story")} ▶",true){dialog.dismiss();showStory()});dialog.setView(card);dialog.show();dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))}
 private fun showEarnedRewards(){
  if(save.constructing)return
  val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(18),dp(18),dp(18));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,20f)}
  val dialog=AlertDialog.Builder(this).setTitle(t("rewards")).setView(card).create()
  val rewardDays=LinearLayout(this)
  for(day in 1..7){val bonus=BonusSchedule.daily(day);rewardDays.addView(label("$day\n${bonus.coins}\n${if(bonus.keys>0)"🔑 ${bonus.keys}" else "•"}",13f,if(day==save.nextDailyRewardDay)0xff386c39.toInt() else 0xff765530.toInt()).apply{
   setPadding(dp(8),dp(9),dp(8),dp(9));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,12f)
   contentDescription="${t("daily_reward")} $day: ${bonus.coins} ${t("coins")}, ${bonus.keys} ${FountainText.get(save.language,"keys")}"},LinearLayout.LayoutParams(dp(64),dp(85)).apply{marginEnd=dp(4)})}
  card.addView(HorizontalScrollView(this).apply{addView(rewardDays)},LinearLayout.LayoutParams(-1,dp(90)))
  val upcoming=BonusSchedule.daily(save.nextDailyRewardDay)
  card.addView(label("${t("daily_reward")}: +${upcoming.coins} ${t("coins")} · 🔑 ${upcoming.keys}",15f,0xff654123.toInt()))
  card.addView(ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply{max=BonusSchedule.CHEST_WINS;progress=save.chestProgress},LinearLayout.LayoutParams(-1,dp(12)))
  card.addView(label("${t("reward_chest")}: ${save.chestProgress}/${BonusSchedule.CHEST_WINS} · +${BonusSchedule.CHEST_COINS} ${t("coins")} · 🔑 ${BonusSchedule.CHEST_KEYS}",14f,0xff654123.toInt()))
  card.addView(premiumButton("${t("daily_reward")} · ${save.dailyStreak}/7"){val reward=save.claimDailyReward();dialog.dismiss();showGarden(true);if(reward!=null)Toast.makeText(this,"+${reward.coins} ${t("coins")} · +${reward.keys} ${FountainText.get(save.language,"keys")}",Toast.LENGTH_LONG).show()}.apply{isEnabled=save.dailyRewardAvailable;contentDescription="claim_daily_reward"})
  card.addView(premiumButton("${t("reward_chest")} · ${save.chestProgress}/5"){val reward=save.openRewardChest();dialog.dismiss();showGarden(true);if(reward!=null)Toast.makeText(this,"+${reward.coins} ${t("coins")} · +${reward.keys} ${FountainText.get(save.language,"keys")}",Toast.LENGTH_LONG).show()}.apply{isEnabled=save.chestProgress>=5;contentDescription="open_reward_chest"})
  card.addView(premiumButton(t("optional_bonus")){dialog.dismiss();showAdOffers()})
  card.addView(premiumButton(t("close")){dialog.dismiss()});dialog.show()
 }
 private fun showAdOffers(){if(save.constructing)return;val dialog=AlertDialog.Builder(this).create();val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(18),dp(20),dp(18),dp(18));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,24f)};card.addView(label(t("optional_bonus"),24f,0xff654123.toInt()));card.addView(label(t("voluntary_video"),15f,0xff54743b.toInt()));listOf("coins" to "+100 ${GameText.get(save.language,"coins")}","star" to "+1 ${t("stars")}","life" to "+1 ${t("life")}").filter{it.first!="life"||save.lives<save.maxLives}.forEach{(kind,title)->card.addView(premiumButton("▶ $title"){dialog.dismiss();watchReward(kind){showGarden()}})};card.addView(premiumButton(t("close")){dialog.dismiss()});dialog.setView(card);dialog.show()}
 private fun watchReward(kind:String,after:()->Unit){val loading=AlertDialog.Builder(this).setMessage(t("video_loading")).setNegativeButton(t("cancel")){_,_->ads.cancel()}.create();loading.setOnCancelListener{ads.cancel()};loading.show();ads.show(kind){success,message->loading.dismiss();if(!isFinishing&&!isDestroyed){Toast.makeText(this,message,Toast.LENGTH_LONG).show();if(success){after();if(screen=="garden"&&kind in listOf("coins","star")){if(save.sfxEnabled)sfx.play(if(kind=="coins")"coin" else "star");window.decorView.findViewById<FrameLayout>(android.R.id.content).addView(RewardFlightView(this,kind=="coins",kind=="star"),FrameLayout.LayoutParams(-1,-1));if(kind=="coins")rewardAnimator=ValueAnimator.ofInt(save.coins-100,save.coins).apply{duration=1200;addUpdateListener{balanceText?.text="${it.animatedValue} ${GameText.get(save.language,"coins")}"};start()} else starText?.animate()?.scaleX(1.2f)?.scaleY(1.2f)?.setDuration(200)?.withEndAction{starText?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(200)?.start()}?.start()}}}}}


 private fun showMap(chapter:Int=(save.level-1)/25){screen="map";engine=null;val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};root.addView(premiumButton("‹ ${t("garden")}"){showGarden()});val scroll=ScrollView(this);val chapters=LinearLayout(this);chapters.addView(premiumButton("‹"){showMap((chapter-1).coerceAtLeast(0))},LinearLayout.LayoutParams(dp(48),dp(44)));chapters.addView(label("${t("chapter")} ${chapter+1} · ${chapter*25+1}–${chapter*25+25}",16f),LinearLayout.LayoutParams(0,dp(44),1f));chapters.addView(premiumButton("›"){showMap((chapter+1).coerceAtMost(9))},LinearLayout.LayoutParams(dp(48),dp(44)));root.addView(chapters);val map=LevelMapView(this,save,chapter){level->showMatch3(levelNumber=level)};scroll.addView(map);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));attach(root);scroll.post{scroll.scrollTo(0,if(chapter==(save.level-1)/25)map.currentY-scroll.height/2 else map.measuredHeight-scroll.height)}}
 private fun tasks(){if(save.tasks==0){showFountainQuest();return};showConstruction()}
 private fun showConstruction(){
  if(save.constructing){showGarden();return}
  screen="construction";engine=null
  attach(ConstructionScreen(this,save,{showGarden()},{if(save.tasks==0)showFountainQuest() else restorationOptions()},{showMatch3()},
   {task->if(task==0)showFountainReplay() else showSceneReplay(task)}, {task->showStory(task,0)}))
 }
 private fun restorationOptions(){
  if(save.constructing)return
  val choices=arrayOf(
   t("build_materials"),
   "${t("use_key")} · ${save.keys}",
   "${t("buy_missing")}: ${save.missingMaterialCost()} ${GameText.get(save.language,"coins")}"
  )
  AlertDialog.Builder(this).setTitle(t("choose_build"))
   .setItems(choices){_,which->
    when(which){
     0->if(save.beginRestoration(save.tasks==0))showGarden() else restorationFailure()
     1->if(save.beginRestorationWithKey())showGarden() else restorationFailure()
     2->if(save.buyMissingMaterials()){
      Toast.makeText(this,t("materials_bought"),Toast.LENGTH_LONG).show()
      tasks()
     }else restorationFailure()
    }
   }.setNegativeButton(t("close"),null).show()
 }
 private fun restorationFailure(){
  AlertDialog.Builder(this).setTitle(t("cannot_build"))
   .setMessage("${t("base_cost")}\n\n${t("missing")}: ${save.missingMaterialCost()} ${GameText.get(save.language,"coins")}")
   .setPositiveButton(t("levels")){_,_->showMap()}.setNegativeButton(t("close"),null).show()
 }
 private fun showEstate(){screen="estate";engine=null;val root=FrameLayout(this);root.addView(GardenWorldView(this,save.tasks,decorations=save.decorations),FrameLayout.LayoutParams(-1,-1));val controls=LinearLayout(this).apply{setPadding(dp(10),dp(8),dp(10),0)};controls.addView(premiumButton("‹ ${t("garden")}"){showGarden()},LinearLayout.LayoutParams(0,dp(46),1f).apply{marginEnd=dp(10)});controls.addView(premiumButton(t("decorate")){showDecor()},LinearLayout.LayoutParams(0,dp(46),1f));controls.addView(premiumButton("⌖"){showConstruction()}.apply{contentDescription=t("estate_plan")},LinearLayout.LayoutParams(dp(52),dp(46)));root.addView(controls,FrameLayout.LayoutParams(-1,-2,Gravity.TOP));root.addView(label("${RestorationUiModel.progress(save.tasks)} ${t("restorations")} · ${save.decorations.size} ${t("decoration")}",14f).apply{setPadding(0,0,0,dp(14))},FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));attach(root)}
 private fun showDecor(category:DecorCategory=DecorCategory.DECOR,preview:DecorItem?=null){screen="decor";engine=null;attach(DecorationScreen(this,save,category,preview,{showGarden()},{showDecor(it)},{showDecor(category,it)},{item->when(save.selectDecoration(item.id)){GardenSave.DecorResult.PURCHASED,GardenSave.DecorResult.SELECTED->showDecor(category);GardenSave.DecorResult.LOCKED->Toast.makeText(this,"${t("complete_restoration")}: ${item.requiredTasks}",Toast.LENGTH_LONG).show();GardenSave.DecorResult.INSUFFICIENT->Toast.makeText(this,"${t("missing")}: ${item.cost} ${GameText.get(save.language,"coins")}",Toast.LENGTH_LONG).show();GardenSave.DecorResult.INVALID->Toast.makeText(this,t("not_saved"),Toast.LENGTH_SHORT).show()}},{save.removeDecoration(category);showDecor(category)}))}
 private fun showStory(replayChapter:Int?=null,replayLine:Int=0){if(replayChapter==null&&!save.hasPendingStory){showStoryArchive();return};val position=save.storyPosition;val chapter=replayChapter?:position/GardenStory.LINES_PER_CHAPTER;val line=if(replayChapter==null)position%GardenStory.LINES_PER_CHAPTER else replayLine;if(chapter !in 0 until save.tasks.coerceAtMost(RestorationUiModel.total)){showGarden();return};screen="story";engine=null;attach(StoryScreen(this,save,chapter,line,replayChapter!=null,{if(replayChapter!=null){if(line<2)showStory(chapter,line+1) else showStoryArchive()}else if(save.advanceStory(position)){if(save.hasPendingStory)showStory() else showGarden()}},{if(replayChapter!=null)showStoryArchive() else showGarden()}))}
 private fun showFountainComplete(){val dialog=AlertDialog.Builder(this).create();completionDialog=dialog;val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(18),dp(20),dp(18),dp(16));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,26f)};card.addView(label(t("work_complete"),25f,0xff654123.toInt()));card.addView(label(t("fountain_alive"),16f,0xff50713b.toInt()).apply{setPadding(0,dp(8),0,dp(12))});val comparison=LinearLayout(this);listOf(t("before") to R.drawable.garden_before,t("after") to R.drawable.garden_estate).forEach{(title,art)->val tile=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};tile.addView(label(title,14f,0xff654123.toInt()));tile.addView(ImageView(this).apply{setImageBitmap(GameSkin.bitmap(this@MainActivity,art));scaleType=ImageView.ScaleType.CENTER_CROP},LinearLayout.LayoutParams(-1,dp(112)));comparison.addView(tile,LinearLayout.LayoutParams(0,-2,1f).apply{setMargins(dp(3),0,dp(3),0)})};card.addView(comparison);card.addView(premiumButton("${t("continue_story")} ▶",true){dialog.dismiss();if(save.hasPendingStory)showStory()});dialog.setView(card);dialog.show();dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));dialog.setOnDismissListener{if(completionDialog===dialog)completionDialog=null}}
 private fun showFlowerComplete(){val dialog=AlertDialog.Builder(this).create();completionDialog=dialog;val flower=FlowerSequence.choices.firstOrNull{it.id==save.flowerChoice};val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(20),dp(22),dp(20),dp(18));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,26f)};card.addView(label(t("garden_alive"),26f,0xff654123.toInt()));card.addView(label(t("flowers_complete"),16f,0xff50713b.toInt()).apply{setPadding(0,dp(10),0,dp(12))});card.addView(premiumButton("${t("continue_story")} ▶",true){dialog.dismiss();if(save.hasPendingStory)showStory() else showGarden()});dialog.setView(card);dialog.show();dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));dialog.setOnDismissListener{if(completionDialog===dialog)completionDialog=null}}
 private fun showFountainReplay(){screen="fountain_replay";engine=null;val root=FrameLayout(this);root.addView(GardenWorldView(this,0,true,onRestorationComplete={showGarden()}),FrameLayout.LayoutParams(-1,-1));val back=premiumButton("‹ ${t("garden")}"){showGarden()};root.addView(back,FrameLayout.LayoutParams(dp(130),dp(48),Gravity.TOP or Gravity.START).apply{setMargins(dp(10),dp(8),0,0)});attach(root)}
 private fun showSceneReplay(task:Int){screen="scene_replay";engine=null;val root=FrameLayout(this);val v:View=when(task){1->FlowerGardenView(this,save.language,save.flowerChoice,onComplete={showGarden()});2->PathGardenView(this,onComplete={showGarden()});else->RestorationGardenView(this,task,onComplete={showGarden()})};root.addView(v,FrameLayout.LayoutParams(-1,-1));root.addView(premiumButton("‹ ${t("garden")}"){showGarden()},FrameLayout.LayoutParams(dp(130),dp(48),Gravity.TOP or Gravity.START).apply{setMargins(dp(10),dp(8),0,0)});attach(root)}
 private fun showStoryArchive(){
  screen="story_archive";engine=null
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(8),dp(12),dp(10));setBackgroundColor(0xff204d40.toInt())}
  root.addView(premiumButton("‹ ${t("garden")}"){showGarden()});root.addView(label(t("estate_story"),26f,0xffffdf83.toInt()))
  if(save.fountainRestored)root.addView(premiumButton("▶ ${t("replay_fountain")}"){showFountainReplay()})
  val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  GardenStory.chapters.indices.forEach{i->
   val unlocked=i<save.tasks;val read=save.storyPosition>=(i+1)*3
   if(unlocked&&i>0)list.addView(premiumButton("▶ ${t("replay")}: ${RestorationCatalog.title(i,save.language)}"){showSceneReplay(i)})
   val status=if(!unlocked)"🔒 ${t("complete_restoration")} ${i+1}" else if(read)t("reread") else t("continue_story")
   list.addView(premiumButton("${i+1}. ${RestorationCatalog.title(i,save.language)}\n$status"){
    if(unlocked){if(read)showStory(i,0) else showStory()}else Toast.makeText(this,t("chapter_locked"),Toast.LENGTH_SHORT).show()
   }.apply{contentDescription="${t("chapter")} ${i+1}"})
  }
  root.addView(ScrollView(this).apply{addView(list)},LinearLayout.LayoutParams(-1,0,1f));attach(root)
 }

 private fun showMatch3(restart:Boolean=false,levelNumber:Int=save.level){
  screen="match"
  val board=Match3Engine(levelNumber)
  if(!restart&&levelNumber==save.level)save.activeBoard?.let{board.restore(it)}
  if(restart&&levelNumber==save.level)save.clearBoard()
  engine=board
  fun t(key:String)=GameText.get(save.language,key)
  val controller=Match3BoosterController(save,board,save.level)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(0xff153f42.toInt())}
  val bar=LinearLayout(this).apply{background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,15f)}
  bar.addView(premiumButton("‹"){save.saveBoard(board);showGarden()},LinearLayout.LayoutParams(dp(48),dp(36)))
  val coins=label("${save.coins} ${t("coins")}",15f,0xff66411f.toInt())
  bar.addView(coins,LinearLayout.LayoutParams(0,dp(36),1f));root.addView(bar)
  val objectiveHud=MaterialObjectiveHud(this,save,board)
  root.addView(objectiveHud,LinearLayout.LayoutParams(-1,dp(64)))
  val boardView=Match3BoardView(this,board,{save.saveBoard(board);objectiveHud.boardChanged()},{won->
   val rewarded=if(won)save.win(board) else false
   engine=null;showResult(board,rewarded)
  })
  lateinit var tray:BoosterInventoryTray
  fun refresh(){coins.text="${save.coins} ${t("coins")}";tray.refresh()}
  tray=BoosterInventoryTray(this,save,onUse={kind->
   if(!boardView.isBusy)boardView.runBooster(kind)
  },onBuy={kind->
   if(!boardView.isBusy&&controller.gameplayEnabled&&!board.won&&!board.failed){
    boardView.targetBooster=null
    boardView.invalidate()
    AlertDialog.Builder(this)
     .setTitle(GameText.booster(save.language,kind))
     .setMessage("${t("buy")} ×1 · ${kind.cost} ${t("coins")}")
     .setPositiveButton(t("buy")){_,_->
      if(!controller.buy(kind))Toast.makeText(this,t("insufficient_coins"),Toast.LENGTH_SHORT).show()
      refresh()
     }.setNegativeButton(t("close"),null).show()
   }
  })
  tray.setGameplayEnabled(controller.gameplayEnabled&&!board.won&&!board.failed)
  boardView.onBoosterTarget={kind,at->
   val result=controller.use(kind,at)
   refresh();objectiveHud.boardChanged()
   when(result.status){
    Match3BoosterController.Status.LOCKED_REPLAY->Toast.makeText(this,t("locked"),Toast.LENGTH_SHORT).show()
    Match3BoosterController.Status.NEEDS_STOCK->Toast.makeText(this,t("no_stock"),Toast.LENGTH_SHORT).show()
    Match3BoosterController.Status.INVALID_TARGET->Toast.makeText(this,t("invalid_target"),Toast.LENGTH_SHORT).show()
    Match3BoosterController.Status.USED->Unit
   }
   result.waves
  }
  root.addView(boardView,LinearLayout.LayoutParams(-1,0,1f))
  root.addView(label(t("boosters"),11f));root.addView(tray)
  attach(root)
  if(board.won||board.failed){val rewarded=if(board.won)save.win(board) else false;engine=null;showResult(board,rewarded)}
 }

 private fun showResult(board:Match3Engine,rewarded:Boolean){screen="result";if(!board.won&&save.sfxEnabled)sfx.play("fail");if(!board.won&&board.level==save.level)save.saveBoard(board);val root=FrameLayout(this);root.addView(GardenWorldView(this,save.tasks,decorations=save.decorations),FrameLayout.LayoutParams(-1,-1));root.addView(View(this).apply{setBackgroundColor(0xb9101826.toInt())},FrameLayout.LayoutParams(-1,-1));val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(22),dp(24),dp(22),dp(20));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,28f)};val stars=LinearLayout(this).apply{gravity=Gravity.CENTER};for(i in 0..2){val star=ImageView(this).apply{setImageResource(R.drawable.reward_star);alpha=if(i<board.earnedStars)1f else .18f;scaleX=.1f;scaleY=.1f};stars.addView(star,LinearLayout.LayoutParams(dp(if(i==1)88 else 65),dp(94)));star.animate().scaleX(1f).scaleY(1f).setStartDelay(i*150L).setDuration(350).setInterpolator(android.view.animation.OvershootInterpolator()).start()};card.addView(stars);card.addView(label(if(board.won)t("level_complete") else t("try_again"),26f,0xff79381d.toInt()));card.addView(label("${t("level")} ${board.level}",16f,0xff765530.toInt()).apply{setPadding(0,dp(10),0,dp(14))});val reward=label(if(board.won)if(rewarded)"+0 ${t("coins")}\n${t("star_reward")}" else "${board.earnedStars} ${t("stars")}\n${t("progress_saved")}" else "${t("score")}: ${board.score}/${board.spec.target}\n${t("ice")}: ${board.clearedIce}/${board.spec.ice}\n${FountainText.get(save.language,RestorationJobs.palette(save.tasks)[board.spec.collectColor].name)}: ${board.collected[board.spec.collectColor]}/${board.spec.collectCount}",20f,0xff704520.toInt());card.addView(reward);if(board.level==save.level&&!board.won)card.addView(premiumButton("▶ ${t("video_moves")}"){watchReward("moves"){showMatch3()}});if(board.won)card.addView(premiumButton("▶ ${t("optional_bonus")} · +100 ${t("coins")}"){watchReward("coins"){showGarden()}});card.addView(premiumButton(if(board.won)t("continue") else t("try_again"),true){if(board.won){if(save.fountainQuestActive)showFountainQuest() else showGarden()} else showMatch3(true,board.level)});card.addView(premiumButton(if(board.won)t("level_map") else t("garden")){if(board.won)showMap() else showGarden()});root.addView(card,FrameLayout.LayoutParams(-1,-2,Gravity.CENTER).apply{setMargins(dp(24),dp(12),dp(24),dp(12))});attach(root);card.alpha=0f;card.translationY=dp(30).toFloat();card.animate().alpha(1f).translationY(0f).setDuration(300).start();if(rewarded){if(save.sfxEnabled)sfx.play("reward");val balance=label("${save.coins-150} ${t("coins")}             ★ ${save.stars-1}",18f,0xff654123.toInt()).apply{background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,16f)};root.addView(balance,FrameLayout.LayoutParams(-1,dp(48),Gravity.TOP).apply{setMargins(dp(12),dp(8),dp(12),0)});root.addView(RewardFlightView(this),FrameLayout.LayoutParams(-1,-1));save.lastMaterialReward?.let{material->
 val materialHud=label("${FountainText.get(save.language,material.material.name)}  ${material.before}/${material.required}",15f,0xff427344.toInt()).apply{background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,16f)}
 root.addView(materialHud,FrameLayout.LayoutParams(-1,dp(42),Gravity.TOP).apply{setMargins(dp(32),dp(64),dp(32),0)})
 root.addView(MaterialRewardFlightView(this,material.material,material.after-material.before){
  materialHud.text="${FountainText.get(save.language,material.material.name)}  ${material.after}/${material.required}"
  if(save.sfxEnabled)sfx.play("plant")
  materialHud.animate().scaleX(1.12f).scaleY(1.12f).setDuration(150).withEndAction{materialHud.animate().scaleX(1f).scaleY(1f).setDuration(180).start()}.start()
 },FrameLayout.LayoutParams(-1,-1))
};rewardAnimator=ValueAnimator.ofInt(0,150).apply{duration=800;addUpdateListener{reward.text="+${it.animatedValue} ${t("coins")}\n${t("star_reward")}";balance.text="${save.coins-150+(it.animatedValue as Int)} ${t("coins")}             ★ ${save.stars}"};start()}}}
}

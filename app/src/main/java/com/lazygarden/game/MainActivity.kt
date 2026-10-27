package com.lazygarden.game

import android.animation.ValueAnimator
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
 private var world:GardenWorldView?=null
 private var screen="garden"
 private var rewardAnimator:ValueAnimator?=null
 private var completionDialog:AlertDialog?=null
 private val names=arrayOf("Rikthe ujin e shatërvanit","Rregullo shtegun","Mbille kopshtin me lule","Riparo stolin","Rikthe gjelbërimin","Restauro hyrjen","Restauro fasadën e pronës")

 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 override fun onCreate(state:Bundle?){
  super.onCreate(state);WindowCompat.setDecorFitsSystemWindows(window,false);save=GardenSave(this);showGarden();if(save.hasPendingStory && !save.constructing)showStory()
  onBackPressedDispatcher.addCallback(this,object:OnBackPressedCallback(true){override fun handleOnBackPressed(){if(screen=="garden")finish() else {engine?.let{save.saveBoard(it)};showGarden()}}})
 }
 override fun onResume(){super.onResume();world?.resumeScene()}
 override fun onPause(){world?.pauseScene();engine?.let{save.saveBoard(it)};super.onPause()}
 override fun onDestroy(){completionDialog?.dismiss();world?.pauseScene();rewardAnimator?.cancel();super.onDestroy()}
 private fun findWorld(view:View):GardenWorldView? {
  if(view is GardenWorldView)return view
  if(view is android.view.ViewGroup)for(i in 0 until view.childCount)findWorld(view.getChildAt(i))?.let{return it}
  return null
 }
 private fun attach(view:View){
  rewardAnimator?.cancel()
  val host=FrameLayout(this).apply{setBackgroundColor(0xff142f28.toInt());addView(view,FrameLayout.LayoutParams(-1,-1))}
  ViewCompat.setOnApplyWindowInsetsListener(host){v,insets->val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);insets}
  setContentView(host);world=findWorld(view);ViewCompat.requestApplyInsets(host)
  WindowInsetsControllerCompat(window,host).apply{systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;hide(WindowInsetsCompat.Type.systemBars())}
 }
 private fun label(value:String,size:Float=18f,color:Int=Color.WHITE)=TextView(this).apply{text=value;textSize=size;setTextColor(color);gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD;setShadowLayer(2f,0f,1f,0x77253020)}
 private fun premiumButton(value:String,primary:Boolean=false,action:()->Unit)=GameButton(this,primary).apply{
  text=value;isAllCaps=false;textSize=if(primary)23f else 15f;setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD
  minHeight=dp(if(primary)66 else 48);setPadding(dp(10),dp(6),dp(10),dp(6))

  layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(4),dp(5),dp(4),dp(5))}
  setOnClickListener{performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);action()}
 }
 private fun showGarden(){
  screen="garden";engine=null
  val root=FrameLayout(this)
  root.addView(GardenWorldView(this,save.tasks,save.constructing,save.decorations,onRestorationComplete={
   if(save.constructing){val fountain=save.tasks==0;save.finishRestoration();showGarden();if(fountain)showFountainComplete()}
  },initialElapsed=save.restorationElapsed,checkpoint={save.checkpointRestoration(it)}).apply{contentDescription="Shiko pronën";setOnClickListener{if(!save.constructing)showEstate()}},FrameLayout.LayoutParams(-1,-1))
  val top=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(8),dp(12),0)}
  val balances=LinearLayout(this).apply{gravity=Gravity.CENTER;background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,18f);setPadding(dp(12),0,dp(12),0)}
  balances.addView(label("${save.coins} Game Coins",16f,0xff64401b.toInt()),LinearLayout.LayoutParams(0,dp(44),1f))
  balances.addView(ImageView(this).apply{setImageResource(R.drawable.reward_star);scaleType=ImageView.ScaleType.FIT_CENTER},LinearLayout.LayoutParams(dp(28),dp(30)))
  balances.addView(label("${save.stars}",19f,0xff64401b.toInt()),LinearLayout.LayoutParams(dp(48),dp(44)))
  top.addView(balances)
  top.addView(ImageView(this).apply{setImageResource(R.drawable.hd_logo);scaleType=ImageView.ScaleType.FIT_CENTER;contentDescription="Logo LazyGarden"},LinearLayout.LayoutParams(-1,dp(132)))
  top.addView(label("Ndërto · Luaj · Dekoro\nKopshti i ëndrrave",15f,0xff633a19.toInt()).apply{background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,18f);setPadding(dp(14),dp(4),dp(14),dp(4))},LinearLayout.LayoutParams(-1,dp(53)).apply{setMargins(dp(48),0,dp(48),0)})
  root.addView(top,FrameLayout.LayoutParams(-1,-2,Gravity.TOP))
  fun side(value:String,icon:Int,action:()->Unit)=premiumButton(value,false,action).apply{
   textSize=13f;setPadding(dp(6),dp(9),dp(6),dp(8))
   val drawable=BitmapDrawable(resources,GameSkin.bitmap(this@MainActivity,icon)).apply{setBounds(0,0,dp(40),dp(40))}
   setCompoundDrawables(null,drawable,null,null);compoundDrawablePadding=dp(1)
  }
  val leftMenu=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  leftMenu.addView(side("Profil",R.drawable.character_meti){profile()},LinearLayout.LayoutParams(dp(79),dp(83)))
  leftMenu.addView(side("Cilësimet",R.drawable.hd_icon_gear){settings()},LinearLayout.LayoutParams(dp(79),dp(83)).apply{topMargin=dp(8)})
  root.addView(leftMenu,FrameLayout.LayoutParams(-2,-2,Gravity.START or Gravity.BOTTOM).apply{setMargins(dp(9),0,0,dp(158))})
  val rightMenu=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  rightMenu.addView(side("Ndërto",R.drawable.boost_hammer){tasks()},LinearLayout.LayoutParams(dp(79),dp(83)))
  rightMenu.addView(side("Dekoro",R.drawable.tile_flower){showDecor()},LinearLayout.LayoutParams(dp(79),dp(83)).apply{topMargin=dp(8)})
  root.addView(rightMenu,FrameLayout.LayoutParams(-2,-2,Gravity.END or Gravity.BOTTOM).apply{setMargins(0,0,dp(9),dp(158))})
  val bottom=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(10),0,dp(10),dp(10))}
  bottom.addView(label("Prona jonë · ${save.tasks}/7 restaurime",14f))
  bottom.addView(premiumButton(if(save.activeBoard!=null)"▶ Vazhdo · ${save.level}" else "▶ Luaj · ${save.level}",true){showMatch3()},LinearLayout.LayoutParams(dp(242),dp(80)).apply{setMargins(0,dp(5),0,dp(5))})
  val nav=LinearLayout(this)
  nav.addView(side("Nivelet",R.drawable.hd_icon_map){showMap()},LinearLayout.LayoutParams(0,dp(57),1f).apply{marginEnd=dp(8)})
  nav.addView(side(if(save.hasPendingStory)"Historia · E re" else "Historia",R.drawable.hd_icon_book){if(save.hasPendingStory)showStory() else showStoryArchive()},LinearLayout.LayoutParams(0,dp(57),1f))
  // Compact footer uses text; the illustrated side controls retain their full icons.
  for(i in 0 until nav.childCount)(nav.getChildAt(i) as Button).setCompoundDrawables(null,null,null,null)
  bottom.addView(nav);root.addView(bottom,FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));attach(root)

  if(save.constructing){bottom.visibility=View.INVISIBLE;leftMenu.visibility=View.INVISIBLE;rightMenu.visibility=View.INVISIBLE
   for(i in 1 until top.childCount)top.getChildAt(i).visibility=View.GONE

  }
 }
 private fun profile(){AlertDialog.Builder(this).setTitle("Profili lokal").setMessage("Niveli ${save.level}/250\nGame Coins: ${save.coins}\nYje për restaurim: ${save.stars}\nRestaurime: ${save.tasks}/7\n\nProgresi ruhet në këtë pajisje.").setPositiveButton("Mbyll",null).show()}
 private fun settings(){AlertDialog.Builder(this).setTitle("Cilësimet").setMessage("Gjuha: Shqip\nRuajtje automatike: aktive\n\nGame Coins përdoren vetëm brenda lojës. Nuk kanë vlerë monetare.").setPositiveButton("Mbyll",null).show()}
 private fun showMap(chapter:Int=(save.level-1)/25){
  screen="map";engine=null
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  root.addView(premiumButton("‹ Kopshti"){showGarden()})
  val scroll=ScrollView(this)
  val chapters=LinearLayout(this)
  chapters.addView(premiumButton("‹"){showMap((chapter-1).coerceAtLeast(0))},LinearLayout.LayoutParams(dp(48),dp(44)))
  chapters.addView(label("Kapitulli ${chapter+1} · ${chapter*25+1}–${chapter*25+25}",16f),LinearLayout.LayoutParams(0,dp(44),1f))
  chapters.addView(premiumButton("›"){showMap((chapter+1).coerceAtMost(9))},LinearLayout.LayoutParams(dp(48),dp(44)))
  root.addView(chapters)
  val map=LevelMapView(this,save,chapter){level->showMatch3(levelNumber=level)}
  scroll.addView(map);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));attach(root)
  scroll.post{scroll.scrollTo(0,if(chapter==(save.level-1)/25)map.currentY-scroll.height/2 else map.measuredHeight-scroll.height)}
 }
 private fun tasks(){
  if(save.constructing)return
  if(save.tasks>=names.size){AlertDialog.Builder(this).setTitle("Prona u restaurua").setMessage("Të shtatë restaurimet janë kryer. Vazhdo udhëtimin në hartën e niveleve.").setPositiveButton("Nivelet"){_,_->showMap()}.show();return}
  AlertDialog.Builder(this).setTitle(names[save.tasks]).setMessage("${names.mapIndexed{i,n->if(i<save.tasks)"✓ $n" else if(i==save.tasks)"→ $n" else "🔒 $n"}.joinToString("\n\n")}\n\nKosto: 120 Game Coins${if(save.tasks>0)" + 1 yll" else ""}")
   .setPositiveButton("Restauro"){_,_->if(save.beginRestoration())showGarden() else Toast.makeText(this,"Fito nivele për të marrë Game Coins dhe yje",Toast.LENGTH_LONG).show()}.setNegativeButton("Mbyll",null).show()
 }
 private fun showEstate(){
  screen="estate";engine=null
  val root=FrameLayout(this)
  root.addView(GardenWorldView(this,save.tasks,decorations=save.decorations),FrameLayout.LayoutParams(-1,-1))
  val controls=LinearLayout(this).apply{setPadding(dp(10),dp(8),dp(10),0)}
  controls.addView(premiumButton("‹ Ballina"){showGarden()},LinearLayout.LayoutParams(0,dp(46),1f).apply{marginEnd=dp(10)})
  controls.addView(premiumButton("Dekoro"){showDecor()},LinearLayout.LayoutParams(0,dp(46),1f))
  root.addView(controls,FrameLayout.LayoutParams(-1,-2,Gravity.TOP))
  root.addView(label("${save.tasks}/7 restaurime · ${save.decorations.size} ${if(save.decorations.size==1)"dekorim" else "dekorime"}",14f).apply{setPadding(0,0,0,dp(14))},FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM))
  attach(root)
 }
 private fun showDecor(category:DecorCategory=DecorCategory.DECOR,preview:DecorItem?=null){
  screen="decor";engine=null
  attach(DecorationScreen(this,save,category,preview,{showGarden()},{showDecor(it)},{showDecor(category,it)},{item->
   when(save.selectDecoration(item.id)){
    GardenSave.DecorResult.PURCHASED,GardenSave.DecorResult.SELECTED->showDecor(category)
    GardenSave.DecorResult.LOCKED->Toast.makeText(this,"Përfundo ${item.requiredTasks} ${if(item.requiredTasks==1)"restaurim" else "restaurime"} për këtë dekorim",Toast.LENGTH_LONG).show()
    GardenSave.DecorResult.INSUFFICIENT->Toast.makeText(this,"Duhen ${item.cost} Game Coins",Toast.LENGTH_LONG).show()
    GardenSave.DecorResult.INVALID->Toast.makeText(this,"Zgjedhja nuk u ruajt",Toast.LENGTH_SHORT).show()
   }
  },{save.removeDecoration(category);showDecor(category)}))
 }
 private fun showStory(replayChapter:Int?=null,replayLine:Int=0){
  if(replayChapter==null && !save.hasPendingStory){showStoryArchive();return}
  val position=save.storyPosition
  val chapter=replayChapter ?: position/GardenStory.LINES_PER_CHAPTER
  val line=if(replayChapter==null)position%GardenStory.LINES_PER_CHAPTER else replayLine
  if(chapter !in 0 until save.tasks.coerceAtMost(7)){showGarden();return}
  screen="story";engine=null
  attach(StoryScreen(this,save,chapter,line,replayChapter!=null,{
   if(replayChapter!=null){if(line<2)showStory(chapter,line+1) else showStoryArchive()}
   else if(save.advanceStory(position)){if(save.hasPendingStory)showStory() else showGarden()}
  },{if(replayChapter!=null)showStoryArchive() else showGarden()}))
 }
 private fun showFountainComplete(){
  val dialog=AlertDialog.Builder(this).create();completionDialog=dialog
  val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(18),dp(20),dp(18),dp(16));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,26f)}
  card.addView(label("Detyra e përfunduar!",25f,0xff654123.toInt()))
  card.addView(label("Shatërvani është përsëri gjallë!",16f,0xff50713b.toInt()).apply{setPadding(0,dp(8),0,dp(12))})
  val comparison=LinearLayout(this)
  listOf("Para" to R.drawable.garden_before,"Pas" to R.drawable.garden_estate).forEach{(title,art)->
   val tile=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
   tile.addView(label(title,14f,0xff654123.toInt()))
   tile.addView(ImageView(this).apply{setImageBitmap(GameSkin.bitmap(this@MainActivity,art));scaleType=ImageView.ScaleType.CENTER_CROP;contentDescription="Shatërvani: $title"},LinearLayout.LayoutParams(-1,dp(112)))
   comparison.addView(tile,LinearLayout.LayoutParams(0,-2,1f).apply{setMargins(dp(3),0,dp(3),0)})
  }
  card.addView(comparison)
  card.addView(premiumButton("Vazhdo ▶",true){dialog.dismiss()})
  dialog.setView(card);dialog.show();dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
  dialog.setOnDismissListener{if(completionDialog===dialog)completionDialog=null}
 }
 private fun showFountainReplay(){
  screen="fountain_replay";engine=null
  val root=FrameLayout(this)
  // This choreography has no save/payment callbacks: replay never changes progression.
  root.addView(GardenWorldView(this,0,true,onRestorationComplete={showGarden()}),FrameLayout.LayoutParams(-1,-1))
  val back=premiumButton("‹ Kopshti"){showGarden()}
  root.addView(back,FrameLayout.LayoutParams(dp(130),dp(48),Gravity.TOP or Gravity.START).apply{setMargins(dp(10),dp(8),0,0)})
  attach(root)
 }
 private fun showStoryArchive(){
  screen="story_archive";engine=null
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(8),dp(12),dp(10));setBackgroundColor(0xff204d40.toInt())}
  root.addView(premiumButton("‹ Kopshti"){showGarden()});root.addView(label("Historia e pronës",26f,0xffffdf83.toInt()))
  if(save.fountainRestored)root.addView(premiumButton("▶ Riluaj shatërvanin"){showFountainReplay()})
  val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  GardenStory.chapters.forEachIndexed{i,chapter->
   val unlocked=i<save.tasks;val read=save.storyPosition>=(i+1)*3
   list.addView(premiumButton("${i+1}. ${chapter.title}\n${if(!unlocked)"🔒 Përfundo restaurimin ${i+1}" else if(read)"Rilexo" else "Vazhdo tregimin"}"){
    if(unlocked){if(read)showStory(i,0) else showStory()}else Toast.makeText(this,"Ky kapitull hapet pas restaurimit",Toast.LENGTH_SHORT).show()
   }.apply{contentDescription="Kapitulli ${i+1}"})
  }
  root.addView(ScrollView(this).apply{addView(list)},LinearLayout.LayoutParams(-1,0,1f));attach(root)
 }
 private fun showMatch3(restart:Boolean=false,levelNumber:Int=save.level){
  screen="match"
  val board=Match3Engine(levelNumber)
  if(!restart && levelNumber==save.level)save.activeBoard?.let{board.restore(it)}
  if(restart && levelNumber==save.level)save.clearBoard()
  engine=board
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(0xff153f42.toInt())}
  val bar=LinearLayout(this).apply{background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,15f)}
  bar.addView(premiumButton("‹"){save.saveBoard(board);showGarden()},LinearLayout.LayoutParams(dp(48),dp(36)))
  val coins=label("${save.coins} Game Coins",15f,0xff66411f.toInt())
  bar.addView(coins,LinearLayout.LayoutParams(0,dp(36),1f));root.addView(bar)
  val boardView=Match3BoardView(this,board,{save.saveBoard(board)},{won->val rewarded=if(won)save.win(board) else false;engine=null;showResult(board,rewarded)})
  boardView.onBoosterTarget={kind,at->
   if(board.level!=save.level){Toast.makeText(this,"Përforcuesit përdoren në nivelin aktual",Toast.LENGTH_SHORT).show();emptyList()}else{
    val waves=save.buyBooster(board,kind,at)
    coins.text="${save.coins} Game Coins"
    if(waves.isEmpty())Toast.makeText(this,"Duhen ${kind.cost} Game Coins",Toast.LENGTH_SHORT).show()
    waves
   }
  }
  root.addView(boardView,LinearLayout.LayoutParams(-1,0,1f))
  val tray=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(dp(5),0,dp(5),dp(7))}
  val icons=intArrayOf(R.drawable.boost_hammer,R.drawable.boost_row,R.drawable.boost_column,R.drawable.boost_cross,R.drawable.boost_bomb,R.drawable.boost_color,R.drawable.boost_shuffle)
  Match3Engine.Booster.entries.forEachIndexed{i,kind->
   val tile=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,10f);contentDescription="${kind.label}, ${kind.cost} Game Coins"}
   tile.addView(ImageView(this).apply{setImageResource(icons[i]);scaleType=ImageView.ScaleType.FIT_CENTER},LinearLayout.LayoutParams(dp(40),dp(42)))
   tile.addView(label("${kind.cost}",11f,0xff50341b.toInt()))
   tile.setOnClickListener{boardView.runBooster(kind)}
   tray.addView(tile,LinearLayout.LayoutParams(0,dp(67),1f).apply{setMargins(dp(2),dp(3),dp(2),0)})
  }
  root.addView(label("Përforcues · Game Coins",11f));root.addView(tray);attach(root)
  if(board.won||board.failed){val rewarded=if(board.won)save.win(board) else false;engine=null;showResult(board,rewarded)}
 }
 private fun showResult(board:Match3Engine,rewarded:Boolean){
  screen="result"
  if(!board.won && board.level==save.level)save.saveBoard(board)
  val root=FrameLayout(this)
  root.addView(GardenWorldView(this,save.tasks,decorations=save.decorations),FrameLayout.LayoutParams(-1,-1))
  root.addView(View(this).apply{setBackgroundColor(0xb9101826.toInt())},FrameLayout.LayoutParams(-1,-1))
  val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(22),dp(24),dp(22),dp(20));background=GameSkin.background(this@MainActivity,R.drawable.hd_hud,28f)}
  val stars=LinearLayout(this).apply{gravity=Gravity.CENTER}
  for(i in 0..2){val star=ImageView(this).apply{setImageResource(R.drawable.reward_star);alpha=if(i<board.earnedStars)1f else .18f;scaleX=.1f;scaleY=.1f}
   stars.addView(star,LinearLayout.LayoutParams(dp(if(i==1)88 else 65),dp(94)))
   star.animate().scaleX(1f).scaleY(1f).setStartDelay(i*150L).setDuration(350).setInterpolator(android.view.animation.OvershootInterpolator()).start()
  }
  card.addView(stars)
  card.addView(label(if(board.won)"Niveli i përfunduar!" else "Provo përsëri",26f,0xff79381d.toInt()))
  card.addView(label("Niveli ${board.level}",16f,0xff765530.toInt()).apply{setPadding(0,dp(10),0,dp(14))})
  val reward=label(if(board.won)if(rewarded)"+0 Game Coins\n+1 yll për restaurim" else "${board.earnedStars} yje\nProgresi është ruajtur" else "Pikët: ${board.score}/${board.spec.target}\nAkulli: ${board.clearedIce}/${board.spec.ice}\n${arrayOf("Mollë","Lule","Gjethe","Ujë","Manaferra","Portokaj")[board.spec.collectColor]}: ${board.collected[board.spec.collectColor]}/${board.spec.collectCount}",20f,0xff704520.toInt())
  card.addView(reward)
  card.addView(premiumButton(if(board.won)"Vazhdo" else "Provo përsëri",true){if(board.won)showGarden() else showMatch3(true,board.level)})
  card.addView(premiumButton(if(board.won)"Harta e niveleve" else "Kopshti"){if(board.won)showMap() else showGarden()})
  root.addView(card,FrameLayout.LayoutParams(-1,-2,Gravity.CENTER).apply{setMargins(dp(24),dp(12),dp(24),dp(12))});attach(root)
  card.alpha=0f;card.translationY=dp(30).toFloat();card.animate().alpha(1f).translationY(0f).setDuration(300).start()
  if(rewarded){rewardAnimator=ValueAnimator.ofInt(0,150).apply{duration=800;addUpdateListener{reward.text="+${it.animatedValue} Game Coins\n+1 yll për restaurim"};start()}}
 }
}

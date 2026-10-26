package com.lazygarden.game

import android.animation.ValueAnimator
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
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
 private var screen="garden"
 private var rewardAnimator:ValueAnimator?=null
 private val names=arrayOf("Rikthe ujin e shatërvanit","Rregullo shtegun","Mbille kopshtin me lule","Riparo stolin","Rikthe gjelbërimin","Restauro hyrjen","Restauro fasadën e pronës")
 private val story=arrayOf("Meli: Dëgjo ujin! Ky shatërvan ruan kujtimet e familjes sonë. Tani të zbulojmë shtegun.","Bejdi: Nën barin e egër gjetëm gurët e vjetër. Do të na çojnë deri te shtëpia.","Lina: Mbollëm lule për çdo njeri që na ndihmoi. Kopshti po merr përsëri ngjyra.","Meti: Gjyshi pushonte këtu çdo mbrëmje. Tani kemi një vend ku të ulemi bashkë.","Lina: Pemët kanë nevojë për kujdes, njësoj si kjo shtëpi. Do të rriten me historinë tonë.","Bejdi: Porta është hapur. Le t'i ftojmë fqinjët ta shohin çfarë kemi bërë.","Meli: Nuk riparuam vetëm muret. I kthyem jetën shtëpisë sonë. Kjo është vetëm fillimi!")
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 override fun onCreate(state:Bundle?){
  super.onCreate(state);save=GardenSave(this);showGarden()
  onBackPressedDispatcher.addCallback(this,object:OnBackPressedCallback(true){override fun handleOnBackPressed(){if(screen=="garden")finish() else {engine?.let{save.saveBoard(it)};showGarden()}}})
 }
 override fun onPause(){engine?.let{save.saveBoard(it)};super.onPause()}
 override fun onDestroy(){rewardAnimator?.cancel();super.onDestroy()}
 private fun attach(view:View){
  rewardAnimator?.cancel()
  val host=FrameLayout(this).apply{setBackgroundColor(0xff142f28.toInt());addView(view,FrameLayout.LayoutParams(-1,-1))}
  ViewCompat.setOnApplyWindowInsetsListener(host){v,insets->val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);insets}
  setContentView(host);ViewCompat.requestApplyInsets(host)
 }
 private fun label(value:String,size:Float=18f,color:Int=Color.WHITE)=TextView(this).apply{text=value;textSize=size;setTextColor(color);gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD;setShadowLayer(2f,0f,1f,0x77253020)}
 private fun premiumButton(value:String,primary:Boolean=false,action:()->Unit)=Button(this).apply{
  text=value;isAllCaps=false;textSize=if(primary)23f else 15f;setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD
  minHeight=dp(if(primary)66 else 48);setPadding(dp(10),dp(6),dp(10),dp(6))
  background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,if(primary)intArrayOf(0xff8bda30.toInt(),0xff218a16.toInt()) else intArrayOf(0xff3b97b3.toInt(),0xff164966.toInt())).apply{cornerRadius=dp(18).toFloat();setStroke(dp(2),0xffffe6a4.toInt())}
  layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(dp(4),dp(5),dp(4),dp(5))}
  setOnClickListener{performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);action()}
 }
 private fun showGarden(){
  screen="garden";engine=null
  val root=FrameLayout(this)
  root.addView(GardenWorldView(this,save.tasks,save.constructing),FrameLayout.LayoutParams(-1,-1))
  val top=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(10),dp(12),0)}
  val utility=LinearLayout(this)
  utility.addView(premiumButton("Profil"){profile()},LinearLayout.LayoutParams(0,-2,1f).apply{marginEnd=dp(8)})
  utility.addView(premiumButton("Cilësimet"){settings()},LinearLayout.LayoutParams(0,-2,1f))
  top.addView(utility)
  top.addView(label("LazyGarden",42f,0xffffdc6f.toInt()))
  top.addView(label("Ndërto · Luaj · Dekoro\nKopshti i ëndrrave",16f))
  top.addView(label("${save.coins} Game Coins     ★ ${save.stars}",18f).apply{setPadding(0,dp(14),0,0)})
  root.addView(top,FrameLayout.LayoutParams(-1,-2,Gravity.TOP))
  val bottom=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),0,dp(16),dp(16))}
  bottom.addView(label("Prona jonë · ${save.tasks}/7 restaurime",15f))
  val row=LinearLayout(this)
  row.addView(premiumButton("Ndërto"){tasks()},LinearLayout.LayoutParams(0,-2,1f).apply{marginEnd=dp(8)})
  row.addView(premiumButton("Nivelet"){showMap()},LinearLayout.LayoutParams(0,-2,1f))
  bottom.addView(row)
  bottom.addView(premiumButton(if(save.activeBoard!=null)"▶ Vazhdo · ${save.level}" else "▶ Luaj · ${save.level}",true){showMatch3()})
  root.addView(bottom,FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));attach(root)
  if(save.constructing){bottom.visibility=View.INVISIBLE
   root.postDelayed({if(root.isAttachedToWindow && screen=="garden" && save.constructing){save.finishRestoration();showGarden();showStory()}},2200)
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
 private fun showStory(){
  val i=(save.tasks-1).coerceIn(0,6)
  AlertDialog.Builder(this).setTitle("${names[i]} · Përfundoi").setMessage(story[i]).setPositiveButton("Vazhdo",null).show()
 }
 private fun showMatch3(restart:Boolean=false,levelNumber:Int=save.level){
  screen="match"
  val board=Match3Engine(levelNumber)
  if(!restart && levelNumber==save.level)save.activeBoard?.let{board.restore(it)}
  if(restart && levelNumber==save.level)save.clearBoard()
  engine=board
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(0xff153f42.toInt())}
  val bar=LinearLayout(this)
  bar.addView(premiumButton("‹ Kopshti"){save.saveBoard(board);showGarden()},LinearLayout.LayoutParams(0,dp(44),1f))
  val coins=label("${save.coins} Coins",14f)
  bar.addView(coins,LinearLayout.LayoutParams(0,dp(44),1f));root.addView(bar)
  val boardView=Match3BoardView(this,board,{save.saveBoard(board)},{won->val rewarded=if(won)save.win(board) else false;engine=null;showResult(board,rewarded)})
  boardView.onBoosterTarget={kind,at->
   if(board.level!=save.level){Toast.makeText(this,"Përforcuesit përdoren në nivelin aktual",Toast.LENGTH_SHORT).show();emptyList()}else{
    val waves=save.buyBooster(board,kind,at)
    coins.text="${save.coins} Coins"
    if(waves.isEmpty())Toast.makeText(this,"Duhen ${kind.cost} Game Coins",Toast.LENGTH_SHORT).show()
    waves
   }
  }
  root.addView(boardView,LinearLayout.LayoutParams(-1,0,1f))
  val tray=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(dp(5),0,dp(5),dp(7))}
  val icons=intArrayOf(R.drawable.boost_hammer,R.drawable.boost_row,R.drawable.boost_column,R.drawable.boost_cross,R.drawable.boost_bomb,R.drawable.boost_color,R.drawable.boost_shuffle)
  Match3Engine.Booster.entries.forEachIndexed{i,kind->
   val tile=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xffffe9bf.toInt(),0xffdba85f.toInt())).apply{cornerRadius=dp(10).toFloat()};contentDescription="${kind.label}, ${kind.cost} Game Coins"}
   tile.addView(ImageView(this).apply{setImageResource(icons[i]);scaleType=ImageView.ScaleType.FIT_CENTER},LinearLayout.LayoutParams(dp(37),dp(38)))
   tile.addView(label("${kind.cost}",11f,0xff50341b.toInt()))
   tile.setOnClickListener{boardView.runBooster(kind)}
   tray.addView(tile,LinearLayout.LayoutParams(0,dp(62),1f).apply{setMargins(dp(2),dp(3),dp(2),0)})
  }
  root.addView(label("Përforcues · Game Coins",11f));root.addView(tray);attach(root)
  if(board.won||board.failed){val rewarded=if(board.won)save.win(board) else false;engine=null;showResult(board,rewarded)}
 }
 private fun showResult(board:Match3Engine,rewarded:Boolean){
  screen="result"
  if(!board.won && board.level==save.level)save.saveBoard(board)
  val root=FrameLayout(this)
  root.addView(GardenWorldView(this,save.tasks),FrameLayout.LayoutParams(-1,-1))
  root.addView(View(this).apply{setBackgroundColor(0xb9101826.toInt())},FrameLayout.LayoutParams(-1,-1))
  val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(22),dp(24),dp(22),dp(20));background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xfffff1d4.toInt(),0xffeac58a.toInt())).apply{cornerRadius=dp(28).toFloat();setStroke(dp(3),0xffa0672b.toInt())}}
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

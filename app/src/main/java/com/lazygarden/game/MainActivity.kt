package com.lazygarden.game

import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
 private lateinit var save:GardenSave
 private var engine:Match3Engine?=null
 private val names=arrayOf("Repair Fountain","Clean Path","Restore Flower Bed","Repair Bench","Plant Trees","Restore Entrance","Repair Manor Exterior")
 private val story=arrayOf("Meli: Water returns to our garden. Let's uncover the path!","Bejdi: These stones lead all the way to the manor.","Lina: Flowers make this place feel like home again.","Meti: A quiet seat for everyone who helped.","Lina: These young trees will grow with our story.","Bejdi: The entrance is ready to welcome our neighbours.","Meli: Our first chapter is complete. This home lives again!")
 override fun onCreate(state:Bundle?){super.onCreate(state);save=GardenSave(this);showGarden()}
 override fun onPause(){engine?.let{save.saveBoard(it)};super.onPause()}
 private fun showGarden(){
  engine=null
  val root=FrameLayout(this)
  root.addView(GardenWorldView(this,save.tasks,save.constructing),FrameLayout.LayoutParams(-1,-1))
  val hud=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,48,24,12)}
  hud.addView(TextView(this).apply{text="LazyGarden";typeface=Typeface.DEFAULT_BOLD;textSize=42f;setTextColor(-1);setShadowLayer(5f,0f,2f,0xff123c37.toInt())})
  hud.addView(TextView(this).apply{text="Ndërto · Luaj · Dekoro\nKopshti i ëndrrave\n\nGame Coins ${save.coins}   ★ ${save.stars}   Level ${save.level}/250";textSize=17f;setTextColor(-1)})
  val controls=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  controls.addView(premiumButton("Ndërto · ${save.tasks}/7 restaurime",false){tasks()})
  controls.addView(premiumButton("Harta e niveleve",false){showMap()})
  controls.addView(premiumButton("Profili dhe progresi",false){AlertDialog.Builder(this).setTitle("Profili lokal").setMessage("Niveli ${save.level}/250\nGame Coins: ${save.coins}\nYje për restaurim: ${save.stars}\nRestaurime: ${save.tasks}/7\n\nProgresi ruhet në këtë pajisje.").setPositiveButton("Mbyll",null).show()})
  controls.addView(premiumButton(if(save.activeBoard!=null)"▶ Vazhdo · ${save.level}" else "▶ Luaj · ${save.level}",true){showMatch3()})
  root.addView(hud);root.addView(controls,FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM).apply{setMargins(24,0,24,28)})
  setContentView(root)
  if(save.constructing)root.postDelayed({if(!isFinishing && engine==null){save.finishRestoration();showGarden();AlertDialog.Builder(this).setTitle("Restoration complete").setMessage(story[save.tasks-1]).setPositiveButton("Continue",null).show()}},2400)
 }
 private fun premiumButton(label:String, primary:Boolean, action:()->Unit):Button = Button(this).apply {
  text=label;isAllCaps=false;textSize=if(primary)24f else 17f;setTextColor(Color.WHITE)
  typeface=Typeface.DEFAULT_BOLD;minHeight=(if(primary)76 else 54).let{(it*resources.displayMetrics.density).toInt()}
  background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,if(primary)intArrayOf(0xff81d82f.toInt(),0xff218a16.toInt()) else intArrayOf(0xff318cb0.toInt(),0xff174a6a.toInt())).apply{cornerRadius=28f;setStroke(3,0xffffe5a3.toInt())}
  layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,6,0,6)}
  setOnClickListener{action()}
 }
 private fun showMap(){
  engine=null
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  root.addView(premiumButton("‹ Kopshti",false){showGarden()})
  val scroll=ScrollView(this)
  val map=LevelMapView(this,save){level->if(level==save.level)showMatch3() else Toast.makeText(this,"Niveli $level · ${save.rating(level)} yje. Riluajtja vjen më vonë.",Toast.LENGTH_SHORT).show()}
  scroll.addView(map);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));setContentView(root)
  scroll.post{scroll.scrollTo(0,map.measuredHeight-scroll.height)}
 }
 private fun tasks(){
  if(save.constructing)return
  if(save.tasks>=names.size){AlertDialog.Builder(this).setTitle("Estate chapter complete").setMessage("All seven restorations are complete. Continue the match campaign.").setPositiveButton("Play"){_,_->showMatch3()}.show();return}
  AlertDialog.Builder(this).setTitle(names[save.tasks]).setMessage("${names.mapIndexed{i,n->if(i<save.tasks)"✓ $n" else if(i==save.tasks)"→ $n" else "Locked · $n"}.joinToString("\n")}\n\nCost: 120 coins${if(save.tasks>0)" + 1 star" else ""}")
   .setPositiveButton("Restore"){_,_->if(save.beginRestoration())showGarden() else Toast.makeText(this,"Win a level to earn coins and stars",Toast.LENGTH_LONG).show()}.setNegativeButton("Close",null).show()
 }
 private fun showMatch3(restart:Boolean=false){
  val board=Match3Engine(save.level)
  if(!restart)save.activeBoard?.let{board.restore(it)} else save.clearBoard()
  engine=board
  fun result(won:Boolean){
   if(won)save.win(board)
   engine=null
   AlertDialog.Builder(this).setTitle(if(won)"Niveli i përfunduar!" else "Mbaruan lëvizjet")
    .setMessage(if(won)"${"★".repeat(board.earnedStars)}\n+150 Game Coins · +1 yll për restaurim" else "Your estate progress is safe. Try another approach.")
    .setCancelable(false).setPositiveButton(if(won)"Garden" else "Restart"){_,_->if(won)showGarden() else showMatch3(true)}
    .setNegativeButton(if(won)"Next level" else "Garden"){_,_->if(won)showMatch3() else {save.clearBoard();showGarden()}}.show()
  }
  setContentView(Match3BoardView(this,board,{save.saveBoard(board)},{result(it)}))
  if(board.won||board.failed)result(board.won)
 }
}

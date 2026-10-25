package com.lazygarden.game

import android.os.Bundle
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
  hud.addView(TextView(this).apply{text="LAZY GARDEN";textSize=28f;setTextColor(-1);setShadowLayer(5f,0f,2f,0xff123c37.toInt())})
  hud.addView(TextView(this).apply{text="Coins ${save.coins}   ★ ${save.stars}   Level ${save.level}/250";textSize=17f;setTextColor(-1)})
  val controls=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
  controls.addView(Button(this).apply{text="TASKS · ${save.tasks}/7 restored";setOnClickListener{tasks()}})
  controls.addView(Button(this).apply{text=if(save.activeBoard!=null)"CONTINUE LEVEL ${save.level}" else "PLAY LEVEL ${save.level}";setOnClickListener{showMatch3()}})
  root.addView(hud);root.addView(controls,FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM).apply{setMargins(24,0,24,28)})
  setContentView(root)
  if(save.constructing)root.postDelayed({if(!isFinishing && engine==null){save.finishRestoration();showGarden();AlertDialog.Builder(this).setTitle("Restoration complete").setMessage(story[save.tasks-1]).setPositiveButton("Continue",null).show()}},2400)
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
   AlertDialog.Builder(this).setTitle(if(won)"Garden victory!" else "Out of moves")
    .setMessage(if(won)"+150 coins · +1 star" else "Your estate progress is safe. Try another approach.")
    .setCancelable(false).setPositiveButton(if(won)"Garden" else "Restart"){_,_->if(won)showGarden() else showMatch3(true)}
    .setNegativeButton(if(won)"Next level" else "Garden"){_,_->if(won)showMatch3() else {save.clearBoard();showGarden()}}.show()
  }
  setContentView(Match3BoardView(this,board,{save.saveBoard(board)},{result(it)}))
  if(board.won||board.failed)result(board.won)
 }
}

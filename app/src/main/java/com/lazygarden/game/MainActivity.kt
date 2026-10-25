package com.lazygarden.game

import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var save: GardenSave
    override fun onCreate(state: Bundle?) { super.onCreate(state); save=GardenSave(this); showGarden() }

    private fun showGarden() {
        val root=FrameLayout(this)
        val scene=GardenWorldView(this, save.fountainRestored)
        root.addView(scene, FrameLayout.LayoutParams(-1,-1))
        val hud=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,28,28,28) }
        hud.addView(TextView(this).apply { text="LAZY GARDEN"; textSize=27f; setTextColor(0xffffffff.toInt()); setShadowLayer(8f,0f,3f,0xaa000000.toInt()) })
        hud.addView(TextView(this).apply { text="Estate restoration • Coins ${save.coins}"; textSize=14f; setTextColor(0xffffe39a.toInt()) })
        val task=Button(this).apply { text=if(save.fountainRestored) "NEXT RESTORATION" else "RESTORE THE FOUNTAIN • 120"; setOnClickListener {
            if(!save.fountainRestored && save.coins>=120){ save.spendAndRestore(); showGarden() } else showMatch3()
        }}
        val lp=FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM).apply { setMargins(32,0,32,38) }
        root.addView(task,lp); root.addView(hud)
        setContentView(root)
    }

    private fun showMatch3(){
        setContentView(Match3BoardView(this,1){won -> if(won){ save.addCoins(150); showGarden() } else showGarden() })
    }
}

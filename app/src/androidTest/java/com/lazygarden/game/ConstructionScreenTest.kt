package com.lazygarden.game

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConstructionScreenTest {
    private fun views(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{views(v.getChildAt(it))}else emptyList()
    @Test fun selectingLockedAndCompletedSitesCannotSpendOrUnlock(){
        val ctx=ApplicationProvider.getApplicationContext<Context>()
        val prefs=ctx.getSharedPreferences("garden_progress",Context.MODE_PRIVATE)
        val backup=HashMap(prefs.all)
        try {
            prefs.edit().clear().putInt("tasks",3).putInt("story_position",9).putInt("coins",500).putInt("stars",2).commit()
            val save=GardenSave(ctx)
            var built=0;var replayed=-1;var narrated=-1
            ActivityScenario.launch(MainActivity::class.java).use{scenario->scenario.onActivity{activity->
                val screen=ConstructionScreen(activity,save,{},{built++},{},{replayed=it},{narrated=it})
                activity.setContentView(screen)
                fun select(task:Int){views(screen).filterIsInstance<Button>().first{it.contentDescription==RestorationCatalog.title(task,"sq")}.performClick()}
                select(19)
                assertFalse(views(screen).filterIsInstance<Button>().any{it.text.startsWith("Ndërto •")})
                select(0)
                views(screen).filterIsInstance<Button>().first{it.text.startsWith("▶ Riluaj")}.performClick()
                views(screen).filterIsInstance<Button>().first{it.text=="Biseda dhe historia"}.performClick()
                assertEquals(0,replayed);assertEquals(0,narrated)
                select(3)
                views(screen).filterIsInstance<Button>().first{it.text.startsWith("Ndërto •")}.performClick()
                assertEquals(1,built)
                assertTrue(views(screen).filterIsInstance<TextView>().any{it.text.contains("Luli")})
                assertEquals(500,save.coins);assertEquals(2,save.stars);assertEquals(3,save.tasks)
                assertFalse(save.constructing)
            }}
        } finally {
            val edit=prefs.edit().clear()
            backup.forEach{(key,value)->when(value){is Int->edit.putInt(key,value);is Long->edit.putLong(key,value);is Boolean->edit.putBoolean(key,value);is String->edit.putString(key,value);is Float->edit.putFloat(key,value)}}
            edit.commit()
        }
    }
}

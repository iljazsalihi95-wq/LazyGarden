package com.lazygarden.game

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoryLanguageFlowTest {
 private fun texts(v:View):List<String> =(if(v is TextView)listOf(v.text.toString())else emptyList())+if(v is ViewGroup)(0 until v.childCount).flatMap{texts(v.getChildAt(it))}else emptyList()
 @Test fun languageSelectionAppliesToStoryTextTitleAndButtonsTogether(){
  val context=ApplicationProvider.getApplicationContext<Context>()
  context.getSharedPreferences("garden_progress",0).edit().clear().putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()
  ActivityScenario.launch(MainActivity::class.java).use { scenario ->
   scenario.onActivity{activity->
    val save=GardenSave(context)
    for(language in listOf("sq","de","it","mk","tr")){
     save.setLanguage(language)
     val screen=StoryScreen(activity,save,17,1,false,{},{})
     activity.setContentView(screen);screen.pauseScene()
     val strings=texts(screen)
     assertTrue(strings.contains(StoryTranslations.line(17,1,language)))
     assertTrue(strings.any{it.contains(RestorationCatalog.title(17,language))})
     assertTrue(strings.contains(UiText.get(language,"continue_story")))
     if(language!="sq")assertFalse(strings.contains(GardenStory.chapters[17].lines[1].text))
    }
   }
  }
 }
}

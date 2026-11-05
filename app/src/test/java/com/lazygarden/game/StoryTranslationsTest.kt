package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test

class StoryTranslationsTest {
 @Test fun allTwentyChaptersHaveThreeOfflineLinesInEveryLanguage(){
  for(chapter in 0..19)for(line in 0..2){
   val sq=StoryTranslations.line(chapter,line,"sq")
   assertEquals(GardenStory.chapters[chapter].lines[line].text,sq)
   for(language in listOf("de","it","mk","tr")){
    val translated=StoryTranslations.line(chapter,line,language)
    assertTrue(translated.length>15);assertNotEquals("Missing $language: $chapter/$line",sq,translated)
   }
  }
 }
 @Test fun menuAndDecorationNamesHaveNoAlbanianFallbackInOtherLanguages(){
  for(key in listOf("continue_story","estate_story","build_materials","choose_build","placed","remove_decoration")+GardenDecor.items.map{it.id}){
   for(language in listOf("de","it","mk","tr"))assertNotEquals("$language/$key",UiText.get("sq",key),UiText.get(language,key))
  }
 }
}

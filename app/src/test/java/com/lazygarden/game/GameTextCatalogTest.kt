package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
class GameTextCatalogTest {
 @Test fun gameplayKeysArePresentInEverySupportedLanguage(){
  for(lang in listOf("sq","de","it","mk","tr"))assertTrue("Missing $lang entries: ${GameText.untranslatedKeys(lang)}",GameText.untranslatedKeys(lang).isEmpty())
 }
 @Test fun primaryGameplayLabelsAreActualSelectedLanguageTranslations(){
  val expected=mapOf("sq" to ("Luaj" to "Niveli i përfunduar!"),"de" to ("Spielen" to "Level geschafft!"),"it" to ("Gioca" to "Livello completato!"),"mk" to ("Играј" to "Нивото е завршено!"),"tr" to ("Oyna" to "Seviye tamamlandı!"))
  for((lang,labels)in expected){assertEquals(labels.first,GameText.get(lang,"play"));assertEquals(labels.second,GameText.get(lang,"level_complete"));assertFalse(GameText.get(lang,"estate_progress").replace("{count}","3").contains('{'))}
 }
}

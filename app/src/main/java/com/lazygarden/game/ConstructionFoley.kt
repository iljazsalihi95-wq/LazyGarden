package com.lazygarden.game
import kotlin.math.*

/** Distinct original impact textures. Returns null for the musical/reward cues. */
object ConstructionFoley {
 fun sample(key:String,t:Double,noise:Double):Double?=when(key){
  "hammer"->sin(2*PI*125*t)*exp(-t*32)*.75+sin(2*PI*340*t)*exp(-t*48)*.25+noise*exp(-t*120)*.45
  "stone"->noise*exp(-t*45)*.75+sin(2*PI*1730*t)*exp(-t*38)*.25+sin(2*PI*2670*t)*exp(-t*64)*.18
  "shovel"->noise*(.3+.7*abs(sin(2*PI*47*t)))*exp(-t*10)*.8+sin(2*PI*92*t)*exp(-t*30)*.18
  "brush"->noise*(.3+.5*abs(sin(2*PI*13*t)))*.55
  "plant"->noise*exp(-t*40)*.5+sin(2*PI*165*t)*exp(-t*28)*.3
  "water"->{val phase=t%.16;noise*.13+sin(2*PI*(1650*phase-2100*phase*phase))*exp(-phase*35)*.52}
  else->null
 }
 fun envelope(t:Double)= (1-exp(-t*650))*exp(-t*7)
}

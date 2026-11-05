package com.lazygarden.game

/** Stable fallback delivery when recorded/AI speech cannot be loaded. */
object VoiceProfiles {
    data class Profile(val pitch:Float,val rate:Float,val localSlot:Int)
    private val profiles=mapOf(
        "lazi" to Profile(.94f,.89f,0),"meli" to Profile(1.05f,.92f,1),
        "bejdi" to Profile(1.14f,.98f,2),"meti" to Profile(1.10f,.95f,4),
        "cufi" to Profile(1.17f,.99f,0),"rona" to Profile(1.19f,.96f,3),
        "rejani" to Profile(1.12f,.97f,6),"medina" to Profile(1.16f,.94f,5),
        "hafsa" to Profile(1.21f,.93f,1),"unejsi" to Profile(1.22f,.96f,2),
        "greku" to Profile(.90f,.91f,2),"rexha" to Profile(.87f,.88f,4),
        "nermiri" to Profile(.84f,.87f,6),"beka" to Profile(1.01f,.94f,0),
        "gezimi" to Profile(.98f,.90f,2),"besi" to Profile(.96f,.93f,4),
        "mevludini" to Profile(.91f,.86f,6),"luli" to Profile(.88f,.85f,0)
    )
    fun forActor(id:String)=profiles[SceneCast.canonical(id)]?:profiles.getValue("lazi")
    fun localIndex(id:String,count:Int):Int{
        if(count<=1)return 0
        val slot=forActor(id).localSlot
        // Keep the existing male/female slots when only two offline voices are installed.
        return if(count==2)slot%2 else slot%count
    }
}

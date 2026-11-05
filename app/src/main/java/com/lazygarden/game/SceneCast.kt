package com.lazygarden.game

/** Canonical identities shared by story blocking, speech and restoration crews. */
object SceneCast {
    data class Actor(val id:String,val sprite:String,val child:Boolean=false,val coat:Int=0)
    val actors=listOf(
        Actor("lazi","lazi"),Actor("meli","meli"),
        Actor("bejdi","bejdi",true),Actor("meti","bejdi",true,0xff8bb3a4.toInt()),
        Actor("cufi","cufi",true),Actor("rona","rona",true),
        Actor("rejani","bejdi",true,0xff749fb8.toInt()),
        Actor("medina","rona",true,0xffb88cc6.toInt()),
        Actor("hafsa","rona",true,0xffdba77c.toInt()),
        Actor("unejsi","cufi",true,0xff9cbe7d.toInt()),
        Actor("greku","lazi",coat=0xff7baba0.toInt()),
        Actor("rexha","rexha"),Actor("nermiri","rexha",coat=0xffa6b4c0.toInt()),
        Actor("beka","lazi",coat=0xffd6bd87.toInt()),
        Actor("gezimi","rexha",coat=0xff8cb68b.toInt()),
        Actor("besi","lazi",coat=0xff8aacc5.toInt()),
        Actor("mevludini","rexha",coat=0xffcaad7b.toInt()),
        Actor("luli","lazi",coat=0xffb4937b.toInt())
    )
    fun canonical(id:String)=when(id.lowercase(java.util.Locale.ROOT)){
        "nermir"->"nermiri";"mevludin"->"mevludini";else->id.lowercase(java.util.Locale.ROOT)
    }
    fun actor(id:String)=actors.first{it.id==canonical(id)}
    fun name(id:String)=when(canonical(id)){
        "gezimi"->"Gëzimi";else->canonical(id).replaceFirstChar{it.uppercase()}
    }
}

/** Scripted blocking is independent of translated dialogue and Android's frame clock. */
object StoryBlocking {
    const val WALK_MILLIS=2200L
    const val INSPECT_MILLIS=700L
    const val DIALOGUE_AT=WALK_MILLIS+INSPECT_MILLIS
    enum class Beat { TALK,CALL,INSPECT }
    fun beat(chapter:Int,line:Int)=if(line==2)Beat.INSPECT else Beat.TALK
    fun cast(speaker:String,partner:String,worker:String):List<String> =
        (listOf(speaker,partner)+if(beatSpeakerIsWorker(speaker,worker))listOf("lazi","meli") else emptyList())
            .map(SceneCast::canonical).distinct().take(4)
    private fun beatSpeakerIsWorker(speaker:String,worker:String)=SceneCast.canonical(speaker)==SceneCast.canonical(worker)
    fun progress(ms:Long)= (ms/WALK_MILLIS.toFloat()).coerceIn(0f,1f)
    fun state(ms:Long,isSpeaker:Boolean,talking:Boolean)=when{
        ms<WALK_MILLIS->CharacterAnimation.State.WALK
        ms<DIALOGUE_AT->CharacterAnimation.State.THINK
        isSpeaker&&talking->CharacterAnimation.State.TALK
        else->CharacterAnimation.State.IDLE
    }
}

package com.lazygarden.game

import android.content.Context

/** Scene 3 delegates to the textured restoration renderer, preserving the existing public API. */
class PathGardenView(context:Context,initialElapsed:Long=0L,checkpoint:(Long)->Unit={},onComplete:()->Unit={}):RestorationGardenView(context,2,initialElapsed,checkpoint,onComplete)

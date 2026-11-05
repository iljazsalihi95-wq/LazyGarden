package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.LinearInterpolator

/** Story characters inhabit the completed worksite; no sliding portrait overlays. */
class StorySceneView(
    context:Context,private val save:GardenSave,private val chapter:Int,private val line:Int,
    private val speaker:String,partner:String,private val speaking:()->Boolean,
    private val ready:()->Unit
):View(context),SceneLifecycle {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val site=RestorationWorksites.forTask(chapter)
    private val cast=StoryBlocking.cast(speaker,partner,site.worker)
    private val backdrop=GardenBackdrop(context,save.tasks)
    private val background=GameSkin.bitmap(context,R.drawable.garden_before)
    private val players=mutableMapOf<String,CharacterAnimation.Player>()
    private var clock:ValueAnimator?=null
    private var paused=false
    private var ms=0L
    private var notified=false
    internal val dialogueReady get()=ms>=StoryBlocking.DIALOGUE_AT
    internal val sceneElapsedMillis get()=ms
    internal fun actorState(id:String)=players[SceneCast.canonical(id)]?.state
    internal val actorIds get()=cast
    internal var onActorRendered:((String,CharacterAnimation.State)->Unit)?=null
    init{contentDescription="Personazhi ${SceneCast.name(speaker)}";importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_YES}
    override fun onAttachedToWindow(){super.onAttachedToWindow();startClock()}
    override fun onDetachedFromWindow(){stopClock();super.onDetachedFromWindow()}
    override fun onWindowVisibilityChanged(v:Int){super.onWindowVisibilityChanged(v);if(v==VISIBLE)startClock()else stopClock()}
    override fun pauseScene(){paused=true;stopClock()}
    override fun resumeScene(){paused=false;startClock()}
    private fun stopClock(){clock?.removeAllUpdateListeners();clock?.cancel();clock=null}
    private fun startClock(){
        if(paused||clock!=null||!isAttachedToWindow||windowVisibility!=VISIBLE)return
        var previous=android.os.SystemClock.uptimeMillis()
        clock=ValueAnimator.ofFloat(0f,1f).apply{
            duration=12000;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator()
            addUpdateListener{
                val now=android.os.SystemClock.uptimeMillis();ms+=(now-previous).coerceIn(0L,250L);previous=now
                if(dialogueReady&&!notified){notified=true;ready()}
                invalidate()
            };start()
        }
    }
    override fun onDraw(c:Canvas){
        super.onDraw(c);if(width==0||height==0)return
        val w=width.toFloat();val h=height.toFloat()
        val scale=maxOf(w/background.width,h/background.height)*1.4f
        val dw=background.width*scale;val dh=background.height*scale
        val dst=RectF((w-dw)/2,(h-dh)/2,(w+dw)/2,(h+dh)/2)
        val focal=when(chapter){0->PointF(.562f,.515f);1->PointF(.50f,.68f);else->PointF(site.workX,site.workY)}
        dst.offset(w*.50f-(dst.left+dw*focal.x),h*.57f-(dst.top+dh*focal.y))
        dst.offset(dst.left.coerceIn(w-dw,0f)-dst.left,dst.top.coerceIn(h-dh,0f)-dst.top)
        backdrop.draw(c,dst);WorldRestorationArt.draw(context,c,dst,save.tasks,ms/12000f)
        if(save.tasks>0)WorldRestorationArt.water(c,p,dst,ms.toFloat())
        if(chapter>=7)EstateRoomArt.draw(context,c,p,EstateRoomArt.bounds(dst),chapter,1f,1f,ms)
        val positions=listOf(.32f to .78f,.70f to .79f,.12f to .88f,.88f to .88f)
        for((i,id)in cast.withIndex()){
            val target=CharacterAnimation.Position(w*positions[i].first,h*positions[i].second)
            val from=CharacterAnimation.Position(if(i%2==0)-w*.20f else w*1.20f,target.y+h*.09f)
            val route=CharacterAnimation.sampleRoute(from,target,StoryBlocking.progress(ms),w*.50f)
            val state=StoryBlocking.state(ms,id==speaker,speaking())
            val player=players.getOrPut(id){CharacterAnimation.Player()};player.update(state,ms)
            val actor=SceneCast.actor(id);val aw=minOf(w*(if(actor.child).15f else .23f),h*.28f)
            val ah=aw*CharacterSprites.aspect(context,actor.sprite)
            val r=RectF(route.position.x-aw/2,route.position.y-ah,route.position.x+aw/2,route.position.y)
            p.shader=null;p.alpha=255;p.color=0x49302010
            c.drawOval(RectF(r.centerX()-aw*.29f,r.bottom-aw*.03f,r.centerX()+aw*.29f,r.bottom+aw*.025f),p)
            val right=if(state==CharacterAnimation.State.WALK)route.facingRight else if(id==speaker)r.centerX()<w*.5f else r.centerX()<w*.32f
            SceneActorArt.draw(context,c,p,id,player,ms,r,right)
            onActorRendered?.invoke(id,state)
            if(id==speaker&&dialogueReady){
                val label=SceneCast.name(id);p.typeface=Typeface.DEFAULT_BOLD;p.textSize=12*resources.displayMetrics.density;p.textAlign=Paint.Align.CENTER
                val tw=p.measureText(label)+20*resources.displayMetrics.density
                val box=RectF(r.centerX()-tw/2,r.top-30*resources.displayMetrics.density,r.centerX()+tw/2,r.top-7*resources.displayMetrics.density)
                p.color=0xf9fff2ce.toInt();c.drawRoundRect(box,8f,8f,p)
                c.drawPath(Path().apply{moveTo(box.centerX()-5f,box.bottom);lineTo(box.centerX()+5f,box.bottom);lineTo(box.centerX(),box.bottom+7f);close()},p)
                p.color=0xff356547.toInt();c.drawText(label,box.centerX(),box.centerY()+p.textSize*.34f,p)
                if(StoryBlocking.beat(chapter,line)==StoryBlocking.Beat.CALL&&id=="lazi")drawPhone(c,r,right)
            }
        }
    }
    private fun drawPhone(c:Canvas,r:RectF,right:Boolean){
        val x=r.centerX()+r.width()*(if(right).21f else -.21f);val y=r.top+r.height()*.36f
        p.color=0xff183b4a.toInt();val phone=RectF(x-r.width()*.045f,y-r.width()*.10f,x+r.width()*.045f,y+r.width()*.05f)
        c.drawRoundRect(phone,3f,3f,p);p.color=0xffb4eadc.toInt();phone.inset(2f,3f);c.drawRect(phone,p)
    }
}

package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

/** Premium illustrated board: animated pieces, water shimmer, combo particles and staged power effects. */
class Match3BoardView(c:Context,val engine:Match3Engine,private val changed:()->Unit,private val done:(Boolean)->Unit):View(c),SceneLifecycle{
 init{contentDescription=GameText.get(GardenSave(c).language,"match_board")}
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val d=resources.displayMetrics.density
 private val materialArt=MaterialArt(c)
 private val pieces=arrayOf(Material.TOOL,Material.PIPE,Material.VALVE,Material.PUMP_PART,Material.BOARD,Material.SCREW)
 private fun piece(c:Canvas,index:Int,r:RectF){
  p.shader=LinearGradient(r.left,r.top,r.right,r.bottom,intArrayOf(0xfff5dfad.toInt(),0xffba8644.toInt()),null,Shader.TileMode.CLAMP)
  c.drawRoundRect(r,r.width()*.18f,r.height()*.18f,p);p.shader=null
  val inset=r.width()*.09f;val inner=RectF(r);inner.inset(inset,inset);materialArt.draw(c,p,pieces[index.coerceIn(0,5)],inner)
 }
 private val background=GameSkin.bitmap(c,R.drawable.garden_estate)
 private val socket=GameSkin.bitmap(c,R.drawable.hd_socket);private val frame=GameSkin.bitmap(c,R.drawable.hd_board_frame);private val hud=GameSkin.bitmap(c,R.drawable.hd_hud)
 private val iceArt=GameSkin.bitmap(c,R.drawable.hd_ice);private val star=GameSkin.bitmap(c,R.drawable.reward_star)
 private val powerArt=intArrayOf(R.drawable.boost_row,R.drawable.boost_column,R.drawable.boost_bomb,R.drawable.boost_color).map{GameSkin.bitmap(c,it)}
 private val obstacleArt=GameSkin.bitmap(c,R.drawable.match_obstacles)
 private val sfx=GardenSfx(c)
 private val save=GardenSave(c)
 private fun t(key:String)=GameText.get(save.language,key)
 private val hammerArt=GameSkin.bitmap(c,R.drawable.boost_hammer)
 private val darkSocket=ColorMatrixColorFilter(ColorMatrix(floatArrayOf(.86f,0f,0f,0f,0f,0f,.89f,0f,0f,0f,0f,0f,.93f,0f,0f,0f,0f,0f,1f,0f)))
 private var selected=-1;private var down=-1;private var downX=0f;private var downY=0f;private var busy=false
 private var wave:Match3Engine.Wave?=null;private var effect=1f;private var life=0f
 private var visual=Array(8){engine.board[it].clone()};private var displayedIce=Array(8){engine.ice[it].clone()};private var displayedScore=engine.score.toFloat()
 private var cell=0f;private var left=0f;private var top=0f;private var swapA=-1;private var swapB=-1;private var swapProgress=0f
 private var animator:ValueAnimator?=null;private var ambient:ValueAnimator?=null;private var boostEffect:Match3Engine.Booster?=null;private var feedback=when(engine.spec.terrainKind){1->t("hint_crates");2,3->t("hint_stone");4->t("hint_soil");5->t("hint_water");else->t("hint_match")}
 var targetBooster:Match3Engine.Booster?=null;var onBoosterTarget:((Match3Engine.Booster,Int)->List<Match3Engine.Wave>)?=null;val isBusy get()=busy
 private var boostOrigin=-1
 private var scenePaused=false
 internal val animationFraction get()=effect
 internal var onEffectCue:((String)->Unit)?=null
 override fun pauseScene(){scenePaused=true;pauseMotion()}
 override fun resumeScene(){scenePaused=false;if(windowVisibility==VISIBLE)resumeMotion()}
 private fun resumeMotion(){sfx.resume();ambient?.resume();animator?.resume()}
 private fun pauseMotion(){ambient?.pause();animator?.pause();sfx.pause()}
 override fun onAttachedToWindow(){super.onAttachedToWindow();ambient=ValueAnimator.ofFloat(0f,1f).apply{duration=2600;repeatCount=ValueAnimator.INFINITE;addUpdateListener{life=it.animatedValue as Float;invalidate()};start();if(scenePaused||windowVisibility!=VISIBLE)pause()}}
 override fun onWindowVisibilityChanged(visibility:Int){super.onWindowVisibilityChanged(visibility)
  if(visibility==VISIBLE&&!scenePaused)resumeMotion()else pauseMotion()
 }
 override fun onDetachedFromWindow(){animator?.removeAllListeners();animator?.cancel();ambient?.removeAllUpdateListeners();ambient?.cancel();ambient=null;sfx.release();super.onDetachedFromWindow()}
 private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int=0xff593817.toInt()){p.shader=null;p.colorFilter=null;p.alpha=255;p.color=color;p.textSize=size;p.typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD);p.textAlign=Paint.Align.CENTER;p.setShadowLayer(d,0f,d,0x44552d11);c.drawText(s,x,y,p);p.clearShadowLayer()}
 private fun image(c:Canvas,b:Bitmap,r:RectF,alpha:Int=255){p.shader=null;p.colorFilter=null;p.alpha=alpha;c.drawBitmap(b,null,r,p);p.alpha=255}
 override fun onDraw(c:Canvas){super.onDraw(c);val w=width.toFloat();val h=height.toFloat();val scale=max(w/background.width,h/background.height);val bw=background.width*scale;val bh=background.height*scale
  image(c,background,RectF((w-bw)/2,(h-bh)/2,(w+bw)/2,(h+bh)/2));c.drawColor(0x8a102a25.toInt());val header=152*d;cell=min((w-30*d)/8,(h-header-28*d)/8).coerceAtLeast(1f);left=(w-cell*8)/2;top=header+max(0f,(h-header-28*d-cell*8)*.35f)
  GameSkin.nine(c,hud,RectF(7*d,2*d,w-7*d,143*d),20*d,p,.20f);text(c,"${t("level")} ${engine.level}",w*.5f,24*d,21*d);text(c,t("moves"),w*.83f,48*d,12*d);text(c,"${engine.moves}",w*.83f,82*d,35*d);text(c,"${displayedScore.toInt()} / ${engine.spec.target}",w*.33f,50*d,18*d)
  val fraction=(displayedScore/engine.spec.target).coerceIn(0f,1f);val bar=RectF(w*.09f,57*d,w*.61f,65*d);p.color=0xff80633a.toInt();c.drawRoundRect(bar,4*d,4*d,p);if(fraction>0){p.shader=LinearGradient(bar.left,bar.top,bar.left,bar.bottom,intArrayOf(0xffffed7a.toInt(),0xffd18a1c.toInt()),null,Shader.TileMode.CLAMP);c.drawRoundRect(RectF(bar.left,bar.top,bar.left+bar.width()*fraction,bar.bottom),4*d,4*d,p);p.shader=null};for(i in 0..2){val x=bar.left+bar.width()*(i+1)/3;image(c,star,RectF(x-7*d,52*d,x+7*d,68*d),if(fraction>=(i+1)/3f)255 else 105)}
  if(engine.spec.collectCount>0){piece(c,engine.spec.collectColor,RectF(w*.13f,73*d,w*.13f+28*d,101*d));text(c,"${engine.collected[engine.spec.collectColor]}/${engine.spec.collectCount}",w*.31f,94*d,17*d)};if(engine.spec.ice>0){image(c,iceArt,RectF(w*.41f,75*d,w*.41f+24*d,99*d));text(c,"${engine.clearedIce}/${engine.spec.ice}",w*.56f,94*d,17*d)}
  if(engine.spec.terrainCount>0||engine.rescueTarget>0){
   val idx=when(engine.spec.terrainKind){1->0;2,3->1;4->2;else->3}
   SpriteArt.draw(c,p,obstacleArt,3,2,idx,RectF(w*.13f,112*d,w*.13f+24*d,136*d));text(c,"${engine.terrainRemaining}",w*.31f,130*d,16*d)
   if(engine.rescueTarget>0){materialArt.draw(c,p,RestorationInventory.rewardForLevel(engine.level,save.tasks)?.first?:Material.TOOL,RectF(w*.42f,112*d,w*.42f+26*d,138*d));text(c,"${engine.rescued}/${engine.rescueTarget}",w*.59f,130*d,16*d)}
  }
  val bounds=RectF(left-12*d,top-12*d,left+8*cell+12*d,top+8*cell+12*d);p.color=0xff082d39.toInt();c.drawRoundRect(bounds,18*d,18*d,p)
  for(r in 0..7)for(col in 0..7){val x=left+col*cell;val y=top+r*cell;p.colorFilter=if((r+col)%2==0)null else darkSocket;c.drawBitmap(socket,null,RectF(x-.6f*d,y-.6f*d,x+cell+.6f*d,y+cell+.6f*d),p);p.colorFilter=null;if(selected==r*8+col){p.shader=RadialGradient(x+cell/2,y+cell/2,cell*.72f,intArrayOf(0xccfff09a.toInt(),0x00fff09a),null,Shader.TileMode.CLAMP);c.drawCircle(x+cell/2,y+cell/2,cell*.72f,p);p.shader=null}}
  val clip=c.save();c.clipRect(left,top,left+8*cell,top+8*cell);val active=wave;val falling=active!=null&&!active.shuffled&&effect>=.35f
  for(r in 0..7)for(col in 0..7){val index=r*8+col;val x=left+col*cell;var y=top+r*cell;var dx=0f;var dy=0f;if(index==swapA||index==swapB){val to=if(index==swapA)swapB else swapA;dx=(to%8-col)*cell*swapProgress;dy=(to/8-r)*cell*swapProgress};val hit=active?.cells?.contains(index)==true;val size=if(active?.shuffled==true)abs(1f-effect*2).coerceAtLeast(.01f) else if(hit&&!falling)(1f-effect/.35f).coerceIn(0f,1f) else 1f;if(falling){val t=((effect-.35f)/.65f).coerceIn(0f,1f);val ease=t*t*(3-2*t);val source=active!!.origins[r][col];y=top+(source+(r-source)*ease)*cell}
   val value=visual[r][col];val base=value%6;val bob=if(!busy)sin(life*PI.toFloat()*2+index*.71f)*cell*.018f else 0f;val pulse=1f+if(!busy)sin(life*PI.toFloat()*2+index)*.018f else 0f;val rect=RectF(x+cell*.055f+dx,y+cell*.055f+dy+bob,x+cell*.945f+dx,y+cell*.945f+dy+bob)
   if(size>0){val saved=c.save();val squash=if(falling){val t=((effect-.35f)/.65f).coerceIn(0f,1f);sin(t*PI.toFloat())*.12f}else if(hit)sin((effect/.35f).coerceIn(0f,1f)*PI.toFloat())*.14f else 0f;c.scale(size*pulse*(1+squash),size*pulse*(1-squash),x+cell/2+dx,y+cell/2+dy);if(value<6)piece(c,value,rect) else{val glow=cell*(.51f+.04f*sin(life*PI.toFloat()*4+index));p.shader=RadialGradient(x+cell/2,y+cell/2,glow,intArrayOf(0x99fff49a.toInt(),0x00fff49a),null,Shader.TileMode.CLAMP);c.drawCircle(x+cell/2,y+cell/2,glow,p);p.shader=null;image(c,powerArt[(value/6-1).coerceIn(0,3)],rect);piece(c,base,RectF(rect.right-cell*.29f,rect.bottom-cell*.29f,rect.right,rect.bottom))};if(false){p.color=0x99d9fbff.toInt();p.style=Paint.Style.STROKE;p.strokeWidth=cell*.025f;c.drawOval(RectF(rect.left+cell*.18f,rect.top+cell*(.18f+.05f*sin(life*PI.toFloat()*2+index)),rect.right-cell*.18f,rect.bottom-cell*.2f),p);p.style=Paint.Style.FILL};c.restoreToCount(saved)}
  };c.restoreToCount(clip)
  for(r in 0..7)for(col in 0..7)if(displayedIce[r][col]>0){val x=left+col*cell;val y=top+r*cell;image(c,iceArt,RectF(x,y,x+cell,y+cell));p.color=0x66ffffff;p.strokeWidth=1.5f*d;c.drawLine(x+cell*.2f,y+cell*.25f,x+cell*.72f,y+cell*.68f,p)};GameSkin.nine(c,frame,bounds,24*d,p,.18f,false)
  for(r in 0..7)for(col in 0..7){val terrain=(if(active!=null)if(effect<.35f)active.terrainBefore else active.terrainAfter else engine.terrain)[r][col];val cargo=(if(active!=null)if(effect<.35f)active.cargoBefore else active.cargoAfter else engine.cargo)[r][col];val x=left+col*cell;val y=top+r*cell
   if(terrain>0)SpriteArt.draw(c,p,obstacleArt,3,2,when(terrain){1->0;2,3->1;4->2;else->3},RectF(x+cell*.03f,y+cell*.03f,x+cell*.97f,y+cell*.97f),if(terrain==4)170 else 235)
   if(cargo>0){
    val material=RestorationInventory.rewardForLevel(engine.level,save.tasks)?.first?:Material.TOOL
    var cy=y
    if(active!=null&&effect>=.35f){
     val u=((effect-.35f)/.65f).coerceIn(0f,1f)
     val source=(0..r).lastOrNull{active.cargoBefore[it][col]>0}?:r
     cy=top+(source+(r-source)*u*u)*cell
    }
    materialArt.draw(c,p,material,RectF(x+cell*.12f,cy+cell*.12f,x+cell*.88f,cy+cell*.88f))
    if(terrain>0)SpriteArt.draw(c,p,obstacleArt,3,2,0,RectF(x,y,x+cell,y+cell),255)
   }
  }
  // Released parts leaving the bottom of the board remain visible until collected.
  if(active!=null&&!active.shuffled&&effect>=.35f){
   val u=((effect-.35f)/.65f).coerceIn(0f,1f)
   val material=RestorationInventory.rewardForLevel(engine.level,save.tasks)?.first?:Material.TOOL
   for(col in 0..7){
    val beforeCount=(0..7).count{active.cargoBefore[it][col]>0}
    val afterCount=(0..7).count{active.cargoAfter[it][col]>0}
    if(beforeCount>afterCount){
     val source=(0..7).last{active.cargoBefore[it][col]>0}
     val x=left+(col+.5f)*cell;val y=top+(source+.5f+(8-source)*u*u)*cell
     val size=cell*(.76f-.30f*u)
     materialArt.draw(c,p,material,RectF(x-size/2,y-size/2,x+size/2,y+size/2))
     p.color=0xffffdc7a.toInt();p.alpha=((1-u)*190).toInt()
     for(k in 0..4)c.drawCircle(x+sin(k*2f)*cell*.25f*u,y-k*cell*.15f,cell*.035f,p)
     p.alpha=255
    }
   }
  }
  if(active!=null && boostOrigin>=0 && boostEffect!=null && effect<.48f){
   val t=(effect/.48f).coerceIn(0f,1f);val x=left+(boostOrigin%8+.5f)*cell;val y=top+(boostOrigin/8+.5f)*cell
   p.color=0xffffedac.toInt();p.alpha=((1-t)*220).toInt();p.strokeWidth=cell*.08f
   if(boostEffect==Match3Engine.Booster.ROW || boostEffect==Match3Engine.Booster.CROSS){
    for(sign in listOf(-1,1)){val rx=x+sign*t*cell*8;image(c,powerArt[0],RectF(rx-cell*.35f,y-cell*.25f,rx+cell*.35f,y+cell*.25f));c.drawLine(x,y,rx,y,p)}
   }
   if(boostEffect==Match3Engine.Booster.COLUMN || boostEffect==Match3Engine.Booster.CROSS){
    for(sign in listOf(-1,1)){val ry=y+sign*t*cell*8;image(c,powerArt[1],RectF(x-cell*.25f,ry-cell*.35f,x+cell*.25f,ry+cell*.35f));c.drawLine(x,y,x,ry,p)}
   }
   if(boostEffect==Match3Engine.Booster.HAMMER){
    val saved=c.save();c.rotate(-65+105*t,x,y);image(c,hammerArt,RectF(x-cell*.6f,y-cell*1.2f,x+cell*.6f,y),255);c.restoreToCount(saved)
    p.style=Paint.Style.STROKE;p.color=0xffffd964.toInt();c.drawCircle(x,y,cell*t*1.5f,p);p.style=Paint.Style.FILL
   }
   if(boostEffect==Match3Engine.Booster.COLOR){for(index in active.cells){val tx=left+(index%8+.5f)*cell;val ty=top+(index/8+.5f)*cell;p.color=Color.HSVToColor(intensityAlpha(t),floatArrayOf(index*37f%360,0.65f,1f));c.drawLine(x,y,tx,ty,p)}}
   p.alpha=255
  }
  if(active!=null&&effect<.48f){val intensity=(1-effect/.48f).coerceIn(0f,1f);for(index in active.cells){val x=left+(index%8+.5f)*cell;val y=top+(index/8+.5f)*cell;val kind=active.before[index/8][index%8]/6;if(kind>0||boostEffect!=null && index==boostOrigin){p.color=if(kind==3||boostEffect==Match3Engine.Booster.BOMB)0xffffb22e.toInt() else 0xffa6f1ff.toInt();p.alpha=(intensity*210).toInt();p.strokeWidth=cell*.12f*intensity;if(kind==1||boostEffect==Match3Engine.Booster.ROW||boostEffect==Match3Engine.Booster.CROSS)c.drawLine(left,y,left+8*cell,y,p);if(kind==2||boostEffect==Match3Engine.Booster.COLUMN||boostEffect==Match3Engine.Booster.CROSS)c.drawLine(x,top,x,top+8*cell,p);if(kind==4){for(target in active.cells){val tx=left+(target%8+.5f)*cell;val ty=top+(target/8+.5f)*cell;p.color=Color.HSVToColor((intensity*210).toInt(),floatArrayOf(target*47f%360,.65f,1f));c.drawLine(x,y,tx,ty,p)}};if(kind==3||boostEffect==Match3Engine.Booster.BOMB){p.style=Paint.Style.STROKE;for(q in 1..3)c.drawCircle(x,y,cell*(.25f+effect*q),p);p.style=Paint.Style.FILL}}
    for(k in 0..5){val a=k*PI/3+index*.37;val travel=cell*(.12f+effect*2.4f);val sx=x+(cos(a)*travel).toFloat();val sy=y+(sin(a)*travel).toFloat();val sz=cell*(.05f+.08f*intensity);p.color=listOf(0xffffd84e.toInt(),0xff8ee9ff.toInt(),0xffff8ec5.toInt())[k%3];p.alpha=(255*intensity).toInt();c.drawCircle(sx,sy,sz,p)}
   }};p.alpha=255;val caption=targetBooster?.let{"${GameText.booster(save.language,it)}: ${t("choose_piece")}"}?:if(busy&&(active?.combo?:0)>1)"${t("excellent")} ×${active?.combo}" else feedback;if(caption.isNotEmpty())text(c,caption,w/2,top+8*cell+24*d,min(16*d,w/24),0xffffefb5.toInt())
 }
 private fun intensityAlpha(t:Float)=((1-t)*200).toInt().coerceIn(0,255)
 internal fun cellCenter(index:Int)=PointF(left+(index%8+.5f)*cell,top+(index/8+.5f)*cell)
 private fun index(x:Float,y:Float):Int{if(x<left||x>=left+8*cell||y<top||y>=top+8*cell)return -1;return ((y-top)/cell).toInt()*8+((x-left)/cell).toInt()}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(busy||scenePaused)return true;if(e.actionMasked==MotionEvent.ACTION_DOWN){down=index(e.x,e.y);downX=e.x;downY=e.y;return true};if(e.actionMasked==MotionEvent.ACTION_UP){performClick();var end=index(e.x,e.y);if(end<0&&down<0)return true;if(down>=0&&hypot(e.x-downX,e.y-downY)>cell*.25f){val dx=e.x-downX;val dy=e.y-downY;end=if(abs(dx)>abs(dy))down+if(dx>0)1 else -1 else down+if(dy>0)8 else -8;if(end !in 0..63||abs(down/8-end/8)+abs(down%8-end%8)!=1){selected=-1;invalidate();return true}};if(end<0)return true;targetBooster?.let{kind->val waves=onBoosterTarget?.invoke(kind,end).orEmpty();targetBooster=null;selected=-1;if(waves.isNotEmpty()){boostOrigin=end;boostEffect=kind;busy=true;changed();animate(waves,0)}else invalidate();return true};val start=if(down>=0&&down!=end)down else selected;if(start>=0&&abs(start/8-end/8)+abs(start%8-end%8)==1){val legal=engine.legal(start,end);busy=true;selected=-1;swapA=start;swapB=end;feedback="";val a=ValueAnimator.ofFloat(0f,1f).apply{duration=if(legal)150 else 130;repeatCount=if(legal)0 else 1;repeatMode=ValueAnimator.REVERSE;addUpdateListener{swapProgress=it.animatedValue as Float;invalidate()}};animator=a;a.addListener(object:android.animation.AnimatorListenerAdapter(){override fun onAnimationEnd(animation:android.animation.Animator){if(!isAttachedToWindow)return;swapA=-1;swapB=-1;if(legal){val waves=engine.swap(start,end);changed();animate(waves,0)}else{busy=false;feedback=t("invalid_swap");invalidate()}}});a.start()}else{selected=end;feedback="";invalidate()};return true};return true}
 override fun performClick():Boolean{super.performClick();return true};fun runBooster(kind:Match3Engine.Booster){if(!busy&&!scenePaused){targetBooster=if(targetBooster==kind)null else kind;invalidate()}}
 private fun animate(waves:List<Match3Engine.Wave>,i:Int){
  if(i>=waves.size){
   visual=Array(8){engine.board[it].clone()};displayedIce=Array(8){engine.ice[it].clone()};displayedScore=engine.score.toFloat()
   wave=null;boostEffect=null;busy=false;invalidate()
   if(engine.won||engine.failed){if(save.sfxEnabled)sfx.play(if(engine.won)"win" else "fail");done(engine.won)}
   return
  }
  val startScore=displayedScore;val endScore=waves[i].scoreAfter.toFloat();wave=waves[i]
  val powers=waves[i].cells.map{waves[i].before[it/8][it%8]/6}.toSet()
  val cue=when(boostEffect){
   Match3Engine.Booster.HAMMER->"hammer";Match3Engine.Booster.BOMB->"bomb"
   Match3Engine.Booster.ROW->"row";Match3Engine.Booster.COLUMN->"column";Match3Engine.Booster.CROSS->"rocket"
   null->when {
    4 in powers->"booster";3 in powers->"bomb"
    1 in powers&&2 in powers->"rocket";1 in powers->"row";2 in powers->"column"
    waves[i].combo>1->"star";else->"coin"
   }
   else->"booster"
  }
  onEffectCue?.invoke(cue)
  if(save.sfxEnabled)sfx.play(cue)
  visual=waves[i].before;displayedIce=waves[i].iceBefore;effect=0f
  animator=ValueAnimator.ofFloat(0f,1f).apply{
   duration=620
   addUpdateListener{
    effect=it.animatedValue as Float
    translationX=if(boostEffect!=null&&effect<.3f)sin(effect*75)*d*2*(1-effect/.3f) else 0f
    if(effect>=(if(waves[i].shuffled).5f else .35f)){visual=waves[i].after;displayedIce=waves[i].iceAfter}
    displayedScore=startScore+(endScore-startScore)*effect;invalidate()
   }
   addListener(object:android.animation.AnimatorListenerAdapter(){
    private var cancelled=false
    override fun onAnimationCancel(animation:android.animation.Animator){cancelled=true}
    override fun onAnimationEnd(animation:android.animation.Animator){
     if(!cancelled&&isAttachedToWindow)animate(waves,i+1)
    }
   })
   start()
   if(scenePaused||windowVisibility!=VISIBLE)pause()
  }
 }
}

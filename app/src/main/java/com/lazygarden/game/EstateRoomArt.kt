package com.lazygarden.game

import android.graphics.*
import kotlin.math.sin

/** Native illustrated cutaways for jobs 8–20; no out-of-range atlas samples. */
object EstateRoomArt {
 fun bounds(d:RectF)=WorldRestorationArt.rect(d,.5f,.74f,.86f,.49f)
 fun draw(c:Canvas,p:Paint,r:RectF,task:Int,repair:Float,finish:Float,time:Long){
  require(task in 7..19)
  val layer=c.save();c.translate(r.left,r.top);c.scale(r.width()/1000f,r.height()/650f)
  fun rect(x:Float,y:Float,w:Float,h:Float,color:Int,radius:Float=8f){p.style=Paint.Style.FILL;p.shader=null;p.color=color;c.drawRoundRect(RectF(x,y,x+w,y+h),radius,radius,p)}
  fun line(x:Float,y:Float,xx:Float,yy:Float,color:Int,width:Float=5f){p.style=Paint.Style.STROKE;p.strokeWidth=width;p.color=color;c.drawLine(x,y,xx,yy,p);p.style=Paint.Style.FILL}
  val done=finish.coerceIn(0f,1f);val fixed=maxOf(repair,done).coerceIn(0f,1f)
  fun mix(a:Int,b:Int,f:Float):Int=Color.rgb((Color.red(a)+(Color.red(b)-Color.red(a))*f).toInt(),(Color.green(a)+(Color.green(b)-Color.green(a))*f).toInt(),(Color.blue(a)+(Color.blue(b)-Color.blue(a))*f).toInt())
  val walls=intArrayOf(0xffefe1bc.toInt(),0xffdce9dc.toInt(),0xffebdbc0.toInt(),0xffdce5ee.toInt(),0xffefe0bf.toInt())
  val wall=mix(0xff827969.toInt(),walls[(task-7)%walls.size],fixed)
  rect(8f,18f,984f,605f,0x66332619,24f);rect(0f,0f,1000f,600f,0xff694e37.toInt(),20f)
  p.shader=LinearGradient(0f,0f,0f,480f,intArrayOf(wall,mix(wall,Color.WHITE,.12f)),null,Shader.TileMode.CLAMP);p.style=Paint.Style.FILL;c.drawRect(15f,15f,985f,470f,p);p.shader=null
  rect(15f,465f,970f,120f,mix(0xff665443.toInt(),0xffbd9460.toInt(),done))
  for(i in 0..8)line(15f+i*122f,470f,15f+i*122f,580f,0x4463442a,2f)
  for(i in 0..2)line(15f,475f+i*42,985f,475f+i*42,0x4463442a,2f)
  rect(15f,450f,970f,18f,0xff714d32.toInt())
  fun window(x:Float=630f,y:Float=70f,w:Float=245f,h:Float=250f){
   rect(x-13,y-13,w+26,h+26,mix(0xff514c40.toInt(),0xfff8f0d6.toInt(),fixed))
   p.shader=LinearGradient(x,y,x,y+h,intArrayOf(0xff6ba9b0.toInt(),0xffe0f3cf.toInt()),null,Shader.TileMode.CLAMP);c.drawRect(x,y,x+w,y+h,p);p.shader=null
   for(i in 0..2){p.color=0xff5f975e.toInt();c.drawOval(RectF(x+i*w/3-15,y+h*.68f,x+(i+1)*w/3+20,y+h+25),p)}
   line(x+w/2,y,x+w/2,y+h,0xfff4ecd4.toInt(),10f);line(x,y+h/2,x+w,y+h/2,0xfff4ecd4.toInt(),10f)
   if(fixed<1){p.alpha=((1-fixed)*255).toInt();line(x+10,y+10,x+w*.55f,y+h*.68f,0xff3a4443.toInt(),7f);line(x+w*.55f,y+h*.68f,x+w-10,y+h-12,0xff3a4443.toInt(),7f);p.alpha=255}
  }
  fun shelf(x:Float,y:Float,w:Float,h:Float){rect(x,y,w,h,mix(0xff5e4a35.toInt(),0xffa27245.toInt(),fixed));for(i in 1..3){rect(x+12,y+i*h/4,w-24,12f,0xffe1bd7b.toInt());for(j in 0..4)rect(x+24+j*(w-42)/5,y+i*h/4-35,((w-60)/5).coerceAtLeast(8f),30f,intArrayOf(0xff51827a.toInt(),0xffba7154.toInt(),0xffd5b55e.toInt())[(j+i)%3])}}
  fun cabinet(x:Float,y:Float,w:Float,h:Float){rect(x,y,w,h,mix(0xff736249.toInt(),0xfffaf0d3.toInt(),fixed));for(i in 0..2){rect(x+8+i*w/3,y+12,w/3-16,h-24,mix(0xff584e42.toInt(),0xff8ca67e.toInt(),done));rect(x+20+i*w/3,y+30,18f,7f,0xffe5bb62.toInt())};rect(x-8,y-15,w+16,20f,0xff594b3e.toInt())}
  fun lamp(x:Float,y:Float){line(x,y,x,y+110,0xff413d33.toInt(),8f);p.color=0xffe7c873.toInt();c.drawOval(RectF(x-40,y+95,x+40,y+130),p);if(done>0){p.shader=RadialGradient(x,y+135,110f,intArrayOf(Color.argb((done*110).toInt(),255,239,157),0x00ffef9d),null,Shader.TileMode.CLAMP);c.drawCircle(x,y+135,110f,p);p.shader=null}}
  fun plant(x:Float,y:Float){rect(x-27,y,54f,50f,0xffb66d45.toInt());for(i in 0..4){p.color=if(i%2==0)0xff4a8655.toInt() else 0xff6fa85d.toInt();c.drawOval(RectF(x-35+i*9,y-72+i*6,x+5+i*8,y+12),p)}}
  window()
  when(task){
   7->{shelf(90f,90f,260f,350f);cabinet(420f,375f,170f,90f);plant(890f,400f)}
   8->{window(120f,60f,320f,330f);rect(85f,410f,820f,25f,0xffb48c54.toInt());plant(190f,370f);plant(820f,370f)}
   9->{val roof=Path().apply{moveTo(25f,240f);lineTo(500f,25f);lineTo(975f,240f);close()};p.color=mix(0xff62554b.toInt(),0xffae5e43.toInt(),fixed);c.drawPath(roof,p);for(i in 1..5)line(500f-i*80,30f+i*36,500f+i*80,30f+i*36,0xff663e32.toInt(),10f);shelf(100f,300f,220f,150f)}
   10->{shelf(90f,100f,220f,350f);rect(360f,330f,205f,100f,0xffe2b65f.toInt());rect(350f,315f,225f,25f,0xff6f9e8e.toInt());rect(380f,350f,30f,115f,0xff896239.toInt());rect(510f,350f,30f,115f,0xff896239.toInt());for(i in 0..2)rect(670f+i*70,390f,60f,65f,intArrayOf(0xffb46a53.toInt(),0xff6c94b3.toInt(),0xffd5b75b.toInt())[i])}
   11,12->{cabinet(75f,315f,850f,145f);cabinet(90f,85f,350f,160f);rect(540f,300f,180f,20f,0xffb8c6be.toInt());line(630f,300f,630f,245f,0xffa9b7b4.toInt(),12f);line(630f,245f,675f,245f,0xffa9b7b4.toInt(),12f);if(task==12){lamp(500f,30f);if(done>0){p.color=Color.argb((done*180).toInt(),120,215,235);c.drawOval(RectF(542f,306f,716f,327f),p)}}}
   13->{rect(115f,290f,440f,155f,0xff608c7d.toInt(),30f);rect(90f,340f,55f,120f,0xff4e7366.toInt(),20f);rect(525f,340f,55f,120f,0xff4e7366.toInt(),20f);for(i in 0..2)rect(165f+i*115,320f,105f,85f,0xffd6bd7f.toInt(),15f);rect(610f,410f,180f,20f,0xffae8256.toInt());plant(900f,390f)}
   14->{for(i in 0..8)rect(60f+i*110,250f,15f,210f,0xffe5dec2.toInt());rect(45f,240f,910f,20f,0xff8a7655.toInt());for(i in 0..4)plant(115f+i*190,390f)}
   15->{shelf(85f,60f,320f,220f);rect(70f,330f,850f,35f,0xffae7e45.toInt());rect(100f,365f,35f,100f,0xff725434.toInt());rect(850f,365f,35f,100f,0xff725434.toInt());for(i in 0..4){line(490f+i*75,100f,490f+i*75,240f,0xff75614b.toInt(),15f);rect(470f+i*75,95f,50f,25f,0xffa6b1a4.toInt())}}
   16->{for(i in 0..2){val x=140f+i*280;p.style=Paint.Style.STROKE;p.strokeWidth=12f;p.color=0xff3c5758.toInt();c.drawCircle(x,390f,62f,p);c.drawCircle(x+150,390f,62f,p);p.style=Paint.Style.FILL;line(x,390f,x+80,285f,0xffc38a4e.toInt(),10f);line(x+80,285f,x+150,390f,0xffc38a4e.toInt(),10f);line(x,390f,x+150,390f,0xffc38a4e.toInt(),10f)}}
   17->{for(i in 0..4)lamp(100f+i*200,40f);for(i in 0..4)plant(100f+i*200,410f)}
   18->{line(140f,460f,300f,90f,0xffaa7854.toInt(),24f);line(300f,90f,460f,460f,0xffaa7854.toInt(),24f);line(155f,100f,810f,100f,0xffb48b5d.toInt(),24f);for(i in 0..1){line(550f+i*170,110f,550f+i*170,375f,0xffe7d6a3.toInt(),7f)};rect(520f,375f,230f,25f,0xff658b88.toInt())}
   19->{for(i in 0..6){plant(90f+i*140,400f);lamp(90f+i*140,15f)};rect(230f,320f,540f,30f,0xffd6b579.toInt());rect(250f,350f,25f,115f,0xff97754c.toInt());rect(720f,350f,25f,115f,0xff97754c.toInt())}
  }
  // Wear belongs to the object; each restoration visibly removes it.
  if(fixed<1){p.color=Color.argb(((1-fixed)*210).toInt(),49,42,35);p.strokeWidth=7f;p.style=Paint.Style.STROKE;for(i in 0..4){val x=80f+i*180;c.drawPath(Path().apply{moveTo(x,90f);lineTo(x+22,145f);lineTo(x+4,175f);lineTo(x+36,228f)},p)};p.style=Paint.Style.FILL}
  if(done<1)for(i in 0..7){p.color=Color.argb(((1-done)*220).toInt(),87,73,49);c.drawOval(RectF(40f+i*125,490f+(i%2)*20,95f+i*125,510f+(i%2)*20),p)}
  if(done>0){p.color=Color.argb((done*110).toInt(),255,238,169);val spark=sin(time/900f);c.drawCircle(900f,60f,5f+spark*2,p)}
  p.shader=null;p.alpha=255;p.style=Paint.Style.FILL;c.restoreToCount(layer)
 }
}

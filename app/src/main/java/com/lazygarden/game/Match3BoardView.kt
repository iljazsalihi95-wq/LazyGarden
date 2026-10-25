package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

class Match3BoardView(c:Context,private val level:Int,private val done:(Boolean)->Unit):View(c){
 private val p=Paint(3); private val n=8; private val board=Array(n){IntArray(n)}; private var moves=25; private var score=0; private var sr=-1;private var sc=-1
 private val colors=intArrayOf(0xffff5b6e.toInt(),0xffffd34e.toInt(),0xff62d16f.toInt(),0xff56b9ff.toInt(),0xffb76cff.toInt(),0xffff8b3d.toInt())
 init{fill()}
 private fun fill(){for(r in 0 until n)for(col in 0 until n){var v:Int;do{v=Random.nextInt(6)}while((col>1&&board[r][col-1]==v&&board[r][col-2]==v)||(r>1&&board[r-1][col]==v&&board[r-2][col]==v));board[r][col]=v}}
 override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(Color.rgb(24,57,69));p.color=Color.WHITE;p.textSize=46f;p.isFakeBoldText=true;c.drawText("LEVEL $level",34f,70f,p);p.textSize=31f;p.isFakeBoldText=false;c.drawText("Moves $moves     Score $score / 900",34f,116f,p);val top=155f;val cell=width/n.toFloat();for(r in 0 until n)for(col in 0 until n){val cx=col*cell+cell/2;val cy=top+r*cell+cell/2;p.color=0x22000000;c.drawRoundRect(col*cell+5,top+r*cell+5,(col+1)*cell-5,top+(r+1)*cell-5,16f,16f,p);p.color=colors[board[r][col]];c.drawCircle(cx,cy,cell*.34f,p);p.color=0x66ffffff;c.drawCircle(cx-cell*.11f,cy-cell*.12f,cell*.1f,p)}}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action!=MotionEvent.ACTION_UP)return true;val top=155f;val cell=width/n.toFloat();val col=(e.x/cell).toInt();val r=((e.y-top)/cell).toInt();if(r !in 0 until n||col !in 0 until n)return true;if(sr<0){sr=r;sc=col;invalidate();return true};if(abs(sr-r)+abs(sc-col)==1){val t=board[sr][sc];board[sr][sc]=board[r][col];board[r][col]=t;if(resolve()){moves--;if(score>=900){done(true);return true};if(moves<=0){done(false);return true}}else{board[r][col]=board[sr][sc];board[sr][sc]=t}};sr=-1;sc=-1;invalidate();return true}
 private fun resolve():Boolean{var any=false;while(true){val mark=Array(n){BooleanArray(n)};var found=false;for(r in 0 until n){var s=0;while(s<n){var e=s+1;while(e<n&&board[r][e]==board[r][s])e++;if(e-s>=3){for(x in s until e)mark[r][x]=true;found=true};s=e}};for(col in 0 until n){var s=0;while(s<n){var e=s+1;while(e<n&&board[e][col]==board[s][col])e++;if(e-s>=3){for(x in s until e)mark[x][col]=true;found=true};s=e}};if(!found)break;any=true;for(col in 0 until n){var wr=n-1;for(r in n-1 downTo 0)if(!mark[r][col])board[wr--][col]=board[r][col] else score+=10;while(wr>=0)board[wr--][col]=Random.nextInt(6)}};return any}
}

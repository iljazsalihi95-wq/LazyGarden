package com.lazygarden.game

import kotlin.random.Random

/** Pure Kotlin rules; presentation never mutates the board directly. */
class Match3Engine(val level: Int, private val random: Random = Random.Default) {
    val spec = Campaign.level(level)
    val board = Array(8) { IntArray(8) }
    val ice = Array(8) { IntArray(8) }
    val collected = IntArray(6)
    var moves = spec.moves; private set
    var score = 0; private set
    var clearedIce = 0; private set
    var combo = 0; private set
    var revision = 0; private set
    val won get() = score >= spec.target && clearedIce >= spec.ice && collected[spec.collectColor] >= spec.collectCount
    val earnedStars get() = if (!won) 0 else if (moves >= spec.moves / 3) 3 else if (moves >= spec.moves / 6) 2 else 1
    val failed get() = moves == 0 && !won
    data class Wave(val cells: List<Int>, val before: Array<IntArray>, val after: Array<IntArray>, val combo: Int,
        val origins:Array<IntArray> = Array(8){r->IntArray(8){r}},
        val iceBefore:Array<IntArray> = Array(8){IntArray(8)},val iceAfter:Array<IntArray> = Array(8){IntArray(8)},
        val scoreAfter:Int=0,val shuffled:Boolean=false)
    init {
        refill()
        val positions = (0..63).shuffled(random)
        positions.take(spec.ice).forEach { ice[it / 8][it % 8] = 1 }
    }
    private fun color(v: Int) = v % 6
    private fun matches(): Set<Int> {
        val result = mutableSetOf<Int>()
        for (axis in 0..1) for (line in 0..7) {
            var start = 0
            while (start < 8) {
                var end = start + 1
                fun value(i: Int) = if (axis == 0) board[line][i] else board[i][line]
                while (end < 8 && color(value(end)) == color(value(start))) end++
                if (end - start >= 3) for (i in start until end) result.add(if (axis == 0) line * 8 + i else i * 8 + line)
                start = end
            }
        }
        return result
    }
    private fun exchange(a: Int, b: Int) {
        val temp = board[a / 8][a % 8]
        board[a / 8][a % 8] = board[b / 8][b % 8]
        board[b / 8][b % 8] = temp
    }
    fun legal(a: Int, b: Int): Boolean {
        if (won || failed) return false
        if (a !in 0..63 || b !in 0..63 || kotlin.math.abs(a/8-b/8)+kotlin.math.abs(a%8-b%8)!=1) return false
        exchange(a,b)
        val valid = matches().isNotEmpty() || board[a/8][a%8]>=6 || board[b/8][b%8]>=6
        exchange(a,b)
        return valid
    }
    fun hasMove(): Boolean = (0..63).any { a -> (a%8<7 && legal(a,a+1)) || (a/8<7 && legal(a,a+8)) }
    private fun refill() {
        do {
            for (r in 0..7) for (c in 0..7) {
                var v: Int
                do { v = random.nextInt(6) } while ((c>1 && board[r][c-1]==v && board[r][c-2]==v) || (r>1 && board[r-1][c]==v && board[r-2][c]==v))
                board[r][c]=v
            }
        } while (!hasMove())
    }
    fun swap(a: Int, b: Int): List<Wave> {
        if (won || failed || !legal(a,b)) return emptyList()
        exchange(a,b); moves--; revision++; combo=0
        val initial = matches().toMutableSet()
        for (i in listOf(a,b)) if (board[i/8][i%8]>=6) initial.add(i)
        return resolve(initial, b)
    }
    enum class Booster(val cost: Int, val label: String) {
        HAMMER(40,"Çekiçi"), ROW(55,"Rreshti"), COLUMN(55,"Kolona"),
        CROSS(75,"Kryqi"), BOMB(65,"Bomba"), COLOR(100,"Ngjyra"), SHUFFLE(30,"Përzieje")
    }
    fun boost(kind: Booster, at: Int): List<Wave> {
        if(won || failed || at !in 0..63) return emptyList()
        revision++; combo=0
        if(kind==Booster.SHUFFLE){
            val before=Array(8){board[it].clone()}
            refill()
            return listOf(Wave(emptyList(),before,Array(8){board[it].clone()},0,iceBefore=Array(8){ice[it].clone()},iceAfter=Array(8){ice[it].clone()},scoreAfter=score,shuffled=true))
        }
        val r=at/8;val c=at%8
        val hits=when(kind){
            Booster.HAMMER -> setOf(at)
            Booster.ROW -> (0..7).map{r*8+it}.toSet()
            Booster.COLUMN -> (0..7).map{it*8+c}.toSet()
            Booster.CROSS -> ((0..7).map{r*8+it}+(0..7).map{it*8+c}).toSet()
            Booster.BOMB -> (0..63).filter{kotlin.math.abs(it/8-r)<=1 && kotlin.math.abs(it%8-c)<=1}.toSet()
            Booster.COLOR -> (0..63).filter{color(board[it/8][it%8])==color(board[r][c])}.toSet()
            Booster.SHUFFLE -> emptySet()
        }
        return resolve(hits, at)
    }
    private fun resolve(initial:Set<Int>, b:Int):List<Wave> {
        val waves = mutableListOf<Wave>()
        var hits = initial.toMutableSet()
        while (hits.isNotEmpty()) {
            combo++
            val before=Array(8){board[it].clone()}
            val iceBefore=Array(8){ice[it].clone()}
            // Four creates a line rocket; five or intersecting runs create a blast.
            val specials=mutableMapOf<Int,Int>()
            for (axis in 0..1) for (line in 0..7) {
                var s=0
                while(s<8) {
                    var e=s+1
                    fun index(i:Int)=if(axis==0)line*8+i else i*8+line
                    while(e<8 && color(board[index(e)/8][index(e)%8])==color(board[index(s)/8][index(s)%8])) e++
                    if(e-s>=4) {
                        val run=(s until e).map{index(it)}
                        val at=run.firstOrNull{it==b}?:run[run.size/2]
                        specials[at]=color(board[at/8][at%8])+if(e-s>=5)18 else if(axis==0)6 else 12
                    }
                    s=e
                }
            }
            val activated=mutableSetOf<Int>()
            do {
                val pending=hits.filter{it !in activated && board[it/8][it%8]>=6}
                for(i in pending) {
                    activated.add(i)
                    val kind=board[i/8][i%8]/6
                    if(kind==1) for(c in 0..7) hits.add(i/8*8+c)
                    if(kind==2) for(r in 0..7) hits.add(r*8+i%8)
                    if(kind>=3) for(r in (i/8-1).coerceAtLeast(0)..(i/8+1).coerceAtMost(7)) for(c in (i%8-1).coerceAtLeast(0)..(i%8+1).coerceAtMost(7)) hits.add(r*8+c)
                }
            } while(pending.isNotEmpty())
            for(i in hits) {
                collected[color(board[i/8][i%8])]++
                if(ice[i/8][i%8]>0){ice[i/8][i%8]=0;clearedIce++}
                score+=10*combo
                board[i/8][i%8]=-1
            }
            for((i,v) in specials) board[i/8][i%8]=v
            val origins=Array(8){IntArray(8)}
            for(c in 0..7) {
                var write=7
                for(r in 7 downTo 0) if(board[r][c]>=0){board[write][c]=board[r][c];origins[write][c]=r;write--}
                val spawned=write+1
                while(write>=0){board[write][c]=random.nextInt(6);origins[write][c]=write-spawned;write--}
            }
            waves.add(Wave(hits.toList(),before,Array(8){board[it].clone()},combo,origins,iceBefore,Array(8){ice[it].clone()},score))
            hits=matches().toMutableSet()
        }
        if(!won && !failed && !hasMove()) {
            val before=Array(8){board[it].clone()};refill()
            waves.add(Wave(emptyList(),before,Array(8){board[it].clone()},combo,
                iceBefore=Array(8){ice[it].clone()},iceAfter=Array(8){ice[it].clone()},scoreAfter=score,shuffled=true))
        }
        return waves
    }
    fun encode(): String = (listOf(level,moves,score,clearedIce) + collected.toList() + board.flatMap{it.toList()} + ice.flatMap{it.toList()}).joinToString(",")
    fun restore(raw:String):Boolean {
        val v=raw.split(',').map{it.toIntOrNull() ?: return false}
        if(v.size!=138 || v[0]!=level || v[1] !in 0..spec.moves || v[2]<0 || v[3] !in 0..spec.ice || v.slice(4..9).any{it<0} || v.slice(10..73).any{it !in 0..23} || v.slice(74..137).any{it !in 0..1}) return false
        moves=v[1];score=v[2];clearedIce=v[3]
        for(i in 0..5) collected[i]=v[4+i]
        for(i in 0..63){board[i/8][i%8]=v[10+i];ice[i/8][i%8]=v[74+i]}
        return true
    }
}

data class LevelSpec(val moves:Int,val target:Int,val ice:Int,val collectColor:Int,val collectCount:Int)
object Campaign {
    const val COUNT=250
    // Ten chapters introduce collection, frost, mixed goals and tighter move budgets.
    fun level(number:Int):LevelSpec {
        require(number in 1..COUNT)
        val chapter=(number-1)/25
        val stage=(number-1)%25
        val pattern=stage%5
        return LevelSpec(30-chapter/2+if(pattern==4)4 else 0,
            650+chapter*170+stage*18,
            if(chapter==0 && stage<5)0 else (4+chapter*4+pattern*2).coerceAtMost(48),
            (chapter+stage)%6,
            if(chapter==0 && stage<3)0 else 8+chapter*3+pattern*3)
    }
}

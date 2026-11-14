package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.Gravity
import android.view.View
import android.widget.*

/** A playable estate work order. Selection never changes saved progression or spends stock. */
class ConstructionScreen(
    context:Context, private val save:GardenSave,
    private val back:()->Unit, private val build:()->Unit,
    private val levels:()->Unit, private val replay:(Int)->Unit,
    private val story:(Int)->Unit,
    private val expansion:(()->Unit)?=null
):LinearLayout(context) {
    private var selected=save.tasks.coerceAtMost(RestorationUiModel.total-1)
    private val details=LinearLayout(context)
    private val map=SiteMap(context)
    private val choices=LinearLayout(context)
    private val zones=LinearLayout(context)
    private val zoneThumbnails=mutableMapOf<Int,Bitmap>()
    private val zoneArt by lazy { GameSkin.bitmap(context,R.drawable.estate_zone_art) }
    private val language get()=save.language
    private fun words(sq:String,de:String,it:String,mk:String,tr:String)=when(language){"de"->de;"it"->it;"mk"->mk;"tr"->tr;else->sq}
    private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
    private fun text(value:String,size:Float=16f,color:Int=0xfff3ecd3.toInt())=TextView(context).apply{
        text=value;textSize=size;setTextColor(color);setPadding(dp(14),dp(7),dp(14),dp(7))
    }
    private fun button(value:String,action:()->Unit)=Button(context).apply{
        text=value;isAllCaps=false;setTextColor(0xff294d3c.toInt())
        background=GameSkin.background(context,R.drawable.hd_hud,16f)
        setOnClickListener{action()};minHeight=dp(48)
    }
    init {
        orientation=VERTICAL;setBackgroundColor(0xff153f35.toInt());setPadding(dp(8),dp(4),dp(8),dp(8))
        addView(button(words("‹ Kopshti","‹ Garten","‹ Giardino","‹ Градина","‹ Bahçe"),back))
        addView(text(words("PLANI I PRONËS","BAUPLAN DES ANWESENS","PROGETTO DELLA TENUTA","ПЛАН НА ИМОТОТ","MÜLK PLANI"),22f,0xffffdf83.toInt()))
        addView(text("${RestorationUiModel.progress(save.tasks)}  •  ${save.coins} ${GameText.get(save.language,"coins")}  •  ★ ${save.stars}  •  🔑 ${save.keys}",14f))
        addView(HorizontalScrollView(context).apply{
            isHorizontalScrollBarEnabled=false
            addView(zones)
        },LayoutParams(-1,dp(86)))
        expansion?.let { open ->
            addView(button(ExpansionScreen.title(language),open),LayoutParams(-1,dp(48)))
        }
        addView(map,LayoutParams(-1,0,0.85f))
        addView(HorizontalScrollView(context).apply{isHorizontalScrollBarEnabled=true;addView(choices)},LayoutParams(-1,dp(55)))
        details.orientation=VERTICAL
        addView(ScrollView(context).apply{addView(details)},LayoutParams(-1,0,1.4f))
        refresh()
    }
    private fun refresh(){
        val activeZone=EstateZones.index(selected)
        renderZones(activeZone)
        choices.removeAllViews()
        EstateZones.all[activeZone].tasks.forEach { task ->
            val marker=when{task<save.tasks->"✓";task==save.tasks->"→";else->"🔒"}
            choices.addView(button("$marker ${task+1}"){selected=task;refresh()},LayoutParams(dp(66),-1).apply{marginEnd=dp(4)})
            val button=choices.getChildAt(choices.childCount-1)
            button.alpha=if(task==selected)1f else .65f
            button.isSelected=task==selected
            button.contentDescription=RestorationCatalog.title(task,language)
        }
        map.moveTo(selected);map.contentDescription=RestorationCatalog.title(selected,language)
        details.removeAllViews()
        (details.parent as? ScrollView)?.scrollTo(0,0)
        details.addView(text("${selected+1}. ${RestorationCatalog.title(selected,language)}",21f,0xffffdf83.toInt()))
        val done=selected<save.tasks
        val current=selected==save.tasks
        details.addView(text(when{done->words("✓ Punimi është ruajtur në pronë","✓ Dauerhaft restauriert","✓ Restauro permanente","✓ Трајно обновено","✓ Kalıcı olarak restore edildi");current->words("NDËRTIMI I RADHËS","NÄCHSTER AUFTRAG","PROSSIMO LAVORO","СЛЕДНА ЗАДАЧА","SIRADAKİ İŞ");else->words("Hapet pasi të përfundosh punimin ${selected}","Nach Auftrag $selected verfügbar","Disponibile dopo il lavoro $selected","Достапно по задача $selected","İş $selected tamamlanınca açılır")},14f))
        val site=RestorationWorksites.forTask(selected)
        details.addView(text(words("Mjeshtri","Fachkraft","Specialista","Мајстор","Usta")+": "+SceneCast.name(site.worker)+"  •  "+site.cast.joinToString(" · "){SceneCast.name(it)},14f))
        if(!done){
            details.addView(text("120 ${GameText.get(save.language,"coins")}"+if(selected>0)" + ★ 1" else "",18f))
            RestorationInventory.forTask(selected).forEach { need ->
                val have=save.materialCount(need.material)
                val row=LinearLayout(context).apply{orientation=VERTICAL}
                row.addView(text("${FountainText.get(language,need.material.name)}   $have / ${need.count}",15f))
                row.addView(ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal).apply{max=need.count;progress=have.coerceAtMost(need.count)},LayoutParams(-1,dp(7)).apply{setMargins(dp(14),0,dp(14),dp(4))})
                details.addView(row)
            }
        }
        details.addView(text(words("Rendi i punimeve","Arbeitsablauf","Fasi dei lavori","Редослед на работа","İş sırası"),18f,0xffffdf83.toInt()))
        // These are the actual playable scene phases, not a separate fictional construction plan.
        val phases=when(selected){
            0->listOf("INSPECT","CLEAR","REPAIR","TRY","CELEBRATE")
            1->listOf("ARRIVAL","SOIL","PLANT","WATER","CELEBRATE")
            else->RestorationSequence.plan(selected).steps.map{it.key}
        }
        details.addView(text(phases.mapIndexed { i,key -> "${i+1}. ${phase(key)}" }.joinToString("  →  "),14f))
        when {
            done->{details.addView(button(words("▶ Riluaj ndërtimin","▶ Bau erneut ansehen","▶ Rivedi il restauro","▶ Повтори ја обновата","▶ İnşaatı tekrar izle")){replay(selected)})
                details.addView(button(words("Biseda dhe historia","Dialog und Geschichte","Dialoghi e storia","Разговор и приказна","Diyalog ve hikâye")){story(selected)})}
            current->{details.addView(button(words("Ndërto • materiale / çelës / monedha","Bauen • Material / Schlüssel / Münzen","Costruisci • materiali / chiave / monete","Гради • материјали / клуч / монети","İnşa et • malzeme / anahtar / para"),build))
                details.addView(text(words("1 çelës zëvendëson materialet. Kostoja bazë dhe ylli mbeten. Monedhat mund të blejnë vetëm materialet që mungojnë.","1 Schlüssel ersetzt Materialien. Grundpreis und Stern bleiben. Münzen kaufen fehlendes Material.","1 chiave sostituisce i materiali. Costo base e stella restano. Le monete comprano i materiali mancanti.","1 клуч ги заменува материјалите. Основната цена и ѕвездата остануваат. Монети купуваат материјали што недостигаат.","1 anahtar malzemelerin yerine geçer. Temel ücret ve yıldız gerekir. Parayla eksik malzeme alınır."),14f))}
        }
        details.addView(button(words("Luaj për të fituar materiale, yje dhe monedha","Spielen: Material, Sterne und Münzen verdienen","Gioca: materiali, stelle e monete","Играј за материјали, ѕвезди и монети","Malzeme, yıldız ve para kazanmak için oyna"),levels))
    }
    private fun renderZones(activeZone: Int) {
        zones.removeAllViews()
        EstateZones.all.forEachIndexed { index, zone ->
            val width=zoneArt.width/2
            val height=zoneArt.height/2
            val thumbnail=zoneThumbnails.getOrPut(index) {
                Bitmap.createBitmap(zoneArt,index%2*width,index/2*height,width,height)
            }
            val card=FrameLayout(context).apply {
                contentDescription="${zone.title(language)}. ${zone.completed(save.tasks)}/${zone.tasks.count()}"
                isFocusable=true
                isSelected=index==activeZone
                setBackgroundColor(if(index==activeZone)0xffe1ba66.toInt() else 0xff52786a.toInt())
                setPadding(dp(2),dp(2),dp(2),dp(2))
                setOnClickListener {
                    if(index!=activeZone){
                        selected=save.tasks.coerceIn(zone.tasks.first,zone.tasks.last)
                        refresh()
                    }
                }
            }
            card.addView(ImageView(context).apply {
                setImageBitmap(thumbnail)
                scaleType=ImageView.ScaleType.CENTER_CROP
                alpha=if(save.tasks<zone.tasks.first).63f else 1f
            },FrameLayout.LayoutParams(-1,-1))
            card.addView(TextView(context).apply {
                text="${zone.title(language)}\n${zone.completed(save.tasks)}/${zone.tasks.count()}"
                textSize=12f
                setTextColor(Color.WHITE)
                typeface=Typeface.DEFAULT_BOLD
                setBackgroundColor(0xd01a362b.toInt())
                gravity=Gravity.CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
                setPadding(dp(5),dp(4),dp(5),dp(4))
            },FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM))
            zones.addView(card,LayoutParams(dp(146),-1).apply{marginEnd=dp(5)})
        }
        zones.post {
            val scroll=zones.parent as? HorizontalScrollView ?: return@post
            val card=zones.getChildAt(activeZone) ?: return@post
            if(card.left<scroll.scrollX || card.right>scroll.scrollX+scroll.width){
                scroll.smoothScrollTo(card.left-(scroll.width-card.width)/2,0)
            }
        }
    }

    private fun phase(key:String):String=when(key){
        "ARRIVAL"->words("Mbërritja","Ankunft","Arrivo","Пристигнување","Varış")
        "INSPECT"->words("Kontrolli","Prüfung","Ispezione","Проверка","İnceleme")
        "CLEAR","CLEAN","REMOVE","WEED"->words("Pastrimi","Aufräumen","Pulizia","Чистење","Temizlik")
        "SOIL","LEVEL"->words("Përgatitja e tokës","Boden vorbereiten","Prepara il terreno","Подготовка на почва","Zemini hazırla")
        "PLANT","DECORATE","GROW"->words("Mbjellja dhe zbukurimi","Pflanzen und dekorieren","Pianta e decora","Садење и украсување","Dikim ve dekorasyon")
        "WATER"->words("Ujitja","Bewässern","Annaffiatura","Наводнување","Sulama")
        "PLACE"->words("Shtrimi i gurëve","Steine verlegen","Posa delle pietre","Поставување камења","Taş döşeme")
        "PAINT"->words("Lyerja","Anstrich","Pittura","Бојадисување","Boyama")
        "ROOF"->words("Tjegullat","Dachziegel","Tegole","Ќерамиди","Kiremitler")
        "LIGHT"->words("Ndriçimi","Beleuchtung","Illuminazione","Осветлување","Aydınlatma")
        "WINDOWS"->words("Dritaret","Fenster","Finestre","Прозорци","Pencereler")
        "TRY","REVEAL","CELEBRATE"->words("Prova dhe përfundimi","Prüfen und abschließen","Verifica e completamento","Проверка и завршување","Kontrol ve tamamlama")
        else->words("Montimi dhe riparimi","Montage und Reparatur","Montaggio e riparazione","Монтажа и поправка","Montaj ve onarım")
    }
    private inner class SiteMap(context:Context):View(context){
        private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val image=GameSkin.bitmap(context,R.drawable.garden_before)
        private val backdrop=GardenBackdrop(context,save.tasks)
        private var target=RestorationWorksites.forTask(selected)
        private var camera=CharacterAnimation.Position(target.workX,target.workY)
        private var animator:ValueAnimator?=null

        fun moveTo(task:Int){
            val site=RestorationWorksites.forTask(task)
            if(site==target)return
            target=site
            val start=camera
            val end=CharacterAnimation.Position(site.workX,site.workY)
            animator?.cancel()
            if(!isAttachedToWindow){
                camera=end
                invalidate()
                return
            }
            animator=ValueAnimator.ofFloat(0f,1f).apply{
                duration=620
                interpolator=android.view.animation.LinearInterpolator()
                addUpdateListener {
                    camera=CharacterAnimation.move(start,end,it.animatedValue as Float)
                    invalidate()
                }
                start()
            }
        }

        override fun onDetachedFromWindow(){
            animator?.cancel()
            animator=null
            camera=CharacterAnimation.Position(target.workX,target.workY)
            super.onDetachedFromWindow()
        }

        override fun onDraw(c:Canvas){
            super.onDraw(c)
            if(width==0||height==0)return
            val scale=maxOf(width/image.width.toFloat(),height/image.height.toFloat())*1.45f
            val w=image.width*scale
            val h=image.height*scale
            val left=(width*.5f-w*camera.x).coerceIn(width-w,0f)
            val top=(height*.5f-h*camera.y).coerceIn(height-h,0f)
            val bounds=RectF(left,top,left+w,top+h)
            backdrop.draw(c,bounds)
            WorldRestorationArt.draw(context,c,bounds,save.tasks,0f)
            val x=left+w*target.workX
            val y=top+h*target.workY
            paint.style=Paint.Style.STROKE;paint.strokeWidth=dp(2).toFloat();paint.color=0xffffdf83.toInt()
            c.drawCircle(x,y,dp(25).toFloat(),paint);c.drawLine(x-dp(38),y,x+dp(38),y,paint);c.drawLine(x,y-dp(38),x,y+dp(38),paint)
            paint.style=Paint.Style.FILL;paint.color=0xee153f35.toInt();c.drawCircle(x,y,dp(17).toFloat(),paint)
            paint.color=Color.WHITE;paint.textAlign=Paint.Align.CENTER;paint.textSize=dp(17).toFloat();paint.typeface=Typeface.DEFAULT_BOLD
            c.drawText("${selected+1}",x,y+dp(6),paint)
        }
    }
}

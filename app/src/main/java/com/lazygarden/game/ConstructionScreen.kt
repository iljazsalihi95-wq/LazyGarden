package com.lazygarden.game

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
    private val story:(Int)->Unit
):LinearLayout(context) {
    private var selected=save.tasks.coerceAtMost(RestorationUiModel.total-1)
    private val details=LinearLayout(context)
    private val map=SiteMap(context)
    private val choices=LinearLayout(context)
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
        addView(map,LayoutParams(-1,0,0.9f))
        addView(HorizontalScrollView(context).apply{isHorizontalScrollBarEnabled=true;addView(choices)},LayoutParams(-1,dp(55)))
        details.orientation=VERTICAL
        addView(ScrollView(context).apply{addView(details)},LayoutParams(-1,0,1.2f))
        refresh()
    }
    private fun refresh(){
        choices.removeAllViews()
        RestorationCatalog.entries.indices.forEach { task ->
            val marker=when{task<save.tasks->"✓";task==save.tasks->"→";else->"🔒"}
            choices.addView(button("$marker ${task+1}"){selected=task;refresh()},LayoutParams(dp(66),-1).apply{marginEnd=dp(4)})
            choices.getChildAt(task).alpha=if(task==selected)1f else .65f
            choices.getChildAt(task).contentDescription=RestorationCatalog.title(task,language)
        }
        map.invalidate();map.contentDescription=RestorationCatalog.title(selected,language)
        details.removeAllViews()
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
        override fun onDraw(c:Canvas){
            super.onDraw(c);if(width==0||height==0)return
            val scale=maxOf(width/image.width.toFloat(),height/image.height.toFloat())
            val w=image.width*scale;val h=image.height*scale
            val site=RestorationWorksites.forTask(selected)
            val left=(width*.5f-w*site.workX).coerceIn(width-w,0f)
            val top=(height*.5f-h*site.workY).coerceIn(height-h,0f)
            val bounds=RectF(left,top,left+w,top+h)
            backdrop.draw(c,bounds);WorldRestorationArt.draw(context,c,bounds,save.tasks,0f)
            val x=left+w*site.workX;val y=top+h*site.workY
            paint.style=Paint.Style.STROKE;paint.strokeWidth=dp(2).toFloat();paint.color=0xffffdf83.toInt()
            c.drawCircle(x,y,dp(25).toFloat(),paint);c.drawLine(x-dp(38),y,x+dp(38),y,paint);c.drawLine(x,y-dp(38),x,y+dp(38),paint)
            paint.style=Paint.Style.FILL;paint.color=0xee153f35.toInt();c.drawCircle(x,y,dp(17).toFloat(),paint)
            paint.color=Color.WHITE;paint.textAlign=Paint.Align.CENTER;paint.textSize=dp(17).toFloat();paint.typeface=Typeface.DEFAULT_BOLD
            c.drawText("${selected+1}",x,y+dp(6),paint)
        }
    }
}

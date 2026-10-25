package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.util.Base64
import android.view.View
import kotlin.math.sin

class GardenWorldView(
    context: Context,
    private val tasks: Int,
    private val constructing: Boolean = false
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f
    private val artwork: Bitmap by lazy {
        val bytes = Base64.decode(HERO_ART, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val src = Rect(0, 0, artwork.width, artwork.height)
        val dst = Rect(0, 0, width, height)
        canvas.drawBitmap(artwork, src, dst, paint)

        val w = width.toFloat(); val h = height.toFloat()
        paint.shader = LinearGradient(0f, 0f, 0f, h,
            intArrayOf(0x22000000, 0x00000000, 0x66000000),
            floatArrayOf(0f, .58f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        phase = (phase + .018f) % 1f
        for (i in 0 until 10) {
            val x = w * (.08f + ((i * .091f) % .84f))
            val y = h * (.18f + ((i * .073f + phase) % .58f))
            paint.color = if (i % 2 == 0) 0x66FFD765 else 0x55FFFFFF
            canvas.drawCircle(x, y, w * (.003f + (i % 3) * .0013f), paint)
        }

        if (constructing) {
            paint.color = 0xB0000000.toInt()
            canvas.drawRoundRect(w*.16f,h*.43f,w*.84f,h*.58f,30f,30f,paint)
            paint.color = 0xFFFFD54F.toInt(); paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD; paint.textSize = w*.052f
            canvas.drawText("RESTORING THE ESTATE",w*.5f,h*.515f,paint)
        }
        if (tasks > 0) {
            paint.color = 0xB0155A2A.toInt()
            canvas.drawRoundRect(w*.08f,h*.80f,w*.92f,h*.89f,28f,28f,paint)
            paint.color = Color.WHITE; paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD; paint.textSize = w*.034f
            canvas.drawText("ESTATE RESTORED  $tasks/7",w*.5f,h*.855f,paint)
        }
        postInvalidateDelayed(32)
    }

    companion object {
        private const val HERO_ART = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAoHBwgHBgoICAgLCgoLDhgQDg0NDh0VFhEYIx8lJCIfIiEmKzcvJik0KSEiMEExNDk7Pj4+JS5ESUM8SDc9Pjv/2wBDAQoLCw4NDhwQEBw7KCIoOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozv/wAARCABVAIADASIAAhEBAxEB/8QAGwAAAgMBAQEAAAAAAAAAAAAABAUAAwYCAQf/xAA0EAABAwMDAwMDAgUEAwAAAAABAgMEAAUREiExBhNBIlFhFHGBMpEjQqGx4RYzYtFSwfD/xAAaAQADAQEBAQAAAAAAAAAAAAACAwQFAQAG/8QAMREAAQQABAQFAwIHAAAAAAAAAQACAxEEEiExIkFR8AUTcYGhYZHBseEUIyQyM9Hx/9oADAMBAAIRAxEAPwDMMAtSEhhzc8/FXXGGpKtRcK9smmFt6UmSIokZ0A75NaVmwMRLWpyRhSgncmkQeHGWpHGgpJ5vLJYBZSPpBlyU92W3UMoAy46vYIT7/f4r6RLs8ddrKYbqkPJRlLgXu59zXzm0FpqM44AU63FLJ8AJ2H9ya0sC49uO0lqcW3W9ilRwFDPt9tqowWBhhPmhwz3339UEkxe0xuGiGXZJLav47bgOMnVXrdtSQCkAg+RR936jeg2qS6tGrSjLW/pOdh496SM3WQ1b7fHa0gvZBKSCrAGTv+1bL/EXMkax/MH4pYpwDCC5zymyLZ/xrtUZlhp111QQhnGsqBA3+fNLJDj0KN3i6oq7oSCpRHqPj3NLLlKmamxcD3lE5bbKlbD7efvzU2N8RMOjdStDA+FslBdIaHzaOlzFOx1uBotRkYVlSfWoZ2OPA+/NMWZqEYVNQQjGpS0pwQPfT/1Vkh55m1x2YrCRJkOkPFTRVjCdR2PJ8e1VXJq4XazoZAbbdC1IUskJzj4+ccCsE+IvMufNxV8dF9IMDCIPKyW3pzvrfXvZNbf1LHacW0whS4bJAL5UlOc8YFMuoL4IHT77zbp7ygENqSeCTjb3rJ9K25VwiKcLymktLCFpCAAVcE0d1XGYasshDrjshpl4JwxkKDnjI/PIrjMY+S/MF306pbsPGIx5XDXWymUy32S+2ZqRMShpxxoKEpJAWk4338jPg184fQLROcjPKDhTwtPC0+FCm8aXMWm2sKZWWkIUpRIyTnge3vS36Y3OPCUpKg4JDjS8pOcH1DP5z+9eeYZw1o/u0v7KCpBuNEpcbL8kqZQcHfFaTp+Ey8RrVhxJ/QaITboFnAkmT3gnIUhlOsj7+1FRHYqZrrjUdZJSk+pQSQT8ePzUGNLstA9+qrw2H5uCGul9+jsbcVo6XCANvFJJ3UMpy3BgrOFDemDtsF3jhQXgpqWnpxmT3PqPUG84zWpHi45oqvUckiSBzpszBoV50/DYes7ElctTLgcUAAM5GrnHB5ouDpmPlPfS6pKlYLjeoAAkbJHGTiho7seJEWyhp3Z0paUlGQklWTn8A0TZYItzb6ZUmIp1YOkKXsRknjGxzVEEkbmta0G+ZO33SpWysN0K5Vdlc9ZQ3nbDHdjMPOJTjuJDZICQSc53xSltL01FuejPJSllSsqWoFIGBk7fatQ1FlOwUMwbjDCloJfSXQFc7DjjFZCXb5XTilpnlLncaWoIaXqAzt+occ8fIqSQEuaSdr70THRtDia0RkuWH0OMSXGShbqnUJ5wonbjfjau3JTd2vcfB7BeIJIWFHAGBjbnbOPmgn7axNgw37Ww5mQcOoCirTx59t6Y26Ey31G2y9HLbaCQzp9CfODnG+cGo3TBzS+7HQ+quhFVSeOOKcJAuZWhC1FBcaKlJBBFCq7dvtVwll9x6QtBShZCgE5UOB481xf1ttzmmsH6XsFSUhwoyrI3OOftRz64L3T7xUE9x2OsNoU6VE6VbYB52HNTtdl0AWhJHwWr7XOXFmRGhntSAp1aAkZyN9vJG/70JfbhIRd23WHkoYkOdwsP4QSrGPPI+fmg2J8pyW1F7ZC4TyA0fKkLSAr7D7bVT1bBdm9QRMPMRkNtk9zTgaio42HwBQst4LCVETbSWiyi7dJWzM0XCIw0sOow1qxpSScEYJz+KVXa6ssyXFFRlB11SiUK0kZyBv4IG3zRF2jzm5LbzkZ5YaYUha0nUnO+k8c7/jilNogJuF0ZadSWlaNaQk5yrOcH8V2BtNt3PXv1QR3sd/qiGm7tLVq0hnUBhSyUJUlJ2yPP70NNduFrnfUyFNS1qGFq2KDsQBgef+6vNouMjqR1p0qkbJV2y5kJzx+RTZ/p+MbQ/IeQUtpbKgdWMHAxnPj7VSHSE6a8+/RPzMc0nNz0v4REaMmMgdtepGPerkThEaVobOFbHagLMl5lHal5II9JruamaiOpppoKyfSTS3tbG4vjbunSxtjYHM1XDVpucpP1sV9DcVbuhaXFaUnfJ3GTnOPFMZzLbKXUPvpLvb/gqGSok5+BmqLZa5CIgUboqI6tWEjXhIHkj3PxV613Fhnum5PPISSpZ7QWEpGwzjODV+EnhjZb9/WllTQyycWagO+qtgQpzdmRLRKCXVKKFqBCigEbcCgH7HKfUzFmrbkJfQtBLqFFSycEHJxvnzkU0gXKSy045JuCnBspttoJyUnjII2I5ohzqq3RVjVcH3yW90BSQUq+6cCuSSxFha5vvdc08YScvDxt0r6flZqJYEW+Q1aJkl7uiOpaNbejSoj/AIkhQz80xt9vnxHlGWvuwEIBaIY074ySRjbfNJZHXEKNc3H5VvzJA0tyxuNI42PB+d6siLv/AFooLtjhjxUK9ctxxSRn2Hv9hWO7zc5oU3ezt7JD5ZIXBjWm/haCbHtMxtp5ztkdopZUHSkAc4/9/is1OjvwpBZuCHYzamyI6kupJST523wfmipXRN0t8d2Y5cmZSWVF/spBTjG5/VkH7bUTaenHupbe3c5l2acVNVp2Rp7ASdhjz/mqMPJd5crvwqIcRI4gSjT6HsoBCb0wEsx2UXBvILThGlKE75CjnnijWrauVNZekdhaSdX8AhQG42OftTSB1FZ2ozcJa0JjqUppa1pwjYHggb8UJCasTF3S3bH5OAkkJQnuDkE7ciha4Zr5km9+/dEJWGThN+66F0YNxn2rtl7DoeDp04I0+wOMDNLbAgK6na0MryY5AyABxjVj/wC/rQV6agO3NQhTn3lvKKy1pCMkq3SPYjfn4pnZYlyiPG5S0tRFnCC0d1KaHAJzt/ii0Bsn22/6uh4cct6goKxSpbBJWtLctIyUvq/lTnON9tzyad3C4ty4KokRzWwYxCltuAHV7Ae9Zq922Vb7kVw2HnGVNJHeY9aSn/sDbFVLmWmG0lJbcBaGFsqSj1ZGMkjjHt80Izx2WGrROjjbGM5r9lIF4lT5qC2MtpO9aO43tm2KS7LYecaCNi2nKUn/AJVkn4D/AE6prKj69zinUV2de7fIjIDpZIGtDadzk4G/tTeFzODn7IhCPKyMf/MJ2PJaTolyJdLbKnPxWVvrexlSQQBpBGB9jWHhmd/qORCaefUouqYTHaXpDu+RqyMAAf28Vo+i3W4s2829hwKbZeTowCAMZSRvv/LigGbHc2+vHLkQuLCMgnvBSQV5TjCQd9ztnFZvm5MRKx50A0tUGIZCDrt8JtKsNxXObevk2MxDd9Lq4ysFJx6QSQBg45opdk6VgxostYL7TryG21uvEpcKjgbbAjzVFwhsyOnptrkIESOt8ZJfLiijWn1kk7E4O1B9VixTrWGXpRQuG2TEaYdwErCfTsOcYFS/xMkrgMxGvIcvnr1TMkjheuibwum4Vu6muFwcjMmEWElltSAUtqydeAeP0j96WS7vcnLHbrdY0oam3Rbi21ABKWW9RUVfGxFU3nqKSx0A3LmoUxMlMJbKFbEKOxOPtv8AmuJcZx2NCdtM5ph+OwWkLcBKVIUBnjg+kV6ISvIMutGtduEc/elkeIyNjyA89ftt91xcekbwjpybFkdVuyilBecYCMleATp1E5AOPam7Unqiz9NF6Xb7c8uHFSmKlk4XHIH6lDGFYGCQD4pM02izWp6M7NU7JuToQ7IJ3KlYTtnwASaPU6IyLr2YikIdY0pfXIU4t86TnIJ2x+M5qlmJmisCiL6V075lZ7MYxt0AEPO6KuL86RdJlyiPOqbLylFohKV8kJTnAHzTLpPpmXYZjs2U9G/jsY0s5GhRIJzsM7AftSi43Bcjqy0w3H3BGDDi3GUrIS6RjAPuNquan3Fp28ybg6ey86Exk68hLeMZHtkn+lKlmxUkZDiNRfzX7rpxvDQA1/2suq2XWYJN/MSWlhT7rqXwn06SonOOSPkUXCvzwZW3IStKVJABYb7md8/ze+OcbfNaW4X4RH3GgmYpAj9tDSGcsnJ0g6gNvbnG9GKudtkXRu0yILT/AG4eourSCG05CQke2cZ29qacYRTnR+lch7pjMXEeKqNVf090I6oSLWh+Octugo/20pKTj4xWHuVnk61FDhfHp/zn3q6LcnhFfcZcWQFlGCs4WM7HHvjzVE+U/FWlkNLQB6gFKznNbEAdXGdlqMjZIP6g0BzW4fgQryiO5cFFAA/QDvSi8WR2xTGnmrg41Ce2DoUR2yBkZ967YmoWwXH14Wn9PzRCrwi6R24cloFAOwNG3DgDILA6rXn8PEwuM8XVZfp+/M2C4ypGh15h4FIVypW4OT+c13d+tLldmzHYaERkqCgtR9QwcgimU3pQW9t51SkGK56krIOWFf8AltyPcfb2oCD0+Y8lqc6FuNJVklkpWSDsk88HNRyQwNmD5RxaUSs0SyQhsRFcr3069/dCfTzZizLlTzM1pBcZKi0SnwcFO4+RnmrrYw/Y7im8spadeTkFnXvpIwQCfNX9SpgtONQEPL7zQy4+tBynVsW8e3BzVVn6XZub6VPONhpCsFCOcYO5J853xRSGNrdToR2NPwhdIxs5A4zyN/g6Iie7K6vntqkNfSwI2SUOrGVKP2NRq0XKI63Dt94bKXNmm3kEn8Ec01h9BTGZLku2yWmTn0xnd8jbZWBwec52zQk+w9UwHxNFuSENnJDDgcwPJxyNqCMUzLHWWtB9VLipJJv8kIcPkd+iGf6ZdfmuNXG7remMAKCWU+lrbIOSf8UM/ap5JU7fl+saM6Rgp9uaMi2bqmY85cV2tWh4gkPOJb1ADA2O+KClxPrJa3LlKSlTY0ltkoUhseACQQfk01jZXFrdDp0/RDCxr3Na6ABo+/6qly2zO8mUboXJLQw24f5R7Vw4i+S2n2nLk32lEFxasjB8Aec7cChUMQhAkKCgXNQ0JWMLHyCNvuKaWVuDFWy5OlBeU4jttE6mlK21qzjxtRV1/RdIwsjmt8sAdegQqupbwyUNOlDjbawVacpLmDxggV011Me7cH1RXEyJSEoaA30gA4/qSaa9RW/6lbENEguNRnFIceW0QgFQyDk74xXkF7p5hCQIDT6228FxzfWr338DxmlubCdGtv0+/okSYDCNkMYOnXu0b0z0ylfT6JEgadysEjfA8/0pUmJJuk5xMNpUlKFnCj4quR1NOSpZEopSQUhtI2AqdM3d2A+56lJCzmmkuLTarErZ3CMGh3SSSJb/AGQnuHArVdOKC7elxY1KzyalSqoyS1V+ESvfMQ43otW62J1qdYdzpUgjasm7a3LDbFPwZzyNX6kcpP4qVKVQc0B2oV+Kja6yRsh1XqZI0OvNw3FnHqVGSSKJkX64oiH1RiRulX0yQUn3GKlSoncLgwbLDJKEtnUt1Ykd1l5tCvPoyD+5rRM9S3lRLn1aQTyNG396lSqSxrRoEURNLiR1NeUoJEpGcY/2x/asHNluLkurUhrUvOcIwB9gNhUqUTUrEOIrVeRJLj+GSlpKE5xhHvz5o9yEmNG7oXrIAxrGeKlSkSvc14AO6nZruuzNm3S26JctxTSDlLSfSj9hz+aVSHlMo0o2qVKoiAacrdAhlF0VS2VPYKjxViZDgXpztUqUZaCEkuIOi//Z"
    }
}

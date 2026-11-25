package br.com.pam

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/** Ícones vetoriais de 24 dp, traço de 1,6 dp e extremidades arredondadas. */
class PamIcon(context: Context, private val kind: String, private val tint: Int) : View(context) {
    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        c.save(); c.scale(width / 24f, height / 24f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = tint; style = Paint.Style.STROKE; strokeWidth = 1.6f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        fun path(vararg xy: Float) { val a = Path(); a.moveTo(xy[0], xy[1]); for (i in 2 until xy.size step 2) a.lineTo(xy[i], xy[i+1]); c.drawPath(a,p) }
        when (kind) {
            "home" -> path(3f,10f,12f,3f,21f,10f,21f,21f,3f,21f,3f,10f)
            "authors" -> { c.drawCircle(12f,7f,4f,p); c.drawArc(3f,13f,21f,29f,180f,180f,false,p) }
            "poems" -> { path(12f,5f,3f,3f,3f,19f,12f,21f,21f,19f,21f,3f,12f,5f,12f,21f) }
            "random" -> { c.drawRoundRect(3f,3f,21f,21f,4f,4f,p); p.style=Paint.Style.FILL; c.drawCircle(8f,8f,1.2f,p); c.drawCircle(16f,16f,1.2f,p); c.drawCircle(12f,12f,1.2f,p) }
            "back" -> { path(20f,12f,4f,12f,9f,7f); path(4f,12f,9f,17f) }
            "share" -> { path(12f,15f,12f,3f,8f,7f); path(12f,3f,16f,7f); path(5f,12f,5f,21f,19f,21f,19f,12f) }
            else -> path(9f,6f,15f,12f,9f,18f)
        }
        c.restore()
    }
}

package com.family.scratchapp.ui.celebration

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var rotation: Float,
        var rotSpeed: Float,
        var color: Int,
        var shape: Int,   // 0=rect, 1=circle, 2=star
        var size: Float,
        var alpha: Float,
        var scaleY: Float,  // for flutter effect
        var scaleDir: Float
    )

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator? = null

    private val colors = intArrayOf(
        Color.rgb(255, 183, 77),   // amber
        Color.rgb(129, 199, 132),  // green
        Color.rgb(100, 181, 246),  // blue
        Color.rgb(240, 98, 146),   // pink
        Color.rgb(186, 104, 200),  // purple
        Color.rgb(255, 138, 101),  // coral
        Color.rgb(255, 241, 118)   // yellow
    )

    fun burst() {
        particles.clear()
        val rng = Random
        val count = 120

        repeat(count) {
            val angle = rng.nextFloat() * 2 * PI.toFloat()
            val speed = rng.nextFloat() * 18f + 8f
            particles.add(
                Particle(
                    x = width * 0.5f + (rng.nextFloat() - 0.5f) * width * 0.3f,
                    y = height * 0.3f,
                    vx = cos(angle) * speed,
                    vy = -abs(sin(angle)) * speed - rng.nextFloat() * 10f,
                    rotation = rng.nextFloat() * 360f,
                    rotSpeed = (rng.nextFloat() - 0.5f) * 12f,
                    color = colors[rng.nextInt(colors.size)],
                    shape = rng.nextInt(3),
                    size = rng.nextFloat() * 14f + 7f,
                    alpha = 1f,
                    scaleY = 1f,
                    scaleDir = if (rng.nextBoolean()) 1f else -1f
                )
            )
        }

        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3500
            repeatCount = 0
            addUpdateListener {
                update()
                invalidate()
            }
            start()
        }
    }

    private fun update() {
        val gravity = 0.55f
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.vy += gravity
            p.x += p.vx
            p.y += p.vy
            p.vx *= 0.99f
            p.rotation += p.rotSpeed
            p.scaleY += p.scaleDir * 0.04f
            if (p.scaleY > 1f || p.scaleY < 0.2f) p.scaleDir = -p.scaleDir
            if (p.y > height + 80f) p.alpha = 0f
            if (p.alpha <= 0f) iter.remove()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (p in particles) {
            paint.color = p.color
            paint.alpha = (p.alpha * 230).toInt()

            canvas.save()
            canvas.translate(p.x, p.y)
            canvas.rotate(p.rotation)
            canvas.scale(1f, p.scaleY)

            when (p.shape) {
                0 -> canvas.drawRect(-p.size / 2, -p.size / 3, p.size / 2, p.size / 3, paint)
                1 -> canvas.drawCircle(0f, 0f, p.size / 2, paint)
                2 -> drawStar(canvas, p.size * 0.5f)
            }
            canvas.restore()
        }
    }

    private fun drawStar(canvas: Canvas, r: Float) {
        val path = Path()
        val innerR = r * 0.45f
        val points = 5
        for (i in 0 until points * 2) {
            val angle = (i * PI / points - PI / 2).toFloat()
            val radius = if (i % 2 == 0) r else innerR
            val x = cos(angle) * radius
            val y = sin(angle) * radius
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    fun stopAndHide() {
        animator?.cancel()
        particles.clear()
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}

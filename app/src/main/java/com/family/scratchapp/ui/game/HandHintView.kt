package com.family.scratchapp.ui.game

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Animated hand icon that sweeps across the screen to hint "scratch here"
 */
class HandHintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = 200
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        alpha = 60
        style = Paint.Style.FILL
    }

    private var handX = 0f
    private var handY = 0f
    private var handScale = 1f
    private var animator: AnimatorSet? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        handX = w * 0.3f
        handY = h * 0.5f
        startAnimation()
    }

    private fun startAnimation() {
        animator?.cancel()

        val startX = width * 0.25f
        val endX = width * 0.72f
        val startY = height * 0.45f
        val endY = height * 0.55f

        val moveX = ObjectAnimator.ofFloat(this, "handX", startX, endX).apply {
            duration = 900
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        }
        val moveY = ObjectAnimator.ofFloat(this, "handY", startY, endY).apply {
            duration = 900
        }
        val scaleDown = ObjectAnimator.ofFloat(this, "handScale", 1f, 0.88f).apply {
            duration = 450
        }
        val scaleUp = ObjectAnimator.ofFloat(this, "handScale", 0.88f, 1f).apply {
            duration = 450
            startDelay = 450
        }

        val sweep = AnimatorSet().apply {
            playTogether(moveX, moveY, scaleDown, scaleUp)
            startDelay = 200
        }

        val fadeIn = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f).apply { duration = 300 }
        val pause = ValueAnimator.ofFloat(0f, 1f).apply { duration = 600 }
        val fadeOut = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply { duration = 300 }

        animator = AnimatorSet().apply {
            playSequentially(fadeIn, sweep, pause, fadeOut)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Reset and loop
                    alpha = 0f
                    handX = startX
                    handY = startY
                    postDelayed({ startAnimation() }, 500)
                }
            })
            start()
        }
    }

    fun setHandX(v: Float) { handX = v; invalidate() }
    fun setHandY(v: Float) { handY = v; invalidate() }
    fun setHandScale(v: Float) { handScale = v; invalidate() }
    fun getHandX() = handX
    fun getHandY() = handY
    fun getHandScale() = handScale

    override fun onDraw(canvas: Canvas) {
        val size = 90f * handScale

        // Shadow
        canvas.drawCircle(handX + 4f, handY + 4f, size * 0.38f, shadowPaint)

        // Palm circle
        canvas.drawCircle(handX, handY, size * 0.38f, handPaint)

        // Fingers (5 rounded rectangles)
        val fingerW = size * 0.13f
        val fingerH = size * 0.35f
        val offsets = floatArrayOf(-size * 0.22f, -size * 0.1f, size * 0.02f, size * 0.14f, size * 0.25f)
        val heights = floatArrayOf(0.28f, 0.35f, 0.35f, 0.32f, 0.25f)
        val rx = fingerW * 0.5f

        for (i in offsets.indices) {
            val fx = handX + offsets[i]
            val fy = handY - size * 0.3f - size * heights[i]
            val rect = RectF(fx - fingerW / 2, fy, fx + fingerW / 2, handY - size * 0.28f)
            canvas.drawRoundRect(rect, rx, rx, handPaint)
        }

        // Thumb
        val thumbRect = RectF(
            handX - size * 0.42f,
            handY - size * 0.1f,
            handX - size * 0.22f,
            handY + size * 0.1f
        )
        canvas.drawRoundRect(thumbRect, fingerW * 0.5f, fingerW * 0.5f, handPaint)

        // Sweep trail dots
        val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 80
        }
        for (i in 1..4) {
            val tx = handX - i * size * 0.12f
            val ty = handY - i * size * 0.03f
            val tr = size * 0.06f * (1f - i * 0.18f)
            if (tr > 0) canvas.drawCircle(tx, ty, tr, trailPaint)
        }
    }

    fun stopAnimation() {
        animator?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}

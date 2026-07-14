package com.family.scratchapp.ui.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.family.scratchapp.data.models.SurfaceType
import com.family.scratchapp.textures.TextureGenerator
import com.family.scratchapp.textures.TextureType

class ScratchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface ScratchListener {
        fun onProgressChanged(percent: Float)
        fun onThresholdReached()
    }

    var listener: ScratchListener? = null
    private var thresholdFired = false

    private var overlayBitmap: Bitmap? = null
    private var overlayCanvas: Canvas? = null

    private val erasePaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 180f
    }

    private val bitmapPaint = Paint(Paint.DITHER_FLAG)
    private var lastX = 0f
    private var lastY = 0f

    fun setup(surfaceType: SurfaceType) {
        val textureType = surfaceType.toTextureType()
        post { if (width > 0 && height > 0) buildOverlay(textureType) }
    }

    private fun buildOverlay(type: TextureType) {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val texture = TextureGenerator.generate(type, width, height)
        canvas.drawBitmap(texture, 0f, 0f, null)
        texture.recycle()
        overlayBitmap = bmp
        overlayCanvas = Canvas(bmp)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        overlayBitmap?.let { canvas.drawBitmap(it, 0f, 0f, bitmapPaint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (thresholdFired) return false
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { lastX = x; lastY = y; scratch(x, y, x, y) }
            MotionEvent.ACTION_MOVE -> { scratch(lastX, lastY, x, y); lastX = x; lastY = y }
        }
        return true
    }

    private fun scratch(x1: Float, y1: Float, x2: Float, y2: Float) {
        overlayCanvas?.drawLine(x1, y1, x2, y2, erasePaint)
        invalidate()
        checkProgress()
    }

    private fun checkProgress() {
        val bmp = overlayBitmap ?: return
        val sample = sampleTransparentPercent(bmp)
        listener?.onProgressChanged(sample)
        if (!thresholdFired && sample >= 50f) {
            thresholdFired = true
            listener?.onThresholdReached()
        }
    }

    private fun sampleTransparentPercent(bmp: Bitmap): Float {
        val sampleSize = 50
        var transparent = 0
        val stepX = bmp.width / sampleSize
        val stepY = bmp.height / sampleSize
        if (stepX == 0 || stepY == 0) return 0f
        for (sx in 0 until sampleSize)
            for (sy in 0 until sampleSize)
                if (Color.alpha(bmp.getPixel(sx * stepX, sy * stepY)) < 10) transparent++
        return (transparent.toFloat() / (sampleSize * sampleSize)) * 100f
    }

    // Auto-reveal: animate the whole overlay view fading out
    fun autoReveal(durationMs: Long, onComplete: () -> Unit) {
        thresholdFired = true
        // Use Android's built-in ValueAnimator on the View's alpha
        val anim = android.animation.ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 700L
            interpolator = android.view.animation.AccelerateInterpolator(1.5f)
            addUpdateListener { animator ->
                alpha = animator.animatedValue as Float
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    alpha = 1f
                    // Clear the bitmap so it's gone
                    overlayCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    invalidate()
                    onComplete()
                }
            })
        }
        anim.start()
    }

    fun reset(surfaceType: SurfaceType) {
        thresholdFired = false
        buildOverlay(surfaceType.toTextureType())
    }

    private fun SurfaceType.toTextureType() = when (this) {
        SurfaceType.DIRT -> TextureType.DIRT
        SurfaceType.SAND -> TextureType.SAND
        SurfaceType.GRASS -> TextureType.GRASS
        SurfaceType.RANDOM -> TextureType.SAND
    }
}

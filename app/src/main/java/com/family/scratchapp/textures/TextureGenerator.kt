package com.family.scratchapp.textures

import android.graphics.*
import kotlin.math.*
import kotlin.random.Random

object TextureGenerator {

    fun generate(type: TextureType, width: Int, height: Int): Bitmap {
        return when (type) {
            TextureType.DIRT -> generateDirt(width, height)
            TextureType.SAND -> generateSand(width, height)
            TextureType.GRASS -> generateGrass(width, height)
        }
    }

    // ── DIRT ──────────────────────────────────────────────────────────────────
    private fun generateDirt(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val rng = Random(42)

        // Rich dark earth base
        canvas.drawColor(Color.rgb(101, 67, 33))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Deep cracks — dark thin lines
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = Color.rgb(60, 35, 15)
        repeat(30) {
            val x0 = rng.nextFloat() * w
            val y0 = rng.nextFloat() * h
            val len = rng.nextFloat() * 80f + 20f
            val angle = rng.nextFloat() * PI.toFloat()
            paint.strokeWidth = rng.nextFloat() * 1.5f + 0.5f
            paint.alpha = rng.nextInt(100, 200)
            val path = Path()
            path.moveTo(x0, y0)
            // Slightly wobbly crack
            val mx = x0 + cos(angle) * len * 0.5f + (rng.nextFloat()-0.5f) * 12f
            val my = y0 + sin(angle) * len * 0.5f + (rng.nextFloat()-0.5f) * 12f
            val ex = x0 + cos(angle) * len
            val ey = y0 + sin(angle) * len
            path.quadTo(mx, my, ex, ey)
            canvas.drawPath(path, paint)
        }

        // Clods of earth — irregular dark blobs
        paint.style = Paint.Style.FILL
        val clodColors = intArrayOf(
            Color.rgb(80, 50, 20), Color.rgb(90, 60, 25),
            Color.rgb(70, 42, 15), Color.rgb(110, 72, 35)
        )
        repeat(200) {
            paint.color = clodColors[rng.nextInt(clodColors.size)]
            paint.alpha = rng.nextInt(80, 180)
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val rx = rng.nextFloat() * 22f + 5f
            val ry = rng.nextFloat() * 14f + 3f
            val rot = rng.nextFloat() * 360f
            canvas.save()
            canvas.rotate(rot, x, y)
            canvas.drawOval(x-rx, y-ry, x+rx, y+ry, paint)
            canvas.restore()
        }

        // Mid-tone dirt patches
        paint.color = Color.rgb(130, 90, 48)
        repeat(150) {
            paint.alpha = rng.nextInt(40, 100)
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val r = rng.nextFloat() * 30f + 8f
            canvas.drawCircle(x, y, r, paint)
        }

        // Pebbles — varied sizes and grey tones
        val pebbleColors = intArrayOf(
            Color.rgb(140, 130, 120), Color.rgb(160, 150, 135),
            Color.rgb(110, 100, 90),  Color.rgb(180, 165, 145)
        )
        repeat(60) {
            paint.color = pebbleColors[rng.nextInt(pebbleColors.size)]
            paint.alpha = 230
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val rx = rng.nextFloat() * 7f + 2f
            val ry = rx * (0.6f + rng.nextFloat() * 0.5f)
            val rot = rng.nextFloat() * 360f
            canvas.save()
            canvas.rotate(rot, x, y)
            canvas.drawOval(x-rx, y-ry, x+rx, y+ry, paint)
            canvas.restore()
            // Highlight on pebble
            paint.color = Color.rgb(200, 190, 175)
            paint.alpha = 100
            canvas.drawCircle(x - rx*0.2f, y - ry*0.3f, rx*0.3f, paint)
        }

        // Fine dirt particles
        paint.color = Color.rgb(155, 110, 60)
        repeat(800) {
            paint.alpha = rng.nextInt(20, 70)
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            canvas.drawCircle(x, y, rng.nextFloat() * 1.5f + 0.3f, paint)
        }

        // Moisture-dark spots
        paint.color = Color.rgb(55, 32, 10)
        repeat(40) {
            paint.alpha = rng.nextInt(30, 80)
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            canvas.drawCircle(x, y, rng.nextFloat() * 18f + 5f, paint)
        }

        // Root fibers — thin brown lines
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.8f
        paint.color = Color.rgb(85, 55, 20)
        repeat(25) {
            val x0 = rng.nextFloat() * w
            val y0 = rng.nextFloat() * h
            paint.alpha = rng.nextInt(60, 130)
            val path = Path()
            path.moveTo(x0, y0)
            var cx = x0; var cy = y0
            repeat(4) {
                cx += (rng.nextFloat() - 0.5f) * 30f
                cy += (rng.nextFloat() - 0.5f) * 30f
                path.lineTo(cx, cy)
            }
            canvas.drawPath(path, paint)
        }

        return bmp
    }

    // ── SAND ──────────────────────────────────────────────────────────────────
    private fun generateSand(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val rng = Random(99)

        // Warm beach sand base
        canvas.drawColor(Color.rgb(210, 180, 130))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Large dune-shadow gradient patches
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(185, 155, 105)
        repeat(20) {
            paint.alpha = rng.nextInt(40, 90)
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val rx = rng.nextFloat() * 120f + 40f
            val ry = rx * (0.3f + rng.nextFloat() * 0.3f)
            val rot = rng.nextFloat() * 180f
            canvas.save()
            canvas.rotate(rot, x, y)
            canvas.drawOval(x-rx, y-ry, x+rx, y+ry, paint)
            canvas.restore()
        }

        // Wind ripples — parallel wavy lines
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val rippleSpacing = h / 28f
        for (i in 0..28) {
            val baseY = i * rippleSpacing
            val amplitude = rng.nextFloat() * 4f + 1f
            val freq = 0.008f + rng.nextFloat() * 0.006f
            val phase = rng.nextFloat() * (2 * PI).toFloat()
            paint.strokeWidth = rng.nextFloat() * 1.2f + 0.4f
            paint.color = if (rng.nextBoolean()) Color.rgb(185, 155, 100) else Color.rgb(235, 210, 165)
            paint.alpha = rng.nextInt(50, 130)
            val path = Path()
            path.moveTo(0f, baseY)
            for (x in 0 until w step 3) {
                val dy = amplitude * sin(freq * x + phase).toFloat()
                path.lineTo(x.toFloat(), baseY + dy)
            }
            canvas.drawPath(path, paint)
        }

        // Individual grains — varying warm tones
        paint.style = Paint.Style.FILL
        val grainColors = intArrayOf(
            Color.rgb(200, 170, 115), Color.rgb(220, 190, 140),
            Color.rgb(175, 145, 90),  Color.rgb(240, 215, 170),
            Color.rgb(160, 130, 80),  Color.rgb(255, 230, 185)
        )
        repeat(3000) {
            paint.color = grainColors[rng.nextInt(grainColors.size)]
            paint.alpha = rng.nextInt(60, 180)
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val r = rng.nextFloat() * 1.8f + 0.3f
            canvas.drawCircle(x, y, r, paint)
        }

        // Slightly darker grain clusters (wet patches)
        paint.color = Color.rgb(170, 140, 90)
        repeat(15) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            val radius = rng.nextFloat() * 35f + 10f
            repeat(80) {
                val angle = rng.nextFloat() * 2 * PI.toFloat()
                val dist = rng.nextFloat() * radius
                val gx = cx + cos(angle) * dist
                val gy = cy + sin(angle) * dist
                paint.alpha = rng.nextInt(40, 100)
                canvas.drawCircle(gx, gy, rng.nextFloat() * 1.5f + 0.5f, paint)
            }
        }

        // Shell fragments — tiny white curved shapes
        paint.color = Color.rgb(250, 245, 235)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        repeat(12) {
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            val r = rng.nextFloat() * 6f + 3f
            paint.alpha = rng.nextInt(140, 220)
            canvas.drawArc(x-r, y-r*0.6f, x+r, y+r*0.6f, -30f, 200f, false, paint)
        }

        // Bright highlights — light catches grain tops
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 248, 220)
        repeat(500) {
            paint.alpha = rng.nextInt(15, 50)
            canvas.drawCircle(
                rng.nextFloat() * w, rng.nextFloat() * h,
                rng.nextFloat() * 1.2f + 0.2f, paint
            )
        }

        return bmp
    }

    // ── GRASS ─────────────────────────────────────────────────────────────────
    private fun generateGrass(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val rng = Random(77)

        // Rich dark green base (soil showing through)
        canvas.drawColor(Color.rgb(45, 90, 35))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Ground color variation patches
        paint.style = Paint.Style.FILL
        val groundColors = intArrayOf(
            Color.rgb(35, 75, 25), Color.rgb(55, 100, 40),
            Color.rgb(40, 85, 30), Color.rgb(65, 110, 50)
        )
        repeat(80) {
            paint.color = groundColors[rng.nextInt(groundColors.size)]
            paint.alpha = rng.nextInt(60, 140)
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            canvas.drawCircle(x, y, rng.nextFloat() * 35f + 10f, paint)
        }

        // Grass blades — draw in layers back to front
        // Layer 1: dark background blades
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        val darkBladeColors = intArrayOf(
            Color.rgb(35, 85, 30), Color.rgb(45, 95, 38),
            Color.rgb(30, 75, 25), Color.rgb(50, 100, 42)
        )
        repeat(600) {
            val baseX = rng.nextFloat() * w
            val baseY = rng.nextFloat() * h + 5f
            val bladeH = rng.nextFloat() * 28f + 12f
            val lean = (rng.nextFloat() - 0.5f) * 20f
            val curve = (rng.nextFloat() - 0.5f) * 10f
            paint.color = darkBladeColors[rng.nextInt(darkBladeColors.size)]
            paint.strokeWidth = rng.nextFloat() * 1.8f + 0.8f
            paint.alpha = rng.nextInt(150, 255)
            val path = Path()
            path.moveTo(baseX, baseY)
            path.cubicTo(
                baseX + lean * 0.3f + curve, baseY - bladeH * 0.4f,
                baseX + lean * 0.7f + curve, baseY - bladeH * 0.7f,
                baseX + lean, baseY - bladeH
            )
            canvas.drawPath(path, paint)
        }

        // Layer 2: mid-tone blades
        val midBladeColors = intArrayOf(
            Color.rgb(70, 140, 55), Color.rgb(85, 155, 65),
            Color.rgb(60, 125, 48), Color.rgb(95, 165, 72)
        )
        repeat(800) {
            val baseX = rng.nextFloat() * w
            val baseY = rng.nextFloat() * h + 3f
            val bladeH = rng.nextFloat() * 24f + 10f
            val lean = (rng.nextFloat() - 0.5f) * 18f
            val curve = (rng.nextFloat() - 0.5f) * 8f
            paint.color = midBladeColors[rng.nextInt(midBladeColors.size)]
            paint.strokeWidth = rng.nextFloat() * 1.5f + 0.6f
            paint.alpha = rng.nextInt(160, 255)
            val path = Path()
            path.moveTo(baseX, baseY)
            path.cubicTo(
                baseX + lean * 0.3f + curve, baseY - bladeH * 0.4f,
                baseX + lean * 0.6f + curve, baseY - bladeH * 0.7f,
                baseX + lean, baseY - bladeH
            )
            canvas.drawPath(path, paint)
        }

        // Layer 3: bright highlight blades (light catching tips)
        val brightBladeColors = intArrayOf(
            Color.rgb(120, 195, 85), Color.rgb(140, 210, 95),
            Color.rgb(105, 180, 75), Color.rgb(155, 220, 105)
        )
        repeat(400) {
            val baseX = rng.nextFloat() * w
            val baseY = rng.nextFloat() * h
            val bladeH = rng.nextFloat() * 18f + 8f
            val lean = (rng.nextFloat() - 0.5f) * 14f
            paint.color = brightBladeColors[rng.nextInt(brightBladeColors.size)]
            paint.strokeWidth = rng.nextFloat() * 1.2f + 0.4f
            paint.alpha = rng.nextInt(120, 210)
            val path = Path()
            path.moveTo(baseX, baseY)
            path.quadTo(
                baseX + lean * 0.5f, baseY - bladeH * 0.6f,
                baseX + lean, baseY - bladeH
            )
            canvas.drawPath(path, paint)
        }

        // Tiny dew drops on some blades
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(200, 235, 255)
        repeat(30) {
            val x = rng.nextFloat() * w
            val y = rng.nextFloat() * h
            paint.alpha = rng.nextInt(80, 160)
            canvas.drawCircle(x, y, rng.nextFloat() * 2.5f + 1f, paint)
        }

        // Small yellow wildflowers
        paint.color = Color.rgb(255, 220, 50)
        repeat(8) {
            val cx = rng.nextFloat() * w
            val cy = rng.nextFloat() * h
            paint.alpha = 220
            for (p in 0..4) {
                val angle = p * (2 * PI / 5).toFloat()
                val px = cx + cos(angle) * 4f
                val py = cy + sin(angle) * 4f
                canvas.drawCircle(px, py, 2.5f, paint)
            }
            paint.color = Color.rgb(255, 170, 30)
            canvas.drawCircle(cx, cy, 2f, paint)
            paint.color = Color.rgb(255, 220, 50)
        }

        return bmp
    }
}

enum class TextureType { DIRT, SAND, GRASS }

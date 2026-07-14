package com.family.scratchapp.ui.game

import android.animation.*
import android.content.Context
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.*
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.family.scratchapp.R
import com.family.scratchapp.audio.ScratchAudioManager
import com.family.scratchapp.data.SettingsPreferences
import com.family.scratchapp.data.db.AppDatabase
import com.family.scratchapp.data.models.*
import com.family.scratchapp.databinding.ActivityGameBinding
import com.family.scratchapp.ui.celebration.ConfettiView
import kotlinx.coroutines.*

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private lateinit var prefs: SettingsPreferences
    private lateinit var audioManager: ScratchAudioManager
    private lateinit var soundPool: SoundPool

    private var scratchSoundId = 0
    private var celebrationSoundId = 0
    private var soundPoolReady = false

    private var photos: List<String> = emptyList()
    private var photoQueue: MutableList<String> = mutableListOf()
    private var currentPhotoIndex = 0
    private lateinit var settings: GameSettings

    private var lastSurfaceIndex = -1

    private fun getNextSurface(): SurfaceType {
        return if (settings.surfaceType == SurfaceType.RANDOM) {
            val options = listOf(SurfaceType.DIRT, SurfaceType.SAND, SurfaceType.GRASS)
            lastSurfaceIndex = (lastSurfaceIndex + 1) % options.size
            options[lastSurfaceIndex]
        } else {
            settings.surfaceType
        }
    }
    private var lockHandler = Handler(Looper.getMainLooper())
    private var lockRunnable: Runnable? = null
    private var lockProgress = 0
    private var lockAnimator: ValueAnimator? = null

    // Auto-reveal timer
    private var autoRevealHandler = Handler(Looper.getMainLooper())
    private var autoRevealRunnable: Runnable? = null

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = SettingsPreferences(this)
        settings = prefs.getGameSettings()
        audioManager = ScratchAudioManager(this)

        setupFullscreen()
        startScreenPin()
        setupSoundPool()
        setupScratchListener()
        setupLockButton()
        loadPhotos()

        audioManager.startMusic(settings.musicTrack)
    }

    private fun setupFullscreen() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }

    private fun startScreenPin() {
        try { startLockTask() } catch (e: Exception) { /* continue without pinning */ }
    }

    private fun setupSoundPool() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attrs)
            .build()
        soundPool.setOnLoadCompleteListener { _, _, _ -> soundPoolReady = true }
        scratchSoundId = soundPool.load(this, R.raw.sound_scratch, 1)
        celebrationSoundId = soundPool.load(this, R.raw.sound_celebration, 1)
    }

    private fun loadPhotos() {
        scope.launch {
            val db = AppDatabase.getInstance(this@GameActivity)
            val activePhotos = withContext(Dispatchers.IO) {
                db.photoDao().getActivePhotos().map { it.filePath }
            }
            if (activePhotos.isEmpty()) { finish(); return@launch }
            photos = activePhotos
            buildQueue()
            showNextPhoto()
        }
    }

    private fun buildQueue() {
        photoQueue = if (settings.photoOrder == PhotoOrder.RANDOM)
            photos.toMutableList().also { it.shuffle() }
        else
            photos.toMutableList()
        currentPhotoIndex = 0
    }

    private fun showNextPhoto() {
        if (currentPhotoIndex >= photoQueue.size) buildQueue()
        val path = photoQueue[currentPhotoIndex++]

        val bitmap = BitmapFactory.decodeFile(path)
        binding.ivPhoto.setImageBitmap(bitmap)
        binding.ivPhoto.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        binding.ivPhoto.alpha = 1f
        binding.ivPhoto.scaleX = 1f
        binding.ivPhoto.scaleY = 1f

        binding.scratchView.reset(getNextSurface())
        binding.handHint.isVisible = true
        binding.handHint.alpha = 1f
        hideCelebration()
        scheduleAutoReveal()
    }

    private fun setupScratchListener() {
        binding.scratchView.listener = object : ScratchView.ScratchListener {
            override fun onProgressChanged(percent: Float) {
                if (percent > 2f && binding.handHint.isVisible) {
                    binding.handHint.animate().alpha(0f).setDuration(300)
                        .withEndAction { binding.handHint.isVisible = false }.start()
                }
                // Play scratch sound on movement
                if (percent > 1f && soundPoolReady) {
                    soundPool.play(scratchSoundId, 0.6f, 0.6f, 1, 0, 1.0f)
                }
            }
            override fun onThresholdReached() {
                cancelAutoReveal()
                val revealMs = (settings.autoRevealTime.seconds * 1000L * 0.3f).toLong().coerceIn(1500L, 3500L)
                binding.scratchView.autoReveal(revealMs) { triggerCelebration() }
            }
        }
    }

    private fun scheduleAutoReveal() {
        cancelAutoReveal()
        autoRevealRunnable = Runnable {
            binding.handHint.animate().alpha(0f).setDuration(300)
                .withEndAction { binding.handHint.isVisible = false }.start()
            binding.scratchView.autoReveal(3000L) { triggerCelebration() }
        }.also { autoRevealHandler.postDelayed(it, settings.autoRevealTime.seconds * 1000L) }
    }

    private fun cancelAutoReveal() {
        autoRevealRunnable?.let { autoRevealHandler.removeCallbacks(it) }
        autoRevealRunnable = null
    }

    private fun triggerCelebration() {
        // 1. Fade out photo briefly then zoom in
        binding.ivPhoto.animate()
            .alpha(0f).setDuration(180)
            .withEndAction {
                binding.ivPhoto.animate()
                    .alpha(1f).scaleX(1.15f).scaleY(1.15f)
                    .setDuration(400)
                    .setInterpolator(OvershootInterpolator(1.5f))
                    .start()
            }.start()

        // 2. Show confetti
        binding.confettiView.isVisible = true
        binding.confettiView.burst()

        // 3. Show celebration banner at top with bounce
        binding.celebrationLayout.isVisible = true
        binding.celebrationLayout.scaleX = 0f
        binding.celebrationLayout.scaleY = 0f
        binding.celebrationLayout.animate()
            .scaleX(1f).scaleY(1f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator(2f))
            .start()

        // 4. Animate star icon
        animateCelebrationStar()

        // 5. Vibrate
        vibrate()

        // 6. Play celebration sound
        if (soundPoolReady) soundPool.play(celebrationSoundId, 1f, 1f, 1, 0, 1f)

        // 7. After 5s advance
        Handler(Looper.getMainLooper()).postDelayed({
            binding.ivPhoto.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            showNextPhoto()
        }, 5000L)
    }

    private fun animateCelebrationStar() {
        val star = binding.tvCelebrationIcon
        val spin = ObjectAnimator.ofFloat(star, "rotation", 0f, 360f).apply {
            duration = 600
            repeatCount = 4
            interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
            star,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.4f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.4f, 1f)
        ).apply {
            duration = 600
            repeatCount = 4
        }
        AnimatorSet().apply {
            playTogether(spin, scaleUp)
            start()
        }
    }

    private fun vibrate() {
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 80, 60, 150), -1))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(longArrayOf(0, 80, 60, 80, 60, 150), -1)
        }
    }

    private fun hideCelebration() {
        binding.celebrationLayout.isVisible = false
        binding.confettiView.isVisible = false
        binding.confettiView.stopAndHide()
    }

    // Lock button: hold 3 seconds to exit
    private fun setupLockButton() {
        binding.btnLock.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startLockCountdown()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> cancelLockCountdown()
            }
            true
        }
        binding.lockProgress.progress = 0
    }

    private fun startLockCountdown() {
        cancelLockCountdown()
        binding.lockProgress.progress = 0
        lockAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = 3000L
            addUpdateListener { binding.lockProgress.progress = it.animatedValue as Int }
            start()
        }
        lockRunnable = Runnable {
            try { stopLockTask() } catch (e: Exception) { }
            audioManager.release()
            finish()
        }.also { lockHandler.postDelayed(it, 3000L) }
    }

    private fun cancelLockCountdown() {
        lockRunnable?.let { lockHandler.removeCallbacks(it) }
        lockRunnable = null
        lockAnimator?.cancel()
        lockAnimator = null
        binding.lockProgress.animate().alpha(0f).setDuration(200).withEndAction {
            binding.lockProgress.progress = 0
            binding.lockProgress.alpha = 1f
        }.start()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) setupFullscreen()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { /* blocked */ }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        audioManager.release()
        soundPool.release()
        cancelAutoReveal()
        cancelLockCountdown()
    }
}

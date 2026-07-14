package com.family.scratchapp.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.family.scratchapp.R
import com.family.scratchapp.data.SettingsPreferences
import com.family.scratchapp.data.db.AppDatabase
import com.family.scratchapp.data.models.*
import com.family.scratchapp.databinding.ActivitySettingsBinding
import com.family.scratchapp.ui.game.GameActivity
import kotlinx.coroutines.*
import java.io.*
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SettingsPreferences
    private lateinit var photoAdapter: PhotoManagerAdapter
    private lateinit var db: AppDatabase

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> uris?.forEach { importPhoto(it) } }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pickImageLauncher.launch("image/*")
        else Toast.makeText(this, "Se necesita permiso para acceder a las fotos", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivitySettingsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            prefs = SettingsPreferences(this)

            // Init DB off main thread
            lifecycleScope.launch {
                db = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(this@SettingsActivity)
                }
                setupPhotoGrid()
                setupControls()
                loadSettings()
            }

            binding.btnAddPhotos.setOnClickListener { requestPhotos() }
            binding.btnStartGame.setOnClickListener { startGame() }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al iniciar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupPhotoGrid() {
        photoAdapter = PhotoManagerAdapter(
            onToggleActive = { photo ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        db.photoDao().setActive(photo.id, !photo.isActive)
                    } catch (e: Exception) { /* ignore */ }
                }
            },
            onDelete = { photo ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        db.photoDao().delete(photo)
                        File(photo.filePath).delete()
                    } catch (e: Exception) { /* ignore */ }
                }
            }
        )
        binding.rvPhotos.layoutManager = GridLayoutManager(this, 3)
        binding.rvPhotos.adapter = photoAdapter

        db.photoDao().getAllPhotos().observe(this) { photos ->
            photoAdapter.submitList(photos)
            val active = photos.count { it.isActive }
            binding.tvPhotoCount.text = "$active fotos activas de ${photos.size}"
        }
    }

    private fun setupControls() {
        binding.togglePhotoOrder.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                prefs.photoOrder = if (checkedId == R.id.btnRandom) PhotoOrder.RANDOM else PhotoOrder.SEQUENTIAL
            }
        }

        binding.toggleRevealTime.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                prefs.autoRevealTime = when (checkedId) {
                    R.id.btn30s -> AutoRevealTime.THIRTY
                    R.id.btn60s -> AutoRevealTime.SIXTY
                    else -> AutoRevealTime.NINETY
                }
            }
        }

        binding.toggleSurface.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                prefs.surfaceType = when (checkedId) {
                    R.id.btnDirt -> SurfaceType.DIRT
                    R.id.btnSand -> SurfaceType.SAND
                    R.id.btnGrass -> SurfaceType.GRASS
                    else -> SurfaceType.RANDOM
                }
            }
        }

        binding.toggleMusic.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                prefs.musicTrack = when (checkedId) {
                    R.id.btnNoMusic -> MusicTrack.NONE
                    R.id.btnMusic1 -> MusicTrack.TRACK_1
                    R.id.btnMusic2 -> MusicTrack.TRACK_2
                    else -> MusicTrack.TRACK_3
                }
            }
        }
    }

    private fun loadSettings() {
        val s = prefs.getGameSettings()

        binding.togglePhotoOrder.check(
            if (s.photoOrder == PhotoOrder.RANDOM) R.id.btnRandom else R.id.btnSequential
        )
        binding.toggleRevealTime.check(
            when (s.autoRevealTime) {
                AutoRevealTime.THIRTY -> R.id.btn30s
                AutoRevealTime.SIXTY -> R.id.btn60s
                AutoRevealTime.NINETY -> R.id.btn90s
            }
        )
        binding.toggleSurface.check(
            when (s.surfaceType) {
                SurfaceType.DIRT -> R.id.btnDirt
                SurfaceType.SAND -> R.id.btnSand
                SurfaceType.GRASS -> R.id.btnGrass
                SurfaceType.RANDOM -> R.id.btnSurfaceRandom
            }
        )
        binding.toggleMusic.check(
            when (s.musicTrack) {
                MusicTrack.NONE -> R.id.btnNoMusic
                MusicTrack.TRACK_1 -> R.id.btnMusic1
                MusicTrack.TRACK_2 -> R.id.btnMusic2
                MusicTrack.TRACK_3 -> R.id.btnMusic3
            }
        )
    }

    private fun requestPhotos() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED ->
                pickImageLauncher.launch("image/*")
            else ->
                permissionLauncher.launch(permission)
        }
    }

    private fun importPhoto(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val stream = contentResolver.openInputStream(uri) ?: return@launch
                val original = BitmapFactory.decodeStream(stream)
                stream.close()

                if (original == null) return@launch

                val dm = resources.displayMetrics
                val targetW = dm.widthPixels.coerceAtLeast(1080)
                val targetH = dm.heightPixels.coerceAtLeast(1920)

                val scaled = scaleCrop(original, targetW, targetH)
                original.recycle()

                val dir = File(filesDir, "photos").also { it.mkdirs() }
                val file = File(dir, "${UUID.randomUUID()}.jpg")
                FileOutputStream(file).use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                scaled.recycle()

                val originalName = uri.lastPathSegment ?: "photo"
                db.photoDao().insert(Photo(filePath = file.absolutePath, originalName = originalName))

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Foto agregada ✓", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Error al importar foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun scaleCrop(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val srcRatio = src.width.toFloat() / src.height.toFloat()
        val dstRatio = targetW.toFloat() / targetH.toFloat()

        val scaledW: Int
        val scaledH: Int
        if (srcRatio > dstRatio) {
            scaledH = targetH
            scaledW = (srcRatio * targetH).toInt()
        } else {
            scaledW = targetW
            scaledH = (targetW / srcRatio).toInt()
        }

        val scaled = Bitmap.createScaledBitmap(src, scaledW.coerceAtLeast(1), scaledH.coerceAtLeast(1), true)
        val x = ((scaledW - targetW) / 2).coerceAtLeast(0)
        val y = ((scaledH - targetH) / 2).coerceAtLeast(0)
        val safeW = (targetW).coerceAtMost(scaled.width - x)
        val safeH = (targetH).coerceAtMost(scaled.height - y)
        val cropped = Bitmap.createBitmap(scaled, x, y, safeW, safeH)
        if (cropped != scaled) scaled.recycle()
        return cropped
    }

    private fun startGame() {
        lifecycleScope.launch {
            try {
                val active = withContext(Dispatchers.IO) {
                    db.photoDao().getActivePhotos()
                }
                if (active.isEmpty()) {
                    Toast.makeText(this@SettingsActivity, "Agregá al menos una foto para jugar", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                startActivity(Intent(this@SettingsActivity, GameActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al iniciar el juego", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

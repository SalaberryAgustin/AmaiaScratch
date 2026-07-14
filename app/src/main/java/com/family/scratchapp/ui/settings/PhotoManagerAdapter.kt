package com.family.scratchapp.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.family.scratchapp.R
import com.family.scratchapp.data.models.Photo
import com.family.scratchapp.databinding.ItemPhotoBinding
import java.io.File

class PhotoManagerAdapter(
    private val onToggleActive: (Photo) -> Unit,
    private val onDelete: (Photo) -> Unit
) : ListAdapter<Photo, PhotoManagerAdapter.PhotoViewHolder>(DIFF) {

    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: Photo) {
            binding.ivThumb.load(File(photo.filePath)) {
                crossfade(true)
                placeholder(R.drawable.placeholder_photo)
            }

            // Active/inactive overlay
            binding.overlayInactive.alpha = if (photo.isActive) 0f else 0.55f
            binding.ivCheck.alpha = if (photo.isActive) 1f else 0.3f

            binding.root.setOnClickListener { onToggleActive(photo) }
            binding.btnDelete.setOnClickListener { onDelete(photo) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PhotoViewHolder(
            ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(a: Photo, b: Photo) = a.id == b.id
            override fun areContentsTheSame(a: Photo, b: Photo) = a == b
        }
    }
}

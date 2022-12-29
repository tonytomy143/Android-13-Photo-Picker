package com.example.android13photopicker

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android13photopicker.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pickSingleMediaLauncher: ActivityResultLauncher<Intent>
    private var player: ExoPlayer? = null

    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView.visibility = View.GONE
        binding.videoPlayer.visibility = View.GONE

        releasePlayer()
        initializeMediaPicker()
        initButtonClickEvents()
    }

    private fun initializeMediaPicker() {
        // Initialize single media picker launcher
        pickSingleMediaLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "Failed picking media.", Toast.LENGTH_SHORT).show()
                } else {
                    val uri = it.data?.data

                    val cR: ContentResolver = contentResolver
                    val type = uri?.let { it1 -> cR.getType(it1) }
                    Log.d("MainActivity", "file type $type")

                    binding.imageView.visibility = View.GONE
                    binding.videoPlayer.visibility = View.GONE
                    binding.tvHint.visibility = View.GONE
                    when {
                        type?.startsWith("image") == true -> {
                            binding.imageView.visibility = View.VISIBLE
                            binding.imageView.setImageURI(uri)
                        }
                        type?.startsWith("video") == true -> {
                            binding.videoPlayer.visibility = View.VISIBLE
                            initializePlayer(uri)
                        }
                        else                              -> {
                            Toast.makeText(this, "Invalid file type", Toast.LENGTH_SHORT).show()
                            binding.tvHint.visibility = View.VISIBLE
                        }
                    }
                }
            }
    }

    @RequiresApi(33)
    private fun initButtonClickEvents() {
        // Setup pick image/video
        binding.buttonPickPhotoVideo.setOnClickListener { pickMedia("*") }

        // Setup pick image
        binding.buttonPickPhoto.setOnClickListener { pickMedia("image") }

        // Setup pick video
        binding.buttonPickVideo.setOnClickListener { pickMedia("video") }
    }

    @RequiresApi(33)
    private fun pickMedia(mime_type: String) = pickSingleMediaLauncher.launch(
        if (PhotoPickerAvailabilityChecker.isPhotoPickerAvailable()) {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = "$mime_type/*"
            }
        } else {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "$mime_type/*"
            }
        }
    )

    private fun initializePlayer(uri: Uri?) {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.videoPlayer.player = exoPlayer
                val mediaItem = uri?.let { MediaItem.fromUri(it) }
                mediaItem?.let {
                    exoPlayer.setMediaItem(it)
                    exoPlayer.playWhenReady = playWhenReady
                    exoPlayer.seekTo(currentItem, playbackPosition)
                    exoPlayer.prepare()
                }
            }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

}

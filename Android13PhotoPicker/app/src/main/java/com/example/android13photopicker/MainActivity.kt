package com.example.android13photopicker

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android13photopicker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pickSingleMediaLauncher: ActivityResultLauncher<Intent>

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.imageView.visibility = View.GONE
        binding.videoPlayer.visibility = View.GONE

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoPlayer);
        binding.videoPlayer.setMediaController(mediaController);

        // Initialize single media picker launcher
        pickSingleMediaLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "Failed picking media.", Toast.LENGTH_SHORT).show()
                } else {
                    val uri = it.data?.data
//                    showSnackBar("SUCCESS: ${uri?.path}")

                    val cR: ContentResolver = contentResolver
                    val type = uri?.let { it1 -> cR.getType(it1) }

                    type?.startsWith("image").let { isTrue ->
                        binding.imageView.visibility = View.GONE
                        binding.videoPlayer.visibility = View.GONE
                        binding.tvHint.visibility = View.GONE
                        if (isTrue!!) {
                            binding.imageView.visibility = View.VISIBLE
                            binding.imageView.setImageURI(uri)
                        } else {
                            binding.videoPlayer.visibility = View.VISIBLE
                            binding.videoPlayer.setVideoURI(uri)
                            binding.videoPlayer.start()
                        }
                    }
                }
            }

        initClickEvents()
    }


    @RequiresApi(33)
    fun initClickEvents() {
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

}

package com.example.android13photopicker

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android13photopicker.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pickSingleMediaLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickMultipleMediaLauncher: ActivityResultLauncher<Intent>
    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        exoPlayer = ExoPlayer.Builder(this).build()
        binding.exoPlayer.player = exoPlayer

        // Initialize single media picker launcher
        pickSingleMediaLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "Failed picking media.", Toast.LENGTH_SHORT).show()
                } else {
                    val uri = it.data?.data
                    showSnackBar("SUCCESS: ${uri?.path}")

                    binding.ivImage.setImageURI(uri)
                }
            }

        // Initialize multiple media picker launcher
        pickMultipleMediaLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, "Failed picking media.", Toast.LENGTH_SHORT).show()
                } else {
                    val uris = it.data?.clipData ?: return@registerForActivityResult
                    var uriPaths = ""
                    for (index in 0 until uris.itemCount) {
                        uriPaths += uris.getItemAt(index).uri.path
                        uriPaths += "\n"
                    }
                    showSnackBar("SUCCESS: $uriPaths")
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Setup pick 1 image/video
            binding.buttonPickPhotoVideo.setOnClickListener {
                pickSingleMediaLauncher.launch(
                    Intent(MediaStore.ACTION_PICK_IMAGES)
                )
            }
            // Setup pick image
            binding.buttonPickPhoto.setOnClickListener {
                pickSingleMediaLauncher.launch(
                    Intent(MediaStore.ACTION_PICK_IMAGES)
                        .apply {
                            type = "image/*"
                        }
                )
            }
            // Setup pick video
            binding.buttonPickVideo.setOnClickListener {
                pickSingleMediaLauncher.launch(
                    Intent(MediaStore.ACTION_PICK_IMAGES)
                        .apply {
                            type = "video/*"
                        }
                )
            }
        } else {
            Toast.makeText(this, "Please use Android 13 device.", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Shows [message] in a [Snackbar].
     */
    private fun showSnackBar(message: String) {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        // Set the max lines of SnackBar
        snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines =
            10
        snackBar.show()
    }

}

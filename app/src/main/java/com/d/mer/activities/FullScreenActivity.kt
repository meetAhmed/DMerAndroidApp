package com.d.mer.activities

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.d.mer.R
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso

class FullScreenActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_full_screen)

        supportActionBar?.hide()

        val url = intent.getStringExtra("url") ?: ""

        if (url.trim().isNotEmpty()) {
            val photoView: PhotoView = findViewById(R.id.photoView)
            Picasso.get().load(url)
                .error(R.drawable.placeholder).into(photoView)
        }

    }

}
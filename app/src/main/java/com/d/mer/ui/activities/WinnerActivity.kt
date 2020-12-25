package com.d.mer.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.d.mer.R
import com.d.mer.data.models.ImageModel
import com.d.mer.ui.viewModels.WinnerActivityViewModel
import com.squareup.picasso.Picasso

class WinnerActivity : BaseActivity() {

    private val viewModel: WinnerActivityViewModel by lazy {
        ViewModelProvider(this).get(WinnerActivityViewModel::class.java)
    }

    private var nodeAddress = ""

    private lateinit var imageView: ImageView
    private lateinit var winnerView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_winner)

        supportActionBar?.title = getString(R.string.winner)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nodeAddress = intent.getStringExtra("nodeAddress") ?: ""

        imageView = findViewById(R.id.image)
        winnerView = findViewById(R.id.winnerView)

        viewModel.getImageData(nodeAddress).observe(this, {
            getModel(it, ImageModel::class.java)?.let { imageModel ->

                Picasso.get().load(imageModel.image_url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder).into(imageView)

                winnerView.text = getString(R.string.winner_text, imageModel.winner)

            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
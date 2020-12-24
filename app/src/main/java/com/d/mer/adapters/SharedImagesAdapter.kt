package com.d.mer.adapters

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.d.mer.R
import com.d.mer.activities.FullScreenActivity
import com.d.mer.dataModels.CategoryModel
import com.d.mer.dataModels.ImageModel
import com.d.mer.dataModels.SharedImageModel
import com.d.mer.interfaces.ImagesClickListener
import com.squareup.picasso.Picasso


class SharedImagesAdapter(
    private var list: ArrayList<SharedImageModel>,
    private val imagesCountInSingleRow: Int
) : RecyclerView.Adapter<SharedImagesAdapter.ViewHolder>() {

    private var context: Context? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_row_shared_image, null)
        context = view.context
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drawable: Drawable? = getPlaceHolderImage()

        if (drawable != null) {
            Picasso.get().load(list[position].imageUrl)
                .placeholder(drawable)
                .resize(
                    Resources.getSystem().displayMetrics.widthPixels / imagesCountInSingleRow,
                    Resources.getSystem().displayMetrics.widthPixels / imagesCountInSingleRow
                ).error(drawable).centerCrop().into(holder.image)
        } else {
            Picasso.get().load(list[position].imageUrl)
                .placeholder(R.drawable.placeholder)
                .resize(
                    Resources.getSystem().displayMetrics.widthPixels / imagesCountInSingleRow,
                    Resources.getSystem().displayMetrics.widthPixels / imagesCountInSingleRow
                ).error(R.drawable.placeholder).centerCrop().into(holder.image)
        }

        holder.image.setOnClickListener {
            context?.let {
                val intent = Intent(it, FullScreenActivity::class.java)
                intent.putExtra("url", list[position].imageUrl)
                it.startActivity(intent)
            }
        }

    }

    private fun getPlaceHolderImage(): Drawable? {
        context?.let {
            val bitmap = BitmapFactory.decodeResource(it.resources, R.drawable.placeholder)
            val image = Bitmap.createScaledBitmap(
                bitmap,
                (Resources.getSystem().displayMetrics.widthPixels / imagesCountInSingleRow),
                (Resources.getSystem().displayMetrics.widthPixels / imagesCountInSingleRow),
                false
            )
            return RoundedBitmapDrawableFactory.create(it.resources, image)
        }
        return null
    }
}
package com.d.mer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.d.mer.R
import com.d.mer.activities.FullScreenActivity
import com.d.mer.common.Store
import com.d.mer.dataModels.CategoryModel
import com.d.mer.dataModels.ImageModel
import com.d.mer.interfaces.ImagesClickListener
import com.squareup.picasso.Picasso


class ImagesAdapter(
    private var list: ArrayList<ImageModel>,
    private val listener: ImagesClickListener
) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

    private var context: Context? = null
    private val originalList: ArrayList<ImageModel> = list
    var currentTime: Long = 0L

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.image)
        var finishView: TextView = view.findViewById(R.id.finishView)
        var shareView: TextView = view.findViewById(R.id.shareView)
        var timerView: TextView = view.findViewById(R.id.timerView)
        var timerEndedView: TextView = view.findViewById(R.id.timerEndedView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_row_image, null)
        view.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        context = view.context
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Picasso.get().load(list[position].image_url)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder).into(holder.image)

        holder.image.setOnClickListener {
            context?.let {
                val intent = Intent(it, FullScreenActivity::class.java)
                intent.putExtra("url", list[position].image_url)
                it.startActivity(intent)
            }
        }

        holder.shareView.setOnClickListener {
            listener.shareImage(list[position])
        }

        holder.finishView.setOnClickListener {
            holder.finishView.visibility = View.INVISIBLE
            holder.timerEndedView.visibility = View.VISIBLE
            holder.timerView.text = ""
            listener.endTimer(list[position], position)
        }

        if (list[position].winner.trim().isNotEmpty()) {
            holder.finishView.visibility = View.INVISIBLE
            holder.timerEndedView.visibility = View.VISIBLE
            context?.let {
                holder.timerView.text = it.getString(R.string.result, list[position].winner)
            }
        } else {
            holder.timerView.text = Store.getTextDate(currentTime, list[position].timer)
            holder.finishView.visibility = View.VISIBLE
            holder.timerEndedView.visibility = View.GONE
        }

    }

    fun applyFilter(listOfSelectedCategories: List<CategoryModel>) {
        if (listOfSelectedCategories.isEmpty()) {
            list = originalList
            notifyDataSetChanged()
        } else {
            val tempList: ArrayList<ImageModel> = ArrayList()
            for (imageModel in originalList) {
                imageModel.categories?.let { imageCategories ->
                    for (i in imageCategories.indices) {
                        if (CategoryModel.isPresent(listOfSelectedCategories, imageCategories[i])) {
                            tempList.add(imageModel)
                            break
                        }
                    }
                }
            }
            list = tempList
            notifyDataSetChanged()
        }
    }
}
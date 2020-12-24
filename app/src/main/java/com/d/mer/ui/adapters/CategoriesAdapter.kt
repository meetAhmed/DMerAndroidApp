package com.d.mer.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.d.mer.R
import com.d.mer.data.models.CategoryModel
import com.d.mer.ui.interfaces.CategoryClickListener
import java.util.*
import kotlin.collections.ArrayList

class CategoriesAdapter(
    private var list: ArrayList<CategoryModel>,
    private val listOfCategoriesForFilter: ArrayList<CategoryModel>
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    lateinit var context: Context
    private var listener: CategoryClickListener? = null
    var originalList: ArrayList<CategoryModel> = list

    fun attachListener(listener: CategoryClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView = view.findViewById(R.id.name)
        var parentView: LinearLayout = view.findViewById(R.id.parentView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].name.trim().length >= 10) {
            1
        } else {
            0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_row_category, null)
        context = view.context
        if (viewType == 1) {
            view.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            view.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (CategoryModel.isPresent(listOfCategoriesForFilter, list[position].id)) {
            holder.parentView.setBackgroundResource(R.drawable.filled_bg_purple)
            holder.name.setTextColor(Color.WHITE)
        } else {
            holder.parentView.setBackgroundResource(R.drawable.hollow_bg_purple)
            holder.name.setTextColor(Color.BLACK)
        }

        holder.name.text = list[position].name

        holder.parentView.setOnClickListener {
            listener?.click(list[position])
        }

    }

    fun searchFilter(query: CharSequence) {
        val charString = query.toString()
        if (charString.isEmpty()) {
            list = originalList
            notifyDataSetChanged()
        } else {
            val tempList: ArrayList<CategoryModel> = ArrayList()
            for (row in originalList) {
                if (row.name.toLowerCase(Locale.ENGLISH)
                        .contains(charString.toLowerCase(Locale.ENGLISH))
                ) {
                    if (!tempList.contains(row)) {
                        tempList.add(row)
                    }
                }
            }
            list = tempList
            notifyDataSetChanged()
        }
    }

}
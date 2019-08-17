package com.teamnexters.plock.ui.write

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.teamnexters.plock.R

class FindLocationAdapter(
    private val suggestionList: ArrayList<String>,
    private val context: Context?
) : RecyclerView.Adapter<FindLocationAdapter.ListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(LayoutInflater.from(context).inflate(R.layout.find_location_item, parent, false))
    }

    override fun getItemCount(): Int {
        return suggestionList.size
    }

    fun addItem(prediction: AutocompletePrediction) {
        suggestionList.add(prediction.getFullText(null).toString())
    }

    fun clearItem() {
        suggestionList.clear()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        if (suggestionList.size == 0) {
            Log.e("size", "Exception")
        } else {
            holder.bindList(suggestionList[position])
        }
    }

    inner class ListViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val txv_fullTitle = itemView?.findViewById<AppCompatTextView>(R.id.txv_fullTitle)
        val txv_subTitle = itemView?.findViewById<AppCompatTextView>(R.id.txv_subTitle)

        fun bindList(str: String) {
            txv_fullTitle?.text = str
        }
    }
}
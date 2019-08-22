package com.teamnexters.plock.ui.write

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.teamnexters.plock.R


class FindLocationAdapter(
    private val context: Context?,
    private var strList: ArrayList<String>
) : RecyclerView.Adapter<FindLocationAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.find_location_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return strList.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        if (strList.size == 0) {
        } else {
            holder.fullTitle?.text = strList[position]
            holder.subTitle?.text = strList[position]
        }
    }

    fun filterList(lst: ArrayList<String>) {
        strList = lst
        notifyDataSetChanged()
    }

    fun clearList() {
        strList.clear()
        notifyDataSetChanged()
    }

    inner class ListViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val fullTitle = itemView?.findViewById<AppCompatTextView>(R.id.txv_fullTitle)
        val subTitle = itemView?.findViewById<AppCompatTextView>(R.id.txv_subTitle)
    }
}
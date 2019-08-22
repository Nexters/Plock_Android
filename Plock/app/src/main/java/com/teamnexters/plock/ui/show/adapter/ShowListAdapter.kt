package com.teamnexters.plock.ui.show.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.ui.show.model.Location
import java.text.SimpleDateFormat
import java.util.*

class ShowListAdapter(
    //private val items: ArrayList<TimeCapsule>,
    private val itemsMap: HashMap<Location, ArrayList<TimeCapsule>>,
    private val context: Context?
) : RecyclerView.Adapter<ShowListAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(LayoutInflater.from(context).inflate(R.layout.show_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return itemsMap.keys.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
//        holder.bindList(itemsMap.keys)
    }

    inner class ListViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val parentLayout = itemView?.findViewById<ConstraintLayout>(R.id.parent_layout_list_item)

        fun bindList(arrayList: ArrayList<TimeCapsule>?) {

        }
    }

    private fun getDateStr(date: Date): String {
        return SimpleDateFormat("yyyy.MM.dd").format(date)
    }
}
package com.teamnexters.plock.ui.show.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.extensions.start
import com.teamnexters.plock.ui.detailcard.DetailCardActivity

class ShowListAdapter(
    private val items: ArrayList<TimeCapsule>,
    private val context: Context?
) : RecyclerView.Adapter<ShowListAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(LayoutInflater.from(context).inflate(R.layout.show_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bindList(items[position])
    }

    inner class ListViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val parentLayout = itemView?.findViewById<ConstraintLayout>(R.id.parent_layout_list_item)

        fun bindList(showListModel: TimeCapsule) {
            parentLayout?.setOnClickListener { context?.start(DetailCardActivity::class) }
        }
    }
}
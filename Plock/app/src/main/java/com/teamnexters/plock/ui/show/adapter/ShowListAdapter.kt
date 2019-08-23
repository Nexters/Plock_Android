package com.teamnexters.plock.ui.show.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.ui.show.model.Location
import java.text.SimpleDateFormat
import java.util.*

class ShowListAdapter(
    private val itemsMap: HashMap<Location, ArrayList<TimeCapsule>>,
    private val context: Context?
) : RecyclerView.Adapter<ShowListAdapter.ListViewHolder>() {

    private var keys = itemsMap.keys.asSequence()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(LayoutInflater.from(context).inflate(R.layout.show_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return itemsMap.size
    }

    private fun getKey(position: Int): Location {
        return keys.elementAt(position)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val location = getKey(position)
        holder.bindList(itemsMap[location])
    }

    inner class ListViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val parentLayout = itemView?.findViewById<ConstraintLayout>(R.id.parent_layout_list_item)
        val date = itemView?.findViewById<AppCompatTextView>(R.id.txv_date_show_list)
        val title = itemView?.findViewById<AppCompatTextView>(R.id.txv_title_show_list)
        val photo = itemView?.findViewById<AppCompatImageView>(R.id.imv_picture_show_list)

        fun bindList(arrayList: ArrayList<TimeCapsule>?) {
            photo?.setImageBitmap(BitmapFactory.decodeByteArray(arrayList?.get(0)?.photo, 0, arrayList?.get(0)?.photo!!.size))
            date?.text = getDateStr(arrayList?.get(0)?.date!!)
            title?.text = arrayList[0].title
        }
    }

    private fun getDateStr(date: Date): String {
        return SimpleDateFormat("yyyy.MM.dd").format(date)
    }
}
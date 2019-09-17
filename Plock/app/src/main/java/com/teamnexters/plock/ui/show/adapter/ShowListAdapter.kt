package com.teamnexters.plock.ui.show.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.ui.show.ShowListFragment
import java.text.SimpleDateFormat
import java.util.*

class ShowListAdapter(
    private val itemsMap: ArrayList<TimeCapsule>,
    private val context: Context?,
    private val lastLocation: android.location.Location,
    private val fragment: ShowListFragment
) : RecyclerView.Adapter<ShowListAdapter.ListViewHolder>() {

    var itemClick: ItemClick? = null

    interface ItemClick {
        fun onClick(view: View, mapPosition: TimeCapsule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(LayoutInflater.from(context).inflate(R.layout.show_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return itemsMap.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        if (itemClick != null) {
            holder.itemView.setOnClickListener { v ->
                itemClick?.onClick(v, itemsMap[position])
            }
        }
        val item = itemsMap[position]
        holder.apply {
            bindList(item)
        }
    }

    fun removeItem(index: Int) {
        itemsMap.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, itemsMap.size)
        if (itemsMap.isEmpty()) {
            fragment.changeView()
        }
    }

    inner class ListViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val view = itemView

        val parentLayout = itemView?.findViewById<ConstraintLayout>(R.id.parent_layout_list_item)
        val date = itemView?.findViewById<AppCompatTextView>(R.id.txv_date_show_list)
        val title = itemView?.findViewById<AppCompatTextView>(R.id.txv_title_show_list)
        val photo = itemView?.findViewById<AppCompatImageView>(R.id.imv_picture_show_list)
        val lock_icon = itemView?.findViewById<AppCompatImageView>(R.id.imv_lock_show_list)

        fun bindList(capsule: TimeCapsule?) {
            photo?.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    capsule?.photo,
                    0,
                    capsule?.photo!!.size
                )
            )
            date?.text = getDateStr(capsule?.date!!)
            title?.text = capsule.title

            // 위치 비교해서 lock, unlock 구분
            if (calculateDistance(capsule.latitude, capsule.longitude, lastLocation.latitude, lastLocation.longitude) < 100) {
                parentLayout?.setBackgroundColor(Color.TRANSPARENT)
                lock_icon?.visibility = View.INVISIBLE
                itemView.isClickable = true
            } else {
                parentLayout?.setBackgroundColor(Color.parseColor("#99000000"))
                lock_icon?.visibility = View.VISIBLE
                itemView.isClickable = false
            }
        }
    }

    private fun getDateStr(date: Date): String {
        return SimpleDateFormat("yyyy.MM.dd").format(date)
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        // distance in meter
        return results[0]
    }

}
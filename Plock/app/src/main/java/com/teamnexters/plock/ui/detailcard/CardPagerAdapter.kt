package com.teamnexters.plock.ui.detailcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.viewpager.widget.PagerAdapter
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import kotlinx.android.synthetic.main.card_back.view.*
import kotlinx.android.synthetic.main.card_front.view.*
import java.text.SimpleDateFormat
import java.util.*

class CardPagerAdapter(private val showTimeCapsuleList: List<TimeCapsule>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val cardView = LayoutInflater.from(container?.context).inflate(R.layout.item_card, container, false)
        with(cardView) {
            cardTitleEditTv.visibility = View.GONE
            cardTitleTv.visibility = View.VISIBLE
            cardMessageEditTv.visibility = View.GONE
            changeDateIv.visibility = View.GONE
            changePlaceIv.visibility = View.GONE

            val item = showTimeCapsuleList[position]
            with(item){
                cardTitleTv.text = title
                cardMessageTv.text = message
                cardDateTv.text = getDateStr(date)
                cardPhotoIv.setImageURI(photo.toUri())
            }
        }

        container?.addView(cardView)
        return cardView
    }

    private fun getDateStr(date :Date) : String {
        return SimpleDateFormat("yyyy.MM.dd").format(date)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return showTimeCapsuleList.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}
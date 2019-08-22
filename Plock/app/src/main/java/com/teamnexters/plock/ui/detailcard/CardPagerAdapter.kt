package com.teamnexters.plock.ui.detailcard

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import kotlinx.android.synthetic.main.card_back.view.*
import kotlinx.android.synthetic.main.card_front.view.*
import kotlinx.android.synthetic.main.item_card.view.*
import java.text.SimpleDateFormat
import java.util.*

class CardPagerAdapter(private val context: Context, private val showTimeCapsuleList: List<TimeCapsule>) : PagerAdapter() {

    init {
        loadFlipAnimations()
    }

    private lateinit var leftInAnim: AnimatorSet
    private lateinit var leftOutAnim: AnimatorSet
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val cardView = LayoutInflater.from(container?.context).inflate(R.layout.item_card, container, false)
        changeCameraDistance(cardView)
        cardView.tag = position

        with(cardView) {
            cardTitleEditTv.visibility = View.GONE
            cardTitleTv.visibility = View.VISIBLE
            cardMessageEditTv.visibility = View.GONE
            plusIv.visibility = View.GONE

            val item = showTimeCapsuleList[position]
            with(item) {
                cardTitleTv.text = title
                cardMessageTv.text = message
                cardDateTv.text = getDateStr(date)
                cardPhotoIv.setImageBitmap(byteArrayToBitmap(photo))
            }

            setOnClickListener {
                if (!item.isBackVisible)
                    flipToBack(findViewWithTag<View>(position), position)
                else if (item.isBackVisible)
                    flipToFront(findViewWithTag<View>(position), position)
            }
        }
        container?.addView(cardView)
        return cardView
    }

    private fun flipToBack(itemView: View, position: Int) {
        leftOutAnim.setTarget(itemView.cardFront)
        leftInAnim.setTarget(itemView.cardBack)
        leftOutAnim.start()
        leftInAnim.start()
        showTimeCapsuleList[position].isBackVisible = true
    }

    private fun flipToFront(itemView: View, position: Int) {
        leftOutAnim.setTarget(itemView.cardBack)
        leftInAnim.setTarget(itemView.cardFront)
        leftOutAnim.start()
        leftInAnim.start()
        showTimeCapsuleList[position].isBackVisible = false
    }

    private fun loadFlipAnimations() {
        leftInAnim = AnimatorInflater.loadAnimator(context, R.animator.anim_flip_left_in) as AnimatorSet
        leftOutAnim = AnimatorInflater.loadAnimator(context, R.animator.anim_flip_left_out) as AnimatorSet
    }

    private fun changeCameraDistance(cardView: View) {
        val distance = 6000
        val scale = context.resources.displayMetrics.density * distance
        cardView.cardFront.cameraDistance = scale
        cardView.cardBack.cameraDistance = scale
    }

    private fun getDateStr(date: Date): String {
        return SimpleDateFormat("yyyy.MM.dd").format(date)
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
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
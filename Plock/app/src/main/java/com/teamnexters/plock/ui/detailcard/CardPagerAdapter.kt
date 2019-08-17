package com.teamnexters.plock.ui.detailcard

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.viewpager.widget.PagerAdapter
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import kotlinx.android.synthetic.main.activity_write_card.*
import kotlinx.android.synthetic.main.card_back.view.*
import kotlinx.android.synthetic.main.card_front.view.*
import kotlinx.android.synthetic.main.item_card.view.*
import java.text.SimpleDateFormat
import java.util.*

class CardPagerAdapter(private val context: Context, private val showTimeCapsuleList: List<TimeCapsule>) :
    PagerAdapter() {

    init {
        loadFlipAnimations()
    }

    private lateinit var rightOutAnim: AnimatorSet
    private lateinit var leftInAnim: AnimatorSet
    private lateinit var rightInAnim: AnimatorSet
    private lateinit var leftOutAnim: AnimatorSet
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val cardView = LayoutInflater.from(container?.context).inflate(R.layout.item_card, container, false)
        changeCameraDistance(cardView)
        cardView.tag = position

        with(cardView) {
            cardTitleEditTv.visibility = View.GONE
            cardTitleTv.visibility = View.VISIBLE
            cardMessageEditTv.visibility = View.GONE
            changeDateIv.visibility = View.GONE
            changePlaceIv.visibility = View.GONE

            val item = showTimeCapsuleList[position]
            with(item) {
                cardTitleTv.text = title
                cardMessageTv.text = message
                cardDateTv.text = getDateStr(date)
                cardPhotoIv.setImageURI(photo.toUri())
            }

            setOnClickListener {
                if (!item.isBackVisible)
                    flipToBack(findViewWithTag<View>(position), position)
                else if(item.isBackVisible)
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
        rightOutAnim = AnimatorInflater.loadAnimator(context, R.animator.anim_flip_right_out) as AnimatorSet
        leftInAnim = AnimatorInflater.loadAnimator(context, R.animator.anim_flip_left_in) as AnimatorSet
        leftOutAnim = AnimatorInflater.loadAnimator(context, R.animator.anim_flip_left_out) as AnimatorSet
        rightInAnim = AnimatorInflater.loadAnimator(context, R.animator.anim_flip_right_in) as AnimatorSet
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
package com.teamnexters.plock.ui.writecard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.teamnexters.plock.R
import com.teamnexters.plock.data.provideTimeCapsuleDao
import kotlinx.android.synthetic.main.activity_write_card.*
import kotlinx.android.synthetic.main.toolbar_custom.*
import android.animation.AnimatorInflater
import android.animation.AnimatorSet


class WriteCardActivity : AppCompatActivity() {
    internal val viewModelFactory by lazy {
        WriteCardViewModelFactory(provideTimeCapsuleDao(this))
    }

    lateinit var viewModel: WriteCardViewModel

    private lateinit var rightOutAnim: AnimatorSet
    private lateinit var leftInAnim: AnimatorSet
    private var isBackVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_card)

        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[WriteCardViewModel::class.java]

        initToolbar()
        loadFlipAnimations()

        imv_toolbar_left.setOnClickListener {
            flipToFront()
            setToolbarRightBtnNext()
        }

        imv_toolbar_right.setOnClickListener {
            if (!isBackVisible) {
                flipToBack()
                setToolbarRightBtnFinish()
            } else {
                showFinalCheckDialog()
            }
        }
    }

    private fun showFinalCheckDialog() {

    }

    private fun flipToBack(){
        rightOutAnim.setTarget(cardFront)
        leftInAnim.setTarget(cardBack)
        isBackVisible = true
        startAnim()
    }

    private fun flipToFront(){
        rightOutAnim.setTarget(cardBack)
        leftInAnim.setTarget(cardFront)
        isBackVisible = false
        startAnim()
    }

    private fun loadFlipAnimations() {
        rightOutAnim = AnimatorInflater.loadAnimator(this, R.animator.anim_flip_right_out) as AnimatorSet
        leftInAnim = AnimatorInflater.loadAnimator(this, R.animator.anim_flip_left_in) as AnimatorSet
        changeCameraDistance()
    }

    private fun changeCameraDistance() {
        val distance = 8000
        val scale = resources.displayMetrics.density * distance
        cardFront.cameraDistance = scale
        cardBack.cameraDistance = scale
    }

    private fun startAnim(){
        rightOutAnim.start()
        leftInAnim.start()
    }

    private fun setToolbarRightBtnNext() =
        imv_toolbar_right.setImageResource(R.drawable.ic_arrow_right)

    private fun setToolbarRightBtnFinish() =
        imv_toolbar_right.setImageResource(R.drawable.toolbar_active)

    private fun initToolbar() {
        tv_toolbar_center.text = "작성"
        setToolbarRightBtnNext()
    }
}
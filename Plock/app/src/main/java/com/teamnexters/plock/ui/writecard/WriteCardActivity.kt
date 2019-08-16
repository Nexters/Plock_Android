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
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.extensions.plusAssign
import com.teamnexters.plock.extensions.px
import com.teamnexters.plock.extensions.start
import com.teamnexters.plock.rx.AutoClearedDisposable
import com.teamnexters.plock.ui.main.MainActivity
import kotlinx.android.synthetic.main.card_front.*
import java.util.*
import android.provider.MediaStore
import android.content.Intent
import android.net.Uri


private const val PICK_FROM_ALBUM = 1

class WriteCardActivity : AppCompatActivity() {
    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    internal val viewModelFactory by lazy {
        WriteCardViewModelFactory(provideTimeCapsuleDao(this))
    }

    lateinit var viewModel: WriteCardViewModel

    private lateinit var selectedImage: Uri

    private lateinit var rightOutAnim: AnimatorSet
    private lateinit var leftInAnim: AnimatorSet
    private lateinit var rightInAnim: AnimatorSet
    private lateinit var leftOutAnim: AnimatorSet
    private var isBackVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_card)

        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[WriteCardViewModel::class.java]

        lifecycle += disposables
        lifecycle += viewDisposables

        initToolbar()
        initCardSize()
        loadFlipAnimations()

        cardPhotoIv.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, PICK_FROM_ALBUM)
        }

        imv_toolbar_left.setOnClickListener {
            if (isBackVisible) {
                flipToFront()
                setToolbarRightBtnNext()
            } else {
                finish()
            }
        }

        imv_toolbar_right.setOnClickListener {
            if (!isBackVisible) {
                flipToBack()
                setToolbarRightBtnFinish()
            } else {
//                showFinalCheckDialog()
                saveCard()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICK_FROM_ALBUM -> {
                selectedImage = data?.data ?: return
                cardPhotoIv.setImageURI(selectedImage)
            }
        }
    }

    private fun saveCard() {
        val timeCapsule = TimeCapsule("제목", Date(), "장소", 37.541, 126.986, selectedImage.toString(), "룰루랄라")
        disposables += viewModel.saveTimeCapsule(timeCapsule)
        start(MainActivity::class)
    }

    private fun showFinalCheckDialog() {

    }

    private fun flipToBack() {
        leftOutAnim.setTarget(cardFront)
        leftInAnim.setTarget(cardBack)
        leftOutAnim.start()
        leftInAnim.start()
        isBackVisible = true
    }

    private fun flipToFront() {
        rightOutAnim.setTarget(cardBack)
        rightInAnim.setTarget(cardFront)
        rightOutAnim.start()
        rightInAnim.start()
        isBackVisible = false
    }

    private fun loadFlipAnimations() {
        rightOutAnim = AnimatorInflater.loadAnimator(this, R.animator.anim_flip_right_out) as AnimatorSet
        leftInAnim = AnimatorInflater.loadAnimator(this, R.animator.anim_flip_left_in) as AnimatorSet
        leftOutAnim = AnimatorInflater.loadAnimator(this, R.animator.anim_flip_left_out) as AnimatorSet
        rightInAnim = AnimatorInflater.loadAnimator(this, R.animator.anim_flip_right_in) as AnimatorSet
        changeCameraDistance()
    }

    private fun changeCameraDistance() {
        val distance = 6000
        val scale = resources.displayMetrics.density * distance
        cardFront.cameraDistance = scale
        cardBack.cameraDistance = scale
    }

    private fun setToolbarRightBtnNext() =
        imv_toolbar_right.setImageResource(R.drawable.ic_arrow_right)

    private fun setToolbarRightBtnFinish() =
        imv_toolbar_right.setImageResource(R.drawable.toolbar_active)

    private fun initCardSize() {
        val margin = 35.px
        cardLayout.setPadding(margin, 0, margin, 8)
    }

    private fun initToolbar() {
        tv_toolbar_center.text = "작성"
        setToolbarRightBtnNext()
    }
}
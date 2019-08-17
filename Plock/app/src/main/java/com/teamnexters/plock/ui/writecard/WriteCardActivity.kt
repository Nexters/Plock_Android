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
import android.app.DatePickerDialog
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
import androidx.core.net.toUri
import com.teamnexters.plock.extensions.toast
import kotlinx.android.synthetic.main.card_back.*
import java.text.SimpleDateFormat


private const val PICK_FROM_ALBUM = 1

class WriteCardActivity : AppCompatActivity() {
    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    internal val viewModelFactory by lazy {
        WriteCardViewModelFactory(provideTimeCapsuleDao(this))
    }

    lateinit var viewModel: WriteCardViewModel

    private var selectedImage: Uri = "".toUri()

    private lateinit var rightOutAnim: AnimatorSet
    private lateinit var leftInAnim: AnimatorSet
    private lateinit var rightInAnim: AnimatorSet
    private lateinit var leftOutAnim: AnimatorSet
    private var isBackVisible = false

    private var year = 0
    private var month = 0
    private var day = 0

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
        setUpTodayDate()

        cardDateLayout.setOnClickListener { showDatePickerDialog() }

        cardPhotoIv.setOnClickListener {
            if (!isBackVisible) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                startActivityForResult(intent, PICK_FROM_ALBUM)
            }
        }

        imv_toolbar_left.setOnClickListener {
            if (isBackVisible) {
                flipToFront()
            } else {
                finish()
            }
        }

        imv_toolbar_right.setOnClickListener {
            if (!isBackVisible) {
                flipToBack()
            } else {
//                showFinalCheckDialog()
                if (checkWriteAll()) saveCard()
            }
        }
    }

    private fun showDatePickerDialog() {
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            this.year = year
            this.month = monthOfYear
            this.day = dayOfMonth
            cardDateTv.text = getDateStr()
        }, year, month, day)

        dpd.show()
    }

    private fun checkWriteAll(): Boolean {
        var infoMsg = ""
        if (selectedImage.toString().isEmpty()) infoMsg = "사진을 선택해주세요"
        else if (cardTitleEditTv.text.isEmpty()) infoMsg = "제목을 입력해주세요"

        if (infoMsg.isNotEmpty()) {
            toast(infoMsg)
            flipToFront()
            return false
        }
        return true
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
        val timeCapsule = TimeCapsule(
            cardTitleEditTv.text.toString(), getDate(), placeNameTv.text.toString(),
            37.541, 126.986, selectedImage.toString(), cardMessageEditTv.text.toString()
        )
        disposables += viewModel.saveTimeCapsule(timeCapsule)
        start(MainActivity::class)
    }

    private fun showFinalCheckDialog() {

    }

    private fun setUpTodayDate() {
        val c = Calendar.getInstance()
        year = c.get(Calendar.YEAR)
        month = c.get(Calendar.MONTH)
        day = c.get(Calendar.DAY_OF_MONTH)
        cardDateTv.text = getDateStr()
    }

    private fun flipToBack() {
        leftOutAnim.setTarget(cardFront)
        leftInAnim.setTarget(cardBack)
        leftOutAnim.start()
        leftInAnim.start()
        isBackVisible = true
        setToolbarRightBtnFinish()
    }

    private fun flipToFront() {
        rightOutAnim.setTarget(cardBack)
        rightInAnim.setTarget(cardFront)
        rightOutAnim.start()
        rightInAnim.start()
        isBackVisible = false
        setToolbarRightBtnNext()
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

    private fun getDateStr(): String {
        return "$year.${month + 1}.$day"
    }

    private fun getDate(): Date {
        val selectDate = SimpleDateFormat("yyyy.MM.dd").parse(getDateStr())
        return selectDate ?: Date()
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
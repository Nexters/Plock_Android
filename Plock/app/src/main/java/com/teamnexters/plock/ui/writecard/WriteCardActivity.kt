package com.teamnexters.plock.ui.writecard

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.teamnexters.plock.R
import com.teamnexters.plock.data.provideTimeCapsuleDao
import kotlinx.android.synthetic.main.activity_write_card.*
import kotlinx.android.synthetic.main.toolbar_custom.*
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.extensions.plusAssign
import com.teamnexters.plock.extensions.px
import com.teamnexters.plock.extensions.start
import com.teamnexters.plock.extensions.toast
import com.teamnexters.plock.rx.AutoClearedDisposable
import com.teamnexters.plock.ui.main.MainActivity
import kotlinx.android.synthetic.main.card_front.*
import java.util.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import kotlinx.android.synthetic.main.card_back.*
import com.teamnexters.plock.ui.write.MapLocationActivity
import kotlinx.android.synthetic.main.dialog_two_button.view.*
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream

private const val PICK_FROM_ALBUM = 1
private const val GET_LOCATION_CODE = 100

class WriteCardActivity : AppCompatActivity() {
    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    internal val viewModelFactory by lazy {
        WriteCardViewModelFactory(provideTimeCapsuleDao(this))
    }

    lateinit var viewModel: WriteCardViewModel

    private lateinit var selectedImage: Bitmap

    private lateinit var rightOutAnim: AnimatorSet
    private lateinit var leftInAnim: AnimatorSet
    private lateinit var rightInAnim: AnimatorSet
    private lateinit var leftOutAnim: AnimatorSet
    private var isBackVisible = false

    private var year = 0
    private var month = 0
    private var day = 0

    private var lat = 0.0
    private var long = 0.0
    private var locationName = ""

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
                if (checkWriteAll()) showSaveDialog()
            }
        }

        changePlaceLayout.setOnClickListener {
            val intent = Intent(applicationContext, MapLocationActivity::class.java)
            startActivityForResult(intent, GET_LOCATION_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_FROM_ALBUM -> {
                val uri = data?.data ?: return
                selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                cardPhotoIv.setImageBitmap(selectedImage)
                if(plusIv.visibility == View.VISIBLE) plusIv.visibility = View.GONE
            }
            GET_LOCATION_CODE -> {
                lat = data?.extras?.getDouble("lat")!!
                long = data.extras?.getDouble("long")!!
                locationName = data.extras?.getString("location")!!
                placeNameTv.text = locationName
            }
        }
    }

    private fun saveCard() {
        val timeCapsule = TimeCapsule(
            cardTitleEditTv.text.toString(), getDate(), placeNameTv.text.toString(),
            lat, long, bitmapToByteArray(), cardMessageEditTv.text.toString()
        )
        disposables += viewModel.saveTimeCapsule(timeCapsule)
        start(MainActivity::class)
    }

    private fun showSaveDialog() {
        layoutInflater.inflate(R.layout.dialog_two_button, null).run {
            val dialog = AlertDialog.Builder(this@WriteCardActivity)
                .setView(this)
                .show()

            setUpDialogSize(dialog)
            titleTv.text = "기억해주세요!"
            msgTv.text = getString(R.string.dialog_save_msg)
            okBtn.text = "저장"
            dialogIv.setImageDrawable(getDrawable(R.drawable.img_person_save))
            cancelBtn.setOnClickListener { dialog.dismiss() }
            okBtn.setOnClickListener {
                saveCard()
                dialog.dismiss()
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

    private fun setUpTodayDate() {
        val c = Calendar.getInstance()
        year = c.get(Calendar.YEAR)
        month = c.get(Calendar.MONTH)
        day = c.get(Calendar.DAY_OF_MONTH)
        cardDateTv.text = getDateStr()
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

    private fun setUpDialogSize(dialog: Dialog) {
        val width = (resources.displayMetrics.widthPixels * 0.60).toInt()
        val height = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.apply {
            setLayout(width, height)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
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

    private fun bitmapToByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
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
package com.teamnexters.plock.ui.writecard

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.data.provideTimeCapsuleDao
import com.teamnexters.plock.extensions.plusAssign
import com.teamnexters.plock.extensions.px
import com.teamnexters.plock.extensions.start
import com.teamnexters.plock.extensions.toast
import com.teamnexters.plock.rx.AutoClearedDisposable
import com.teamnexters.plock.ui.main.MainActivity
import com.teamnexters.plock.ui.write.MapLocationActivity
import kotlinx.android.synthetic.main.activity_write_card.*
import kotlinx.android.synthetic.main.card_back.*
import kotlinx.android.synthetic.main.card_front.*
import kotlinx.android.synthetic.main.dialog_two_button.view.*
import kotlinx.android.synthetic.main.toolbar_custom.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

private const val PICK_FROM_ALBUM = 1
private const val GET_LOCATION_CODE = 100

class WriteCardActivity : AppCompatActivity() {
    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    internal val viewModelFactory by lazy {
        WriteCardViewModelFactory(provideTimeCapsuleDao(this))
    }

    lateinit var viewModel: WriteCardViewModel

    private var selectedImage: Bitmap? = null

    private lateinit var leftInAnim: AnimatorSet
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
        setEditTvEnabled()

        cardMessageEditTv.visibility = View.VISIBLE
        cardDateLayout.setOnClickListener { if (!isBackVisible) showDatePickerDialog() }

        cardPhotoIv.setOnClickListener {
            if (!isBackVisible) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                startActivityForResult(intent, PICK_FROM_ALBUM)
            }
        }

        flipBtnInFront.setOnClickListener { flipToBack() }
        flipBtnInBack.setOnClickListener { flipToFront() }

        imv_toolbar_left.setOnClickListener { finish() }
        imv_toolbar_right.setOnClickListener { if (checkWriteAll()) showSaveDialog() }

        changePlaceLayout.setOnClickListener {
            if (!isBackVisible) {
                val intent = Intent(applicationContext, MapLocationActivity::class.java)
                if (selectedImage != null) intent.putExtra("photo", bitmapToByteArrayToMap())
                else intent.putExtra("photo", "null")
                startActivityForResult(intent, GET_LOCATION_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_FROM_ALBUM -> {
                val uri = data?.data ?: return
                selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                cardPhotoIv.setImageBitmap(selectedImage)
                if (plusIv.visibility == View.VISIBLE) plusIv.visibility = View.GONE
            }
            GET_LOCATION_CODE -> {
                lat = data?.extras?.getDouble("lat")!!
                long = data.extras?.getDouble("long")!!
                locationName = data.extras?.getString("location")!!
                if (locationName == "") placeNameTv.text = getUnderLineStr("저장 위치를 선택해주세요.")
                else placeNameTv.text = getUnderLineStr(locationName)
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
        finish()
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
            cardDateTv.text = getUnderLineStr(getDateStr())
        }, year, month, day)

        dpd.show()
    }

    private fun setUpTodayDate() {
        val c = Calendar.getInstance()
        year = c.get(Calendar.YEAR)
        month = c.get(Calendar.MONTH)
        day = c.get(Calendar.DAY_OF_MONTH)
        cardDateTv.text = getUnderLineStr(getDateStr())
    }

    private fun getUnderLineStr(str: String): SpannableString {
        val underLineStr = SpannableString(str)
        underLineStr.setSpan(UnderlineSpan(), 0, str.length, 0)
        return underLineStr
    }

    private fun checkWriteAll(): Boolean {
        var infoMsg = ""
        if (selectedImage == null) infoMsg = "사진을 선택해주세요"
        else if (cardTitleEditTv.text.isEmpty()) infoMsg = "제목을 입력해주세요"
        else if (lat.equals(0.0)) infoMsg = "장소를 선택해주세요"

        if (infoMsg.isNotEmpty()) {
            toast(infoMsg)
            if (isBackVisible) flipToFront()
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
        startAnim()
        isBackVisible = true
        setEditTvEnabled()
    }

    private fun flipToFront() {
        leftOutAnim.setTarget(cardBack)
        leftInAnim.setTarget(cardFront)
        startAnim()
        isBackVisible = false
        setEditTvEnabled()
    }

    private fun startAnim() {
        leftOutAnim.start()
        leftInAnim.start()
    }

    private fun setEditTvEnabled() {
        cardPhotoIv.isClickable = !isBackVisible
        cardTitleEditTv.isEnabled = !isBackVisible
        cardMessageEditTv.isEnabled = isBackVisible
        if (isBackVisible) {
            cardMessageEditTv.isFocusableInTouchMode = true
            cardMessageEditTv.requestFocus()
        }
    }

    private fun loadFlipAnimations() {
        leftInAnim = AnimatorInflater.loadAnimator(this, R.animator.anim_flip_left_in) as AnimatorSet
        leftOutAnim = AnimatorInflater.loadAnimator(this, R.animator.anim_flip_left_out) as AnimatorSet
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
        selectedImage?.compress(Bitmap.CompressFormat.JPEG, 60, stream)
        return stream.toByteArray()
    }

    private fun bitmapToByteArrayToMap(): ByteArray {
        val stream = ByteArrayOutputStream()
        selectedImage?.compress(Bitmap.CompressFormat.JPEG, 20, stream)
        return stream.toByteArray()
    }

    private fun initCardSize() {
        val margin = 35.px
        cardLayout.setPadding(margin, 0, margin, 8)
    }

    private fun initToolbar() {
        tv_toolbar_center.text = "작성"
        imv_toolbar_right.setImageResource(R.drawable.ic_check)
    }
}
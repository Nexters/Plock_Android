package com.teamnexters.plock.ui.writecard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.teamnexters.plock.R
import com.teamnexters.plock.data.provideTimeCapsuleDao
import kotlinx.android.synthetic.main.toolbar_custom.*


class WriteCardActivity : AppCompatActivity() {
    private var isFinalLevel = false
    var isFinishBtnEnable = false

    internal val viewModelFactory by lazy {
        WriteCardViewModelFactory(provideTimeCapsuleDao(this))
    }

    lateinit var viewModel: WriteCardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_card)

        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[WriteCardViewModel::class.java]

        initToolbar()
    }

    private fun setToolbarRightBtnNext() =
        imv_toolbar_right.setImageResource(R.drawable.toolbar_next)

    private fun setToolbarRightBtnFinish() =
        imv_toolbar_right.setImageResource(R.drawable.toolbar_active)

    private fun setToolbarRightBtnColorActive() =
        imv_toolbar_right.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))

    private fun setToolbarRightBtnColorInactive() =
        imv_toolbar_right.setColorFilter(ContextCompat.getColor(this, R.color.gray4))

    private fun initToolbar() {
        tv_toolbar_center.text = "작성"
        setToolbarRightBtnNext()
    }
}
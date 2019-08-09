package com.teamnexters.plock.ui.writecard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamnexters.plock.R
import kotlinx.android.synthetic.main.toolbar_custom.*

class WriteCardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_card)
        initToolbar()

    }

    private fun initToolbar() {
        tv_toolbar_center.text = "작성"
        imv_toolbar_right.setImageResource(R.drawable.toolbar_next)
    }

}
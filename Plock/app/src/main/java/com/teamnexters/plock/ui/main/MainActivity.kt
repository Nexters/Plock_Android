package com.teamnexters.plock.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamnexters.plock.R
import com.teamnexters.plock.extensions.start
import com.teamnexters.plock.ui.detailcard.DetailCardActivity
import com.teamnexters.plock.ui.writecard.WriteCardActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        writeCardLayout.setOnClickListener { start(WriteCardActivity::class) }
        showCardLayout.setOnClickListener { start(DetailCardActivity::class) }
    }
}

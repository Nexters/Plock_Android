package com.teamnexters.plock.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamnexters.plock.R
import com.teamnexters.plock.ui.show.ShowActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        writeCardLayout.setOnClickListener {  }
        showCardLayout.setOnClickListener { startActivity<ShowActivity>() }
    }
}

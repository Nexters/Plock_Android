package com.teamnexters.plock.ui.write

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.teamnexters.plock.R

class FindLocationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_location)

        initToolbar()
    }

    private fun initToolbar() {
        val textView = findViewById<TextView>(R.id.txv_toolbar_center)
        textView.text = "위치 설정"
    }
}

package com.teamnexters.plock.ui.show

import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.teamnexters.plock.R
import com.teamnexters.plock.extensions.start
import com.teamnexters.plock.ui.writecard.WriteCardActivity
import kotlinx.android.synthetic.main.activity_show_activity.*
import kotlinx.android.synthetic.main.toolbar_segment.*

class ShowActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    private val mFragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_activity)

        mFragmentManager.beginTransaction()
            .add(R.id.fl_show, ShowMapFragment())
            .commit()

        segment_group_toolbar.setOnCheckedChangeListener(this)
        fabClickListener()
    }

    private fun fabClickListener() {
        fab_write.setOnClickListener { start(WriteCardActivity::class) }
    }

    private fun changeFragment(id: Int) {
        val startTransaction = mFragmentManager.beginTransaction()
        when (id) {
            R.id.btn_map_segment_toolbar -> {
                startTransaction.replace(R.id.fl_show, ShowMapFragment()).commit()
            }
            R.id.btn_list_segment_toolbar -> {
                startTransaction.replace(R.id.fl_show, ShowListFragment()).commit()
            }
        }
    }

    private fun checkCurrentFragment(id: Int) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fl_show)
        when (currentFragment?.id) {
            id -> { }
            else -> {
                changeFragment(id)
            }
        }
    }

    override fun onCheckedChanged(p0: RadioGroup?, id: Int) {
        checkCurrentFragment(id)
    }
}
package com.teamnexters.plock.ui.show

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.teamnexters.plock.R
import com.teamnexters.plock.extensions.start
import com.teamnexters.plock.ui.writecard.WriteCardActivity
import kotlinx.android.synthetic.main.activity_show_activity.*
import kotlinx.android.synthetic.main.toolbar_segment.*

class ShowActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    private val mFragmentManager = supportFragmentManager
    private val LOCATION_RESULT_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_activity)

        initToolbar()
        checkGPS()

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
            id -> {
            }
            else -> {
                changeFragment(id)
            }
        }
    }

    override fun onCheckedChanged(p0: RadioGroup?, id: Int) {
        checkCurrentFragment(id)
    }

    /*
    * 유저가 gps 비활성 시 위치 정보 다이얼로그에서 선택 시
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // getDeviceLocation()
                mFragmentManager.beginTransaction()
                    .add(R.id.fl_show, ShowMapFragment())
                    .commit()
            } else {
                finish()
            }
        }
    }

    private fun checkGPS() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(builder.build())

        /* 활성화 & 비활성화 시 */
        task.addOnSuccessListener {
            mFragmentManager.beginTransaction()
                .add(R.id.fl_show, ShowMapFragment())
                .commit()
        }
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                val resolvable: ResolvableApiException = it
                try {
                    resolvable.startResolutionForResult(this, LOCATION_RESULT_CODE)
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initToolbar() {
//        val textView = findViewById<AppCompatImageView>(R.id.imv_toolbar_back)
        imv_toolbar_back.setOnClickListener { finish() }
    }
}
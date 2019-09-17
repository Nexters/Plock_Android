package com.teamnexters.plock.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.teamnexters.plock.ui.write.MapLocationActivity

// 앱 내 위치 권한
class CheckLocationPermission {
    companion object {
        fun checkPermission(activity: Activity) {
            Dexter.withActivity(activity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    @SuppressLint("MissingPermission")
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        Toast.makeText(activity, "Permission Granted!!", Toast.LENGTH_SHORT).show()
                        (activity as MapLocationActivity).getDeviceLocation()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        val intent = Intent()
                        intent.putExtra("lat", 0.0)
                        intent.putExtra("long", 0.0)
                        intent.putExtra("location", "")
                        activity.setResult(100, intent)
                        activity.finish()
                        Toast.makeText(activity, "앱 내 위치권한을 확인하세요", Toast.LENGTH_SHORT).show()
                    }
                })
                .check()
        }
    }
}
package com.teamnexters.plock.ui.write

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.teamnexters.plock.R
import com.teamnexters.plock.util.CheckLocationPermission
import com.teamnexters.plock.util.MapTools
import kotlinx.android.synthetic.main.activity_map_location.*
import kotlinx.android.synthetic.main.bottom_sheet_map.*
import kotlinx.android.synthetic.main.toolbar_custom.*
import java.io.IOException
import java.util.*

class MapLocationActivity : AppCompatActivity(),
    GoogleMap.OnCameraIdleListener,
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location
    private lateinit var locationCallback: LocationCallback
    private val LOCATION_RESULT_CODE = 100

    private lateinit var geoCoder: Geocoder
    private var checkOnce = false
    private var latitude = 0.0
    private var longitude = 0.0
    private var locationName = ""

    private fun substringAddress(res: String): java.lang.StringBuilder {
        val resArr = res.split(" ")
        val sb = StringBuilder("")
        if (resArr.size == 1) {
            sb.append(resArr[0])
        } else {
            for (i in resArr.indices) {
                sb.append(resArr[i+1])
                sb.append(" ")
                if (i == resArr.size - 2) {
                    break
                }
            }
        }

        return sb
    }

    override fun onCameraIdle() {
        if (!checkOnce) {
            checkOnce = true
        } else {
            latitude = mMap.cameraPosition.target.latitude
            longitude = mMap.cameraPosition.target.longitude
            try {
                val mResultList = geoCoder.getFromLocation(
                    latitude,
                    longitude,
                    1
                )
                if (mResultList != null && mResultList.size > 0) {
                    val res = mResultList[0].getAddressLine(0)
                    locationName = substringAddress(res).toString()
                    cardETxt.text = substringAddress(res)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_location)

        // intent로 사진 정보 받기
        val data = intent
        if (data.extras?.get("photo")?.equals("null")!!) {
            imv_picture_write_map.background = ContextCompat.getDrawable(applicationContext, R.drawable.ic_photo_empty)
        } else {
            val byteArray = data.getByteArrayExtra("photo")
            val photo: Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size!!)
            imv_picture_write_map.setImageBitmap(photo)
        }

        initToolbar()
        initFusedLocation()
        initMapFragment()
        initBottomSheet()
        geoCoder = Geocoder(applicationContext, Locale.KOREAN)

//        sheet_custom_layout.setOnClickListener { start(FindLocationActivity::class) }
        fab_map_location.setOnClickListener { checkGPS() }
        cardBtn.setOnClickListener {
            val intent = Intent()
            intent.putExtra("lat", latitude)
            intent.putExtra("long", longitude)
            intent.putExtra("location", locationName)
            setResult(100, intent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = MapTools.configActivityMaps(googleMap)
        mMap.setOnCameraIdleListener(this)
        checkGPS()
    }

    /*
    * 유저가 gps 비활성 시 위치 정보 다이얼로그에서 선택 시
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    getDeviceLocation()
                } else {
                    CheckLocationPermission.checkPermission(this)
                }
            } else {
                finish()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(task: Task<Location>) {
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            lastKnownLocation = task.result!!
                            latitude = lastKnownLocation.latitude
                            longitude = lastKnownLocation.longitude

                            try {
                                val mResultList = geoCoder.getFromLocation(
                                    lastKnownLocation.latitude,
                                    lastKnownLocation.longitude,
                                    1
                                )

                                if (mResultList != null && mResultList.size > 0) {
                                    val res = mResultList[0].getAddressLine(0)
                                    locationName = substringAddress(res).toString()
                                    cardETxt.text = substringAddress(res)
                                }

                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude),
                                    17F
                                )
                            )
                        } else {
                            val locationRequest: LocationRequest = LocationRequest.create()
                            locationRequest.interval = 8000
                            locationRequest.fastestInterval = 4000
                            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                            locationCallback = object : LocationCallback() {
                                override fun onLocationResult(result: LocationResult?) {
                                    super.onLocationResult(result)
                                    if (result == null) {
                                        return
                                    }

                                    lastKnownLocation = result.lastLocation
                                    latitude = lastKnownLocation.latitude
                                    longitude = lastKnownLocation.longitude

                                    try {
                                        val mResultList = geoCoder.getFromLocation(
                                            lastKnownLocation.latitude,
                                            lastKnownLocation.longitude,
                                            1
                                        )

                                        if (mResultList != null && mResultList.size > 0) {
                                            val res = mResultList[0].getAddressLine(0)
                                            locationName = substringAddress(res).toString()
                                            cardETxt.text = substringAddress(res)
                                        }

                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }

                                    mMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                lastKnownLocation.latitude,
                                                lastKnownLocation.longitude
                                            ), 17F
                                        )
                                    )

                                    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                                }
                            }
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
                        }
                    } else {
                        Toast.makeText(applicationContext, "위치를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                    }
                }

            })
    }

    private fun checkGPS() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.interval = 8000
        locationRequest.fastestInterval = 4000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                getDeviceLocation()
            } else {
                CheckLocationPermission.checkPermission(this)
            }
        }
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                val resolvable: ResolvableApiException = it
                try {
                    // 위치 정보 다이얼로그 띄움
                    resolvable.startResolutionForResult(this, LOCATION_RESULT_CODE)
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initToolbar() {
        tv_toolbar_center.text = "위치 설정"
        imv_toolbar_left.setOnClickListener {
            val intent = Intent()
            intent.putExtra("lat", 0.0)
            intent.putExtra("long", 0.0)
            intent.putExtra("location", "")
            setResult(100, intent)
            finish()
        }
    }

    private fun initFusedLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private fun initMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapLocationFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initBottomSheet() {
        val bottomSheetLayout: LinearLayoutCompat = findViewById(R.id.bottom_sheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("lat", 0.0)
        intent.putExtra("long", 0.0)
        intent.putExtra("location", "")
        setResult(100, intent)
        finish()
    }
}

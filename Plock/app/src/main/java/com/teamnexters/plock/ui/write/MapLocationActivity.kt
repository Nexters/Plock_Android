package com.teamnexters.plock.ui.write

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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
import com.teamnexters.plock.extensions.start
import com.teamnexters.plock.util.CheckLocationPermission
import com.teamnexters.plock.util.MapTools
import kotlinx.android.synthetic.main.activity_map_location.*
import kotlinx.android.synthetic.main.bottom_sheet_map.*
import java.io.IOException
import java.util.*

class MapLocationActivity : AppCompatActivity(),
    GoogleMap.OnCameraMoveCanceledListener,
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveStartedListener,
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    // 기기의 마지막 위치 저장
    private lateinit var lastKnownLocation: Location
    private lateinit var locationCallback: LocationCallback
    private val LOCATION_RESULT_CODE = 100

    private lateinit var geoCoder: Geocoder
    private var a = 0
    private var latitude = 0.0
    private var longitude = 0.0
    private var locationName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_location)

        initToolbar()
        initFusedLocation()
        initMapFragment()
        initBottomSheet()
        geoCoder = Geocoder(applicationContext, Locale.KOREAN)

        sheet_custom_layout.setOnClickListener { start(FindLocationActivity::class) }
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

    override fun onCameraMoveCanceled() {
    }

    override fun onCameraIdle() {
        if (a == 0) {
            a++
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
                    cardETxt.text = mResultList[0].getAddressLine(0)
                    locationName = mResultList[0].getAddressLine(0)
                    Log.e("address", "주소 = ${mResultList[0].getAddressLine(0)}")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCameraMove() {
    }

    override fun onCameraMoveStarted(reason: Int) {
        when (reason) {
            // 유저 제스처에 따라
            GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> Log.e("camera", "The user gestured on the map")
            GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION -> Log.e(
                "camera",
                "The user tapped something on the map"
            )
            // 앱이 카메라 움직임 시작했을 때
            GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> Log.e(
                "camera",
                "The app moved the camera"
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = MapTools.configActivityMaps(googleMap)
        mMap.setOnCameraIdleListener(this)
        mMap.setOnCameraMoveCanceledListener(this)
        mMap.setOnCameraMoveStartedListener(this)
        mMap.setOnCameraMoveListener(this)
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
                    CheckLocationPermission.checkPermission(this, mMap, null)
                }
            } else {
                finish()
            }
        }
    }

    /*
    * 기기의 현재 위치를 가져오는 메소드
    * 현재 위치를 가져오기 위해서 fused location client provider 사용
    * 마지막 위치를 fused location client provider 에게 물어보고 분기
    */
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
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
                                    cardETxt.text = mResultList[0].getAddressLine(0)
                                    locationName = mResultList[0].getAddressLine(0)
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
                            /*
                            *   2가지 생성 ( location request & callback )
                            */
                            val locationRequest: LocationRequest = LocationRequest.create()
                            locationRequest.interval = 10000
                            locationRequest.fastestInterval = 5000
                            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                            /*
                            *   콜백은 위치가 업데이트 되었을 때 실행됨
                            */
                            locationCallback = object : LocationCallback() {
                                override fun onLocationResult(result: LocationResult?) {
                                    super.onLocationResult(result)
                                    /*
                                    *   업데이트 된 위치가 아직도 null 일 때
                                    */
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
                                            cardETxt.text = mResultList[0].getAddressLine(0)
                                            locationName = mResultList[0].getAddressLine(0)
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

                                    /*
                                    *   위치 업데이트를 제거하는 것 중요! ( prevent recursion of location updates )
                                    */
                                    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                                }
                            }
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
                        }
                    } else {
                        Toast.makeText(applicationContext, "Unable to get last Location", Toast.LENGTH_SHORT).show()
                        //CheckLocationPermission.checkPermission(this@MapLocationActivity, mMap, null)
                        //checkPermission()
                    }
                }

            })
    }

    private fun checkGPS() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
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
                CheckLocationPermission.checkPermission(this, mMap, null)
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
        val textView = findViewById<TextView>(R.id.tv_toolbar_center)
        textView.text = "위치 설정"
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
}

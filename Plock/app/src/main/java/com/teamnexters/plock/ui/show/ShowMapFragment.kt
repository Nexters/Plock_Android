package com.teamnexters.plock.ui.show

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.data.provideTimeCapsuleDao
import com.teamnexters.plock.extensions.runOnIoScheduler
import com.teamnexters.plock.ui.detailcard.DetailCardActivity
import com.teamnexters.plock.ui.show.model.Location
import com.teamnexters.plock.util.MapTools

class ShowMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnMarkerClickListener {

    lateinit var list: ArrayList<TimeCapsule>
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var locationBtn: View
    private lateinit var mapView: View

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var markerRootView: View
    private lateinit var markerPhoto: AppCompatImageView
    private lateinit var markerLock: RelativeLayout
    private lateinit var markerPhotoNum: AppCompatTextView
    private lateinit var hashMap: HashMap<Location, ArrayList<TimeCapsule>>

    private var saveMarker: HashMap<Location, Marker> = hashMapOf()

    private lateinit var lastKnownLocation: android.location.Location

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        if (calculateDistance(
                p0?.position?.latitude!!,
                p0.position?.longitude!!,
                lastKnownLocation.latitude,
                lastKnownLocation.longitude
            ) < 100
        ) {
            activity?.let {
                val intent = Intent(context, DetailCardActivity::class.java)
                val loc = Location(p0.position?.latitude!!, p0.position?.longitude!!, null)
                intent.putExtra("list", hashMap[loc])
                startActivityForResult(intent, 5)
            }
        } else {
            Toast.makeText(context, "근처로 이동해야 열람하실 수 있습니다!", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            5 -> {
                if (data != null) {
                    val intentList = data.getSerializableExtra("list") as ArrayList<TimeCapsule>
                    val isBack = data.getBooleanExtra("back", true)
                    // 단일 갯수
                    if (isBack) {
                        val markerName = saveMarker[Location(intentList[0].latitude, intentList[0].longitude, null)]
                        markerName?.remove()
                        saveMarker[Location(intentList[0].latitude, intentList[0].longitude, null)]?.remove()
                        for (i in intentList.indices) {
                            hashMap[Location(
                                intentList[0].latitude,
                                intentList[0].longitude,
                                null
                            )]?.remove(intentList[i])
                        }

                        markerLock.visibility = View.INVISIBLE
                        markerPhotoNum.visibility = View.VISIBLE
                        markerPhotoNum.text = (hashMap[Location(
                            intentList[0].latitude,
                            intentList[0].longitude,
                            null
                        )]?.size!!).toString()

                        val byteArray =
                            hashMap[Location(intentList[0].latitude, intentList[0].longitude, null)]?.get(0)?.photo
                        val photo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size!!)
                        markerPhoto.setImageBitmap(photo)

                        val latLng = LatLng(intentList[0].latitude, intentList[0].longitude)
                        val markerOptions = MarkerOptions()
                        markerOptions.position(latLng)
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerRootView)))

                        val marker: Marker = mMap.addMarker(markerOptions)
                        mMap.addMarker(markerOptions)
                        saveMarker[Location(intentList[0].latitude, intentList[0].longitude, null)] = marker
                    } else {
                        val markerName = saveMarker[Location(intentList[0].latitude, intentList[0].longitude, null)]
                        markerName?.remove()
                    }
                }
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = MapTools.configActivityMaps(googleMap)
            mMap.setOnMarkerClickListener(this)
            mMap.setOnMyLocationButtonClickListener(this)
            locationBtn = (mapView.findViewById<View>(Integer.parseInt("1")).parent as View)
            locationBtn = locationBtn.rootView.findViewById(Integer.parseInt("2"))
            mMap.setOnCameraMoveListener(this)
            mMap.setOnCameraIdleListener(this)

            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
                locationBtn.visibility = View.GONE
            } else {
                checkPermission(activity!!)
            }
        }
    }

    private fun addMarker() {
        for (i in hashMap.keys) {
            val byteArray = hashMap[i]?.get(0)?.photo
            val photo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size!!)

            markerPhoto.setImageBitmap(photo)

            if (hashMap[i]?.size!! > 1) {
                markerPhotoNum.visibility = View.VISIBLE
                markerPhotoNum.text = hashMap[i]?.size!!.toString()
            } else {
                markerPhotoNum.visibility = View.INVISIBLE
            }

            val latLng = LatLng(i.latitude, i.longitude)
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)

            if (calculateDistance(
                    i.latitude,
                    i.longitude,
                    lastKnownLocation.latitude,
                    lastKnownLocation.longitude
                ) < 100
            ) {
                markerLock.visibility = View.INVISIBLE
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerRootView)))
                val marker = mMap.addMarker(markerOptions)

                marker.tag = true
                saveMarker[i] = marker
            } else {
                markerLock.visibility = View.VISIBLE
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerRootView)))
                val marker = mMap.addMarker(markerOptions)
                marker.tag = false
                saveMarker[i] = marker
            }
        }
    }

    override fun onCameraMove() {
    }

    override fun onCameraIdle() {
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 8000
            fastestInterval = 4000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun callbackLocation() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                lastKnownLocation = locationResult.lastLocation
                // 마커 체크
                for (i in saveMarker.values) {
                    val lat = i.position.latitude
                    val long = i.position.longitude
                    // 범위 내로 들어올 때
                    if (calculateDistance(
                            lat,
                            long,
                            lastKnownLocation.latitude,
                            lastKnownLocation.longitude
                        ) < 100
                    ) {
                        // 해당 마커가 범위 안으로 들어왔을 때
                        if (i.tag == false) {
                            val markerName = saveMarker[Location(lat, long, null)]
                            markerName?.remove()

                            val byteArray = hashMap[Location(lat, long, null)]?.get(0)?.photo
                            val photo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size!!)
                            markerPhoto.setImageBitmap(photo)

                            if (hashMap[Location(lat, long, null)]?.size!! > 1) {
                                markerPhotoNum.visibility = View.VISIBLE
                                markerPhotoNum.text = hashMap[Location(lat, long, null)]?.size!!.toString()
                            } else {
                                markerPhotoNum.visibility = View.INVISIBLE
                            }

                            markerLock.visibility = View.INVISIBLE

                            val latLng = LatLng(lat, long)
                            val markerOptions = MarkerOptions()
                            markerOptions.position(latLng)
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerRootView)))

                            val marker = mMap.addMarker(markerOptions)
                            marker.tag = true
                            saveMarker[Location(lat, long, null)] = marker
                        }
                    } else {
                        // 밖으로 나가게 되었을 때
                        if (i.tag == true) {
                            val markerName = saveMarker[Location(lat, long, null)]
                            markerName?.remove()

                            val byteArray = hashMap[Location(lat, long, null)]?.get(0)?.photo
                            val photo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size!!)
                            markerPhoto.setImageBitmap(photo)

                            if (hashMap[Location(lat, long, null)]?.size!! > 1) {
                                markerPhotoNum.visibility = View.VISIBLE
                                markerPhotoNum.text = hashMap[Location(lat, long, null)]?.size!!.toString()
                            } else {
                                markerPhotoNum.visibility = View.INVISIBLE
                            }

                            markerLock.visibility = View.VISIBLE

                            val latLng = LatLng(lat, long)
                            val markerOptions = MarkerOptions()
                            markerOptions.position(latLng)
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerRootView)))

                            val marker = mMap.addMarker(markerOptions)
                            marker.tag = false

                            saveMarker[Location(lat, long, null)] = marker
                        }
                    }
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findMarkerView()
        runOnIoScheduler {
            list = ArrayList(provideTimeCapsuleDao(context!!).loadAllTimeCapsule())
        }

        callbackLocation()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_show_map, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.showMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapView = mapFragment.view!!

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
        getDeviceLocation()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        hashMap = hashMapOf()
        if (this::list.isInitialized) {
            for (i in list) {
                if (hashMap.containsKey(Location(i.latitude, i.longitude, null))) {
                    hashMap[Location(i.latitude, i.longitude, null)]?.add(i)
                } else {
                    hashMap[Location(i.latitude, i.longitude, null)] = arrayListOf(i)
                }
            }
        } else {
        }

        setFabVisible()
        setFabClickListener()
    }

    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener(object : OnCompleteListener<android.location.Location> {
                override fun onComplete(task: Task<android.location.Location>) {
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            lastKnownLocation = task.result!!
                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude),
                                    15F
                                )
                            )
                            addMarker()
                        } else {
                            val locationRequest: LocationRequest = LocationRequest.create()
                            locationRequest.interval = 8000
                            locationRequest.fastestInterval = 4000
                            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                            locationCallback = object : LocationCallback() {
                                override fun onLocationResult(result: LocationResult?) {
                                    super.onLocationResult(result)
                                    if (result == null) return

                                    lastKnownLocation = result.lastLocation
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
                        Toast.makeText(context, "위치를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun setFabVisible() {
        val fab_location = activity?.findViewById<FloatingActionButton>(R.id.fab_location_show)
        fab_location?.show()
    }

    private fun setFabClickListener() {
        activity?.findViewById<FloatingActionButton>(R.id.fab_location_show)?.setOnClickListener {
            locationBtn.callOnClick()
        }
    }

    private fun findMarkerView() {
        markerRootView = LayoutInflater.from(context).inflate(R.layout.marker_custom, null)
        markerPhoto = markerRootView.findViewById(R.id.marker_photo)
        markerLock = markerRootView.findViewById(R.id.marker_locked_layout)
        markerPhotoNum = markerRootView.findViewById(R.id.marker_photo_num_txv)
    }

    private fun createBitmapFromView(v: View): Bitmap {
        v.layoutParams = FrameLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        v.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            v.measuredWidth,
            v.measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        val c = Canvas(bitmap)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return bitmap
    }

    private fun checkPermission(activity: Activity) {
        Dexter.withActivity(activity)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    mMap.isMyLocationEnabled = true
                    locationBtn.visibility = View.GONE
                    getDeviceLocation()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    activity.finish()
                    Toast.makeText(activity, "앱 내 위치권한을 확인하세요", Toast.LENGTH_SHORT).show()
                }
            })
            .check()
    }
}



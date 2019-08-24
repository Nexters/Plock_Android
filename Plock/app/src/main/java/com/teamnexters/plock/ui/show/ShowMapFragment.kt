package com.teamnexters.plock.ui.show

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.data.provideTimeCapsuleDao
import com.teamnexters.plock.extensions.runOnIoScheduler
import com.teamnexters.plock.ui.detailcard.DetailCardActivity
import com.teamnexters.plock.ui.show.model.Location
import com.teamnexters.plock.util.CheckLocationPermission
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

    private var saveMarker: HashMap<Location, MarkerOptions> = hashMapOf()

    private lateinit var lastKnownLocation: android.location.Location

    override fun onMarkerClick(p0: Marker?): Boolean {
        if (((p0?.position?.latitude!! < lastKnownLocation.latitude + 0.01) && (p0.position?.longitude!! < lastKnownLocation.longitude + 0.01)
                    && (p0.position?.latitude!! > lastKnownLocation.latitude - 0.01) && (p0.position.longitude > lastKnownLocation.longitude - 0.01))
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
                // hashMap.clear()
                if (data != null) {
                    val intentList = data.getSerializableExtra("list") as ArrayList<TimeCapsule>
                    for (i in intentList) {
                        saveMarker.remove(Location(i.latitude, i.longitude, i.date))
                    }
                    //mMap.clear()
                }

                runOnIoScheduler {
                    list = ArrayList(provideTimeCapsuleDao(context!!).loadAllTimeCapsule())
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
                // Show rationale and request permission.
                CheckLocationPermission.checkPermission(activity!!, mMap, locationBtn)
            }

            //addMarker()

        } else {
            Log.e("error", "Error loading google Map")
        }

    }

    override fun onCameraMove() {
    }

    override fun onCameraIdle() {
        // startLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        Log.e("dd", "onResume")
//        hashMap = hashMapOf()
//        for (i in list) {
//            Log.e("L", "LIST")
//            if (hashMap.containsKey(Location(i.latitude, i.longitude, null))) {
//                hashMap[Location(i.latitude, i.longitude, null)]?.add(i)
//            } else {
//                hashMap[Location(i.latitude, i.longitude, null)] = arrayListOf(i)
//            }
//        }
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findMarkerView()
        runOnIoScheduler {
            list = ArrayList(provideTimeCapsuleDao(context!!).loadAllTimeCapsule())
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                lastKnownLocation = locationResult.lastLocation

                for (location in locationResult.locations) {
                    mMap.clear()

                    for (i in hashMap.keys) {
                        if (((i.latitude < location.latitude + 0.01) && (i.longitude < location.longitude + 0.01)
                                    && (i.latitude > location.latitude - 0.01) && (i.longitude > location.longitude - 0.01))
                        ) {
                            markerLock.visibility = View.INVISIBLE
                        } else {
                            markerLock.visibility = View.VISIBLE
                        }

                        if (hashMap[i]?.size!! > 1) {
                            markerPhotoNum.visibility = View.VISIBLE
                            markerPhotoNum.text = hashMap[i]?.size!!.toString()
                        } else {
                            markerPhotoNum.visibility = View.INVISIBLE
                        }

                        val byteArray = hashMap[i]?.get(0)?.photo
                        val photo = BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size!!)
                        markerPhoto.setImageBitmap(photo)

                        val latLng = LatLng(i.latitude, i.longitude)
                        val markerOptions = MarkerOptions()
                        markerOptions.position(latLng)
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerRootView)))

                        mMap.addMarker(markerOptions)
                        saveMarker[i] = markerOptions
                    }

                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_show_map, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.showMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapView = mapFragment.view!!

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
        hashMap = hashMapOf()
        for (i in list) {
            Log.e("L", "LIST")
            if (hashMap.containsKey(Location(i.latitude, i.longitude, null))) {
                hashMap[Location(i.latitude, i.longitude, null)]?.add(i)
            } else {
                hashMap[Location(i.latitude, i.longitude, null)] = arrayListOf(i)
            }
        }
        setFabVisible()
        setFabClickListener()
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
}



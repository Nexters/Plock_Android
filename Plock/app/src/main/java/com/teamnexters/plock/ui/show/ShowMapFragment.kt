package com.teamnexters.plock.ui.show

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.teamnexters.plock.R
import com.teamnexters.plock.util.MapTools
import kotlin.math.absoluteValue

class ShowMapFragment : Fragment(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var locationBtn: View
    private lateinit var mapView: View

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = MapTools.configActivityMaps(googleMap)
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true

                locationBtn = (mapView.findViewById<View>(Integer.parseInt("1")).parent as View)
                locationBtn = locationBtn.rootView.findViewById(Integer.parseInt("2"))

                locationBtn.visibility = View.GONE
            } else {
                // Show rationale and request permission.
                Log.e("error", "Error Permission")
            }

            mMap.setOnMyLocationClickListener {
                Toast.makeText(context, "d" + {it.latitude.absoluteValue}, Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("error", "Error loading google Map")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


}


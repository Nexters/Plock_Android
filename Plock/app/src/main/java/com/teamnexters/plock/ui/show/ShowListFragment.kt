package com.teamnexters.plock.ui.show

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.data.provideTimeCapsuleDao
import com.teamnexters.plock.extensions.runOnIoScheduler
import com.teamnexters.plock.ui.show.adapter.ShowListAdapter
import com.teamnexters.plock.ui.show.model.Location
import kotlinx.android.synthetic.main.fragment_show_list.*

class ShowListFragment : Fragment() {

    lateinit var list: ArrayList<TimeCapsule>
    private lateinit var hashMap: LinkedHashMap<Location, ArrayList<TimeCapsule>>

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var lastKnownLocation: android.location.Location

    private lateinit var adapter: ShowListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initFusedLocation()
//        getDeviceLocation()

        runOnIoScheduler {
            list = ArrayList(provideTimeCapsuleDao(context!!).loadAllTimeCapsule())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_show_list, container, false)
        hashMap = linkedMapOf()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        for (i in list) {
//            if (hashMap.containsKey(Location(i.latitude, i.longitude, i.date))) {
//                hashMap[Location(i.latitude, i.longitude, i.date)]?.add(i)
//            } else {
//                hashMap[Location(i.latitude, i.longitude, i.date)] = arrayListOf(i)
//            }
//        }
        setFabInvisible()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            5 -> {
                if (data != null) {
                    val intentList = data.getSerializableExtra("list") as ArrayList<TimeCapsule>
                    adapter.removeItem(getKey(hashMap, intentList))
//                    hashMap.clear()
//                    adapter.notifyDataSetChanged()
                }

                runOnIoScheduler {
                    list = ArrayList(provideTimeCapsuleDao(context!!).loadAllTimeCapsule())
                }
            }
        }
    }

    private fun getKey(map: LinkedHashMap<Location, ArrayList<TimeCapsule>>, value: ArrayList<TimeCapsule>): Location? {
        for (key in map.keys) {
            if ((value[0].longitude == map[key]?.get(0)?.longitude) && (value[0].latitude == map[key]?.get(0)?.latitude)) {
                return key
            } else {
            }
        }
        return null
    }

    override fun onResume() {
        super.onResume()

        for (i in list) {
            if (hashMap.containsKey(Location(i.latitude, i.longitude, i.date))) {
                hashMap[Location(i.latitude, i.longitude, i.date)]?.add(i)
            } else {
                hashMap[Location(i.latitude, i.longitude, i.date)] = arrayListOf(i)
            }
        }
        getDeviceLocation()
    }

    override fun onPause() {
        super.onPause()
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener(object : OnCompleteListener<android.location.Location> {
                override fun onComplete(task: Task<android.location.Location>) {
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            lastKnownLocation = task.result!!

                            when {
                                list.size > 0 -> {
                                    show_list_layout.setImageResource(0)
                                    show_list_empty_txv.text = null
                                    adapter = ShowListAdapter(hashMap, context, lastKnownLocation, this@ShowListFragment)
                                    rv_show_list.adapter = adapter
                                }
                                else -> {
                                    show_list_layout.setImageResource(R.drawable.ic_list_empty)
                                    show_list_empty_txv.text = "아직 기록된 카드가 없어요..."
                                }
                            }


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

                                    /*
                                    *   위치 업데이트를 제거하는 것 중요! ( prevent recursion of location updates )
                                    */
                                    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                                }
                            }
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
                        }
                    } else {
                    }
                }
            })
    }

    private fun setFabInvisible() {
        val fab_location = activity?.findViewById<FloatingActionButton>(R.id.fab_location_show)
        fab_location?.hide()
    }

    private fun initFusedLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
    }
}
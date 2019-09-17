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
import com.teamnexters.plock.ui.detailcard.DetailCardActivity
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
    private lateinit var lockList: ArrayList<TimeCapsule>
    private lateinit var unlockList: ArrayList<TimeCapsule>
    private lateinit var allList: ArrayList<TimeCapsule>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFusedLocation()

        runOnIoScheduler {
            list = ArrayList(provideTimeCapsuleDao(context!!).loadAllTimeCapsule())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_show_list, container, false)
        hashMap = linkedMapOf()
        lockList = ArrayList()
        unlockList = ArrayList()
        allList = ArrayList()

        for (i in list) {
            if (hashMap.containsKey(Location(i.latitude, i.longitude, i.date))) {
                hashMap[Location(i.latitude, i.longitude, i.date)]?.add(i)
            } else {
                hashMap[Location(i.latitude, i.longitude, i.date)] = arrayListOf(i)
            }
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getDeviceLocation()
        setFabInvisible()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            5 -> {
                if (data != null) {
                    val intentList = data.getSerializableExtra("list") as ArrayList<TimeCapsule>
                    val isBack = data.getBooleanExtra("back", true)

                    if (isBack) {
                        for (i in intentList.indices) {
                            hashMap[Location(
                                intentList[0].latitude,
                                intentList[0].longitude,
                                intentList[0].date
                            )]?.remove(intentList[i])
                        }
                    } else {
                        adapter.removeItem(allList.indexOf(intentList[0]))
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener(object : OnCompleteListener<android.location.Location> {
                override fun onComplete(task: Task<android.location.Location>) {
                    if (task.isSuccessful) {
                        if (task.result != null) {
                            lastKnownLocation = task.result!!

                            for (i in hashMap.keys) {
                                if (calculateDistance(
                                        i.latitude,
                                        i.longitude,
                                        lastKnownLocation.latitude,
                                        lastKnownLocation.longitude
                                    ) < 100
                                ) {
                                    unlockList.add(hashMap[i]?.get(0)!!).and(true)
                                } else {
                                    lockList.add(hashMap[i]?.get(0)!!).and(false)

                                }
                            }

                            unlockList.sortByDescending { it.date }
                            lockList.sortByDescending { it.date }

                            allList.addAll(unlockList)
                            allList.addAll(lockList)

                            when {
                                list.size > 0 -> {
                                    show_list_layout.setImageResource(0)
                                    show_list_empty_txv.text = null
                                    adapter =
                                        ShowListAdapter(
                                            allList,
                                            context,
                                            lastKnownLocation,
                                            this@ShowListFragment
                                        )
                                    adapter.itemClick = object : ShowListAdapter.ItemClick {
                                        override fun onClick(view: View, mapPosition: TimeCapsule) {
                                            val intent = Intent(context, DetailCardActivity::class.java)
                                            intent.putExtra(
                                                "list",
                                                hashMap[Location(
                                                    mapPosition.latitude,
                                                    mapPosition.longitude,
                                                    mapPosition.date
                                                )]
                                            )
                                            startActivityForResult(intent, 5)
                                        }

                                    }
                                    rv_show_list.adapter = adapter
                                }
                                else -> {
                                    show_list_layout.setImageResource(R.drawable.ic_list_empty)
                                    show_list_empty_txv.text = "아직 기록된 카드가 없어요..."
                                }
                            }

                        } else {
                            val locationRequest: LocationRequest = LocationRequest.create()
                            locationRequest.interval = 10000
                            locationRequest.fastestInterval = 5000
                            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                            locationCallback = object : LocationCallback() {
                                override fun onLocationResult(result: LocationResult?) {
                                    super.onLocationResult(result)
                                    if (result == null) {
                                        return
                                    }

                                    lastKnownLocation = result.lastLocation
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

    fun changeView() {
        if (allList.isEmpty()) {
            show_list_layout.setImageResource(R.drawable.ic_list_empty)
            show_list_empty_txv.text = "아직 기록된 카드가 없어요..."
        } else {

        }
    }

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        // distance in meter
        return results[0]
    }
}
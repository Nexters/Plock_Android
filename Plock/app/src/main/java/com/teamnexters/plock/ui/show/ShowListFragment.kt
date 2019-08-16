package com.teamnexters.plock.ui.show

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.ui.show.adapter.ShowListAdapter
import kotlinx.android.synthetic.main.fragment_show_list.*
import java.util.*
import kotlin.collections.ArrayList

class ShowListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val showList = ArrayList<TimeCapsule>()
        showList.add(TimeCapsule("처음만난곳", Date(), "Seoul", 37.500853, 126.98738,
            "photo", "message"))
        showList.add(TimeCapsule("처음만난곳", Date(), "Seoul", 37.500853, 126.98738,
            "photo", "message"))
        showList.add(TimeCapsule("처음만난곳", Date(), "Seoul", 37.500853, 126.98738,
            "photo", "message"))
        showList.add(TimeCapsule("처음만난곳", Date(), "Seoul", 37.500853, 126.98738,
            "photo", "message"))
        showList.add(TimeCapsule("처음만난곳", Date(), "Seoul", 37.500853, 126.98738,
            "photo", "message"))
        showList.add(TimeCapsule("처음만난곳", Date(), "Seoul", 37.500853, 126.98738,
            "photo", "message"))
        showList.add(TimeCapsule("처음만난곳", Date(), "Seoul", 37.500853, 126.98738,
            "photo", "message"))
        showList.add(TimeCapsule("처음만난곳", Date(), "Seoul", 37.500853, 126.98738,
            "photo", "message"))

        val adapter = ShowListAdapter(showList, context)
        rv_show_list.adapter = adapter

        setFabInvisible()
    }

    private fun setFabInvisible() {
        val fab_location = activity?.findViewById<FloatingActionButton>(R.id.fab_location_show)
        fab_location?.hide()
    }
}
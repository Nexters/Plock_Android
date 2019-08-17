package com.teamnexters.plock.ui.show

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.data.provideTimeCapsuleDao
import com.teamnexters.plock.extensions.runOnIoScheduler
import com.teamnexters.plock.ui.show.adapter.ShowListAdapter
import kotlinx.android.synthetic.main.fragment_show_list.*

class ShowListFragment : Fragment() {

    lateinit var list : ArrayList<TimeCapsule>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runOnIoScheduler {
            list = ArrayList(provideTimeCapsuleDao(context!!).loadAllTimeCapsule())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val showList = ArrayList<TimeCapsule>()

        val adapter = ShowListAdapter(list, context)
        rv_show_list.adapter = adapter

        setFabInvisible()
    }

    private fun setFabInvisible() {
        val fab_location = activity?.findViewById<FloatingActionButton>(R.id.fab_location_show)
        fab_location?.hide()
    }
}
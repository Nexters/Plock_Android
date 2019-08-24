package com.teamnexters.plock.ui.write

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.teamnexters.plock.R
import kotlinx.android.synthetic.main.activity_find_location.*
import kotlinx.android.synthetic.main.toolbar_custom.*


class FindLocationActivity : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var predictionList: List<AutocompletePrediction>
    private lateinit var adapter: FindLocationAdapter
    private lateinit var strList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_location)

        initToolbar()
        initPlaceApi()
        initRvAdapter()

        find_location_editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val predictionsRequest =
                    FindAutocompletePredictionsRequest.builder()
                        .setCountry("pk")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(AutocompleteSessionToken.newInstance())
                        .setQuery(p0.toString())
                        .build()
                // then we need to pass predictionsRequest to places API
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val result = it.result
                        if (result != null) {
                            predictionList = result.autocompletePredictions
                            val suggestionList = ArrayList<String>()
                            for (i in predictionList.indices) {
                                val prediction = predictionList[i]
                                suggestionList.add(prediction.getFullText(null).toString())
                            }
                            adapter.filterList(suggestionList)
                        }
                    }
                }.addOnFailureListener {
                    Log.e("failure", it.message!!)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }

    private fun initToolbar() {
        tv_toolbar_center.text = "위치 설정"
        imv_toolbar_left.setOnClickListener { finish() }
    }

    private fun initPlaceApi() {
//      Initialize the SDK
        Places.initialize(applicationContext, getString(R.string.geo_api_key))

        // Create a new Places client instance
        placesClient = Places.createClient(this)
    }

    private fun initRvAdapter() {
        strList = ArrayList()
        adapter = FindLocationAdapter(this, strList)
        rv_find_location.adapter = adapter
    }
}

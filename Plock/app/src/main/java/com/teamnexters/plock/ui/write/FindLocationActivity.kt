package com.teamnexters.plock.ui.write

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.teamnexters.plock.R
import kotlinx.android.synthetic.main.activity_find_location.*

class FindLocationActivity : AppCompatActivity() {

    private lateinit var placesClient: PlacesClient
    private lateinit var predictionList: List<AutocompletePrediction>
    private lateinit var token: AutocompleteSessionToken
    private lateinit var adapter: FindLocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_location)

        initToolbar()
        initPlaceApi()
        initRvAdapter()

        find_location_editText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.clearItem()
                val predictionsRequest =
                    FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(p0?.toString())
                        .build()

                // then we need to pass predictionsRequest to places API
                placesClient.findAutocompletePredictions(predictionsRequest).addOnSuccessListener {
                    if (it != null) {
                        predictionList = it.autocompletePredictions
                        val suggestionList = java.util.ArrayList<String>()
                        for (index in predictionList.indices) {
                            val prediction: AutocompletePrediction = predictionList[index]
                            suggestionList.add(prediction.getFullText(null).toString())
                            Log.e("list", prediction.getFullText(null).toString())
                            adapter.addItem(prediction)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }

        })
    }

    private fun initToolbar() {
        val textView = findViewById<TextView>(R.id.tv_toolbar_center)
        textView.text = "위치 설정"
    }

    private fun initPlaceApi() {
        // Initialize the SDK
        Places.initialize(applicationContext, "AIzaSyBM83L9S7bUzGz0oZuLndHV2Z_i-NGEn48")

        // Create a new Places client instance
        placesClient = Places.createClient(this)

        token = AutocompleteSessionToken.newInstance()
    }

    private fun initRvAdapter() {
        val strList = ArrayList<String>()
        adapter = FindLocationAdapter(strList, applicationContext)
        rv_find_location.adapter = adapter
    }
}

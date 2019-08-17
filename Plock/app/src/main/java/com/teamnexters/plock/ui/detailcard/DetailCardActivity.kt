package com.teamnexters.plock.ui.detailcard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.data.provideTimeCapsuleDao
import com.teamnexters.plock.extensions.plusAssign
import com.teamnexters.plock.extensions.px
import com.teamnexters.plock.rx.AutoClearedDisposable
import kotlinx.android.synthetic.main.activity_detail_card.*
import kotlinx.android.synthetic.main.toolbar_custom.*
import java.util.*



class DetailCardActivity : AppCompatActivity() {
    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    internal val viewModelFactory by lazy {
        DetailCardViewModelFactory(provideTimeCapsuleDao(this))
    }
    lateinit var viewModel: DetailCardViewModel
    lateinit var list: ArrayList<TimeCapsule>
    lateinit var adapter: CardPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.teamnexters.plock.R.layout.activity_detail_card)
        initToolbar()
        initCardSize()

        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[DetailCardViewModel::class.java]

        lifecycle += disposables
        lifecycle += viewDisposables

        if (intent.extras != null) {
            list = intent.getSerializableExtra("list") as ArrayList<TimeCapsule>
        }

        runOnUiThread {
            adapter = CardPagerAdapter(this, list)
            cardViewPager.adapter = adapter
        }


        imv_toolbar_right.setOnClickListener {
            deleteCard()
            adapter.notifyDataSetChanged()
        }
    }

    private fun deleteCard() {
        val deleteItem = list.get(cardViewPager.currentItem)
        viewModel.deleteCard(deleteItem)
        list.remove(deleteItem)

        if (list.size == 0) finish()
    }

    private fun initCardSize(){
        val margin = 35.px

        cardViewPager.run {
            clipToPadding = false
            setPadding(margin, 0, margin, 16)
            pageMargin = margin / 4
        }
    }

    private fun initToolbar() {
        imv_toolbar_right.setImageResource(com.teamnexters.plock.R.drawable.ic_delete)
        imv_toolbar_left.setOnClickListener { finish() }
    }
}
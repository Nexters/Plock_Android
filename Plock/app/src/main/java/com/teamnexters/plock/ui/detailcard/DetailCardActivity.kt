package com.teamnexters.plock.ui.detailcard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.data.provideTimeCapsuleDao
import com.teamnexters.plock.extensions.plusAssign
import com.teamnexters.plock.extensions.runOnIoScheduler
import com.teamnexters.plock.rx.AutoClearedDisposable
import com.teamnexters.plock.ui.writecard.WriteCardViewModel
import com.teamnexters.plock.ui.writecard.WriteCardViewModelFactory
import kotlinx.android.synthetic.main.activity_detail_card.*
import kotlinx.android.synthetic.main.toolbar_custom.*

class DetailCardActivity : AppCompatActivity() {
    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    internal val viewModelFactory by lazy {
        DetailCardViewModelFactory(provideTimeCapsuleDao(this))
    }
    lateinit var viewModel: DetailCardViewModel
    lateinit var list: List<TimeCapsule>
    lateinit var adapter: CardPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_card)
        initToolbar()

        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[DetailCardViewModel::class.java]

        lifecycle += disposables
        lifecycle += viewDisposables

        cardViewPager.run {
            val dpValue = 40
            val d = resources.displayMetrics.density
            val margin = (dpValue * d).toInt()

            clipToPadding = false
            setPadding(margin, 0, margin, 0)
            pageMargin = margin / 2

        }
        runOnIoScheduler {
            list = provideTimeCapsuleDao(this).loadAllTimeCapsule()
            runOnUiThread {
                adapter = CardPagerAdapter(list)
                cardViewPager.adapter = adapter
            }

            imv_toolbar_right.setOnClickListener {
                deleteCard()
            }
        }
    }

    private fun deleteCard() {
        viewModel.deleteCard(list.get(cardViewPager.currentItem))
    }

    private fun initToolbar() {
        tv_toolbar_center.text = "작성"
        imv_toolbar_right.setImageResource(R.drawable.ic_delete)
    }
}
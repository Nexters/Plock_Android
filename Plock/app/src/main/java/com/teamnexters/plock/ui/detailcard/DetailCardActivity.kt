package com.teamnexters.plock.ui.detailcard

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.teamnexters.plock.R
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.data.provideTimeCapsuleDao
import com.teamnexters.plock.extensions.plusAssign
import com.teamnexters.plock.extensions.px
import com.teamnexters.plock.rx.AutoClearedDisposable
import kotlinx.android.synthetic.main.activity_detail_card.*
import kotlinx.android.synthetic.main.dialog_two_button.view.*
import kotlinx.android.synthetic.main.toolbar_custom.*

class DetailCardActivity : AppCompatActivity() {
    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    internal val viewModelFactory by lazy {
        DetailCardViewModelFactory(provideTimeCapsuleDao(this))
    }
    lateinit var viewModel: DetailCardViewModel
    lateinit var list: ArrayList<TimeCapsule>
    lateinit var adapter: CardPagerAdapter
    private var tempList: ArrayList<TimeCapsule> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_card)
        initToolbar()
        initCardSize()

        tempList.clear()

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
            showDeleteDialog()
        }
    }

    private fun deleteCard() {
        val deleteItem = list.get(cardViewPager.currentItem)
        viewModel.deleteCard(deleteItem)
        tempList.add(deleteItem)
        list.remove(deleteItem)

        val intent = Intent()

        if (list.size == 0) {
            intent.putExtra("list", tempList)
            setResult(5, intent)
            finish()
        } else {
            tempList.add(deleteItem)
        }

        adapter.notifyDataSetChanged()
    }

    private fun showDeleteDialog() {
        layoutInflater.inflate(R.layout.dialog_two_button, null).run {
            val dialog = AlertDialog.Builder(this@DetailCardActivity)
                .setView(this)
                .show()

            setUpDialogSize(dialog)
            okBtn.text = "삭제"
            cancelBtn.setOnClickListener { dialog.dismiss() }
            okBtn.setOnClickListener {
                deleteCard()
                dialog.dismiss()
            }
        }
    }

    private fun setUpDialogSize(dialog: Dialog){
        val width = (resources.displayMetrics.widthPixels * 0.60).toInt()
        val height = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.apply {
            setLayout(width, height)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun initCardSize() {
        val margin = 35.px

        cardViewPager.run {
            clipToPadding = false
            setPadding(margin, 0, margin, 16)
            pageMargin = margin / 4
        }
    }

    private fun initToolbar() {
        imv_toolbar_right.setImageResource(R.drawable.ic_delete)
        imv_toolbar_left.setOnClickListener { finish() }
    }
}
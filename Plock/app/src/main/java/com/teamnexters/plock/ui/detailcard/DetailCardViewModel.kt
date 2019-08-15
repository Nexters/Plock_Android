package com.teamnexters.plock.ui.detailcard

import androidx.lifecycle.ViewModel
import com.teamnexters.plock.data.dao.TimeCapsuleDao
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.extensions.runOnIoScheduler
import io.reactivex.disposables.Disposable


class DetailCardViewModel(val timeCapsuleDao: TimeCapsuleDao) : ViewModel() {

    fun deleteCard(timeCapsule: TimeCapsule): Disposable
            = runOnIoScheduler { timeCapsuleDao.deleteTimeCapsule(timeCapsule) }
}
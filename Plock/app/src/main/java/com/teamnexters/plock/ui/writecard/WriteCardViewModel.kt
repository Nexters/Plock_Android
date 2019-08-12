package com.teamnexters.plock.ui.writecard

import androidx.lifecycle.ViewModel
import com.teamnexters.plock.data.dao.TimeCapsuleDao
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.extensions.runOnIoScheduler
import io.reactivex.disposables.Disposable

class WriteCardViewModel(val timeCapsuleDao: TimeCapsuleDao) : ViewModel() {

    fun saveTimeCapsule(timeCapsule: TimeCapsule): Disposable
            = runOnIoScheduler { timeCapsuleDao.saveTimeCapsule(timeCapsule) }
}
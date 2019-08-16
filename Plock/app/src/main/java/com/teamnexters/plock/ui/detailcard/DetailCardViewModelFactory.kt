package com.teamnexters.plock.ui.detailcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.teamnexters.plock.data.dao.TimeCapsuleDao


class DetailCardViewModelFactory(val timeCapsuleDao: TimeCapsuleDao): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DetailCardViewModel(timeCapsuleDao) as T
    }

}
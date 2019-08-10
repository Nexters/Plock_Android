package com.teamnexters.plock.ui.writecard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.teamnexters.plock.data.dao.TimeCapsuleDao

class WriteCardViewModelFactory(val timeCapsuleDao: TimeCapsuleDao):ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WriteCardViewModel(timeCapsuleDao) as T
    }

}
package com.teamnexters.plock.data

import android.content.Context
import androidx.room.Room
import com.teamnexters.plock.data.dao.TimeCapsuleDao

private var instance: TimeCapsuleDatabase? = null

fun provideTimeCapsuleDao(context: Context): TimeCapsuleDao = provideDatabase(context).TimeCapsuleDao()

private fun provideDatabase(context: Context): TimeCapsuleDatabase {
    if (instance == null) {
        instance = Room.databaseBuilder(
            context.applicationContext,
            TimeCapsuleDatabase::class.java, "plock.db"
        )
            .build();
    }
    return instance!!
}
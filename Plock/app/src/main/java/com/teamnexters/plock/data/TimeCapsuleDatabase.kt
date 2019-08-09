package com.teamnexters.plock.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.teamnexters.plock.data.dao.TimeCapsuleDao
import com.teamnexters.plock.data.entity.TimeCapsule

@Database(entities = [(TimeCapsule::class)], version = 1)
abstract class TimeCapsuleDatabase : RoomDatabase() {
    abstract fun TimeCapsuleDao(): TimeCapsuleDao
}
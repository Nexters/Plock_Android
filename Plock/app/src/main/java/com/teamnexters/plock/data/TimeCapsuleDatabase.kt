package com.teamnexters.plock.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.teamnexters.plock.data.dao.TimeCapsuleDao
import com.teamnexters.plock.data.entity.TimeCapsule
import com.teamnexters.plock.util.TimestampConverter

@Database(entities = [(TimeCapsule::class)], version = 1)
@TypeConverters(TimestampConverter::class)
abstract class TimeCapsuleDatabase : RoomDatabase() {
    abstract fun TimeCapsuleDao(): TimeCapsuleDao
}
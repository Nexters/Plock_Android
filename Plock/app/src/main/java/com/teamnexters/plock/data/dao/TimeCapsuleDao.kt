package com.teamnexters.plock.data.dao

import androidx.room.*
import com.teamnexters.plock.data.entity.TimeCapsule

@Dao
interface TimeCapsuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTimeCapsule(timeCapsule: TimeCapsule)

    @Delete
    fun deleteTimeCapsule(timeCapsule: TimeCapsule)

    @Query("SELECT * FROM time_capsule")
    fun loadAllTimeCapsule(): List<TimeCapsule>

    @Query("SELECT * FROM time_capsule WHERE latitude=:latitude AND longitude=:longitude ")
    fun loadSamePlaceTimeCapsule(latitude: Double, longitude: Double): List<TimeCapsule>
}
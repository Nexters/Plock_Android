package com.teamnexters.plock.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "time_capsule")
data class TimeCapsule(
    @PrimaryKey val id: String,
    val title: String,
    val date: Date,
    @ColumnInfo(name="place_name") val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val photo: String,
    val message: String
)
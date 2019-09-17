package com.teamnexters.plock.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "time_capsule")
data class TimeCapsule(
    val title: String,
    val date: Date,
    @ColumnInfo(name = "place_name") val placeName: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val photo: ByteArray,
    val message: String?
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @Ignore var isBackVisible: Boolean = false

    override fun equals(other: Any?): Boolean {
        val obj = other as TimeCapsule
        return (this.latitude == obj.latitude) &&
                (this.longitude == obj.longitude) && (this.date == obj.date) && (this.message == obj.message) &&
                (this.placeName == obj.placeName) && (this.title == obj.title) && (this.id == obj.id)
    }
}
package com.sensor.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "OrientationData")
data class OrientationData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val xAngle: Float,
    val yAngle: Float,
    val zAngle: Float
)


package com.sensor.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrientationDao {
    @Insert
    suspend fun insert(orientationData: OrientationData)

    @Query("SELECT * FROM OrientationData ORDER BY id ASC")
    fun getAllOrientationData(): Flow<List<OrientationData>>
}

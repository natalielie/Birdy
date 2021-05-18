package com.example.birdyapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_birds")
data class OfflineBirdsModel(
    val lat: Double,
    val longitude: Double,
    val photo: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null
)

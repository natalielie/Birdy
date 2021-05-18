package com.example.birdyapp.features.sign_up.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class User (
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("middle_name")
    val middleName: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("b_day")
    val bDay: Date
)
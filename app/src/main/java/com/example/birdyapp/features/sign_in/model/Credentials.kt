package com.example.birdyapp.features.sign_in.model

import com.google.gson.annotations.SerializedName

data class Credentials (
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)
package com.example.birdyapp.identity

import com.example.birdyapp.features.sign_up.model.UserFields

interface KycProvider {
    fun setKyc(credentials: UserFields?)
    fun getKyc(): UserFields?
}
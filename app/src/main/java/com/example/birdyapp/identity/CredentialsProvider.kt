package com.example.birdyapp.identity

import com.example.birdyapp.features.sign_in.model.Credentials

interface CredentialsProvider {
    fun setCredentials(credentials: Credentials?)
    fun getCredentials(): Credentials?
}
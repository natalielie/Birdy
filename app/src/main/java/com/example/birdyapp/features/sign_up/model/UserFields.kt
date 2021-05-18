package com.example.birdyapp.features.sign_up.model

import androidx.lifecycle.MutableLiveData
import java.util.*

class UserFields() {
    constructor(firstName : String,
                lastName  : String,
                middleName  : String,
                birthdayDate : Date,
                city  : String): this(){
        this.firstName.value = firstName
        this.lastName.value = lastName
        this.middleName.value = middleName
        this.birthdayDate.value = birthdayDate
        this.city.value = city
    }

    val firstName = MutableLiveData<String>()
    val middleName = MutableLiveData<String>()
    val lastName = MutableLiveData<String>()
    val birthdayDate = MutableLiveData<Date>()
    val city = MutableLiveData<String>()

    val fullName = MutableLiveData(firstName.value + " " + lastName.value)

    fun validate(): Boolean {
        return !(firstName.value.isNullOrBlank()
                || lastName.value.isNullOrBlank()
                || birthdayDate.value == null
                || city.value.isNullOrBlank())
    }
}
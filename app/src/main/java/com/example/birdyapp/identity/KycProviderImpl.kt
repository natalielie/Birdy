package com.example.birdyapp.identity

import android.content.Context
import com.example.birdyapp.features.sign_up.model.User
import com.example.birdyapp.features.sign_up.model.UserFields
import com.google.gson.Gson

class KycProviderImpl(
    val context: Context
) : KycProvider {
    private val info = context.getSharedPreferences("allWalletInfo", Context.MODE_PRIVATE)

    private var userInfo: UserFields? = null

    override fun setKyc(user: UserFields?) {
        if (user == null) {
            info.edit().clear().apply()
        } else {
            val prefsEditor = info.edit()
            val jsonData = User(
                user.firstName.value!!,
                user.lastName.value!!,
                user.middleName.value!!,
                user.city.value!!,
                user.birthdayDate.value!!
            )
            val newData = Gson().toJson(jsonData)

            prefsEditor.putString("userInfo", newData)
            prefsEditor.apply()

            Gson().fromJson(newData, User::class.java)
        }
        userInfo = user
    }

    override fun getKyc(): UserFields? {
        if (userInfo == null) {
            val wallet = info.getString("userInfo", null)
            Gson().fromJson(wallet, User::class.java).also {
                userInfo = UserFields(
                    it.firstName,
                    it.lastName,
                    it.middleName,
                    it.bDay,
                    it.city
                )
            }
            return userInfo
        }
        return userInfo
    }
}
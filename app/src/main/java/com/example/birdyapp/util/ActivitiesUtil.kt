package com.example.birdyapp.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import io.grpc.Channel
import io.grpc.ManagedChannelBuilder

object ActivitiesUtil {
    fun hideKeyboard(context: Activity) {
        val view = context.currentFocus
        if (view != null) {
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun initChannel(): Channel {
        return ManagedChannelBuilder
                //178.150.141.36
                //192.168.1.20
            .forAddress("178.150.141.36", 1488)
            .usePlaintext()
            .build()
    }
}
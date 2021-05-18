package com.example.birdyapp.util.input

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("isVisible")
fun View.isVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("shortDate")
fun TextView.shortDate(date: Date?) {
    if (date != null) {
        val locale = Locale.getDefault()
        this.text = SimpleDateFormat("dd MMM YYYY", locale).format(date)
    }
}

@BindingAdapter("isInVisible")
fun View.isInVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter("isEnabled")
fun Button.isEnabled(isEnabled: Boolean) {
    this.isEnabled = isEnabled
}

@BindingAdapter("isEnabled")
fun View.isEnabled(isEnabled: Boolean) {
    this.isEnabled = isEnabled
}

@BindingAdapter("isEnabled")
fun TextView.isEnabled(isEnabled: Boolean) {
    this.isEnabled = isEnabled
}
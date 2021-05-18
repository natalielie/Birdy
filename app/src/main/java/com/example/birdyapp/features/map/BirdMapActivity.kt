package com.example.birdyapp.features.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.birdyapp.R
import com.example.birdyapp.databinding.ActivityBirdMapBinding
import kotlinx.android.synthetic.main.toolbar_with_image.*
import kotlinx.android.synthetic.main.toolbar_with_image.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein

class BirdMapActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var binding: ActivityBirdMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bird_map)
        binding.lifecycleOwner = this

        initToolbar()
    }

    private fun initToolbar() {
        toolbar_with_image.title_text_view.text = getString(R.string.search)
    }
}
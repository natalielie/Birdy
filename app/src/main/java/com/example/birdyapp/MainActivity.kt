package com.example.birdyapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.birdyapp.databinding.ActivityMainBinding
import com.example.birdyapp.features.messages.MessagesFragment
import com.example.birdyapp.features.profile.ProfileFragment
import com.example.birdyapp.features.searching_by_name.view.OfflineFragment
import com.example.birdyapp.features.searching_by_name.view.SearchBirdByNameFragment
import com.example.birdyapp.features.sign_in.view.SignInActivity
import com.example.birdyapp.features.top.TopFragment
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.identity.KycProvider
import com.example.birdyapp.util.ActivitiesUtil.initChannel
import io.grpc.Channel
import io.grpc.ManagedChannel
import kotlinx.android.synthetic.main.toolbar_with_image.*
import kotlinx.android.synthetic.main.toolbar_with_image.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()

    private lateinit var binding: ActivityMainBinding
    private val credentialsProvider: CredentialsProvider by instance()
    private val kycProvider: KycProvider by instance()

    private lateinit var channel: Channel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        channel = initChannel()

        initBottomNavBar()
        initToolbar()
    }

    private fun initToolbar() {
        toolbar_with_image.title_text_view.text = getString(R.string.search)
        toolbar_with_image.log_out_imageView.setOnClickListener {
            credentialsProvider.setCredentials(null)
            kycProvider.setKyc(null)
            startActivity(
                Intent(
                    this,
                    SignInActivity::class.java
                )
            )
            finish()
        }
    }

    private fun initBottomNavBar() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener {

            val selectedFragment: Fragment = when (it.itemId) {
                R.id.find_bird -> {
                    SearchBirdByNameFragment.getInstance(channel)
                }
                R.id.top -> {
                    TopFragment.getInstance(channel)
                }
                R.id.messages -> {
                    //OfflineFragment.getInstance(channel)

                    MessagesFragment.getInstance(channel)
                }
                R.id.profile -> {
                    ProfileFragment.getInstance()
                }
                else -> return@setOnNavigationItemSelectedListener false
            }

            //this.onBackPressedListener = selectedFragment

            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment).commit()

            return@setOnNavigationItemSelectedListener true
        }
        binding.bottomNavigation.selectedItemId = R.id.find_bird
    }



    override fun onDestroy() {
        super.onDestroy()
        (channel as ManagedChannel?)?.shutdownNow()
    }

}
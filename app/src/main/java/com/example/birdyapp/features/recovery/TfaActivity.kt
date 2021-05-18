package com.example.birdyapp.features.recovery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.birdyapp.R
import com.example.birdyapp.Repository
import com.example.birdyapp.databinding.ActivityTfaBinding
import com.example.birdyapp.features.sign_in.view.SignInActivity
import com.example.birdyapp.features.sign_up.model.UserFields
import com.example.birdyapp.util.ActivitiesUtil
import com.example.birdyapp.util.ObservableTransformers
import com.example.birdyapp.util.ToastManager
import io.grpc.Channel
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_tfa.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.util.*

class TfaActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var channel: Channel
    private val toastManager: ToastManager by instance()
    private lateinit var email: String
    private lateinit var password: String

    val otpCode = MutableLiveData<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        email = intent.getStringExtra("email")!!
        password = intent.getStringExtra("password")!!

        val binding: ActivityTfaBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_tfa)
        binding.lifecycleOwner = this
        binding.activity = this

        channel = ActivitiesUtil.initChannel()
        initButtons()
    }

    private fun initButtons() {
        continue_button.setOnClickListener {
            verifyCode(otpInputLayout.editText?.text.toString())
        }
    }

    private fun verifyCode(token: String) {
        Repository(channel).verifyToken(token, email)
            .compose(ObservableTransformers.defaultSchedulersSingle())
            .subscribeBy(
                onSuccess = {
                    if (it.result) {
                        updateUser()
                    }
                },
                onError = {
                    it.printStackTrace()
                    toastManager.short("Something went wrong...")
                }
            )
    }

    private fun updateUser() {
        Repository(channel).updateUserInfo(
            UserFields("Tamara", "Gambarova", "-", Date(), "Kharkiv"),
            email, password
        ).compose(ObservableTransformers.defaultSchedulersCompletable())
            .subscribeBy(
                onComplete = {
                    toastManager.long("Password changed!")
                    openSignIn()
                },
                onError = {
                    it.printStackTrace()
                    toastManager.short("Something wrong")
                }
            )
    }

    private fun openSignIn() {
        startActivity(
            Intent(
                this,
                SignInActivity::class.java
            )
        )
    }
}
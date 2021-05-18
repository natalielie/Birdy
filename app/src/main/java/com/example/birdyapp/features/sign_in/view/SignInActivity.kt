package com.example.birdyapp.features.sign_in.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.birdyapp.MainActivity
import com.example.birdyapp.R
import com.example.birdyapp.Repository
import com.example.birdyapp.databinding.ActivitySignInBinding
import com.example.birdyapp.extensions.makeLinks
import com.example.birdyapp.features.recovery.RecoveryActivity
import com.example.birdyapp.features.sign_in.model.Credentials
import com.example.birdyapp.features.sign_up.view.SignUpActivity
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.identity.KycProvider
import com.example.birdyapp.util.ActivitiesUtil
import com.example.birdyapp.util.ConnectivityInterceptor
import com.example.birdyapp.util.ToastManager
import io.grpc.Channel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class SignInActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private val credentialsProvider: CredentialsProvider by instance()
    private val kycProvider: KycProvider by instance()
    private val connectivityInterceptor: ConnectivityInterceptor by instance()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var channel: Channel
    private val toastManager: ToastManager by instance()

    val isLoading = MutableLiveData(false)

    val fields: MutableMap<String, MutableLiveData<String>> = mutableMapOf(
        "email" to MutableLiveData(),
        "password" to MutableLiveData()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySignInBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        binding.lifecycleOwner = this
        binding.activity = this

        channel = ActivitiesUtil.initChannel()
        initButtons()
        initFields()
    }

    private fun initButtons() {
        login_button.setOnClickListener {
            if(ConnectivityInterceptor.isOnline(this)){
                ActivitiesUtil.hideKeyboard(this)
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(fields["email"]?.value!!).matches()) {
                    emailInputLayout.error = resources.getString(R.string.error_invalid_email)
                } else {
                    emailInputLayout.error = null
                }
                signIn(
                    fields["email"]?.value!!,
                    fields["password"]?.value!!
                )
            } else {
                toastManager.short(R.string.no_connection)
            }
        }

        forgot_password.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RecoveryActivity::class.java
                )
            )
        }
    }

    private fun initFields() {
        dont_have_account_text_view.makeLinks(
            Pair("Create one!", View.OnClickListener {
                startActivity(
                    Intent(
                        this,
                        SignUpActivity::class.java
                    )
                )
                finish()
            })
        )
    }

    private fun signIn(email: String, password: String)
    {
        Repository(channel).loginUser(email, password)
            .doOnSubscribe {
                progress_sign_in.visibility = View.VISIBLE
            }
            .subscribe({
                Log.d("res--", it.first.number.toString())
                when (it.first.number) {
                    0 -> {
                        kycProvider.setKyc(it.second)
                        goToMainActivity()
                    }
                    1 -> {
                        toastManager.short("Invalid password, try again!")
                        progress_sign_in.visibility = View.GONE
                    }
                    2 -> {
                        toastManager.short("User with this email was not found, try again!")
                        progress_sign_in.visibility = View.GONE
                    }
                }
            }, {
                toastManager.short("Something went wrong, try again!")
                progress_sign_in.visibility = View.GONE
            })

            .addTo(compositeDisposable)
        credentialsProvider.setCredentials(Credentials(email, password))
    }

    private fun goToMainActivity() {
        startActivity( Intent(
            this,
            MainActivity::class.java
        ))
        finish()
    }
}
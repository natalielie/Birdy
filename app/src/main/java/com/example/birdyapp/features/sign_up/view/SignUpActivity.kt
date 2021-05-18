package com.example.birdyapp.features.sign_up.view

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
import com.example.birdyapp.databinding.ActivitySignUpBinding
import com.example.birdyapp.features.sign_in.model.Credentials
import com.example.birdyapp.features.sign_up.model.UserFields
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.util.ActivitiesUtil
import com.example.birdyapp.util.ObservableTransformers
import com.example.birdyapp.util.ToastManager
import com.example.birdyapp.util.UserFlowFragmentDisplayer
import io.grpc.Channel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class SignUpActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val credentialsProvider: CredentialsProvider by instance()
    private lateinit var channel: Channel
    private val toastManager: ToastManager by instance()

    val isLoading = MutableLiveData(false)
    var userForm = UserFields()

    private var email = ""
    private var password = ""

    private val fragmentDisplayer =
        UserFlowFragmentDisplayer(this, R.id.fragment_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySignUpBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.lifecycleOwner = this
        binding.activity = this
        channel = ActivitiesUtil.initChannel()

        toMainDataFilling()
    }

    private fun toMainDataFilling() {
        val fragment = MainSignUpDataFragment.getInstance()

        subscribeToMainSignUpResult(fragment)

        fragmentDisplayer.display(fragment, "mainSignUp_fields", null)
    }

    private fun subscribeToMainSignUpResult(fragment: MainSignUpDataFragment) {
        fragment
            .result
            .compose(ObservableTransformers.defaultSchedulers())
            .subscribeBy(
                onNext = this::onMainSignUpFieldsEntered,
                onError = { it.printStackTrace() }
            )
            .addTo(compositeDisposable)
    }

    private fun onMainSignUpFieldsEntered(credentialsPair: Pair<String, String>) {
        this.email = credentialsPair.first
        this.password = credentialsPair.second
        toProfileFilling()
    }

    private fun toProfileFilling() {
        val fragment = UserProfileFragment.getInstance()

        subscribeToProfileInfoResult(fragment)
        fragmentDisplayer.display(fragment, "mainKyc_fields", true)

    }

    private fun subscribeToProfileInfoResult(fragment: UserProfileFragment) {
        fragment
            .result
            .compose(ObservableTransformers.defaultSchedulers())
            .subscribeBy(
                onNext = {
                    this.userForm = it
                    signUp()
                },
                onError = {
                    it.printStackTrace()
                    toastManager.short("Error occurred!")
                }
            )
            .addTo(compositeDisposable)
    }

/*
    private fun initFields() {
        already_have_account_text_view.makeLinks(
            Pair("Log in!", View.OnClickListener {
                startActivity(
                    Intent(
                        this,
                        SignInActivity::class.java
                    )
                )
            })
        )

    }
*/

    private fun signUp() {
        Repository(channel).registerUser(
            email = email,
            password = password,
            user = userForm
        )
            .compose(ObservableTransformers.defaultSchedulersSingle())
            .doOnSubscribe {
                progress.visibility = View.VISIBLE
            }
            .subscribe({
                Log.d("res--", it.number.toString())
                when (it.number) {
                    0 -> {
                        credentialsProvider.setCredentials(Credentials(email, password))
                        goToMainActivity()
                    }
                    1 -> {
                        toastManager.short("This email is already taken, try again!")
                        progress.visibility = View.GONE
                    }
                }
            }, {
                toastManager.short("Something went wrong, try again!")
            })

            .addTo(compositeDisposable)
    }

    private fun goToMainActivity() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
        )
        finish()
    }
}
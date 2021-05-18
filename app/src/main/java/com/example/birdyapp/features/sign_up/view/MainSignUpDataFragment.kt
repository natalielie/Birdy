package com.example.birdyapp.features.sign_up.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.birdyapp.R
import com.example.birdyapp.databinding.FragmentSignUpBinding
import com.example.birdyapp.extensions.makeLinks
import com.example.birdyapp.features.sign_in.view.SignInActivity
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.util.ScopedFragment
import com.example.birdyapp.util.ToastManager
import com.example.birdyapp.util.input.EditTextHelper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class MainSignUpDataFragment: ScopedFragment(), KodeinAware {
    override val kodein by closestKodein()
    lateinit var binding: FragmentSignUpBinding

    private val credentialsProvider: CredentialsProvider by instance()

    private val toastManager: ToastManager by instance()

    private val resultSubject = PublishSubject.create<Pair<String, String>>()
    val result: Observable<Pair<String, String>> = resultSubject

    val fields: MutableMap<String, MutableLiveData<String>> = mutableMapOf(
        "email" to MutableLiveData(),
        "password" to MutableLiveData(),
        "confirm_password" to MutableLiveData()
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        binding.fragment =  this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initFields()
        initButtons()
        EditTextHelper.initEmailEditText(emailInputLayout)
        EditTextHelper.initPasswordEditText(passwordInputLayout)
    }

    private fun initButtons() {
        next_sign_up_button.setOnClickListener {
            if(verifyFields()){
                resultSubject.onNext(fields["email"]?.value!! to fields["password"]?.value!!)
            }
        }
    }

    private fun initFields() {
        already_have_account_text_view.makeLinks(
            Pair("Log in!", View.OnClickListener {
                startActivity(
                    Intent(
                        requireContext(),
                        SignInActivity::class.java
                    )
                )
                requireActivity().finish()
            })
        )

    }

    private fun verifyFields(): Boolean {
        var isAllCorrect = true
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(fields["email"]?.value!!).matches()) {
            emailInputLayout.error = "Invalid email"
            isAllCorrect = false
        } else {
            emailInputLayout.error = null
        }
        if (fields["password"]?.value!! != fields["confirm_password"]?.value!!) {
            passwordInputLayout.error = "Passwords didn`t match"
            passwordRepeatedInputLayout.error = "Passwords didn`t match"
            isAllCorrect = false
        } else {
            passwordInputLayout.error = null
            passwordRepeatedInputLayout.error = null
        }
        return isAllCorrect
    }

    companion object {
        fun getInstance() = MainSignUpDataFragment()
    }

}
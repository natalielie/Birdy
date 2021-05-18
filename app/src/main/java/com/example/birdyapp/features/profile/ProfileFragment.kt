package com.example.birdyapp.features.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.birdyapp.R
import com.example.birdyapp.databinding.FragmentProfileBinding
import com.example.birdyapp.features.sign_up.model.UserFields
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.identity.KycProvider
import com.example.birdyapp.util.ScopedFragment
import com.example.birdyapp.util.ToastManager
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.toolbar_with_image.*
import kotlinx.android.synthetic.main.toolbar_with_image.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class ProfileFragment : ScopedFragment(), KodeinAware {
    override val kodein by closestKodein()
    private val toastManager: ToastManager by instance()
    private val credentialsProvider: CredentialsProvider by instance()
    private val kycProvider: KycProvider by instance()

    val userForm = UserFields()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding =
            FragmentProfileBinding.inflate(inflater, container, false)
        binding.fragment = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar()
        initFields()
        initButtons()
    }

    private fun initButtons() {
        profile_settings_btn.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    EditProfileActivity::class.java
                )
            )
        }
    }

    private fun initToolbar() {
        requireActivity().toolbar_with_image.title_text_view.text = getString(R.string.my_profile)
    }

    private fun initFields() {
        val activeKyc = kycProvider.getKyc()
        userForm.firstName.value = activeKyc?.firstName?.value!!
        userForm.lastName.value = activeKyc.lastName.value!!
        userForm.middleName.value = activeKyc.middleName.value!!
        userForm.birthdayDate.value = activeKyc.birthdayDate.value!!
        userForm.city.value = activeKyc.city.value!!

        fullName.text = "${activeKyc?.firstName.value!!} ${activeKyc.lastName.value!!}"
    }

    companion object {
        fun getInstance() = ProfileFragment()
    }
}
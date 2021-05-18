package com.example.birdyapp.features.sign_up.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.birdyapp.R
import com.example.birdyapp.databinding.FragmentSignUpProfileBinding
import com.example.birdyapp.features.sign_up.model.UserFields
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.util.ScopedFragment
import com.example.birdyapp.util.ToastManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_sign_up_profile.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.util.*

class UserProfileFragment : ScopedFragment(), KodeinAware {
    override val kodein by closestKodein()
    lateinit var binding: FragmentSignUpProfileBinding
    val userForm = UserFields()

    private val credentialsProvider: CredentialsProvider by instance()

    private val toastManager: ToastManager by instance()

    private val resultSubject = PublishSubject.create<UserFields>()
    val result: Observable<UserFields> = resultSubject

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up_profile, container, false)
        binding.fragment = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initFields()
        initButtons()
    }

    private fun initFields() {
        birthDayDateInputLayout.setEndIconOnClickListener {
            val c = Calendar.getInstance()
            c.roll(Calendar.YEAR, -18)
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val dpd = DatePickerDialog(
                requireContext(),
                { _, chosenYear, monthOfYear, dayOfMonth ->
                    val date = GregorianCalendar(chosenYear, monthOfYear, dayOfMonth).time
                    userForm.birthdayDate.value = date
                },
                year,
                month,
                day
            )
            dpd.datePicker.maxDate = c.timeInMillis
            dpd.show()
        }
    }

    private fun initButtons() {
        returnBtn.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        sign_up_button.setOnClickListener {
            if (userForm.validate()) {
                resultSubject.onNext(
                    userForm
                )
            } else {
                toastManager.long(R.string.empty_fields)
            }
        }
    }

    companion object {
        fun getInstance() = UserProfileFragment()
    }
}
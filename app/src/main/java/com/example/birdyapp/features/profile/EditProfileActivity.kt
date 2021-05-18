package com.example.birdyapp.features.profile

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.birdyapp.R
import com.example.birdyapp.Repository
import com.example.birdyapp.databinding.ActivityEditProfileBinding
import com.example.birdyapp.features.sign_in.model.Credentials
import com.example.birdyapp.features.sign_up.model.UserFields
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.identity.KycProvider
import com.example.birdyapp.util.ActivitiesUtil
import com.example.birdyapp.util.ObservableTransformers
import com.example.birdyapp.util.ToastManager
import io.grpc.Channel
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.util.*

class EditProfileActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private val credentialsProvider: CredentialsProvider by instance()
    private val kycProvider: KycProvider by instance()
    private lateinit var channel: Channel
    private val toastManager: ToastManager by instance()

    val isLoading = MutableLiveData(false)
    val userForm = UserFields()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityEditProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        binding.lifecycleOwner = this
        binding.activity = this
        channel = ActivitiesUtil.initChannel()
        initToolbar()
        initFields()
        initButtons()
    }

    private fun initToolbar() {
        toolbar.title_text_view.text = getString(R.string.edit_profile)
    }

    private fun initFields() {
        val activeKyc = kycProvider.getKyc()
        userForm.firstName.value = activeKyc?.firstName?.value!!
        userForm.lastName.value = activeKyc.lastName.value!!
        userForm.middleName.value = activeKyc.middleName.value!!
        userForm.birthdayDate.value = activeKyc.birthdayDate.value!!
        userForm.city.value = activeKyc.city.value!!

        birthDayDateInputLayout.setEndIconOnClickListener {
            val c = Calendar.getInstance()
            c.roll(Calendar.YEAR, -18)
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val dpd = DatePickerDialog(
                this,
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
        save_button.setOnClickListener {
            if (userForm.validate()) {
                update()
            } else {
                toastManager.long(R.string.empty_fields)
            }
        }
    }

    private fun update() {
        Repository(channel).updateUserInfo(
            userForm,
            credentialsProvider.getCredentials()!!.email,
            credentialsProvider.getCredentials()!!.password
        )
            .compose(ObservableTransformers.defaultSchedulersCompletable())
            .doOnSubscribe {
                isLoading.postValue(true)
            }
            .subscribeBy(
                onComplete = {
                    toastManager.short("Data has been updated")
                }, onError = {
                    it.printStackTrace()
                }
            )

        val oldCreds = credentialsProvider.getCredentials()
        oldCreds?.let {
            credentialsProvider.setCredentials(
                Credentials(
                    it.password,
                    it.email
                )
            )
            kycProvider.setKyc(userForm)
        }
    }
}
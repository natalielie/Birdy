package com.example.birdyapp.features.recovery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.example.birdyapp.R
import com.example.birdyapp.Repository
import com.example.birdyapp.databinding.ActivityRecoveryBinding
import com.example.birdyapp.util.ActivitiesUtil
import com.example.birdyapp.util.ObservableTransformers
import com.example.birdyapp.util.ToastManager
import com.example.birdyapp.util.input.EditTextHelper
import io.grpc.Channel
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_recovery.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class RecoveryActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var channel: Channel
    private val toastManager: ToastManager by instance()

    val fields: MutableMap<String, MutableLiveData<String>> = mutableMapOf(
        "email" to MutableLiveData(),
        "password" to MutableLiveData(),
        "confirm_password" to MutableLiveData()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityRecoveryBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_recovery)
        binding.activity = this
        binding.lifecycleOwner = this

        channel = ActivitiesUtil.initChannel()
        initFields()
        initButtons()
    }

    private fun initFields() {
        EditTextHelper.initEmailEditText(emailInputLayout)
        EditTextHelper.initPasswordEditText(passwordInputLayout)
        EditTextHelper.initPasswordEditText(confirmPasswordInputLayout)
    }

    private fun initButtons() {
        reset_button.setOnClickListener {
            if (fields["email"]?.value != null) {
                resetPass()
            }
        }
    }

    private fun resetPass() {
        Repository(channel)
            .sendResetCode(fields["email"]?.value!!)
            .compose(ObservableTransformers.defaultSchedulersSingle())
            .subscribeBy(
                onSuccess = {
                    showDialog()
                },
                onError = {
                    it.printStackTrace()
                    toastManager.short("Something went wrong...")
                }
            )

    }

    private fun showDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogStyle)
            .setTitle(R.string.almost_done)
            .setMessage(R.string.check_your_email_to_reset_pass)
            .setPositiveButton(R.string.ok) { _, _ ->
                openMailbox()
                openTfa()
            }
            .setNeutralButton(R.string.open_email_app) { _, _ ->
                openMailbox()
                openTfa()
            }
            .setOnCancelListener {
                openMailbox()
                openTfa()
            }
            .show()

    }

    private fun openMailbox() {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_APP_EMAIL),
                getString(R.string.open_email_app)
            )
        )
    }

    private fun openTfa() {
        val intent = Intent(
            this,
            TfaActivity::class.java
        )
        intent.putExtra("email", fields["email"]?.value!!)
        intent.putExtra("password", fields["password"]?.value!!)
        startActivity(
            intent
        )
    }
}
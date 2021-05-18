package com.example.birdyapp.features.searching_by_name.view

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.birdyapp.R
import com.example.birdyapp.Repository
import com.example.birdyapp.databinding.ActivityRecordAudioBinding
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.util.ActivitiesUtil
import com.example.birdyapp.util.ObservableTransformers
import com.example.birdyapp.util.PermissionManager
import com.example.birdyapp.util.ToastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.protobuf.ByteString
import io.grpc.Channel
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_record_audio.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class AudioRecordActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private val credentialsProvider: CredentialsProvider by instance()

    private lateinit var channel: Channel
    private val toastManager: ToastManager by instance()

    private var fileName: String = ""

    private var recorder: MediaRecorder? = null

    private var player: MediaPlayer? = null

    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    var mStartPlaying = true
    var mStartRecording = true

    var countDownTimer: CountDownTimer? = null
    var second = -1
    var minute: Int = 0
    var hour: Int = 0

    private val fineLocationPermission =
        PermissionManager(Manifest.permission.ACCESS_FINE_LOCATION, 2)
    private val coarseLocationPermission =
        PermissionManager(Manifest.permission.ACCESS_COARSE_LOCATION, 3)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.mp3"

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        val binding: ActivityRecordAudioBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_record_audio)
        binding.lifecycleOwner = this
        binding.activity = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        channel = ActivitiesUtil.initChannel()

        initButtons()
        initToolbar()
    }

    private fun initButtons() {
        record.setOnClickListener {
            onRecord(mStartRecording)

            mStartRecording = !mStartRecording
        }
        play.setOnClickListener {
            onPlay(mStartPlaying)

            mStartPlaying = !mStartPlaying
        }
        send.setOnClickListener {
            val audioBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendBirdSound(getAudioBytesArray())
            } else {
                TODO("VERSION.SDK_INT < O")
            }
        }
    }

    private fun sendBirdSound(sound: ByteArray) {
        fineLocationPermission.check(this, {}) {
            toastManager.short(R.string.grant_location_permission)
        }
        coarseLocationPermission.check(this, {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLocation = location
                            Log.d("lat", currentLocation.latitude.toString())
                            Log.d("long", currentLocation.longitude.toString())

                            Repository(channel).birdBySound(
                                sound = ByteString.copyFrom(sound),
                                lat = currentLocation.latitude,
                                long = currentLocation.longitude,
                                finder = credentialsProvider.getCredentials()!!.email
                            ).compose(ObservableTransformers.defaultSchedulersSingle())
                                .subscribeBy(
                                    onSuccess = {
                                        openDialog(it.birdName)
                                        Log.d("found-by-sound", it.birdName)
                                    }, onError = {
                                        it.printStackTrace()
                                    }
                                )
                        }
                    }
            }


        }) {
            toastManager.short(R.string.grant_location_permission)
        }
    }

    private fun initToolbar() {
        toolbar.title_text_view.text = getString(R.string.record_audio)
    }

    private fun onRecord(start: Boolean) = if (start) {
        record.setImageResource(R.drawable.ic_stop)
        record_label.text = getString(R.string.stop)
        showTimer()
        startRecording()
    } else {
        record.setImageResource(R.drawable.ic_record)
        record_label.text = getString(R.string.record)
        countDownTimer?.cancel()
        time.text = getString(R.string.start_time)
        stopRecording()
    }

    private fun onPlay(start: Boolean) = if (start) {
        play.setImageResource(R.drawable.ic_pause)
        play_label.text = getString(R.string.pause)
        startPlaying()
    } else {
        play.setImageResource(R.drawable.ic_play)
        play_label.text = getString(R.string.play)
        stopPlaying()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("LOG_TAG", "prepare() failed")
            }

            start()
        }
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    private fun showTimer() {
        countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                second++
                time.text = recorderTime()
            }

            override fun onFinish() {}
        }
        (countDownTimer as CountDownTimer).start()
    }

    private fun recorderTime(): String {
        if (second == 60) {
            minute++
            second = 0
        }
        if (minute == 60) {
            hour++
            minute = 0
        }
        return String.format("%02d:%02d:%02d", hour, minute, second)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAudioBytesArray(): ByteArray {
        return Files.readAllBytes(Paths.get(fileName))
    }

    private fun openDialog(name: String) {
        AlertDialog.Builder(this)
            .setMessage("Found bird: " + name)
            .setPositiveButton(R.string.yes) { _, _ ->
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
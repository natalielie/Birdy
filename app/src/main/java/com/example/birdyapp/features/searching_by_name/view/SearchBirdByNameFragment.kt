package com.example.birdyapp.features.searching_by_name.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.example.birdyapp.R
import com.example.birdyapp.Repository
import com.example.birdyapp.databinding.FragmentFindBirdByNameBinding
import com.example.birdyapp.features.map.MapsActivity
import com.example.birdyapp.features.searching_by_name.model.BirdModel
import com.example.birdyapp.features.searching_by_name.view.adapters.BirdsAdapter
import com.example.birdyapp.identity.CredentialsProvider
import com.example.birdyapp.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.protobuf.ByteString
import com.theartofdev.edmodo.cropper.CropImage
import io.grpc.Channel
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_find_bird_by_name.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class SearchBirdByNameFragment(val channel: Channel) : ScopedFragment(), KodeinAware {
    override val kodein by closestKodein()
    private val credentialsProvider: CredentialsProvider by instance()

    private val toastManager: ToastManager by instance()

    private val cameraPermission = PermissionManager(Manifest.permission.CAMERA, 404)
    private val fineLocationPermission =
        PermissionManager(Manifest.permission.ACCESS_FINE_LOCATION, 2)
    private val coarseLocationPermission =
        PermissionManager(Manifest.permission.ACCESS_COARSE_LOCATION, 3)

    private val birdsAdapter: BirdsAdapter by lazy {
        BirdsAdapter()
    }

    private lateinit var currentLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var birdImageFile: File
    val isLoading = MutableLiveData(false)

    val birdName = MutableLiveData<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding =
            FragmentFindBirdByNameBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.fragment = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initButtons()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initButtons() {
        searchBtn.setOnClickListener {

            searchByName()
            //getUsersByCity()
            //dialog.hide()
        }
        uploadBtn.setOnClickListener {
            cameraPermission.check(
                requireActivity(),
                this::toCapture
            ) { toastManager.short(R.string.grant_camera_permission) }
        }

        recordBtn.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    AudioRecordActivity::class.java
                )
            )
           }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun searchByName() {
        progress.visibility = View.VISIBLE


        if (validate(birdNameLayout.editText?.text.toString().trimStart())) {
            try {
                Repository(channel).findBirdByName(birdNameLayout.editText?.text.toString().trim())
                    .compose(ObservableTransformers.defaultSchedulersSingle())

                    .subscribeBy({
                        fillBirdsRecyclerView(it)
                    })
            } catch (e: Exception) {
                e.printStackTrace()
                toastManager.long("Something went wrong, try again")
            }
        } else {
            toastManager.long("Incorrect bird name, try again")
        }
        progress.visibility = View.GONE
    }

    private fun fillBirdsRecyclerView(list: List<BirdModel>) {
        with(birdsRecycler) {
            layoutManager =
                GridLayoutManager(requireContext(), 2)
            adapter = birdsAdapter
            birdsAdapter.onClick = {
                val intent = Intent(
                    requireContext(),
                    MapsActivity::class.java
                )
                intent.apply {
                    intent.putExtra("birdName", it.name)
                    startActivity(this)
                }
/*
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    Repository(channel).getBirdLocations(it.name)
                        .compose(ObservableTransformers.defaultSchedulersSingle())
                        .doOnSubscribe {
                            isLoading.value = true
                        }
                        .doOnError {
                            isLoading.value = false
                        }
                        .subscribeBy(
                            onSuccess = {
                                startActivity(
                                    Intent(
                                        requireContext(),
                                        MapsActivity::class.java
                                    )
                                )
                            }, onError = {
                                it.printStackTrace()
                            }
                        )
                }
*/
            }
            birdsAdapter.replace(list)
        }
    }

    private fun toCapture() {
        birdImageFile = createImageFile()

        val pictureIntent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )
        if (pictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            //Create a file to store the image
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.example.birdyapp.provider",
                birdImageFile
            )
            pictureIntent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                photoURI
            )
            startActivityForResult(
                pictureIntent,
                404
            )
        }
    }

    private fun createImageFile(): File {

        val imageFileName =
            "${"birdy_picture"}.${System.currentTimeMillis()}"
        val storageDir =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        )
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_CANCELED) {
            toastManager.short(R.string.canceled)
        }
        if (requestCode == 404 && resultCode == Activity.RESULT_OK) {
            Log.d("testPh", birdImageFile.name)
            //ImageViewUtil.loadImageFromFile(testImg, avatarFile)
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "com.example.birdyapp.provider",
                birdImageFile
            )
            launchImageCrop(photoURI)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                ImageViewUtil.loadImage(
                    testImg,
                    result.uri.toString(),
                    resources.getDrawable(R.drawable.background_button)
                )
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e("", "Crop error: ${result.error}")
            }
            getLastKnownLocation()
        }
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .start(requireContext(), this)

    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        fineLocationPermission.check(requireActivity(), {}) {
            toastManager.short(R.string.grant_location_permission)
        }
        coarseLocationPermission.check(requireActivity(), {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = location
                        Log.d("lat", currentLocation.latitude.toString())
                        Log.d("long", currentLocation.longitude.toString())
                        var fis: FileInputStream? = null
                        try {
                            fis = FileInputStream(birdImageFile)
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }

                        val bm: Bitmap = BitmapFactory.decodeStream(fis)
                        val baos = ByteArrayOutputStream()
                        getResizedBitmap(bm, 640, 640)?.compress(
                            Bitmap.CompressFormat.JPEG,
                            50,
                            baos
                        )
                        val b = baos.toByteArray()
                        Log.d("initial-size", ByteString.copyFrom(b).size().toString())
                        Repository(channel).setBirdLocation(
                            photo = ByteString.copyFrom(b),
                            lat = currentLocation.latitude,
                            long = currentLocation.longitude,
                            finder = credentialsProvider.getCredentials()!!.email
                        ).compose(ObservableTransformers.defaultSchedulersSingle())
                            .subscribeBy(
                                onSuccess = {
                                    //fillBirdsRecyclerView(BirdModel())
                                    /* bird_by_photo_name.visibility = View.VISIBLE
                                     bird_by_photo_name.text = it.birdName*/
                                    openDialog(it.birdName)
                                    Log.d("onSuccess", it.birdName)
                                }, onError = {
                                    it.printStackTrace()
                                }
                            )
                    }
                }

        }) {
            toastManager.short(R.string.grant_location_permission)
        }
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
    }

    private fun openDialog(name: String) {
        AlertDialog.Builder(requireContext())
            .setMessage("Found bird: " + name)
            .setPositiveButton(R.string.yes) { _, _ ->
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun validate(value: String): Boolean {
        return value.filter {
            it in 'A'..'Z' || it in 'a'..'z' || it == '-' ||
                    it == ' ' || it in 'А'..'Я' || it in 'а'..'я'
        }.length == value.length
    }

    companion object {
        fun getInstance(channel: Channel) = SearchBirdByNameFragment(channel)
    }

}
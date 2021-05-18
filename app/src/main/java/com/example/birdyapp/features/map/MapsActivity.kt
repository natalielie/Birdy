package com.example.birdyapp.features.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import birdy_grpc.Birdy
import com.example.birdyapp.R
import com.example.birdyapp.Repository
import com.example.birdyapp.util.ActivitiesUtil
import com.example.birdyapp.util.ObservableTransformers
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.grpc.Channel
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var channel: Channel
    val isLoading = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        channel = ActivitiesUtil.initChannel()

        val birdName =intent.getStringExtra("birdName")
        birdName?.let{
            initToolbar(birdName)

            getCoordinates(it)
        }
    }

    private fun initToolbar(name: String) {
        maps_toolbar.title_text_view.text = name + getString(R.string.location)
    }

    private fun getCoordinates(name: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Repository(channel).getBirdLocations(name)
                .compose(ObservableTransformers.defaultSchedulersSingle())
                /*.doOnSubscribe {
                    isLoading.value = true
                }
                .doOnError {
                    isLoading.value = false
                }*/
                .subscribeBy(
                    onSuccess = {
                        setMarkers(name, it)
                    }, onError = {
                        it.printStackTrace()
                    }
                )
        }
    }

    private fun setMarkers(name: String, coordinatesList: List<Birdy.UserBirdInfo.Point>) {
        for(coordinate in coordinatesList){
            val newCoordinate = LatLng(coordinate.latitude, coordinate.longitude)
            mMap.addMarker(MarkerOptions().position(newCoordinate).title("$name was found here!"))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(50.0, 36.2)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
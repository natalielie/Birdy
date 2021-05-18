package com.example.birdyapp.features.top

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.example.birdyapp.Repository
import com.example.birdyapp.databinding.FragmentTopBirdsBinding
import com.example.birdyapp.features.searching_by_name.model.BirdModel
import com.example.birdyapp.features.searching_by_name.view.adapters.BirdsAdapter
import com.example.birdyapp.util.ObservableTransformers
import com.example.birdyapp.util.ScopedFragment
import com.example.birdyapp.util.ToastManager
import io.grpc.Channel
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_top_birds.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class TopFragment(val channel: Channel) : ScopedFragment(), KodeinAware {
    override val kodein: Kodein by closestKodein()

    private val toastManager: ToastManager by instance()
    val count = MutableLiveData<String>()

    private val birdsAdapter: BirdsAdapter by lazy {
        BirdsAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding =
            FragmentTopBirdsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.fragment = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initButtons()
        }
    }

    private fun initButtons() {
        searchBtn.setOnClickListener {
            if (count.value != null) {
                getBirdsTop()
            }
        }
    }

    private fun getBirdsTop() {
        Repository(channel).getBirdsTop(count.value!!.toInt())
            .compose(ObservableTransformers.defaultSchedulersSingle())
            .subscribeBy (
                onSuccess = {
                    val birdsModelList = mutableListOf<BirdModel>()
                    it.map {
                        birdsModelList.add(
                            BirdModel(
                                name = it.name,
                                description = it.description,
                                photo = it.photo.toByteArray()
                            )
                        )
                    }
                    fillBirdsRecyclerView(birdsModelList)
                },
                onError = {
                    it.printStackTrace()
                    toastManager.short("Something went wrong, try later!")
                }
            )
    }

    private fun fillBirdsRecyclerView(list: List<BirdModel>) {
        with(topRecycler) {
            layoutManager =
                GridLayoutManager(requireContext(), 2)
            adapter = birdsAdapter
            /*birdsAdapter.onClick = {
                val intent = Intent(
                    requireContext(),
                    MapsActivity::class.java
                )
                intent.apply {
                    intent.putExtra("birdName", it.name)
                    startActivity(this)
                }
*//*
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
*//*
            }*/
            birdsAdapter.replace(list)
        }
    }

    companion object {
        fun getInstance(channel: Channel) = TopFragment(channel)
    }
}
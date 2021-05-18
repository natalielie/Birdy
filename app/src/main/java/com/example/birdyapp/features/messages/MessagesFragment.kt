package com.example.birdyapp.features.messages

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import birdy_grpc.Birdy
import com.example.birdyapp.R
import com.example.birdyapp.Repository
import com.example.birdyapp.databinding.FragmentMessagingBinding
import com.example.birdyapp.features.messages.logic.UsersAdapter
import com.example.birdyapp.features.sign_up.model.UserFields
import com.example.birdyapp.util.ObservableTransformers
import com.example.birdyapp.util.ScopedFragment
import com.example.birdyapp.util.ToastManager
import io.grpc.Channel
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_messaging.*
import kotlinx.android.synthetic.main.toolbar_with_image.*
import kotlinx.android.synthetic.main.toolbar_with_image.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.util.*

class MessagesFragment(val channel: Channel) : ScopedFragment(), KodeinAware {
    override val kodein by closestKodein()
    private val toastManager: ToastManager by instance()
    private val usersAdapter: UsersAdapter by lazy {
        UsersAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding =
            FragmentMessagingBinding.inflate(inflater, container, false)
        binding.fragment = this
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar()
        //initFields()
        initButtons()
    }

    private fun initToolbar() {
        requireActivity().toolbar_with_image.title_text_view.text = getString(R.string.find_users)
    }

    private fun initButtons() {
        searchBtn.setOnClickListener {
            getUsersByCity(cityLayout.editText?.text.toString().trim())
        }
    }

    private fun fillUsersRecyclerView(items: List<UserFields>) {

        users_recycler.apply {
            layoutManager =
                    // GridLayoutManager(requireContext(), 2)
                LinearLayoutManager(this@MessagesFragment.context)
            adapter = usersAdapter
        }
        usersAdapter.replace(items)
    }

    private fun getUsersByCity(city: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Repository(channel).getUsersByCity(city)
                    .compose(ObservableTransformers.defaultSchedulersSingle())
                    .subscribeBy(
                        onSuccess = {
                            resultsLabel.visibility = View.VISIBLE
                            fillUsersRecyclerView(it.toUsersList())
                        }, onError = {
                            it.printStackTrace()
                        }
                    )
            } catch (e: Exception) {
                e.printStackTrace()
                toastManager.long("Something went wrong, try again")
            }
        }
    }

    companion object {
        fun getInstance(channel: Channel) = MessagesFragment(channel)
    }
}

private fun List<Birdy.UserInfo>.toUsersList(): List<UserFields> {
    return this.map {
        UserFields(
            it.firstName,
            it.lastName,
            it.middleName,
            Date(),
            it.city
        )
    }
}

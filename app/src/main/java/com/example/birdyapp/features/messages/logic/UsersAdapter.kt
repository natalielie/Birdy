package com.example.birdyapp.features.messages.logic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.birdyapp.base.BaseAdapter
import com.example.birdyapp.databinding.BirdwatcherItemBinding
import com.example.birdyapp.features.sign_up.model.UserFields
import java.util.*

class UsersAdapter:
    BaseAdapter<UserFields, UsersAdapter.UsersViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder =
        UsersViewHolder(
            BirdwatcherItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            onClick?.invoke(getItem(position))
        }
    }

    inner class UsersViewHolder(private val binding: BirdwatcherItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserFields) {
            binding.user = item
        }
    }
}
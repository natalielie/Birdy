package com.example.birdyapp.features.searching_by_name.view.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.birdyapp.base.BaseAdapter
import com.example.birdyapp.databinding.LayoutBirdItemBinding
import com.example.birdyapp.databinding.LayoutOfflineBirdBinding
import com.example.birdyapp.db.OfflineBirdsModel
import com.example.birdyapp.features.searching_by_name.model.BirdModel

class OfflineBirdsAdapter:
    BaseAdapter<OfflineBirdsModel, OfflineBirdsAdapter.BirdsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdsViewHolder =
        BirdsViewHolder(
            LayoutOfflineBirdBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: BirdsViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            onClick?.invoke(getItem(position))
        }
    }

    inner class BirdsViewHolder(private val binding: LayoutOfflineBirdBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OfflineBirdsModel) {

            binding.description = item.lat.toString() + ":" + item.longitude
            binding.birdPhoto.setImageURI(Uri.parse(item.photo))
        }
    }

}
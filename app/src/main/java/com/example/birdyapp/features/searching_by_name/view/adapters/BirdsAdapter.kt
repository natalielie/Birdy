package com.example.birdyapp.features.searching_by_name.view.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.birdyapp.base.BaseAdapter
import com.example.birdyapp.databinding.LayoutBirdItemBinding
import com.example.birdyapp.features.searching_by_name.model.BirdModel
import java.nio.ByteBuffer


class BirdsAdapter :
    BaseAdapter<BirdModel, BirdsAdapter.BirdsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirdsViewHolder =
        BirdsViewHolder(
            LayoutBirdItemBinding.inflate(
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

    inner class BirdsViewHolder(private val binding: LayoutBirdItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BirdModel) {
            //Create bitmap with width, height, and 4 bytes color (RGBA)

            //Create bitmap with width, height, and 4 bytes color (RGBA)
            val bmp = Bitmap.createBitmap(10000, 10000, Bitmap.Config.ARGB_8888)
            //val buffer: ByteBuffer = ByteBuffer.wrap(item.photo)
            //bmp.copyPixelsFromBuffer(buffer)
            val bmp_ = BitmapFactory.decodeByteArray(item.photo, 0, item.photo.size)

            binding.bird = item
            binding.birdPhoto.setImageBitmap(bmp_)
            if(bmp_ == null) {
                Log.d("test--", "bmp is null")
            }
            Log.d("test--", item.photo.size.toString())
        }
    }

}
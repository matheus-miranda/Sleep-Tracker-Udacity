package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepBinding

class SleepNightAdapter :
    ListAdapter<SleepNight, SleepNightAdapter.ItemViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ItemViewHolder private constructor(val binding: ListItemSleepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SleepNight) {
            binding.sleep = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ItemViewHolder(ListItemSleepBinding.inflate(inflater, parent, false))
            }
        }
    }
}

class DiffCallBack : DiffUtil.ItemCallback<SleepNight>() {
    override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean =
        oldItem.nightId == newItem.nightId

    override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean =
        oldItem == newItem
}

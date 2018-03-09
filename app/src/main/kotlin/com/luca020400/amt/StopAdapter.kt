package com.luca020400.amt

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.stop_adapter.view.*

internal class StopAdapter : RecyclerView.Adapter<StopAdapter.StopViewHolder>() {
    private val stops = mutableListOf<StopData>()

    fun addAll(stops: List<StopData>) {
        if (this.stops != stops) {
            this.stops.clear()
            this.stops.addAll(stops)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            StopViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.stop_adapter, parent, false))

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.bindStop(stops[holder.adapterPosition])
    }

    override fun getItemCount() = stops.size

    class StopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindStop(stopData: StopData) = with(itemView) {
            line.text = stopData.line
            eta.text = stopData.remaining_time
            destination.text = stopData.destination
            schedule.text = stopData.schedule
        }
    }
}
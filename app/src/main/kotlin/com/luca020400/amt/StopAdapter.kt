package com.luca020400.amt

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.stop_adapter.view.*

internal class StopAdapter : RecyclerView.Adapter<StopAdapter.StopViewHolder>() {
    private val stops = arrayListOf<StopData>()

    fun addAll(stops: ArrayList<StopData>) {
        if (this.stops != stops) {
            this.stops.clear()
            this.stops.addAll(stops)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.stop_adapter, parent, false)
        return StopViewHolder(v)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.bindStop(stops[holder.adapterPosition])
    }

    override fun getItemCount() = stops.size

    class StopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindStop(stopData: StopData) {
            itemView.line.text = stopData.line
            itemView.eta.text = stopData.remaining_time
            itemView.destination.text = stopData.destination
            itemView.schedule.text = stopData.schedule
        }
    }
}
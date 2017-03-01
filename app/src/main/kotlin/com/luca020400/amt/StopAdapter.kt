package com.luca020400.amt

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.stop_adapter.view.*

internal class StopAdapter(var stops: MutableList<StopData>) : RecyclerView.Adapter<StopAdapter.ViewHolder>() {
    fun addAll(stops: List<StopData>) {
        this.stops.addAll(stops)
        notifyDataSetChanged()
    }

    fun clear() {
        stops.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.stop_adapter, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindStop(stops[holder.adapterPosition])
    }

    override fun getItemCount() = stops.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindStop(stopData: StopData) {
            itemView.line.text = stopData.line
            itemView.eta.text = stopData.remainingtime
            itemView.destination.text = stopData.destination
            itemView.schedule.text = stopData.schedule
        }
    }
}

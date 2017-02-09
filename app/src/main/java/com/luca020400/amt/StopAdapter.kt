package com.luca020400.amt

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.stop_adapter.view.*

internal class StopAdapter : RecyclerView.Adapter<StopAdapter.ViewHolder>() {
    private val stops: MutableList<Stop> = mutableListOf()

    fun addAll(stopsList: List<Stop>) {
        stops.addAll(stopsList)
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
        fun bindStop(stop: Stop) {
            with(stop) {
                itemView.line.text = stop.line
                itemView.eta.text = stop.remainingtime
                itemView.destination.text = stop.destination
                itemView.schedule.text = stop.schedule
            }
        }
    }
}

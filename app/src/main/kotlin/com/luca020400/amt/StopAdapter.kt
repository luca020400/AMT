package com.luca020400.amt

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

internal class StopAdapter(var stops: ArrayList<StopData>)
    : RecyclerView.Adapter<StopViewHolder>() {
    fun addAll(stops: ArrayList<StopData>) {
        this.stops.clear()
        this.stops.addAll(stops)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                R.layout.stop_adapter, parent, false)
        return StopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.viewDataBinding.setVariable(BR.stop, stops[holder.adapterPosition])
    }

    override fun getItemCount() = stops.size
}

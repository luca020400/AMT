package com.luca020400.amt

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

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
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                R.layout.stop_adapter, parent, false)
        return StopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        holder.viewDataBinding.setVariable(BR.stop, stops[holder.adapterPosition])
        holder.viewDataBinding.executePendingBindings()
    }

    override fun getItemCount() = stops.size

    class StopViewHolder(val viewDataBinding: ViewDataBinding) :
            RecyclerView.ViewHolder(viewDataBinding.root)
}

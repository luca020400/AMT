package com.luca020400.amt

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

internal class StopAdapter(var stops: ArrayList<StopData>) : RecyclerView.Adapter<StopAdapter.ViewHolder>() {
    fun addAll(stops: ArrayList<StopData>) {
        this.stops.addAll(stops)
        notifyDataSetChanged()
    }

    fun clear() {
        stops.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                R.layout.stop_adapter, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewDataBinding.setVariable(BR.stop, stops[holder.adapterPosition])
    }

    override fun getItemCount() = stops.size

    class ViewHolder(val viewDataBinding: ViewDataBinding) :
            RecyclerView.ViewHolder(viewDataBinding.root) {
        init {
            viewDataBinding.executePendingBindings()
        }
    }
}

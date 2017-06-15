package com.luca020400.amt

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

class StopViewHolder(val viewDataBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
    init {
        viewDataBinding.executePendingBindings()
    }
}

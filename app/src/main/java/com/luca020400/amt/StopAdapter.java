package com.luca020400.amt;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

class StopAdapter extends RecyclerView.Adapter<StopAdapter.ViewHolder> {
    private final List<Stop> stops;

    StopAdapter(List<Stop> stops) {
        this.stops = stops;
    }

    void addAll(Collection<Stop> StopsCollection) {
        stops.addAll(StopsCollection);
        notifyDataSetChanged();
    }

    void clear() {
        stops.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stop_adapter, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Stop stop = stops.get(holder.getAdapterPosition());
        holder.line.setText(stop.getLine());
        holder.eta.setText(stop.getRemainingtime());
        holder.destination.setText(stop.getDestination());
        holder.schedule.setText(stop.getSchedule());
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView line;
        private final TextView eta;
        private final TextView destination;
        private final TextView schedule;

        ViewHolder(View itemView) {
            super(itemView);
            line = (TextView) itemView.findViewById(R.id.line);
            eta = (TextView) itemView.findViewById(R.id.eta);
            destination = (TextView) itemView.findViewById(R.id.destination);
            schedule = (TextView) itemView.findViewById(R.id.schedule);
        }
    }
}

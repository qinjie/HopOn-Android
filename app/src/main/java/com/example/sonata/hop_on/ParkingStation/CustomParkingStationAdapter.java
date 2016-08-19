package com.example.sonata.hop_on.ParkingStation;

/**
 * Created by Sonata on 8/11/2016.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sonata.hop_on.R;

import java.util.ArrayList;

public class CustomParkingStationAdapter extends RecyclerView.Adapter<CustomParkingStationAdapter.MyViewHolder> {

    private ArrayList<ParkingStationClass> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewPKName;
        TextView textViewPKAvailableBicycle;
        TextView textViewPKTotalBicycle;
        TextView textViewPKAddress;
        TextView textViewPKDistance;


        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewPKName             = (TextView) itemView.findViewById(R.id.station_name);
            this.textViewPKAvailableBicycle = (TextView) itemView.findViewById(R.id.available_bicycles);
            this.textViewPKTotalBicycle     = (TextView) itemView.findViewById(R.id.total_bicycles);
            this.textViewPKAddress          = (TextView) itemView.findViewById(R.id.station_address);
            this.textViewPKDistance         = (TextView) itemView.findViewById(R.id.distance);
        }
    }

    public CustomParkingStationAdapter(ArrayList<ParkingStationClass> data) {
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parking_station_card_view, parent, false);

        view.setOnClickListener(ParkingStationListActivity.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {


        TextView _textViewPKName             = holder.textViewPKAddress;
        TextView _textViewPKAvailableBicycle = holder.textViewPKAvailableBicycle;
        TextView _textViewPKTotalBicycle     = holder.textViewPKTotalBicycle;
        TextView _textViewPKAddress          = holder.textViewPKAddress;
        TextView _textViewPKDistance         = holder.textViewPKDistance;

        _textViewPKName.setText(dataSet.get(listPosition).getStationName());
        _textViewPKAvailableBicycle.setText(dataSet.get(listPosition).getAvailableBicycles());
        _textViewPKTotalBicycle.setText(dataSet.get(listPosition).getTotalBicycles());
        _textViewPKAddress.setText(dataSet.get(listPosition).getStationAddress());
        _textViewPKDistance.setText(dataSet.get(listPosition).getDistance());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
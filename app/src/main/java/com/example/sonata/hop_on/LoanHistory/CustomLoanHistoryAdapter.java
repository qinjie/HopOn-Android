package com.example.sonata.hop_on.LoanHistory;

/**
 * Created by Sonata on 8/18/2016.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sonata.hop_on.BicycleBooking.BookingInformationClass;
import com.example.sonata.hop_on.R;

import java.util.ArrayList;

public class CustomLoanHistoryAdapter extends RecyclerView.Adapter<CustomLoanHistoryAdapter.MyViewHolder> {

    private ArrayList<BookingInformationClass> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewBookingId;
        TextView textViewBrandModel;
        TextView textViewSerial;
        TextView textViewBookedTime;
        TextView textViewBookedStationAddress;
        TextView textViewReturnTime;
        TextView textViewReturnStationAddress;


        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewBookingId            = (TextView) itemView.findViewById(R.id.booking_id_detail);
            this.textViewBrandModel           = (TextView) itemView.findViewById(R.id.bicycle_brand_model);
            this.textViewSerial               = (TextView) itemView.findViewById(R.id.bicycle_serial);
            this.textViewBookedTime           = (TextView) itemView.findViewById(R.id.booked_time_detail);
            this.textViewBookedStationAddress = (TextView) itemView.findViewById(R.id.booked_station_address);
            this.textViewReturnTime           = (TextView) itemView.findViewById(R.id.returned_time_detail);
            this.textViewReturnStationAddress = (TextView) itemView.findViewById(R.id.return_station_address);
        }
    }

    public CustomLoanHistoryAdapter(ArrayList<BookingInformationClass> data) {
        this.dataSet = data;
    }

    @Override
    public CustomLoanHistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                       int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_card_view, parent, false);

        view.setOnClickListener(LoanHistoryFragment.myOnClickListener);

        CustomLoanHistoryAdapter.MyViewHolder myViewHolder = new CustomLoanHistoryAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final CustomLoanHistoryAdapter.MyViewHolder holder, final int listPosition) {

        TextView _textViewBookingId            = holder.textViewBookingId;
        TextView _textViewBrandModel           = holder.textViewBrandModel;
        TextView _textViewSerial               = holder.textViewSerial;
        TextView _textViewBookedTime           = holder.textViewBookedTime;
        TextView _textViewBookedStationAddress = holder.textViewBookedStationAddress;
        TextView _textViewReturnTime           = holder.textViewReturnTime;
        TextView _textViewReturnStationAddress = holder.textViewReturnStationAddress;

        _textViewBookingId.setText(dataSet.get(listPosition).getBooking_id());
        _textViewBrandModel.setText(dataSet.get(listPosition).getBicycleInfo());
        _textViewSerial.setText(dataSet.get(listPosition).getBicycleSerial());
        _textViewBookedTime.setText(dataSet.get(listPosition).getBookedTime());
        _textViewBookedStationAddress.setText(dataSet.get(listPosition).getPickUpStationAddress());
        _textViewReturnTime.setText(dataSet.get(listPosition).getReturnTime());
        _textViewReturnStationAddress.setText(dataSet.get(listPosition).getReturnStationAddress());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

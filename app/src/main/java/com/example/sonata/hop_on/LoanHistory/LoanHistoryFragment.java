package com.example.sonata.hop_on.LoanHistory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sonata.hop_on.BicycleBooking.BookingActivity;
import com.example.sonata.hop_on.BicycleBooking.BookingInformationClass;
import com.example.sonata.hop_on.BicycleBooking.CurrentBookingActivity;
import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.LogIn.LogInActivity;
import com.example.sonata.hop_on.NavigationDrawer.NavigationDrawerActivity;
import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.ParkingStation.ParkingStationClass;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoanHistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoanHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoanHistoryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Activity context;

    static View.OnClickListener myOnClickListener;

    private static RecyclerView recyclerView;
    private static ArrayList<BookingInformationClass> dataSet;
    private OnFragmentInteractionListener mListener;

    public LoanHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoanHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoanHistoryFragment newInstance(String param1, String param2) {
        LoanHistoryFragment fragment = new LoanHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_loan_history, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.card_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        loadingDataFromServer();

        return view;
    }

    private void loadingDataFromServer()
    {
        Preferences.showLoading(context, "History", "Loading data from server...");

        SharedPreferences pref = context.getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);

        Call<ResponseBody> call = client.getBookingHistory();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Preferences.dismissLoading();
                    JSONArray data =  new JSONArray(URLDecoder.decode( response.body().string(), "UTF-8" ));
                    int messageCode = response.code();

                    if (messageCode == 200)
                    {
                        renewLoanHistoryData(data.length());
                        for(int i = 0; i < data.length(); i++)
                        {
                            JSONObject loan = data.getJSONObject(i);
                            BookingInformationClass info = new BookingInformationClass(loan, true);
                            dataSet.add(info);
                        }

                        CustomLoanHistoryAdapter adapter = new CustomLoanHistoryAdapter(dataSet);
                        recyclerView.setAdapter(adapter);

                        addOnClickListenerToCardView();
                    }
                    else if (messageCode == 400)
                    {

                    }
                }
                catch(Exception e){

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void addOnClickListenerToCardView()
    {
        myOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = recyclerView.getChildLayoutPosition(view);
                GlobalVariable.setSelectedLoanRecord(dataSet.get(itemPosition));
                Intent intent = new Intent(context, LoanDetailedActivity.class);
                startActivity(intent);
            }
        };
    }

    private void renewLoanHistoryData(int size)
    {
        dataSet = new ArrayList<>(size);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

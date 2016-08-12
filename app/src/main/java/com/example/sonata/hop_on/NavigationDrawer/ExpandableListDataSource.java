package com.example.sonata.hop_on.NavigationDrawer;

import android.content.Context;

import com.example.sonata.hop_on.R;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by msahakyan on 22/10/15.
 */
public class ExpandableListDataSource {

    /**
     * Returns fake data of Hop On function
     *
     * @param context
     * @return
     */
    public static Map<String, List<String>> getData(Context context) {
        Map<String, List<String>> expandableListData = new LinkedHashMap<>();

        List<String> hopOnFunctions = Arrays.asList(context.getResources().getStringArray(R.array.functions));

        List<String> parkingStations = Arrays.asList(context.getResources().getStringArray(R.array.parkingStations));
        expandableListData.put(hopOnFunctions.get(0), parkingStations);

        List<String> currentBooking = Arrays.asList(context.getResources().getStringArray(R.array.currentBooking));
        expandableListData.put(hopOnFunctions.get(1), currentBooking);

        List<String> history = Arrays.asList(context.getResources().getStringArray(R.array.history));
        expandableListData.put(hopOnFunctions.get(2), history);

        List<String> profile = Arrays.asList(context.getResources().getStringArray(R.array.profile));
        expandableListData.put(hopOnFunctions.get(3), profile);

        return expandableListData;
    }
}

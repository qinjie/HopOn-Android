package com.example.sonata.hop_on.BicycleBooking;

import java.util.ArrayList;

/**
 * Created by Sonata on 8/18/2016.
 */

public class BicycleInformationClass {
    String bicycleId;
    String bicycleBrand;
    String bicycleModel;
    String availabelNumber;
    String totalNumber;
    ArrayList<String> listImageUrl;


    public BicycleInformationClass(String _id, String _brand, String _model,
                                   String _availabelNumber, String _totalNumber, ArrayList<String> _listImageUrl)
    {
        bicycleId       = _id;
        bicycleBrand    = _brand;
        bicycleModel    = _model;
        availabelNumber = _availabelNumber;
        totalNumber     = _totalNumber;
        listImageUrl    = _listImageUrl;
    }

    public String getBicycleId()       { return bicycleId;       }
    public String getBicycleBrand()    { return bicycleBrand;    }
    public String getBicycleModel()    { return bicycleModel;    }
    public String getAvailabelNumber() { return availabelNumber; }
    public String getTotalNumber()     { return totalNumber;     }
}

package com.example.sonata.hop_on.BicycleBooking;

/**
 * Created by Sonata on 8/18/2016.
 */

public class BicycleInformationClass {
    String bicycleId;
    String bicycleBrand;
    String bicycleModel;
    String availabelNumber;
    String totalNumber;


    public BicycleInformationClass(String _id, String _brand, String _model, String _availabelNumber, String _totalNumber)
    {
        bicycleId       = _id;
        bicycleBrand    = _brand;
        bicycleModel    = _model;
        availabelNumber = _availabelNumber;
        totalNumber     = _totalNumber;
    }

    public String getBicycleId()       { return bicycleId;       }
    public String getBicycleBrand()    { return bicycleBrand;    }
    public String getBicycleModel()    { return bicycleModel;    }
    public String getAvailabelNumber() { return availabelNumber; }
    public String getTotalNumber()     { return totalNumber;     }
}

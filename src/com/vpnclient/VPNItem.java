package com.vpnclient;

public class VPNItem {
    private String Country;
    private int CountryId;

    public VPNItem(int CountryId, String Country){
        this.Country    = Country;
        this.CountryId  = CountryId;
    }

    @Override
    public String toString() {
        return Country;
    }

    public String getCountry() {
        return Country;
    }

    public int getCountryId() {
        return CountryId;
    }
}

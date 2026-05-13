package com.bycomsolutions.bycomvpn.dialog;



import unified.vpn.sdk.Location;

public class CountryData {

    private boolean pro = false;
    private Location countryvalue;

    public boolean isPro() {
        return pro;
    }

    public void setPro(boolean pro) {
        this.pro = pro;
    }

    public Location getCountryvalue() {
        return countryvalue;
    }

    public void setCountryvalue(Location countryvalue) {
        this.countryvalue = countryvalue;
    }

}

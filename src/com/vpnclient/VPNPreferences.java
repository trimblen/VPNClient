package com.vpnclient;

import java.util.prefs.Preferences;

public class VPNPreferences {
    private Preferences prefs;

    public VPNPreferences (){
        prefs = Preferences.userRoot().node(this.getClass().getName());
    }

    public void setPreference(String CountryId, String JSONString) {
        // This will define a node in which the preferences can be stored
        prefs.put(String.valueOf(CountryId), JSONString);
    };

    public void deletePreference(String CountryId) {
        // This will define a node in which the preferences can be stored
        prefs.remove(CountryId);
    };

    public String getPreference(String CountryId){
        String JSONRes = prefs.get(CountryId, CountryId);
        return JSONRes;
    };
}

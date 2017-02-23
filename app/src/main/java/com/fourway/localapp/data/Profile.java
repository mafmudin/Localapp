package com.fourway.localapp.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by 4 way on 20-02-2017.
 */

public class Profile {
    public static String TAG = "Profile";

    private String uId;
    private String uName;
    private String uEmail;
    private String uMobile;
    private String uPictureURL;
    private String uToken;
    private LatLng uLatLng;
    private String uSpeciality;
    private String uNotes;

    public Profile(String uId ) {
        this.uId = uId;
    }




    public String getuToken() {
        return uToken;
    }

    public void setuToken(String uToken) {
        this.uToken = uToken;
    }
    
    public String getuPictureURL() {
        return uPictureURL;
    }

    public void setuPictureURL(String uPictureURL) {
        this.uPictureURL = uPictureURL;
    }

    public String getuSpeciality() {
        return uSpeciality;
    }

    public void setuSpeciality(String uSpeciality) {
        this.uSpeciality = uSpeciality;
    }


    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuMobile() {
        return uMobile;
    }

    public void setuMobile(String uMobile) {
        this.uMobile = uMobile;
    }

    public LatLng getuLatLng() {
        return uLatLng;
    }

    public void setuLatLng(LatLng uLatLng) {
        this.uLatLng = uLatLng;
    }

    public String getuNotes() {
        return uNotes;
    }

    public void setuNotes(String uNotes) {
        this.uNotes = uNotes;
    }
}

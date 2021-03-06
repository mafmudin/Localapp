package com.localapp.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.localapp.ui.activities.HomeActivity;
import com.localapp.ui.activities.PublicProfileActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkArgument;


/**
 * Created by Vijay Kumar on 17-04-2017.
 */

public class Utility {


    public static String getTimeAndDate(String milliseconds) {
//        DateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        try {
            DateFormat sdfDay = new SimpleDateFormat("d");
            DateFormat sdf = new SimpleDateFormat("MMM yyyy EEEE,  hh:mm a");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(milliseconds));

            Log.v("date time: ",sdf.format(calendar.getTime()));

            return getDayOfMonthSuffix(Integer.parseInt(sdfDay.format(calendar.getTime())))+" "+sdf.format(calendar.getTime()).replace("AM", "am").replace("PM","pm");
        }catch (Exception e) {
            return "";
        }

    }

    private static String getDayOfMonthSuffix(final int n) {
        checkArgument(n >= 1 && n <= 31, "illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return n+"th";
        }
        switch (n % 10) {
            case 1:
                return n+"st";
            case 2:
                return n+"nd";
            case 3:
                return n+"rd";
            default:
                return n+"th";
        }
    }

    public static boolean isLocationAvailable(Context mContext) {
        if (HomeActivity.mLastKnownLocation == null) {
            Toast.makeText(mContext, "Please wait for getting your location...", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    public static boolean hasPermissionsGranted(Context mContexts, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(mContexts, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }




    private static DateFormat sdfTime = new SimpleDateFormat("h:mm aa", Locale.ENGLISH);
    private static DateFormat sdfTimeDay = new SimpleDateFormat("EEEE,  h:mm aa", Locale.ENGLISH);
    private static DateFormat sdf = new SimpleDateFormat("d MMMM,  h:mm aa", Locale.ENGLISH);
    private static DateFormat sdfY = new SimpleDateFormat("d MMMM yyyy,  h:mm aa", Locale.ENGLISH);
    public static String getSmsTime(String milliseconds) {

        try {
            Calendar smsTime = Calendar.getInstance();
            smsTime.setTimeInMillis(Long.parseLong(milliseconds));

            Calendar now = Calendar.getInstance();



            if (String.valueOf(smsTime.get(Calendar.YEAR)).equals("1970")) {
                return "";
            }

            boolean isCurrentMonthAndYear = (now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH))
                    &&  (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR));

            if (isCurrentMonthAndYear) {
                if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
                    return "Today, " + sdfTime.format(smsTime.getTime());
                } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1  ){
                    return "Yesterday, " + sdfTime.format(smsTime.getTime());
                }else if(now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) < 7  ){
                    return sdfTimeDay.format(smsTime.getTime());
                }else {
                    return sdf.format(smsTime.getTime());
                }
            } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
                return sdf.format(smsTime.getTime());
            } else {
                return sdfY.format(smsTime.getTime());
            }
        }catch (NumberFormatException nfe) {
            return "";
        }catch (NullPointerException npe) {
            return "";
        }

    }


    public static List<String> getProfessionList (String professionGroup) {

        switch (professionGroup) {
            case Constants.PROFESSION_GROUP_STUDENT: return Constants.PROFESSION_GROUP_STUDENT_LIST;
            case Constants.PROFESSION_GROUP_PROFESSIONALS: return Constants.PROFESSION_GROUP_PROFESSIONALS_LIST;
            case Constants.PROFESSION_GROUP_SKILLS: return Constants.PROFESSION_GROUP_SKILLS_LIST;
            case Constants.PROFESSION_GROUP_HEALTH: return Constants.PROFESSION_GROUP_HEALTH_LIST;
            case Constants.PROFESSION_GROUP_REPAIR: return Constants.PROFESSION_GROUP_REPAIR_LIST;
            case Constants.PROFESSION_GROUP_WEDDING: return Constants.PROFESSION_GROUP_WEDDING_LIST;
            case Constants.PROFESSION_GROUP_BEAUTY: return Constants.PROFESSION_GROUP_BEAUTY_LIST;
            case Constants.PROFESSION_GROUP_HOUSEWIFE: return Constants.PROFESSION_GROUP_HOUSEWIFE_LIST;
            default: return null;
        }
    }

    public static boolean isServiceRunning(Context mContext,Class<?> serviceClass){
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        // Loop through the running services
        for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                // If the service is running then return true
                return true;
            }
        }
        return false;
    }




    /*********** calculate distance by google**********/
    /**
     *
     * @param from
     * @param to
     * @param unit
     * @param showUnit
     * @return
     */
    public static String calcDistance(@NonNull LatLng from, @NonNull LatLng to, String unit, boolean showUnit) throws NullPointerException {
        double distance = SphericalUtil.computeDistanceBetween(from, to);
        if (unit != null){
            return formatNumber(distance,unit,showUnit);
        }else {
            return formatNumber(distance);
        }
    }

    private static String formatNumber(double distance,String unit,boolean showUnit) {
        /*String unit = "m";
        if (distance < 1) {
            distance *= 1000;
            unit = "mm";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
        }*/

        switch (unit) {
            case "km":
                distance /= 1000;
                unit = "km";
                break;
            case "mm":
                distance *= 1000;
                unit = "mm";
                break;
            default:
                unit = "m";
        }

        if (!showUnit) {
            unit = "";
        }

        return String.format("%4.3f%s", distance, unit);
    }


    private static String formatNumber(double distance) {
        String unit = " metre";
        if (distance > 1000) {
            distance /= 1000;
            unit = " km";

            return String.format("%4.1f%s", distance, unit);
        }

        return String.format("%4.0f%s", distance, unit);
    }


    public static void openPublicProfile(Context mContext, String userId, @Nullable String pic_url){
        Intent intent = new Intent(mContext,PublicProfileActivity.class);
        intent.putExtra(Constants.PIC_URL, pic_url);
        intent.putExtra("action_id",userId);
        mContext.startActivity(intent);

    }


    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(Activity activity){
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public static void showSoftKeyboard(View view) {
        if (view != null && view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            view.requestFocus();
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }






}

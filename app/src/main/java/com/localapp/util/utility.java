package com.localapp.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.localapp.ui.ExpandableListAdapter;
import com.localapp.ui.HomeActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkArgument;


/**
 * Created by 4 way on 17-04-2017.
 */

public class utility {


    public static String getTimeAndDate(String milliseconds) {
//        DateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        DateFormat sdfDay = new SimpleDateFormat("d");
        DateFormat sdf = new SimpleDateFormat("MMM yyyy EEEE,  hh:mm a");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(milliseconds));

        Log.v("date time: ",sdf.format(calendar.getTime()));

        return getDayOfMonthSuffix(Integer.parseInt(sdfDay.format(calendar.getTime())))+" "+sdf.format(calendar.getTime()).replace("AM", "am").replace("PM","pm");
    }

    static  String getDayOfMonthSuffix(final int n) {
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


    public static List<String> getProfessionList (String professionGroup) {

        switch (professionGroup) {
            case ExpandableListAdapter.PROFESSION_GROUP_STUDENT: return ExpandableListAdapter.PROFESSION_GROUP_STUDENT_LIST;
            case ExpandableListAdapter.PROFESSION_GROUP_PROFESSIONALS: return ExpandableListAdapter.PROFESSION_GROUP_PROFESSIONALS_LIST;
            case ExpandableListAdapter.PROFESSION_GROUP_SKILLS: return ExpandableListAdapter.PROFESSION_GROUP_SKILLS_LIST;
            case ExpandableListAdapter.PROFESSION_GROUP_HEALTH: return ExpandableListAdapter.PROFESSION_GROUP_HEALTH_LIST;
            case ExpandableListAdapter.PROFESSION_GROUP_REPAIR: return ExpandableListAdapter.PROFESSION_GROUP_REPAIR_LIST;
            case ExpandableListAdapter.PROFESSION_GROUP_WEDDING: return ExpandableListAdapter.PROFESSION_GROUP_WEDDING_LIST;
            case ExpandableListAdapter.PROFESSION_GROUP_BEAUTY: return ExpandableListAdapter.PROFESSION_GROUP_BEAUTY_LIST;
            case ExpandableListAdapter.PROFESSION_GROUP_HOUSEWIFE: return ExpandableListAdapter.PROFESSION_GROUP_HOUSEWIFE_LIST;
            default: return null;
        }
    }

}
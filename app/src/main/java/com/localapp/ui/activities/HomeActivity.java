package com.localapp.ui.activities;


import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RatingBar;

import com.google.android.gms.maps.model.LatLng;
import com.localapp.R;
import com.localapp.appcontroller.AppController;
import com.localapp.appcontroller.AppExceptionHandler;
import com.localapp.background.ConnectivityReceiver;
import com.localapp.background.LocationService;
import com.localapp.models.MessageNotificationData;
import com.localapp.preferences.AppPreferences;
import com.localapp.preferences.SessionManager;
import com.localapp.ui.adapters.SectionsPagerAdapter;
import com.localapp.ui.fragments.FeedFragment;
import com.localapp.ui.fragments.MapFragment;
import com.localapp.utils.Constants;
import com.localapp.utils.NetworkUtil;
import com.localapp.utils.Utility;

import java.util.HashMap;

import static com.localapp.utils.NotificationUtils.notificationList;
import static com.localapp.utils.NotificationUtils.numMessage;

public class HomeActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG = "HomeActivity";
    SessionManager session;
    public static String mLoginToken = "";
    public static LatLng mLastKnownLocation = null;
    public static String mUserId = "";
    public static String mPicUrl = null;
    public static String mUserName = "";
    TabLayout tabLayout;

    public static SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public static ViewPager mViewPager;

    private int[] tabIcons = {
            R.drawable.ic_map,
            R.drawable.ic_broadcast,
            R.drawable.ic_notice_board,
            R.drawable.ic_setting
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new AppExceptionHandler(this)); //DefaultUncaughtException

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        if (!NetworkUtil.isConnected()) {
            NetworkUtil.ErrorAppDialog(this);
        }

        if (!Utility.isServiceRunning(this,LocationService.class) &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.M  || Utility.hasPermissionsGranted(this, MapFragment.LOCATION_PERMISSIONS))) {
            startService(new Intent(AppController.getAppContext(), LocationService.class));
        }

        AppPreferences.getInstance(AppController.getAppContext()).incrementLaunchCount();
        showRateAppDialogIfNeeded();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        session = SessionManager.getInstance(this);

//        if (session.isLoggedIn()) {
            getLastLoginDetails();
//        }

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
//        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#2196f3"));
        setupTabIcons();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(ContextCompat.getColor(HomeActivity.this,R.color.titleColor), PorterDuff.Mode.SRC_IN);
                }

                if (FeedFragment.mActionMode != null && tab.getPosition() != 1) {
                    FeedFragment.mActionMode.finish();
                }

                Utility.hideSoftKeyboard(HomeActivity.this);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(ContextCompat.getColor(HomeActivity.this, R.color.iconColor), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });




        Intent intent = getIntent();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);



        for (MessageNotificationData data:notificationList) {
            manager.cancel(data.getNotificationId());//cancel message notification
        }

        manager.cancel(1);
        notificationList.clear();
        numMessage = 0;

        try {
            String notification = intent.getStringExtra("noti");
            if ( notification != null) {
                actionNotification(notification);
            }
        }catch (NullPointerException e) {
            e.printStackTrace();
        }


        //send crash report to server if crashed app
        try {
            final String errorReport = intent.getStringExtra(Constants.ERROR_REPORT);
            final String errorMessage = intent.getStringExtra(Constants.ERROR_MESSAGE);

            if (errorMessage != null || errorReport != null) {
                NetworkUtil.CrashReport(this,errorMessage,errorReport);
            }
        }catch (NullPointerException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        AppController.getInstance().addConnectivityListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppController.getInstance().clearConnectivityListener();
    }

    private void actionNotification(String notification) {
        switch (notification) {
            case "new_message":
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    private void setupTabIcons() {
        try {
            for (int i=0; i<tabIcons.length ;i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setIcon(tabIcons[i]);
                }
            }

            TabLayout.Tab tab = tabLayout.getTabAt(0);
            if (tab != null && tab.getIcon() != null) {
                tab.getIcon().setColorFilter(ContextCompat.getColor(this,R.color.titleColor), PorterDuff.Mode.SRC_IN);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (!isConnected) {
            NetworkUtil.ErrorAppDialog(this);
        }
    }



    void getLastLoginDetails() {

        HashMap<String, String> user = session.getUserDetails();
        mLoginToken = user.get(SessionManager.KEY_LOGIN_TOKEN);
        mUserId = user.get(SessionManager.KEY_LOGIN_USER_ID);
//        mUserId = "58b909b1f81fde3f9ce5ea31";//hardcoded
        mPicUrl = user.get(SessionManager.KEY_LOGIN_USER_PIC_URL);
        mUserName = user.get(SessionManager.KEY_LOGIN_USER_NAME);
        try {
            Double lat = Double.valueOf(user.get(SessionManager.KEY_LAT));
            Double lng = Double.valueOf(user.get(SessionManager.KEY_LNG));


            mLastKnownLocation = new LatLng(lat,lng);
        }catch (NullPointerException e){
            e.printStackTrace();
        }



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUEST_CHECK_SETTINGS:
                MapFragment mapFragment = MapFragment.getInstance();
                mapFragment.onActivityResult(requestCode,resultCode,data);
                break;
        }
    }



    //============================================== feedback ===================================//

    private void showRateAppDialogIfNeeded() {
        boolean bool = AppPreferences.getInstance(AppController.getAppContext()).getAppRate();
        int i = AppPreferences.getInstance(AppController.getAppContext()).getLaunchCount();
        if ((bool) && (i == 3)) {
            new CountDownTimerTask(10000,10000).start();

        }

        if ((bool) && (i%20 == 0)) {
            new CountDownTimerTask(10000,10000).start();
        }
    }

    float ratingStar = 0f;
    private AlertDialog createAppRatingDialog(String rateAppTitle, String rateAppMessage) {

       final View view = LayoutInflater.from(this).inflate(R.layout.app_rating,null);
        final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);







        final AlertDialog dialog  = new AlertDialog.Builder(this).setPositiveButton(getString(R.string.dialog_app_rate), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {

                if (ratingStar > 2) {
                    openAppInPlayStore(HomeActivity.this);
                    AppPreferences.getInstance(AppController.getAppContext()).setAppRate(false);
                }else {
                    openFeedback(HomeActivity.this);
                }

            }
        }).setNegativeButton(getString(R.string.dialog_your_feedback), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                openFeedback(HomeActivity.this);
                AppPreferences.getInstance(HomeActivity.this.getApplicationContext()).setAppRate(false);
            }
        }).setNeutralButton(getString(R.string.dialog_ask_later), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                paramAnonymousDialogInterface.dismiss();
                if (AppPreferences.getInstance(AppController.getAppContext()).getLaunchCount() < 4) {
                    AppPreferences.getInstance(AppController.getAppContext()).resetLaunchCount();
                }
            }
        }).setMessage(rateAppMessage).setTitle(rateAppTitle).setView(view).setCancelable(false).create();


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingStar = Math.round(rating);
                ratingBar.setRating(ratingStar);


            }
        });

        return dialog;
    }

    public static void openAppInPlayStore(Context paramContext) {
        try {
            paramContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LOCAL_APP_MARKET_URL)));
        }catch (ActivityNotFoundException e){
            paramContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LOCAL_APP_PLAY_STORE_URL)));
        }
    }

    public static void openFeedback(Context paramContext) {
        Intent localIntent = new Intent(Intent.ACTION_SEND);
        localIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.CONNECT_LOCAL_APP_EMAIL});
        localIntent.putExtra(Intent.EXTRA_CC, "");
        String str = null;
        int versionCode;
        try {
            str = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionName;
            versionCode = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionCode;
            localIntent.setPackage(Constants.GMAIL_PACKAGE);
            localIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for Localapp");
            localIntent.putExtra(Intent.EXTRA_TEXT, "\n\n----------------------------------\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + str + "\n App Version Code: " + versionCode + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER);
            localIntent.setType("message/rfc822");
            paramContext.startActivity(localIntent);
        } catch (Exception e) {
            Log.d("OpenFeedback", e.getMessage());
        }
    }


    private class CountDownTimerTask extends CountDownTimer {


        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CountDownTimerTask(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("CountDownTimerTask",": "+millisUntilFinished / 1000);
        }


        @Override
        public void onFinish() {
            try {
                createAppRatingDialog(getString(R.string.rate_app_title), getString(R.string.rate_app_message)).show();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }



}

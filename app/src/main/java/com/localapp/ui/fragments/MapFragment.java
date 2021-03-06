package com.localapp.ui.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.localapp.R;
import com.localapp.appcontroller.AppController;
import com.localapp.background.ConnectivityReceiver;
import com.localapp.background.LocationService;
import com.localapp.camera.Camera2Activity;
import com.localapp.models.GetUsersRequestData;
import com.localapp.models.NoticeBoard;
import com.localapp.models.NoticeBoardMessage;
import com.localapp.models.Profile;
import com.localapp.preferences.AppPreferences;
import com.localapp.preferences.SessionManager;
import com.localapp.network.helper.CommonRequest;
import com.localapp.network.GetNearestNoticeBoardRequest;
import com.localapp.network.GetNoticeBoardMessageRequest;
import com.localapp.network.GetProfileByIdRequest;
import com.localapp.network.GetUsersRequest;
import com.localapp.network.ImageSearchRequest;
import com.localapp.network.SubscribeUnsubscribeNoticeBoardRequest;
import com.localapp.network.UpdatePostBackRequest;
import com.localapp.ui.activities.HomeActivity;
import com.localapp.ui.adapters.DialogNoticeBoardMessageAdapter;
import com.localapp.ui.custom_views.MultiDrawable;
import com.localapp.ui.activities.InviteActivity;
import com.localapp.utils.AnimationUtility;
import com.localapp.utils.Constants;
import com.localapp.utils.CustomStringList;
import com.localapp.utils.NetworkUtil;
import com.localapp.utils.Utility;
import com.mobiruck.Mobiruck;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;



public class MapFragment extends Fragment implements OnMapReadyCallback, GetUsersRequest.GetUsersResponseCallback,
        ClusterManager.OnClusterClickListener<Profile>, ClusterManager.OnClusterInfoWindowClickListener<Profile>, ClusterManager.OnClusterItemClickListener<Profile>, ClusterManager.OnClusterItemInfoWindowClickListener<Profile>,
        ImageSearchRequest.ImageSearchResponseCallback, GetNearestNoticeBoardRequest.GetNearestNoticeBoardRequestCallback, GetNoticeBoardMessageRequest.GetNoticeBoardMessageRequestCallback, SubscribeUnsubscribeNoticeBoardRequest.SubscribeUnsubscribeNoticeBoardCallback,
        GetProfileByIdRequest.GetProfileByIdRequestCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,ConnectivityReceiver.ConnectivityReceiverListener,GoogleMap.OnCameraMoveStartedListener,GoogleMap.OnCameraMoveCanceledListener{


    //==============================================//

    // Keys for storing activity state in the Bundle.
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;


    //==============================================//


    public static String TAG = MapFragment.class.getSimpleName();

    private static final int REQUEST_CAMERA_PERMISSION_CODE = 201;
    public static final int REQUEST_CALL_PHONE_PERMISSION_CODE = 202;
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 200;
    private static final int REQUEST_READ_PHONE_STATE_CODE = 225;
    final static String[] CAMERA_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    public final static String[] CALL_PHONE_PERMISSIONS = {Manifest.permission.CALL_PHONE};
    public final static String[] LOCATION_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    final static String[] READ_PHONE_STATE_PERMISSIONS = {Manifest.permission.READ_PHONE_STATE};

//    ToolTipRelativeLayout toolTipRelativeLayout;

    SessionManager session;

    private GoogleMap mMap;
    private MapView mMapView;
    private View locationButton;
//    private LocationManager mLocationManager;
    public ArrayList<Profile> profileList;
    public ArrayList<Profile> noticeBoardProfileList;

    private ImageView professionalBtn, studentBtn,
            repairBtn, emergencyBtn,
            notice_boardBtn, hobbiesBtn;

    private ImageButton searchBtn, searchCameraBtn;
    private RelativeLayout uDetailLayout;
    private AutoCompleteTextView searchBoxView;

    private ArrayAdapter<String> autoCompleteAdapter;
    private List<String> searchContaintList;

    private LinearLayout botomFilter;
    private Button inviteButton;

    @Bind(R.id.searchCard)
    protected CardView searchCard;


    Toolbar toolbar;

    private ActionBar actionBar;


    private FirebaseAnalytics mFirebaseAnalytics;


    DialogNoticeBoardMessageAdapter messageAdapter;


    //******************************** tool tips *******************//

    private RelativeLayout overlayRL;
    private LinearLayout overlaySerachLL;
    private TextView textHelp;


    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment mapFragmentinstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        this.mapFragmentinstance = this;

        actionBar = ((HomeActivity)getActivity()).getSupportActionBar();

        //===========================//
        mRequestingLocationUpdates = true;
//        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();


        //===========================//
    }

    public static MapFragment getInstance() {
        return mapFragmentinstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        MapsInitializer.initialize(this.getActivity());

        ButterKnife.bind(this,view);

        Bundle extras = getActivity().getIntent().getExtras();

        //for notification
        try {
            if (extras != null) {
                String notificationUserID = extras.getString("userId");
                if (notificationUserID != null && !notificationUserID.equals("")) {
                    profileRequest(notificationUserID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        session = new SessionManager(getActivity());
        profileList = new ArrayList<>();
        noticeBoardProfileList = new ArrayList<>();
        searchContaintList = new CustomStringList();
        autoCompleteAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, searchContaintList);

        setupView(view);    //initialization view object

        mMapView = (MapView) view.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);


        if (!AppPreferences.getInstance(AppController.getAppContext()).isLaunchedMapToolTip()) {
            toolTips(view);
        }
        Log.v(TAG, "View ready");
        // Inflate the layout for this fragment


        return view;
    }

    /**
     * initialization view objects
     * @param view
     */
    private void setupView(View view) {


        studentBtn = (ImageView) view.findViewById(R.id.student_iv);
        hobbiesBtn = (ImageView) view.findViewById(R.id.hobbies_iv);
        professionalBtn = (ImageView) view.findViewById(R.id.professionals_iv);
        repairBtn = (ImageView) view.findViewById(R.id.repair_iv);
        emergencyBtn = (ImageView) view.findViewById(R.id.emergency_iv);
        notice_boardBtn = (ImageView) view.findViewById(R.id.notice_board_iv);
        searchBtn = (ImageButton) view.findViewById(R.id.search_btn);
//        searchCameraBtn = (ImageButton) view.findViewById(R.id.search_camera_btn);//TODO: remove image search image button from everywhere
        searchBoxView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
        uDetailLayout = (RelativeLayout) view.findViewById(R.id.user_detail_rl);
        botomFilter = (LinearLayout) view.findViewById(R.id.bottom_filter_lt);
        inviteButton = (Button) view.findViewById(R.id.invite_btn);

        //=======================//
        botomFilter.setVisibility(View.GONE);
        inviteButton.setVisibility(View.VISIBLE);
        //=======================//

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());




        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),InviteActivity.class));
            }
        });


        searchBoxView.addTextChangedListener(textWatcherForSearchBox);
        searchBoxView.setOnKeyListener(onKeyListener);
        searchBoxView.setAdapter(autoCompleteAdapter);
        searchBoxView.setThreshold(1);
        searchBoxView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(null);
                    return true;
                }
                return false;

            }
        });

        searchBoxView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                performSearch(autoCompleteAdapter.getItem(position));
            }
        });

        searchBoxView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && uDetailLayout.getVisibility() == View.VISIBLE) {
                    uDetailLayout.setVisibility(View.GONE);
                }
            }
        });


        searchBtn.setOnClickListener(searchOnClickListener);
//        searchCameraBtn.setOnClickListener(searchOnClickListener); //removed search listener
        studentBtn.setOnClickListener(filterClickListener);
        professionalBtn.setOnClickListener(filterClickListener);
        repairBtn.setOnClickListener(filterClickListener);
        emergencyBtn.setOnClickListener(filterClickListener);
        notice_boardBtn.setOnClickListener(filterClickListener);
        hobbiesBtn.setOnClickListener(filterClickListener);




    }



    /**
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //User has previously accepted this permission
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            //Not in api-23, no need to prompt
            mMap.setMyLocationEnabled(true);

        }


        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mClusterManager = new ClusterManager<Profile>(getContext(), mMap);
        mClusterManager.setRenderer(new ProfileRenderer());


        final CameraPosition[] mPreviousCameraPosition = {null};
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                CameraPosition position = mMap.getCameraPosition();
                if(mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position.zoom) {
                    mPreviousCameraPosition[0] = googleMap.getCameraPosition();
                    mClusterManager.cluster();
                }

                showMapController();
            }
        });
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mMap.setOnMapClickListener(onMapClickListener);

        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveCanceledListener(this);
//        mMap.setOnCameraIdleListener(this);

        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);


        if (NetworkUtil.isConnected() && HomeActivity.mLastKnownLocation != null) {
            request(HomeActivity.mLastKnownLocation);
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HomeActivity.mLastKnownLocation, 16.2f));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    HomeActivity.mLastKnownLocation).zoom(14f).build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            new CountDownTimerTask(5000, 5000).start();
        }
//        addItems();
//        mClusterManager.cluster();
        setMyLocationButton(); //adjust my location button
        Log.v(TAG, "Map Ready");
    }

    /**
     *
     * @return GoogleMap
     */
    protected GoogleMap getMap() {
        return mMap;
    }



    /**
     * set potion of myLocationButton
     */
    void setMyLocationButton() {

        if (mMapView != null &&
                mMapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                locationButton.setElevation(10f);
            }
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 0, 200);
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    boolean isStartedLocationUpdate = false;
    @Override
    public void onResume() {
        super.onResume();
        AppController.activityResumed();
        AppController.getInstance().addConnectivityListener(this);

        mMapView.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates && !isStartedLocationUpdate) {
            startLocationUpdates();
            Log.d(TAG,"onResume");
        }
        updateUI();

        View view = getView();
        if (view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(onKeyListener);
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        /*if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }*/
        AppController.activityPaused();
    }


    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
//        mLocationManager.removeUpdates(this);
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }


    /**
     * filter map marker
     */
    boolean isSelectedFilterButton = false;
    View.OnClickListener filterClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (uDetailLayout.getVisibility() != View.VISIBLE) {
                ArrayList<Integer> indexs;
                initFilterButtonSelection();
                isSelectedFilterButton = true;
                switch (v.getId()) {
                    case R.id.emergency_iv:
                        emergencyBtn.setImageResource(R.drawable.ic_health_selected);
                        indexs = filterIndexByProfession(profileList, Constants.PROFESSION_GROUP_HEALTH);
                        if (indexs != null && indexs.size() > 0) {
                            addMarkerByProfile(true, indexs);
                        }

                        break;
                    case R.id.student_iv:
                        studentBtn.setImageResource(R.drawable.ic_student_selected);
                        indexs = filterIndexByProfession(profileList, Constants.PROFESSION_GROUP_STUDENT);
                        if (indexs != null && indexs.size() > 0) {
                            addMarkerByProfile(true, indexs);
                        }
                        break;
                    case R.id.professionals_iv:
                        professionalBtn.setImageResource(R.drawable.ic_professionals_selected);
                        indexs = filterIndexByProfession(profileList, Constants.PROFESSION_GROUP_PROFESSIONALS);
                        if (indexs != null && indexs.size() > 0) {
                            addMarkerByProfile(true, indexs);
                        }
                        break;
                    case R.id.repair_iv:
                        repairBtn.setImageResource(R.drawable.ic_repair_selected);
                        indexs = filterIndexByProfession(profileList, Constants.PROFESSION_GROUP_REPAIR);
                        if (indexs != null && indexs.size() > 0) {
                            addMarkerByProfile(true, indexs);
                        }
                        break;
                    case R.id.notice_board_iv:
                        notice_boardBtn.setImageResource(R.drawable.ic_notice_selected);
                        requestForNearbyNoticeBoard();
                        break;

                    case R.id.hobbies_iv:
                        hobbiesBtn.setImageResource(R.drawable.ic_hobby_selected);
                        indexs = filterIndexByProfession(profileList, Constants.PROFESSION_GROUP_SKILLS);
                        if (indexs != null && indexs.size() > 0) {
                            addMarkerByProfile(true, indexs);
                        }
                        break;

                }
            }

        }
    };


    private void initFilterButtonSelection() {
        emergencyBtn.setImageResource(R.drawable.ic_health);
        studentBtn.setImageResource(R.drawable.ic_student);
        professionalBtn.setImageResource(R.drawable.ic_professionals);
        repairBtn.setImageResource(R.drawable.ic_repair);
        notice_boardBtn.setImageResource(R.drawable.ic_notice);
        hobbiesBtn.setImageResource(R.drawable.ic_hobby);
        isSelectedFilterButton = false;
    }



    private boolean isPermissionGrated() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) && getActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                    && getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("", "Permission is grated");
                return true;
            } else {
                Log.v("", "Permission not grated");
                return false;
            }
        }
        return false;
    }

    /**
     * Alert dialog for if your location setting is off
     * @param status
     */
    public void showAlertForLocationSetting(final int status) {
        String msg, title, btnText;
        /*if (status == 1) {
            msg = getString(R.string.alert_msg_location_setting);
            title = getString(R.string.enable_location);
            btnText = getString(R.string.location_Settings);
        } else {
            msg = getString(R.string.alert_msg_access_location);
            title = getString(R.string.permission_access);
            btnText = getString(R.string.grant);
        }*/

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final Dialog dialogperm = new Dialog(getActivity(),R.style.AppTheme);
        dialogperm.setCancelable(false);



        try {
            dialogperm.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        }catch (NullPointerException ne){
            ne.printStackTrace();
        }

        dialogperm.getWindow().setGravity(Gravity.BOTTOM);
        dialogperm.setContentView(R.layout.permission_dialog);

        View.OnClickListener permissionDiaogListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.permission_button){

                    if (status == 1){
                        requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSION_CODE);
                        dialogperm.dismiss();
                    }else {
                        startLocationUpdates();
                        dialogperm.dismiss();
                    }


                }else {
                    dialogperm.dismiss();
                }
            }
        };


        ImageView crossImageView = (ImageView) dialogperm.findViewById(R.id.imageView_close);
        Button permissionButton = (Button) dialogperm.findViewById(R.id.permission_button);

        crossImageView.setOnClickListener(permissionDiaogListener);
        permissionButton.setOnClickListener(permissionDiaogListener);


        if (!dialogperm.isShowing()) {
            dialogperm.show();
        }
    }


    AlertDialog dialog;

    public void showNoticeBoardDialog(final NoticeBoard noticeBoard, final boolean hasSubscribed) {

//        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        View view = LayoutInflater.from(getContext()).inflate(R.layout.notice_board_dialog, null);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.ll_notice_dialog);
        Button subButton = (Button) view.findViewById(R.id.subscribe_btn);

        if (HomeActivity.mUserId != null && HomeActivity.mUserId.equals(noticeBoard.getAdminId())) {
            subButton.setVisibility(View.GONE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }


        TextView noticeName = (TextView) view.findViewById(R.id.notice_board_name_textView);
        RecyclerView messageRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewDialog);

        final EditText messageEditText = (EditText) view.findViewById(R.id._input_notice_message);
        final ImageButton postBtn = (ImageButton) view.findViewById(R.id._notice_post_btn);

        noticeName.setText(noticeBoard.getName());


        if (hasSubscribed) {
            subButton.setText(R.string.unsubscribe);
        } else {
            subButton.setText(R.string.subscribe);
        }


        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HomeActivity.mUserId != null && !HomeActivity.mUserId.equals("")) {
                    CommonRequest.RequestType type;
                    if (hasSubscribed) {
                        type = CommonRequest.RequestType.COMMON_REQUEST_UNSUBSCRIBE_NOTICE_BOARD;

                    } else {
                        type = CommonRequest.RequestType.COMMON_REQUEST_SUBSCRIBE_NOTICE_BOARD;
                    }

                    requestSubscribeAndUnsub(noticeBoard, type);
                } else {
                    Toast.makeText(getContext(), R.string.login_first, Toast.LENGTH_SHORT).show();
                }

                if (dialog != null)
                    dialog.dismiss();

            }
        });


        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = messageEditText.getText().toString().trim();
                if (!msg.isEmpty()) {
                    NoticeBoardMessage message = new NoticeBoardMessage(msg);
                    message.setAdminId(noticeBoard.getId());

                    /*PostNoticeBoardMessageRequest postNoticeBoardMessageRequest = new PostNoticeBoardMessageRequest(getContext(),message,MapFragment.this);
                    postNoticeBoardMessageRequest.executeRequest();
                    noticeBoard.getMessagesList().add(message);
                    messageAdapter.notifyDataSetChanged();
                    messageEditText.setText("");*/
                }
            }
        });


        messageAdapter = new DialogNoticeBoardMessageAdapter(getContext(), noticeBoard);

        messageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        messageRecyclerView.setAdapter(messageAdapter);


//        dialog.setCancelable(false);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }


    /**
     * request for map data
     * @param latLng
     */
    void request(LatLng latLng) {
        GetUsersRequest usersRequest = new GetUsersRequest(getActivity(), latLng, HomeActivity.mLoginToken, MapFragment.this);
        usersRequest.executeRequest();


        //check location
        try {
            if (AppPreferences.getInstance(getActivity()).isMobiruckPostBack()) {
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);


                    String ncr = null;
                    try {
                        ncr = addresses.get(0).getLocality();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (ncr == null || !isMarketingLocation(ncr, true)) {

                        try {
                            String state = addresses.get(0).getAdminArea();

                            if (state == null || !isMarketingLocation(state, false)){

                                String state2 = addresses.get(0).getAddressLine(2);

                                if (state2 != null) {

                                    String stateArray[] = addresses.get(0).getAddressLine(2).split(",");

                                    if (!isMarketingLocation(stateArray[0], false)){
                                        AppPreferences.getInstance(getActivity()).setMobiruckSignupPostback(false);
                                    }

                                }else {
                                    Log.d("Signup Location",addresses.get(0).getAddressLine(0));
                                    AppPreferences.getInstance(getActivity()).setMobiruckSignupPostback(false);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }catch (Exception e) {
            e.printStackTrace();
        }


    }


    private boolean isMarketingLocation(String cityName, boolean isNCR) {

        Log.d(TAG, "isMarketingLocation called");
        if (isNCR) {
            switch (cityName) {
                case "Delhi":
                case "New Delhi":
                case "Mumbai":
                case "Pune":
                /*case "Bengaluru":
                case "Noida":
                case "Gurgaon":*/

                    String utm_source = AppPreferences.getInstance(getActivity()).getUtmSource();
                    if (utm_source.equals(Constants.UTM_SOURCE_EXPLETUS)) {
                        mobiRuckPostBack(cityName);
                    }else {
                        Log.d(TAG,utm_source+" not mobiRuckPostBack");
                        AppPreferences.getInstance(getActivity()).setMobiruckSignupPostback(false);
                    }
                    Log.d("state1", cityName);

                    return true;

                default: return false;
            }
        }

        switch (cityName) {
            case "New Delhi":
            case "Delhi":
            case "Mumbai":
            case "Pune":
//            case "Bengaluru":

                String utm_source = AppPreferences.getInstance(getActivity()).getUtmSource();
                if (utm_source.equals(Constants.UTM_SOURCE_EXPLETUS)) {
                    mobiRuckPostBack(cityName);
                }else {
                    Log.d(TAG,utm_source+" not mobiRuckPostBack");
                    AppPreferences.getInstance(getActivity()).setMobiruckSignupPostback(false);
                }
                Log.d("state2", cityName);
                return true;
            default:
                Log.d("state2", "location not found");
                return false;
        }

        /*


        case "Noida":
        case "Faridabad":
        case "Gurgaon":
        case "Gurugram":
        case "Ghaziabad":
        case "Pune":


        case "Pune":

        */


    }


    String postBackLocation;
    private void mobiRuckPostBack(String location){
        postBackLocation = location;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){

            String source = AppPreferences.getInstance(getActivity()).getUtmSource();

            if (AppPreferences.getInstance(getActivity()).isMobiruckPostBack() && source.equals(Constants.UTM_SOURCE_EXPLETUS)) {

                postBackUpdate();//update in db

                Mobiruck mMobiruck = new Mobiruck(getActivity());
                mMobiruck.triggerConversion();
                AppPreferences.getInstance(getActivity()).setUtm_source(Constants.BLANK_STRING);
                AppPreferences.getInstance(getActivity()).setMobiruckSignupPostback(false);
                recordFirebaseSignupEvent(location);
                Log.d("mobiRuckPostBack", "called");
            }else {
                AppPreferences.getInstance(getActivity()).setMobiruckSignupPostback(false);
            }


        }else {
            requestPermissions(READ_PHONE_STATE_PERMISSIONS, REQUEST_READ_PHONE_STATE_CODE);
        }

    }


    private void recordFirebaseSignupEvent(String location) {
        String id =  HomeActivity.mUserId;
        String name = HomeActivity.mUserName;

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "signup");
        bundle.putString(FirebaseAnalytics.Param.LOCATION, location);

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void postBackUpdate() {
        UpdatePostBackRequest postBackRequest = new UpdatePostBackRequest(getActivity(),HomeActivity.mUserId);
        postBackRequest.executeRequest();
    }



    private void profileRequest(String profileID) {
        Profile mProfile = new Profile(profileID);

        GetProfileByIdRequest request = new GetProfileByIdRequest(getContext(), mProfile, this);
        request.executeRequest();
    }


    private void requestForNearbyNoticeBoard() {
        if (HomeActivity.mLastKnownLocation != null) {
            GetNearestNoticeBoardRequest nearestNoticeBoardRequest = new GetNearestNoticeBoardRequest(getContext(), this, HomeActivity.mLastKnownLocation);
            nearestNoticeBoardRequest.executeRequest();
        } else {
            Toast.makeText(getApplicationContext(), R.string.please_wait_getting_location, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestForNoticeBoardMsg(NoticeBoard mNoticeBoard, boolean hasSubscribed) {
        GetNoticeBoardMessageRequest getNoticeBoardMessageRequest = new GetNoticeBoardMessageRequest(getContext(), mNoticeBoard, hasSubscribed, this);
        getNoticeBoardMessageRequest.executeRequest();
    }

    private void requestSubscribeAndUnsub(NoticeBoard mNoticeBoard, CommonRequest.RequestType requestType) {
        SubscribeUnsubscribeNoticeBoardRequest request = new SubscribeUnsubscribeNoticeBoardRequest(getContext(), mNoticeBoard.getId(), HomeActivity.mUserId, requestType, MapFragment.this);
        request.executeRequest();
    }


    @Override
    public void onGetUsersResponse(CommonRequest.ResponseCode res, GetUsersRequestData data) {
        switch (res) {
            case COMMON_RES_SUCCESS:
                if (profileList.size() > 0) {
                    profileList.clear();
                }
                profileList = data.getProfileList();
                if (uDetailLayout.getVisibility() != View.VISIBLE) {
                    mClusterManager.clearItems();
                    mClusterManager.addItems(profileList);
//                addMarkerByProfile(false, null);
//                addItems();
                    mClusterManager.cluster();
                }

                setSearchHintData(profileList);

                /*if (profileList.size() > 10) {
                    botomFilter.setVisibility(View.VISIBLE);
                    inviteButton.setVisibility(View.GONE);
                }else {
                    botomFilter.setVisibility(View.GONE);
                    inviteButton.setVisibility(View.VISIBLE);
                }*/

                break;
            case COMMON_RES_CONNECTION_TIMEOUT:
                break;
            case COMMON_RES_FAILED_TO_CONNECT:
//                Toast.makeText(getContext(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                break;
            case COMMON_RES_INTERNAL_ERROR:
                break;
            case COMMON_RES_SERVER_ERROR_WITH_MESSAGE:
                break;
        }
    }


    @Override
    public void GetNearestNoticeBoardResponse(CommonRequest.ResponseCode responseCode, List<NoticeBoard> mNoticeBoards) {
        if (responseCode == CommonRequest.ResponseCode.COMMON_RES_SUCCESS) {

            if (noticeBoardProfileList.size() > 0) {
                noticeBoardProfileList.clear();
            }

            for (NoticeBoard noticeBoard : mNoticeBoards) {

                Profile profile = new Profile(noticeBoard.getId());
                profile.setuToken(noticeBoard.getAdminId());
                profile.setuLatLng(noticeBoard.getLocation());
                profile.setuSpeciality("nnnnnnnnnn");
                profile.setuName(noticeBoard.getName());
                noticeBoardProfileList.add(profile);

            }

            if (mClusterManager != null) {
                mClusterManager.clearItems();
                mClusterManager.addItems(noticeBoardProfileList);
                mClusterManager.cluster();
            }




            /*if (nearestNoticeBoardList.size()>0) {
                nearestNoticeBoardList.clear();
            }

            if (mNoticeBoards.size() != 0){
                nearestNoticeBoardList.addAll(mNoticeBoards);
                noticeAdapterNearYou.notifyDataSetChanged();
            }*/
        }
    }

    @Override
    public void GetNoticeBoardMessageResponse(CommonRequest.ResponseCode responseCode, NoticeBoard mNoticeBoard, boolean hasSubscribed) {
        if (responseCode == CommonRequest.ResponseCode.COMMON_RES_SUCCESS) {
            showNoticeBoardDialog(mNoticeBoard, hasSubscribed);
        }
    }

    @Override
    public void SubscribeUnsubscribeNoticeBoardResponse(CommonRequest.ResponseCode responseCode, CommonRequest.RequestType mRequestType, String errorMsg) {
        String req = "subscribed";
        if (mRequestType == CommonRequest.RequestType.COMMON_REQUEST_SUBSCRIBE_NOTICE_BOARD) {
            req = "subscribed";
        }else {
            req = "unsubscribed";
        }
        if (responseCode == CommonRequest.ResponseCode.COMMON_RES_SUCCESS) {
            Toast.makeText(getContext(), "Noticeboard " + req , Toast.LENGTH_SHORT).show();
        } else if (responseCode == CommonRequest.ResponseCode.COMMON_RES_SERVER_ERROR_WITH_MESSAGE) {
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * add marker by profile data
     */
    void addMarkerByProfile(boolean isFilter, ArrayList<Integer> filterIndex) {
        int filterIndexSize = 0;

        if (mClusterManager != null) {
            mClusterManager.clearItems();

            if (filterIndex != null) {
                filterIndexSize = filterIndex.size();
            }

            if (!isFilter) {
                mClusterManager.addItems(profileList);
            } else {
                for (int i = 0; i < filterIndexSize; i++) {
                    int profileIndex = filterIndex.get(i);
                    mClusterManager.addItem(profileList.get(profileIndex));
                }
            }
            mClusterManager.cluster();
            if (filterIndexSize == 1) {
                markerClickWindow(profileList.get(filterIndex.get(0)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(profileList.get(filterIndex.get(0)).getuLatLng(), 16.8f));
            }else {
                if (isFilter) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HomeActivity.mLastKnownLocation, 12.5f));
                }
            }
        }
    }


    void showNotificationProfile(Profile mProfile) {

        mProfile.setuPrivacy("0");
        if (mClusterManager != null) {
            mClusterManager.clearItems();

            mClusterManager.addItem(mProfile);

            mClusterManager.cluster();

            markerClickWindow(mProfile);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mProfile.getuLatLng(), 16.8f));

        }
    }

    /**
     * filter index by search string
     * @param profileList
     * @param searchString
     * @return
     */

    ArrayList<Integer> search(ArrayList<Profile> profileList, String searchString) {
        searchString = searchString.toLowerCase();
        ArrayList<Integer> profileIndex = new ArrayList<>();

        int size = profileList.size();

        for (int i = 0; i < size; i++) {
            Profile profile = profileList.get(i);
            String dataString = "";
            String pName = profile.getuName();
            String pNotes = profile.getuNotes();
            String pSpeciality = profile.getuSpeciality();
            String[] pProfessionStrings = profile.getProfession().split(",");
            StringBuilder pProfession = new StringBuilder();

            if (pName.equals("null")) {
                pName = "";
            }

            if (pNotes == "null") {
                pNotes = "";
            }

            if (pSpeciality == "null") {
                pSpeciality = "";
            }

            for (String s : pProfessionStrings){
                if (pProfession.length() != 0) {
                    pProfession.append(", ").append(s);
                }else {
                    pProfession.append(s);
                }
            }





            dataString = pName + " " + pNotes + " " + pSpeciality + " " + pProfession.toString();
            dataString = dataString.toLowerCase();

            StringTokenizer st = new StringTokenizer(dataString, ",");
            boolean isFound = false;
            /*while (st.hasMoreTokens()) {
                String ss = st.nextToken();
                Log.d("search st ",ss);
                if (ss.equals(searchString)) {
                    isFound = true;
                }
            }

            if (!isFound) {
                StringTokenizer st1 = new StringTokenizer(dataString, " ");
                while (st1.hasMoreTokens()) {
                    String ss = st1.nextToken();
                    Log.d("search st1 ",ss);
                    if (ss.equals(searchString)) {
                        isFound = true;
                    }
                }
            }*/

            if (pName.toLowerCase().equals(searchString)) {  //for full name
                isFound = true;
            }

            if (dataString.matches(".*\\b"+searchString+"\\b.*")){
                isFound = true;
            }

            if (!isFound) {
                String[] search = searchString.split("[\\s,;]+");

                for (String s : search) {
                    if (dataString.matches(".*\\b"+s+"\\b.*")) {
                        isFound = true;
                        break;
                    }
                }
            }

            if (isFound) {
                profileIndex.add(i);
                isFound = false;
            }


        }

        return profileIndex;
    }


    public void setSearchHintData(List<Profile> mProfileList) {
        String[] uSpeciality = new String[0];
        String[] uNotes = new String[0];
        String[] profession = new String[0];
        if (searchContaintList.size() > 0) {
            searchContaintList.clear();
        }
        for (Profile profile : mProfileList) {

            try {
                uSpeciality = profile.getuSpeciality().split(" ");
                uNotes = profile.getuNotes().split(" ");
                profession = profile.getProfession().split(",");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!searchContaintList.contains(profile.getuName())) {
                searchContaintList.add(profile.getuName().toLowerCase());
            }

            for (String s : uSpeciality) {
                if (s.length() > 2 && !searchContaintList.contains(s)) {
                    searchContaintList.add(s);
                }
            }

            for (String s : uNotes) {
                if (s.length() > 2 && !searchContaintList.contains(s)) {
                    searchContaintList.add(s);
                }
            }

            for (String s : profession) {
                if (s.length() > 2 && !searchContaintList.contains(s)) {
                    searchContaintList.add(s);
                }
            }


        }



        Set<String> searchListWithoutDuplicates = new LinkedHashSet<>(searchContaintList);
        searchContaintList.clear();
        searchContaintList.addAll(searchListWithoutDuplicates);

        autoCompleteAdapter.notifyDataSetChanged();
    }

    /**
     * filter index by profession
     * @param profileList
     * @param professionGroup
     * @return
     */
    /*ArrayList<Integer> filterIndexByProfession(ArrayList<Profile> profileList, String profession) {
        profession = profession.toLowerCase();
        ArrayList<Integer> profileIndex = new ArrayList<>();

        int size = profileList.size();

        for (int i = 0; i < size; i++) {
            Profile profile = profileList.get(i);
            String profileProfession = profile.getProfession();

            if (profession.equals(profileProfession.toLowerCase())) {
                profileIndex.add(i);
            }
        }
        return profileIndex;
    }*/

    ArrayList<Integer> filterIndexByProfession(ArrayList<Profile> profileList, String professionGroup) {

        ArrayList<Integer> profileIndex = new ArrayList<>();

        int size = profileList.size();

        for (int i = 0; i < size; i++) {
            Profile profile = profileList.get(i);
            String[] profileProfession = profile.getProfession().split(",");

            for (String profession : profileProfession) {
                try {
                    if (Utility.getProfessionList(professionGroup).contains(profession)) {
                        profileIndex.add(i);
                        break;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }


        }
        return profileIndex;
    }


    /**
     * text watcher for search box
     */
    TextWatcher textWatcherForSearchBox = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (TextUtils.isEmpty(searchBoxView.getText()) && profileList != null) {
                addMarkerByProfile(false, null);
//                searchCameraBtn.setVisibility(View.VISIBLE);

            } /*else {
                searchCameraBtn.setVisibility(View.GONE);
            }*/


        }


        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(searchBoxView.getText()) && profileList != null) {
                addMarkerByProfile(false, null);
//                searchCameraBtn.setVisibility(View.VISIBLE);

            } /*else {
                searchCameraBtn.setVisibility(View.GONE);
            }*/
        }
    };



    /**
     * search listener
     */
    View.OnClickListener searchOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.search_btn) {
                performSearch(null);
            } /*else {                                  //removed image search button listener
                if (isCameraPermissionGrated()) {
                    openCamera();
                } else {
                    requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSION_CODE);
                }

            }*/

        }
    };


    private void performSearch(@Nullable String searchString) {

        Utility.hideSoftKeyboard(getActivity());
        searchBoxView.clearFocus();

        String searchQuery = searchString != null ? searchString.trim() : searchBoxView.getText().toString().trim();
        if (!TextUtils.isEmpty(searchQuery)) {
            ArrayList<Integer> indexs = search(profileList, searchQuery);
            if (indexs != null && indexs.size() > 0) {
                addMarkerByProfile(true, indexs);
            }else {
                Toast.makeText(getActivity(), getText(R.string.message_no_result_found_try_diffrent), Toast.LENGTH_SHORT).show();
            }
        }

    }

    void openCamera() {
        Intent i = new Intent(getContext(), Camera2Activity.class);
        i.putExtra("requestCode", 20);
        startActivityForResult(i, 20);
    }

    boolean isCameraPermissionGrated() {
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
//                        requestLocation();
                        startLocationUpdates();
                        if (!Utility.isServiceRunning(getActivity(),LocationService.class)) {
                            getActivity().startService(new Intent(AppController.getAppContext(), LocationService.class));
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
//                    Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();

                    searchBoxView.requestFocus();
                    showAlertForLocationSetting(1);

                }
                break;

            case REQUEST_CAMERA_PERMISSION_CODE:
                //commented code to disable open camera for image search
                /*if (grantResults.length > 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
                }*/
                break;

            case REQUEST_CALL_PHONE_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "You can now call!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_READ_PHONE_STATE_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mobiRuckPostBack(postBackLocation);
                }else {
//                    Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
                }


        }

    }

    protected Context getApplicationContext() {
        return getContext();
    }

    /**
     * show user profile when click on map profile
     * user can call and email from this window
     * @param profile
     */
    private void markerClickWindow(final Profile profile) {

        String pName = profile.getuName();
        String pTitle = profile.getProfession();
        String pPrivacy = profile.getuPrivacy();
        String mMobile = profile.getuMobile();
        final  String pic_url = profile.getuPictureURL();
        final String pEmail = profile.getuEmail();
        final String user_id = profile.getuId();

        View view = getView();
        if (view == null) return;

        TextView textView = (TextView) view.findViewById(R.id.user_name);
        TextView titleView = (TextView) view.findViewById(R.id.user_title);
        ImageView actionEmail = (ImageView) view.findViewById(R.id.action_email);
        ImageView actionCall = (ImageView) view.findViewById(R.id.action_call);
        if (mMobile != null && !mMobile.equals("null")) {
            actionCall.setVisibility(View.VISIBLE);
        }else {
            actionCall.setVisibility(View.GONE);
        }

        CircularImageView proPicNetworkImageView = (CircularImageView) getView().findViewById(R.id.user_pic);
        Picasso.with(AppController.getAppContext()).load(pic_url).into(proPicNetworkImageView);
        proPicNetworkImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_id != null) {
                    Utility.openPublicProfile(getContext(),user_id, pic_url);
                }
            }
        });
//            proPicNetworkImageView.setImageUrl(profile.getuPictureURL(), VolleySingleton.getInstance(getApplicationContext()).getImageLoader());
        if (pName != null) {
            textView.setText(pName);
        }else {
            textView.setText("");
        }

        if (pTitle != null && !pTitle.equals("null")) {
            titleView.setText(pTitle);
        }else {
            titleView.setText("");
        }
        if (pPrivacy != null && !pPrivacy.equals("null") && !pPrivacy.equals("0")) {
            actionCall.setVisibility(View.GONE);
        }
        actionCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    String mMobile = profile.getuMobile();
                    if (mMobile != "null") {
                        mMobile = "+91" + mMobile;
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", mMobile, null));
                        startActivity(callIntent);
                    }
                }else {
                    requestPermissions(CALL_PHONE_PERMISSIONS,REQUEST_CALL_PHONE_PERMISSION_CODE);
                }

            }
        });

        actionEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Email = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", pEmail, null));

                startActivity(Intent.createChooser(Email, "Send Email:"));
            }
        });

        uDetailLayout.setVisibility(View.VISIBLE);

    }


    /**
     * Demonstrates heavy customisation of the look of rendered clusters.
     */

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */

    private ClusterManager<Profile> mClusterManager;

    @Override
    public void onProfileIdResponse(CommonRequest.ResponseCode responseCode, Profile mProfile) {
        if (responseCode == CommonRequest.ResponseCode.COMMON_RES_SUCCESS) {
            showNotificationProfile(mProfile);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected && HomeActivity.mLastKnownLocation != null) {
            request(HomeActivity.mLastKnownLocation);
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) { //hide map controller when user use gesture on map
            hideMapController();
        }
    }

    @Override
    public void onCameraMoveCanceled() {
        hideMapController();
    }

    /**
     * hide all supporting view components
     * from the map
     * with animation
     */
    private void hideMapController() {
        AnimationUtility.hideViewWithAnimation(inviteButton, AnimationUtility.Direction.DOWN);
        AnimationUtility.hideViewWithAnimation(searchCard, AnimationUtility.Direction.UP);
        AnimationUtility.hideViewWithAnimation(locationButton, AnimationUtility.Direction.LEFT);

        if (uDetailLayout.getVisibility() == View.VISIBLE) {
            AnimationUtility.hideViewWithAnimation(uDetailLayout, AnimationUtility.Direction.DOWN);
        }


        if (actionBar != null) {
//            actionBar.hide();
//            hideActionBar(actionBar);
        }
    }

    /**
     * show all supporting view components
     * from the map
     * with animation
     */
    private void showMapController() {
        AnimationUtility.showViewWithAnimation(inviteButton, AnimationUtility.Direction.DOWN);
        AnimationUtility.showViewWithAnimation(searchCard, AnimationUtility.Direction.UP);
        AnimationUtility.showViewWithAnimation(locationButton, AnimationUtility.Direction.LEFT);

        if (uDetailLayout.getVisibility() == View.INVISIBLE) {
            AnimationUtility.showViewWithAnimation(uDetailLayout, AnimationUtility.Direction.DOWN);
        }

        if (actionBar != null) {
//            actionBar.show();
//            showActionBar(actionBar);
        }
    }


    protected void hideActionBar(final ActionBar ab){

        if (ab != null && ab.isShowing()) {
            if(toolbar != null) {
                toolbar.animate().translationY(-112).setDuration(600L)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                ab.hide();
                            }
                        }).start();
            } else {
                ab.hide();
            }
        }
    }

    protected void showActionBar(ActionBar ab){
        if (ab != null && !ab.isShowing()) {
            ab.show();
            if(toolbar != null) {
                toolbar.animate().translationY(0).setDuration(600L).start();
            }
        }
    }




    private class ProfileRenderer extends DefaultClusterRenderer<Profile> {


        private IconGenerator mIconGenerator = new IconGenerator(getActivity().getApplication());

        private IconGenerator mClusterIconGenerator = new IconGenerator(getActivity().getApplication());
        private ImageView mImageView;
        private ImageView mImageViewC;
        private ImageView mClusterImageView;
        private int mDimension;

        public ProfileRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.cluster_image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }



        @Override
        protected void onBeforeClusterItemRendered(Profile profile, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            Picasso.with(AppController.getAppContext()).load(profile.getuPictureURL()).into(mImageView);


            if (profile.getuSpeciality().equals("nnnnnnnnnn")) {
                mImageView.setImageResource(R.drawable.ic_notice);
            }

            Bitmap icon = mIconGenerator.makeIcon();
            try {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(profile.getuName());
            }catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Profile> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).

            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;


            for (Profile p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;

                try {
                    Drawable drawable = null;
                    try {
                        mImageViewC = (ImageView) getView().findViewById(R.id.temp);
                        Picasso.with(AppController.getAppContext()).load(p.getuPictureURL()).placeholder(R.drawable.ic_user).into(mImageViewC);
                        drawable = getResources().getDrawable(R.drawable.ic_user);
                        drawable = mImageViewC.getDrawable();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        drawable = getResources().getDrawable(R.drawable.ic_user);
                    }

                    if (p.getuSpeciality().equals("nnnnnnnnnn")) {
                        mImageViewC.setImageResource(R.drawable.ic_notice);
                        drawable = mImageViewC.getDrawable();
                    }
                /*if (p.getProfession() != null){
                    drawable = getResources().getDrawable(getClusterDrawable(p.getProfession()));
                }*/

//                Drawable drawable = getResources().getDrawable(p.profilePhoto);
                    drawable.setBounds(0, 0, width, height);
                    profilePhotos.add(drawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

            if (cluster.getSize() == 1) {
                for (Profile p2 : cluster.getItems()) {
                    markerClickWindow(p2);
                }

            }
        }
        float zoom;
        @Override
        protected boolean shouldRenderAsCluster(Cluster<Profile> cluster) {

           try {
               getActivity().runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       zoom = mMap.getCameraPosition().zoom;
                   }
               });
           }catch (NullPointerException nep) {
               nep.printStackTrace();
           }


            if (zoom > 20.9f) {
                return false;
            }

            return cluster.getSize() > 1;
        }


    }


    @Override
    public boolean onClusterClick(Cluster<Profile> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().getuName();
//        Toast.makeText(getContext(), cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    @Override
    public void onClusterInfoWindowClick(Cluster<Profile> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Profile profile) {
        // Does nothing, but you could go into the user's profile page, for example.
        searchBoxView.clearFocus();
        Utility.hideSoftKeyboard(getActivity());

        if (profile.getuSpeciality() != null && !profile.getuSpeciality().equals("nnnnnnnnnn")) {
            markerClickWindow(profile);
        } else {
            NoticeBoard noticeBoard = new NoticeBoard();
            noticeBoard.setId(profile.getuId());
            noticeBoard.setName(profile.getuName());
            requestForNoticeBoardMsg(noticeBoard, false);
        }



        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(Profile profile) {
        // Does nothing, but you could go into the user's profile page, for example.

    }


    ProgressDialog mProgressDialog;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 20:
                if (resultCode == 20) {
                    //disable image search request
                   /* Uri resultData = Uri.parse(data.getStringExtra("result"));

                    File imgFile = new File(resultData.getPath());
                    int file_size = Integer.parseInt(String.valueOf(imgFile.length()/1024));

                    if (file_size > 80) {//compress if file size more than 80kb
                        imgFile = Compressor.getDefault(getActivity()).compressToFile(imgFile);
                    }
                    ImageSearchRequest searchRequest = new ImageSearchRequest(getContext(), imgFile , this);
                    searchRequest.executeRequest();
                    mProgressDialog = new ProgressDialog(getContext());
                    mProgressDialog.setMessage(getString(R.string.message_please_wait_getting_result));
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();*/
                }
                break;

            case Constants.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
//                        showAlertForLocationSetting(2);
                        getActivity().finish();
                        /*mRequestingLocationUpdates = false;
                        updateUI();*/
                        break;
                }
                break;

            case REQUEST_RESOLVE_ERROR:
                mResolvingError = false;
                if (resultCode == RESULT_OK) {
                    // Make sure the app is not already connected or attempting to connect
                    if (!mGoogleApiClient.isConnecting() &&
                            !mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.connect();
                    }
                }



        }


    }


    @Override
    public void ImageSearchResponse(CommonRequest.ResponseCode responseCode, List<Profile> uProfile, String errorMsg) {
        mProgressDialog.dismiss();
        switch (responseCode) {
            case COMMON_RES_SUCCESS:
                ArrayList<Integer> indexs = new ArrayList<>();
                for (Profile p : profileList) {

                    boolean isFound = false;
                    for (Profile rProfile : uProfile){
                        if (p.getuId() != null && p.getuId().contentEquals(rProfile.getuId())) {
                            indexs.add(profileList.indexOf(p));
                            isFound = true;

                            break;
                        }
                    }

                    if (isFound)break;


                }

                if (indexs.size() > 0) {
                    addMarkerByProfile(true, indexs);
                } /*else if (!uProfile.isEmpty()){
                    profileList.add(uProfile.get(0));
                    indexs.add(profileList.indexOf(uProfile.get(0)));
                    addMarkerByProfile(true, indexs);
//
                }*/else {
                    Toast.makeText(getContext(), getText(R.string.message_no_result_found), Toast.LENGTH_SHORT).show();
                }
                break;
            case COMMON_RES_CONNECTION_TIMEOUT:
                Toast.makeText(getContext(), getText(R.string.error_something_went_wrong_try_again), Toast.LENGTH_SHORT).show();
                break;
            case COMMON_RES_FAILED_TO_CONNECT:
                Toast.makeText(getContext(), R.string.no_internet_msg, Toast.LENGTH_SHORT).show();
                break;
            case COMMON_RES_INTERNAL_ERROR:
                break;
            case COMMON_RES_SERVER_ERROR_WITH_MESSAGE:
                Toast.makeText(getContext(), getText(R.string.message_no_result_found), Toast.LENGTH_SHORT).show();
                addMarkerByProfile(false, null);
                break;
        }
    }

    /**
     * for testing only
     * generate random location
     */
    private Random mRandom = new Random(1984);

    private LatLng position() {
        return new LatLng(random(28.545623, 28.28494009999999), random(77.330507, 76.3514683));
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }

    int onlyOneTime = 0;
    View.OnKeyListener onKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() != KeyEvent.ACTION_DOWN && (uDetailLayout.getVisibility() == View.VISIBLE || isSelectedFilterButton)) {

                uDetailLayout.setVisibility(View.GONE);
                initFilterButtonSelection();
                addMarkerByProfile(false, null);
                searchBoxView.clearFocus();
                return true;
            } else {
                if (onlyOneTime == 0 && keyCode == KeyEvent.KEYCODE_BACK && event.getAction() != KeyEvent.ACTION_DOWN) {
                    Snackbar closeAppSnackbar = Snackbar.make(getView(), R.string.close_app_msg, Snackbar.LENGTH_LONG);
                    closeAppSnackbar.show();
                    closeAppSnackbar.setCallback(snackbarCallback);
                    searchBoxView.clearFocus();
                    onlyOneTime++;
                    return true;
                }
            }
            return false;
        }
    };

    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            uDetailLayout.setVisibility(View.GONE);
            initFilterButtonSelection();
            addMarkerByProfile(false, null);
            searchBoxView.clearFocus();

            try {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
    };


    Snackbar.Callback snackbarCallback = new Snackbar.Callback() {
        @Override
        public void onDismissed(Snackbar snackbar, int event) {
            super.onDismissed(snackbar, event);
            onlyOneTime = 0;
        }
    };



    private class CountDownTimerTask extends CountDownTimer {


        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        private CountDownTimerTask(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("CountDownTimerTask", ": " + millisUntilFinished / 1000);
        }

        @Override
        public void onFinish() {
            if (AppController.isActivityVisible()) {
                if (NetworkUtil.isConnected() && HomeActivity.mLastKnownLocation != null) {
                    request(HomeActivity.mLastKnownLocation);
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HomeActivity.mLastKnownLocation, 16.3f));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(
                            HomeActivity.mLastKnownLocation).zoom(14f).build();

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    new CountDownTimerTask(5000, 5000).start();
                }
            }else {
                new CountDownTimerTask(5000, 5000).start();
            }
        }
    }


    //================================================================================================================================//


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }



    private void updateUI() {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    protected void startLocationUpdates() {
        Log.d(TAG,"startLocationUpdates");
        LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                        }

                        try {
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mGoogleApiClient, mLocationRequest, gLocationListener);
                            isStartedLocationUpdate = true;

                        }catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                "location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            status.startResolutionForResult(getActivity(), Constants.REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                        mRequestingLocationUpdates = false;
                }
                updateUI();
            }
        });

    }


    protected void stopLocationUpdates() {
        Log.d(TAG,"stopLocationUpdates");
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                gLocationListener
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.d(TAG,status.toString());

                isStartedLocationUpdate = false;
//                mRequestingLocationUpdates = false;
//                setButtonsEnabledState();
            }
        });
    }

    com.google.android.gms.location.LocationListener gLocationListener = new com.google.android.gms.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateLocationUI();

            Log.d(TAG,mLastUpdateTime);
        }

    };

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            Log.v(TAG, "onLocationChanged");
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            HomeActivity.mLastKnownLocation = latLng;
            session.saveLastLocation(latLng);
            request(latLng);
//            drawCircle(latLng);
        }
    }








    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSION_CODE);
            return;
        }

//        showErrorDialog(0);
        Log.d(TAG,"onConnected");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
//            updateLocationUI();
        }
        if (mRequestingLocationUpdates) {
            Log.i(TAG, "in onConnected(), starting location updates");
            startLocationUpdates();
        }

    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }




    private boolean mResolvingError = false;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(getActivity(), REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }

    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getActivity().getFragmentManager(), "errordialog");
    }


    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            MapFragment.getInstance().onDialogDismissed();
        }
    }


    /**
     *  Draw circle in the map
     * @param mLatLng
     */
    public void drawCircle(LatLng mLatLng) {
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(mLatLng)
                .radius(5000)
                .strokeColor(getResources().getColor(R.color.black_overlay)).strokeWidth(4.0f));
    }



    private void toolTips (View view) {
        overlayRL = (RelativeLayout) view.findViewById(R.id.rlOverlay);
        overlaySerachLL = (LinearLayout) view.findViewById(R.id.rlSearchBox);
        textHelp = (TextView) view.findViewById(R.id.textHelp);

        overlayRL.setVisibility(View.VISIBLE);

        textHelp.setOnClickListener(toolTipClickListener);
        overlayRL.setOnClickListener(toolTipClickListener);
    }

    private View.OnClickListener toolTipClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            overlayRL.setVisibility(View.GONE);
            AppPreferences.getInstance(AppController.getAppContext()).mapToolTipLaunched();
        }
    };


}

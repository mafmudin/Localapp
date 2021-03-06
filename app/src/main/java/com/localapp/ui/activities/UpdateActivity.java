package com.localapp.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.localapp.R;
import com.localapp.models.Profile;
import com.localapp.network.helper.CommonRequest;
import com.localapp.network.GetProfileRequest;
import com.localapp.network.UpdateEmailRequest;
import com.localapp.network.UpdateProfileRequest;
import com.localapp.ui.adapters.ExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class UpdateActivity extends AppCompatActivity implements GetProfileRequest.GetProfileRequestCallback,UpdateProfileRequest.UpdateProfileResponseCallback,
        UpdateEmailRequest.UpdateEmailRequestCallback {

    LinearLayout personalLayout, aboutLayout;

    private ProgressBar mProgressBar;
    EditText mNameView, mNumberView,mProfessionView, mEmailView, mInfoView, mDetailView;
    private int whichUpdate;
    //The "x" and "y" position of the "Show Button" on screen.
    Point p;
    public static final int REQUEST_PERSONAL = 0;
    public static final int REQUEST_ABOUT = 1;
    public static final int REQUEST_ALL = 2;

    boolean numberVisibility = true;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    ArrayList<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    HashMap<String, List<Drawable>> listIconChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.hide();
        }

        whichUpdate = getIntent().getIntExtra("request",REQUEST_PERSONAL);


        personalLayout = (LinearLayout) findViewById(R.id._personal);
        aboutLayout = (LinearLayout) findViewById(R.id._about);

        mNameView = (EditText) findViewById(R.id.input_name);
        mNumberView = (EditText) findViewById(R.id.input_phoneNumber);
        mEmailView = (EditText) findViewById(R.id.input_email);
        mProfessionView = (EditText) findViewById(R.id.input_profession);
        mInfoView = (EditText) findViewById(R.id.input_brief_intro);
        mDetailView = (EditText) findViewById(R.id.input_details_des);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        /*mProfessionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onItemClick(View v) {
                showPopup(UpdateActivity.this);
            }
        });
*/
        mEmailView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    emailUpdateDialog("").show();
                }
                return false;
            }

        });
        mProfessionView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    showPopup(UpdateActivity.this);
                }
                return false;
            }
        });

        mNumberView.setTag("0");//privacy 0 means visible and 1 means hide
        mNumberView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (mNumberView.getRight() - mNumberView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if (numberVisibility) {
//                            mNumberView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, R.drawable.ic_password_hidden, 0);
                            mNumberView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_phone,0,R.drawable.ic_password_hidden,0);
                            numberVisibility = false;
                            mNumberView.setTag("1");
                        }else {
//                            mNumberView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, R.drawable.ic_password_visible, 0);
                            mNumberView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_phone, 0, R.drawable.ic_password_visible, 0);
                            numberVisibility = true;
                            mNumberView.setTag("0");
                        }

                    }
                }
                return false;
            }
        });



        if (whichUpdate == REQUEST_PERSONAL) {
            personalLayout.setVisibility(View.VISIBLE);
            aboutLayout.setVisibility(View.GONE);
        }else if (whichUpdate == REQUEST_ABOUT){
            personalLayout.setVisibility(View.GONE);
            aboutLayout.setVisibility(View.VISIBLE);
        }else {
            personalLayout.setVisibility(View.VISIBLE);
            aboutLayout.setVisibility(View.VISIBLE);
            mNumberView.requestFocus();
        }

        profileRequest();
    }


    // The method that displays the popup.
    private void showPopup(final Activity context) {

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.activity_select_profession);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.expendable_list, null);




        // get the listview
        expListView = (ExpandableListView) layout.findViewById(R.id.lvExp);

        // preparing list data
//        prepareListData();

        listAdapter = new ExpandableListAdapter(this);

        // setting list adapter
        expListView.setAdapter(listAdapter);


        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setTitle(R.string.title_select_profession);
        builder.setIcon(R.drawable.ic_profession);
        builder.setPositiveButton(R.string.btn_done, null);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String items = "";
                for(int mGroupPosition =0; mGroupPosition < listAdapter.getGroupCount(); mGroupPosition++) {
                    items = items +  listAdapter.getItemAtPostion(mGroupPosition);

                }
                if (items.length() > 2) {
                    mProfessionView.setText(items.substring(0,items.length()-2));
                }
            }
        });
        builder.show();

    }

    private AlertDialog emailUpdateDialog(@Nullable String email) {
        final View view = LayoutInflater.from(this).inflate(R.layout.email_update_dialog,null);
        final EditText updateEditText = (EditText) view.findViewById(R.id.input_update_email);

        updateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String txt = updateEditText.getText().toString();
                if (txt.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(txt).matches()) {
                    updateEditText.setError(getString(R.string.error_enter_valid_email));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return new AlertDialog.Builder(this).setTitle(R.string.title_update_email).setView(view)
                .setPositiveButton(R.string.btn_update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String txt = updateEditText.getText().toString();
                        if (txt.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(txt).matches()) {
                            Toast.makeText(UpdateActivity.this, getText(R.string.error_enter_valid_email), Toast.LENGTH_SHORT).show();
                        }else {
                            emailUpdateRequest(txt);
                        }

                    }
                })
                .setNegativeButton(R.string.btn_cancel,null).setCancelable(false).create();
    }


    @Override
    protected void onPause() {
        super.onPause();
        RelativeLayout mainLayout;
        mainLayout = (RelativeLayout)findViewById(R.id.activity_update);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
    }

    public void onBack(View view) {
        onBackPressed();
    }

    public void onUpdate(View v) {
        if (!validate()) {
            Toast.makeText(this, "Check input field", Toast.LENGTH_SHORT).show();
            return;
        }

        profileUpdateRequest();
    }

    private void profileRequest() {
        Profile mProfile = new Profile(HomeActivity.mUserId);
        mProfile.setuToken(HomeActivity.mLoginToken);

        GetProfileRequest request = new GetProfileRequest(this,mProfile,this);
        request.executeRequest();

        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void profileUpdateRequest() {
        Profile mProfile = new Profile(HomeActivity.mUserId);
        mProfile.setuToken(HomeActivity.mLoginToken);

        if (whichUpdate == REQUEST_PERSONAL) {
            mProfile.setuName(mNameView.getText().toString().trim());
            mProfile.setuMobile(mNumberView.getText().toString().trim());
            mProfile.setuEmail(mEmailView.getText().toString().trim());
            mProfile.setuPrivacy(mNumberView.getTag().toString());
        }else if (whichUpdate == REQUEST_ABOUT){
            mProfile.setProfession(mProfessionView.getText().toString().trim());
            mProfile.setuSpeciality(mInfoView.getText().toString().trim());
            mProfile.setuNotes(mDetailView.getText().toString().trim());
        }else {
            mProfile.setuName(mNameView.getText().toString().trim());
            mProfile.setuMobile(mNumberView.getText().toString().trim());
            mProfile.setuEmail(mEmailView.getText().toString().trim());
            mProfile.setuPrivacy(mNumberView.getTag().toString());

            mProfile.setProfession(mProfessionView.getText().toString().trim());
            mProfile.setuSpeciality(mInfoView.getText().toString().trim());
            mProfile.setuNotes(mDetailView.getText().toString().trim());
        }

        UpdateProfileRequest request = new UpdateProfileRequest(this,mProfile,this);
        request.executeRequest();

        mProgressBar.setVisibility(View.VISIBLE);


    }

    private void emailUpdateRequest(@NonNull String newEmail) {
        UpdateEmailRequest updateEmailRequest = new UpdateEmailRequest(this,newEmail,HomeActivity.mLoginToken,this);
        updateEmailRequest.executeRequest();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setProfileData(Profile profile) {


        mNameView.setText(profile.getuName());
        mEmailView.setText(profile.getuEmail());


        if (profile.getuMobile() !=null && !profile.getuMobile().equals("null") && !profile.getuMobile().trim().isEmpty()) {
            mNumberView.setText(profile.getuMobile());
            mNumberView.setVisibility(View.VISIBLE);
        }



        if (profile.getProfession() !=null && !profile.getProfession().equals("null") && !profile.getProfession().trim().isEmpty()) {
            mProfessionView.setText(profile.getProfession());
        }


        if (profile.getuSpeciality() !=null && !profile.getuSpeciality().equals("null") && !profile.getuSpeciality().trim().isEmpty()) {
            mInfoView.setText(profile.getuSpeciality());
        }


        if (profile.getuNotes() !=null && !profile.getuNotes().equals("null") && !profile.getuNotes().trim().isEmpty()) {
            mDetailView.setText(profile.getuNotes());
        }




        if (profile.getuPrivacy().equals("0")) {
            mNumberView.setTag("0");
//            mNumberView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, R.drawable.ic_password_visible, 0);
            mNumberView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_phone, 0, R.drawable.ic_password_visible, 0);

            numberVisibility = true;
        }else {
            mNumberView.setTag("1");
//            mNumberView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, R.drawable.ic_password_hidden, 0);
            mNumberView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_phone,0,R.drawable.ic_password_hidden,0);
            numberVisibility = false;
        }
    }

    @Override
    public void onProfileResponse(CommonRequest.ResponseCode responseCode, Profile mProfile) {
        mProgressBar.setVisibility(View.GONE);
        switch (responseCode) {
            case COMMON_RES_SUCCESS:
                setProfileData(mProfile);
                break;
        }
    }

    @Override
    public void onUpdateProfileResponse(CommonRequest.ResponseCode responseCode) {
        mProgressBar.setVisibility(View.GONE);
        if (responseCode == CommonRequest.ResponseCode.COMMON_RES_SUCCESS) {
            Toast.makeText(this, getText(R.string.message_profile_update_successfully), Toast.LENGTH_SHORT).show();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result",true);
            setResult(10,returnIntent);
            finish();
        }
    }


    public boolean phone_val(String ph_number) {
        return android.util.Patterns.PHONE.matcher(ph_number).matches();
    }

    private boolean isValidMobile(String phone2) {
        boolean check=false;
        if(!Pattern.matches("[a-zA-Z]+", phone2))
        {
            if(phone2.length() < 13 || phone2.length() > 13)
            {
                check = false;

            }
            else
            {
                check = true;
            }
        }
        else
        {
            check=false;
        }
        return check;
    }

    public boolean validate() {
        boolean valid = true;
        boolean valid_num = true;

        String name = mNameView.getText().toString();
        String number = mNumberView.getText().toString();
        String email = mEmailView.getText().toString();
        String profession = mProfessionView.getText().toString();
        String brifIntro = mInfoView.getText().toString();
        String detail = mDetailView.getText().toString();

        number = "+91"+number;

        if (whichUpdate == REQUEST_PERSONAL || whichUpdate == REQUEST_ALL) {
            if (name.isEmpty() || name.length() < 3) {
                mNameView.setError(getString(R.string.error_enter_valid_name));
                valid = false;
                return valid;
            } else {
                mNameView.setError(null);
            }


            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                // phone must begin with '+'
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number, "");
                mNumberView.setError(null);
            } catch (NumberParseException e) {
                mNumberView.setError("Enter a valid Mobile re Exception");

                valid = false;
                System.err.println("NumberParseException was thrown: " + e.toString());
            }

            valid_num = phone_val(number);

            if (valid_num) {
                mNumberView.setError(null);
            }
            else {
                mNumberView.setError(getString(R.string.error_enter_valid_mobile));
                valid =false;
            }
            valid_num = isValidMobile(number);
            if (valid_num) {
                mNumberView.setError(null);
            }
            else {
                mNumberView.setError(getString(R.string.error_enter_valid_mobile));
                valid =false;
            }



            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmailView.setError(getString(R.string.error_enter_valid_email));
                valid = false;
                return valid;
            } else {
                mEmailView.setError(null);
            }

            if (whichUpdate == REQUEST_ALL) {
                if (profession.isEmpty()) {
                    mProfessionView.setError(getString(R.string.error_select_profession));
                    valid =  false;
                    return valid;
                }else {
                    mProfessionView.setError(null);
                }

                if (brifIntro.isEmpty() || brifIntro.length() < 1) {
                    mInfoView.setError(getString(R.string.error_field_required));
                    valid = false;
                    return valid;
                } else {
                    mInfoView.setError(null);
                }
            }
        }else {

            if (profession.isEmpty()) {
                mProfessionView.setError(getString(R.string.error_select_profession));
                valid =  false;
                return valid;
            }else {
                mProfessionView.setError(null);
            }

            if (brifIntro.isEmpty() || brifIntro.length() < 1) {
                mInfoView.setError(getString(R.string.error_field_required));
                valid = false;
                return valid;
            } else {
                mInfoView.setError(null);
            }
        }




        return valid;
    }

    @Override
    public void UpdateEmailResponse(CommonRequest.ResponseCode responseCode, String statusCode) {
        mProgressBar.setVisibility(View.GONE);
        if (responseCode == CommonRequest.ResponseCode.COMMON_RES_SUCCESS) {
            Toast.makeText(this, getText(R.string.message_email_update_success), Toast.LENGTH_SHORT).show();
            profileRequest();
        }else if (responseCode == CommonRequest.ResponseCode.COMMON_RES_SERVER_ERROR_WITH_MESSAGE) {
            if (statusCode != null && statusCode.equals("0")) {
                Toast.makeText(this,getText(R.string.error_email_already_exist), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getText(R.string.error_something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, getText(R.string.error_something_went_wrong), Toast.LENGTH_SHORT).show();
        }

        profileRequest();
    }
}

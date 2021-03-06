package com.localapp.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.localapp.models.SignUpData;
import com.localapp.network.helper.CommonFileUpload;
import com.localapp.network.helper.CommonRequest;
import com.localapp.network.helper.VolleyErrorHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vijay Kumar on 27-02-2017.
 */

public class SignUpRequest {

    public static final String JSON_FIELD_NAME = "name";
    public static final String JSON_FIELD_PASSWORD = "password";
    public static final String JSON_FIELD_EMAIL_ID = "email";
    public static final String JSON_FIELD_PROFESSION = "profession";
    public static final String JSON_FIELD_MOBILE_NUMBER = "mobile";
    public static final String JSON_FIELD_MOBILE_PRIVACY = "mobilePrivacy";
    public static final String JSON_FIELD_SPECIALITY = "speciality";
    public static final String JSON_FIELD_NOTES = "notes";
    public static final String JSON_FIELD_FILE = "file";
    public static final String JSON_FIELD_M_PLACE = "place";
    public static final String JSON_FIELD_M_LOCATION = "location";
    public static final String JSON_FIELD_TYPE = "type";


    private Context mContext;

    private Map<String, String> mParams;
    private SignUpData mSignUpData;

    private CommonFileUpload mFileUpload;

    public interface SignUpResponseCallback {
        void onSignUpResponse(CommonRequest.ResponseCode res, SignUpData data);
    }
    private SignUpResponseCallback mSignUpResponseCallback;

    public SignUpRequest(Context context, SignUpData data, SignUpResponseCallback cb) {

        mContext = context;
        mSignUpData = data;
        mParams = new HashMap<>();
        /*mParams.put("Content-Type", "multipart/form-data");*/
        mParams.put(JSON_FIELD_NAME, data.getmName());
        mParams.put(JSON_FIELD_EMAIL_ID, data.getmEmail());
        mParams.put(JSON_FIELD_MOBILE_NUMBER, data.getmMobile());
        mParams.put(JSON_FIELD_PASSWORD, data.getmPassword());
        mParams.put(JSON_FIELD_SPECIALITY, data.getmSpeciality());
        mParams.put(JSON_FIELD_NOTES, data.getmDetails());
        mParams.put(JSON_FIELD_PROFESSION, data.getProfession());
        mParams.put(JSON_FIELD_MOBILE_PRIVACY, data.getmPrivacy());
//        mParams.put(JSON_FIELD_TYPE, data.getmType());

        mSignUpResponseCallback = cb;
    }


    public void executeRequest() {
//        final String DOMAIN = "http://192.172.2.178:8080";//localhost
        final String url = CommonRequest.DOMAIN + "/registerPic";//url for registration with pic

        Response.Listener<NetworkResponse> listener = new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String jsonStr = new String(response.data);

                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(jsonStr);
                    JSONObject dataObject = new JSONObject(jsonObject.getString("data"));
                    String token = dataObject.getString("token");
                    String picUrl = dataObject.getString("picUrl");
                    String userID = dataObject.getString("UserId");
                    mSignUpData.setPicUrl(picUrl);
                    mSignUpData.setmToken(token);
                    mSignUpData.setmUserId(userID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSignUpResponseCallback.onSignUpResponse(CommonRequest.ResponseCode.COMMON_RES_SUCCESS, mSignUpData);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMsg = VolleyErrorHelper.getMessage(error, mContext);
                Log.v("onErrorHandler","error is" + error);
                CommonRequest.ResponseCode resCode = CommonRequest.ResponseCode.COMMON_RES_INTERNAL_ERROR;
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    resCode = CommonRequest.ResponseCode.COMMON_RES_CONNECTION_TIMEOUT;
                    mSignUpResponseCallback.onSignUpResponse(resCode,mSignUpData);
                    return;
                }


                if (errorMsg == VolleyErrorHelper.COMMON_NETWORK_ERROR_TIMEOUT)
                {
                    resCode = CommonRequest.ResponseCode.COMMON_RES_CONNECTION_TIMEOUT;
                }
                else if (errorMsg == VolleyErrorHelper.COMMON_NETWORK_ERROR_UNKNOWN){
                    resCode = CommonRequest.ResponseCode.COMMON_RES_INTERNAL_ERROR;
                }
                else if (errorMsg == VolleyErrorHelper.COMMON_NETWORK_ERROR_NO_INTERNET){
                    resCode = CommonRequest.ResponseCode.COMMON_RES_FAILED_TO_CONNECT;
                }else
                {
                    resCode = CommonRequest.ResponseCode.COMMON_RES_SERVER_ERROR_WITH_MESSAGE;
                    mSignUpData.setmErrorMessage(errorMsg);
                }

                mSignUpResponseCallback.onSignUpResponse (resCode, mSignUpData);
            }
        };

        mFileUpload = new CommonFileUpload(mContext, mSignUpData.getPicFile(), CommonFileUpload.FileType.COMMON_UPLOAD_FILE_TYPE_IMAGE,
                mSignUpData.getmEmail()+System.currentTimeMillis(),url,null,listener,errorListener);

        mFileUpload.setRetryPolicy(60000,0);

        mFileUpload.setParam(mParams);
        mFileUpload.uploadFile();
    }
}

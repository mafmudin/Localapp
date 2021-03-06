package com.localapp.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.localapp.models.FbLoginError;
import com.localapp.models.LoginData;
import com.localapp.network.helper.CommonRequest;
import com.localapp.network.helper.VolleyErrorHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vijay Kumar on 04-05-2017.
 */

public class FbLoginRequest extends CommonRequest {
    private static final String JSON_FIELD_FB_ID = "fbId";
    private static final String JSON_FIELD_FB_TOKEN = "fbToken";

    private LoginData mLoginData;
    private Map<String, String> mParams;
    private Context mContext;
    private FbLoginResponseCallback mFbLoginResponseCallback;



    public interface FbLoginResponseCallback {
        void onFbLoginResponse(ResponseCode responseCode, LoginData data);
    }

    public FbLoginRequest(Context context, LoginData data, FbLoginResponseCallback cb) {
        super(context, RequestType.COMMON_REQUEST_FB_LOGIN, CommonRequestMethod.COMMON_REQUEST_METHOD_POST, null);
        mContext = context;
        mLoginData = data;

        mParams = new HashMap<>();
        mParams.put(JSON_FIELD_FB_ID, mLoginData.getFbLoginResult().getAccessToken().getUserId());
        mParams.put(JSON_FIELD_FB_TOKEN, mLoginData.getFbLoginResult().getAccessToken().getToken());
        super.setParams(mParams);

        mFbLoginResponseCallback = cb;

    }

    @Override
    public void onResponseHandler(JSONObject response) {
//TODO: Need to change parsing as per response from server
        try {
            JSONObject jsonObject = response.getJSONObject("data");
            mLoginData.setUserId(jsonObject.getString("id"));
            mLoginData.setAccessToken(jsonObject.getString("token"));
            mLoginData.setmName(jsonObject.getString("name"));
            mLoginData.setmMobile(jsonObject.getString("mobile"));
            mLoginData.setmSpeciality(jsonObject.getString("speciality"));
            mLoginData.setPicUrl(jsonObject.getString("picUrl"));
            mLoginData.setmProfession(jsonObject.getString("profession"));
            mLoginData.setmNotes(jsonObject.getString("notes"));

            mFbLoginResponseCallback.onFbLoginResponse(ResponseCode.COMMON_RES_SUCCESS, mLoginData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorHandler(VolleyError error) {
        String errorMsg = VolleyErrorHelper.getMessage(error, mContext);
        Log.v("onErrorHandler","error is" + error);

        CommonRequest.ResponseCode resCode;

        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
            resCode = ResponseCode.COMMON_RES_CONNECTION_TIMEOUT;
            mFbLoginResponseCallback.onFbLoginResponse(resCode, mLoginData);
            return;
        }else if (error.networkResponse != null && error.networkResponse.statusCode == 422) {

            NetworkResponse response = error.networkResponse;
            String errorResponse = new String(response.data);

            try {
                JSONObject obj = new JSONObject(errorResponse);
                JSONObject errorObject = obj.getJSONObject("error");
                mLoginData.setFbLoginError(new FbLoginError(errorObject.getInt("status"),errorObject.getString("message")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            resCode = ResponseCode.COMMON_RES_SERVER_ERROR_WITH_MESSAGE;
            mFbLoginResponseCallback.onFbLoginResponse(resCode, mLoginData);
            return;

        }
        if (errorMsg == VolleyErrorHelper.COMMON_NETWORK_ERROR_TIMEOUT)
        {
            resCode = ResponseCode.COMMON_RES_CONNECTION_TIMEOUT;
        }
        else if (errorMsg == VolleyErrorHelper.COMMON_NETWORK_ERROR_UNKNOWN){
            resCode = ResponseCode.COMMON_RES_INTERNAL_ERROR;
        }
        else if (errorMsg == VolleyErrorHelper.COMMON_NETWORK_ERROR_NO_INTERNET){
            resCode = ResponseCode.COMMON_RES_FAILED_TO_CONNECT;
        }else
        {
            resCode = ResponseCode.COMMON_RES_SERVER_ERROR_WITH_MESSAGE;
            mLoginData.setErrorMessage(errorMsg);
        }

        mFbLoginResponseCallback.onFbLoginResponse(resCode, mLoginData);
    }
}

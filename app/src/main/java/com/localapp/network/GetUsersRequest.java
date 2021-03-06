package com.localapp.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.localapp.models.GetUsersRequestData;
import com.localapp.models.Profile;
import com.localapp.network.helper.CommonRequest;
import com.localapp.network.helper.VolleyErrorHelper;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.localapp.appcontroller.AppController.getAppContext;

/**
 * Created by Vijay Kumar on 20-02-2017.
 */

public class GetUsersRequest extends CommonRequest {


    public interface GetUsersResponseCallback {
        void onGetUsersResponse(CommonRequest.ResponseCode res, GetUsersRequestData data);
    }

    private GetUsersResponseCallback mGetUsersResponseCallback;
    private GetUsersRequestData mRequestData;
    private Context mContext;
    private Map<String, String> mParams;
    private Map<String, String> mHeaders;
    private String mToken;

    public GetUsersRequest(Context context, LatLng latLng, String mToken, GetUsersResponseCallback cb) {
        super(context, RequestType.COMMON_REQUEST_MAP, CommonRequestMethod.COMMON_REQUEST_METHOD_POST, null);
        mContext = context;

        mRequestData = new GetUsersRequestData();

        this.mToken = mToken;

        mParams = new HashMap<>();
        mParams.put("latitude", String.valueOf(latLng.latitude));
        mParams.put("longitude", String.valueOf(latLng.longitude));
        super.setParams(mParams);

        mHeaders = new HashMap<>();
        if (mToken != null &&!mToken.equals("")) {
            mHeaders.put("token", mToken);
        }
        super.setPostHeader(mHeaders);

        mGetUsersResponseCallback = cb;


    }


    @Override
    public void onResponseHandler(JSONObject response)  {
        JSONArray profileList = null;
        try {
            profileList = response.getJSONArray("data");
            int size = profileList.length();
            for (int i = 0; i < size; i++) {
                JSONObject profile = profileList.getJSONObject(i);
                Picasso.with(getAppContext()).load(profile.getString("picUrl"));
                String uId = profile.getString("id");
                String uName =  profile.getString("name");
                String uEmail =  profile.getString("email");
                String uPictureURL =  profile.getString("picUrl");
                String uToken =  profile.getString("token");
                String uMobile =  profile.getString("mobile");
                String uSpeciality =  profile.getString("speciality");
                String uNotes =  profile.getString("notes");
                String uProfession = profile.getString("profession");
                String mPrivacy = null;
                try {
                    mPrivacy = profile.getString("mobilePrivacy");
                }catch (JSONException e) {
                    e.printStackTrace();
                }


                LatLng latLng = null;
                try {
                    JSONArray lngJsonArray = profile.getJSONArray("longlat");
                    if (lngJsonArray.length()>0) {
                        latLng = new LatLng(Double.parseDouble(lngJsonArray.getString(0)), Double.parseDouble(lngJsonArray.getString(1)));
                    }
                }catch (JSONException e){
                    Log.v("Request",e.getMessage());
                }


                Profile mProfile = new Profile(uId);

                mProfile.setuEmail(uEmail);
                mProfile.setuName(uName);
                mProfile.setuPictureURL(uPictureURL);
                mProfile.setuToken(uToken);
                mProfile.setuMobile(uMobile);
                mProfile.setuSpeciality(uSpeciality);
                mProfile.setuNotes(uNotes);
                mProfile.setuLatLng(latLng);
                mProfile.setProfession(uProfession);
                mProfile.setuPrivacy(mPrivacy);


//                if (mProfile.getuPictureURL() != "null") {
                    mRequestData.addProfile(mProfile);
//                }


            }
            mGetUsersResponseCallback.onGetUsersResponse(ResponseCode.COMMON_RES_SUCCESS, mRequestData);
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
            mGetUsersResponseCallback.onGetUsersResponse (resCode, mRequestData);
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
            mRequestData.setmErrorMessage(errorMsg);
        }

        mGetUsersResponseCallback.onGetUsersResponse (resCode, mRequestData);
        
    }


}

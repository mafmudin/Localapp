package com.fourway.localapp.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fourway.localapp.request.helper.CommonFileUpload;
import com.fourway.localapp.request.helper.VolleyErrorHelper;
import com.fourway.localapp.ui.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.fourway.localapp.request.CommonRequest.ResponseCode.COMMON_RES_CONNECTION_TIMEOUT;
import static com.fourway.localapp.request.CommonRequest.ResponseCode.COMMON_RES_FAILED_TO_CONNECT;
import static com.fourway.localapp.request.CommonRequest.ResponseCode.COMMON_RES_INTERNAL_ERROR;
import static com.fourway.localapp.request.CommonRequest.ResponseCode.COMMON_RES_SERVER_ERROR_WITH_MESSAGE;

/**
 * Created by 4 way on 30-03-2017.
 */

public class PicUrlRequest {
    public interface PicUrlResponseCallback {
        void PicUrlResponse(CommonRequest.ResponseCode responseCode, String picUrl);
    }

    Context mContext;
    private File imageFile;
    private CommonFileUpload mFileUpload;

    private PicUrlResponseCallback mPicUrlResponseCallback;

    public PicUrlRequest(Context mContext, File imageFile, PicUrlResponseCallback mPicUrlResponseCallback) {
        this.mContext = mContext;
        this.imageFile = imageFile;
        this.mPicUrlResponseCallback = mPicUrlResponseCallback;
    }

    public void executeRequest() {
        final String url = "http://ec2-52-53-110-212.us-west-1.compute.amazonaws.com:8080/message/uploadPic";

        Response.Listener<NetworkResponse> listener = new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String jsonStr = new String(response.data);
                String picUrl = null;
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(jsonStr);
                    picUrl = jsonObject.getString("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mPicUrlResponseCallback.PicUrlResponse(CommonRequest.ResponseCode.COMMON_RES_SUCCESS, picUrl);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMsg = VolleyErrorHelper.getMessage(error, mContext);
                Log.v("onErrorHandler","error is" + error);
                CommonRequest.ResponseCode resCode = COMMON_RES_INTERNAL_ERROR;
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    resCode = COMMON_RES_CONNECTION_TIMEOUT;
                    mPicUrlResponseCallback.PicUrlResponse(resCode,null);
                    return;
                }
                if (errorMsg == VolleyErrorHelper.COMMON_NETWORK_ERROR_TIMEOUT)
                {
                    resCode = COMMON_RES_CONNECTION_TIMEOUT;
                }
                else if (errorMsg == VolleyErrorHelper.COMMON_NETWORK_ERROR_UNKNOWN){
                    resCode = COMMON_RES_INTERNAL_ERROR;
                }
                else if (errorMsg == VolleyErrorHelper.COMMON_NETWORK_ERROR_NO_INTERNET){
                    resCode = COMMON_RES_FAILED_TO_CONNECT;
                }else
                {
                    resCode = COMMON_RES_SERVER_ERROR_WITH_MESSAGE;
//                    mSignUpData.setmErrorMessage(errorMsg);
                }

                mPicUrlResponseCallback.PicUrlResponse (resCode, null);
            }
        };

        mFileUpload = new CommonFileUpload(mContext, imageFile, CommonFileUpload.FileType.COMMON_UPLOAD_FILE_TYPE_IMAGE,
                String.valueOf(System.currentTimeMillis()/1000)+ HomeActivity.mUserId ,url,null,listener,errorListener);

        mFileUpload.uploadFile();
    }
}

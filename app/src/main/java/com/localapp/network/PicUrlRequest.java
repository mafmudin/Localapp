package com.localapp.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.localapp.network.helper.CommonFileUpload;
import com.localapp.network.helper.CommonRequest;
import com.localapp.network.helper.VolleyErrorHelper;
import com.localapp.ui.fragments.FeedFragment;
import com.localapp.ui.activities.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Vijay Kumar on 30-03-2017.
 */

public class PicUrlRequest {
    public interface PicUrlResponseCallback {
        void PicUrlResponse(CommonRequest.ResponseCode responseCode, String picUrl, FeedFragment.MediaType mediaType);
    }

    Context mContext;
    private File imageFile;
    private CommonFileUpload mFileUpload;
    private FeedFragment.MediaType mMediaType;

    private PicUrlResponseCallback mPicUrlResponseCallback;

    public PicUrlRequest(Context mContext, File imageFile, FeedFragment.MediaType mMediaType,PicUrlResponseCallback mPicUrlResponseCallback) {
        this.mContext = mContext;
        this.imageFile = imageFile;
        this.mPicUrlResponseCallback = mPicUrlResponseCallback;
        this.mMediaType = mMediaType;
    }

    public void executeRequest() {

        final String url = CommonRequest.DOMAIN + "/message/uploadPic";

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
                mPicUrlResponseCallback.PicUrlResponse(CommonRequest.ResponseCode.COMMON_RES_SUCCESS, picUrl,mMediaType);
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
                    mPicUrlResponseCallback.PicUrlResponse(resCode,null,mMediaType);
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
//                    mSignUpData.setmErrorMessage(errorMsg);
                }

                mPicUrlResponseCallback.PicUrlResponse (resCode, null ,mMediaType);
            }
        };

        if (mMediaType == FeedFragment.MediaType.MEDIA_IMAGE) {
            mFileUpload = new CommonFileUpload(mContext, imageFile, CommonFileUpload.FileType.COMMON_UPLOAD_FILE_TYPE_IMAGE,
                    String.valueOf(System.currentTimeMillis() / 1000) + HomeActivity.mUserId, url, null, listener, errorListener);
        }else if (mMediaType == FeedFragment.MediaType.MEDIA_VIDEO) {
            mFileUpload = new CommonFileUpload(mContext, imageFile, CommonFileUpload.FileType.COMMON_UPLOAD_FILE_TYPE_VIDEO,
                    String.valueOf(System.currentTimeMillis() / 1000) + HomeActivity.mUserId, url, null, listener, errorListener);
        }else {
            mFileUpload = new CommonFileUpload(mContext, imageFile, CommonFileUpload.FileType.COMMON_UPLOAD_FILE_TYPE_AUDIO,
                    HomeActivity.mUserId + String.valueOf(System.currentTimeMillis() / 1000), url, null, listener, errorListener);
        }

        mFileUpload.uploadFile();
    }
}

package com.fourway.localapp.request;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fourway.localapp.data.BroadcastRequestData;
import com.fourway.localapp.data.Message;
import com.fourway.localapp.request.helper.VolleyErrorHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.fourway.localapp.request.CommonRequest.ResponseCode.COMMON_RES_CONNECTION_TIMEOUT;
import static com.fourway.localapp.request.CommonRequest.ResponseCode.COMMON_RES_FAILED_TO_CONNECT;
import static com.fourway.localapp.request.CommonRequest.ResponseCode.COMMON_RES_INTERNAL_ERROR;
import static com.fourway.localapp.request.CommonRequest.ResponseCode.COMMON_RES_SERVER_ERROR_WITH_MESSAGE;

/**
 * Created by 4 way on 21-02-2017.
 */

public class BroadcastRequest {

    public interface BroadcastResponseCallback {
        void onBroadcastResponce(CommonRequest.ResponseCode res);
    }

    private BroadcastResponseCallback broadcastResponseCallback;
    private Message mRequestData;
    private Context mContext;

    public BroadcastRequest(Context context, Message data, BroadcastResponseCallback cb) {

        mContext = context;
        mRequestData = data;
        broadcastResponseCallback = cb;
    }


    public void executeRequest () {
        String url = "http://52.172.157.120:8080/broadcast";

        JSONObject js = new JSONObject();
        try {
            js.put("text",mRequestData.getmText());
            js.put("mediaUrl",mRequestData.getMediaURL());
            js.put("emoji",mRequestData.getEmoji());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                broadcastResponseCallback.onBroadcastResponce(CommonRequest.ResponseCode.COMMON_RES_SUCCESS);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorHandler(error);
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, js, listener, errorListener) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("token",mRequestData.getToken());//hardcoded token for test
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }


    public void onErrorHandler(VolleyError error) {
        String errorMsg = VolleyErrorHelper.getMessage(error, mContext);
        Log.v("onErrorHandler","error is" + error);
        CommonRequest.ResponseCode resCode = COMMON_RES_INTERNAL_ERROR;
        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
            resCode = COMMON_RES_CONNECTION_TIMEOUT;
            broadcastResponseCallback.onBroadcastResponce(resCode);
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
        }
        else
        {
            resCode = COMMON_RES_SERVER_ERROR_WITH_MESSAGE;
//            mProfile.setErrorMessage(errorMsg);
        }
//        mSaveProfileResponseCallback.onProfileSaveResponse (resCode, mProfile);

    }


}

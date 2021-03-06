package com.localapp.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.localapp.models.NoticeBoard;
import com.localapp.network.helper.CommonRequest;
import com.localapp.network.helper.VolleyErrorHelper;
import com.localapp.ui.activities.HomeActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vijay Kumar on 08-04-2017.
 */

public class GetNearestNoticeBoardRequest extends CommonRequest {
    private Context mContext;
    private List<NoticeBoard> mNoticeBoardList;
    private GetNearestNoticeBoardRequestCallback mRequestCallback;
    private LatLng mLatLng;


    public interface GetNearestNoticeBoardRequestCallback {
        void GetNearestNoticeBoardResponse(CommonRequest.ResponseCode responseCode, List<NoticeBoard> mNoticeBoards);
    }

    public GetNearestNoticeBoardRequest(Context mContext, GetNearestNoticeBoardRequestCallback mRequestCallback, LatLng mLatLng) {
        super(mContext, RequestType.COMMON_REQUEST_NEAREST_NOTICE_BOARD, CommonRequestMethod.COMMON_REQUEST_METHOD_GET, null);
        this.mContext = mContext;
        this.mRequestCallback = mRequestCallback;
        this.mLatLng = mLatLng;

        mNoticeBoardList = new ArrayList<>();

        String url = getRequestTypeURL(RequestType.COMMON_REQUEST_NEAREST_NOTICE_BOARD);
        url += "latitude="+mLatLng.latitude;
        url += "&longitude="+mLatLng.longitude;
        url += "&radius=3";
        super.setURL(url);
    }

    @Override
    public void onResponseHandler(JSONObject response) {

        try {
            JSONArray mJsonArray = response.getJSONArray("data");
            int size = mJsonArray.length();
            for (int i = 0; i<size ;i ++) {
                JSONObject jsonObject = mJsonArray.getJSONObject(i);

                String noticeBoardId = jsonObject.getString("id");
                String noticeBoardName = jsonObject.getString("name");
                String noticeBoardAdminId = jsonObject.getString("adminId");

                LatLng latLng = null;
                try {
                    JSONArray lngJsonArray = jsonObject.getJSONArray("longlat");
                    if (lngJsonArray.length()>0) {
                        latLng = new LatLng(Double.parseDouble(lngJsonArray.getString(0)), Double.parseDouble(lngJsonArray.getString(1)));
                    }
                }catch (JSONException e){
                    Log.v("Request",e.getMessage());
                }

                NoticeBoard mNoticeBoard = new NoticeBoard(noticeBoardAdminId,noticeBoardName);
                mNoticeBoard.setId(noticeBoardId);
                mNoticeBoard.setLocation(latLng);

                if (HomeActivity.mUserId == null || !HomeActivity.mUserId.equals(mNoticeBoard.getAdminId())) {
                    mNoticeBoardList.add(mNoticeBoard);
                }
            }

            mRequestCallback.GetNearestNoticeBoardResponse(ResponseCode.COMMON_RES_SUCCESS,mNoticeBoardList);

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
            mRequestCallback.GetNearestNoticeBoardResponse(resCode,mNoticeBoardList);
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
//            mRequestData.setmErrorMessage(errorMsg);
        }

        mRequestCallback.GetNearestNoticeBoardResponse(resCode,mNoticeBoardList);
    }
}

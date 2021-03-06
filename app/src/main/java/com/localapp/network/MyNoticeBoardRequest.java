package com.localapp.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.localapp.models.NoticeBoard;
import com.localapp.models.NoticeBoardMessage;
import com.localapp.network.helper.CommonRequest;
import com.localapp.network.helper.VolleyErrorHelper;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vijay Kumar on 31-03-2017.
 */

public class MyNoticeBoardRequest extends CommonRequest {
    private Context mContext;
    private List<NoticeBoard> myNoticeBoardList;
    private List<NoticeBoard> subscribedNoticeBoardList;
    private MyNoticeBoardRequestCallback myNoticeBoardRequestCallback;


    public MyNoticeBoardRequest(Context mContext, String userID, MyNoticeBoardRequestCallback cb) {
        super(mContext, RequestType.COMMON_REQUEST_MY_NOTICE_BOARD, CommonRequestMethod.COMMON_REQUEST_METHOD_GET, null);
        this.mContext = mContext;
        this.myNoticeBoardList = new ArrayList<>();
        this.subscribedNoticeBoardList = new ArrayList<>();

        String url = getRequestTypeURL(RequestType.COMMON_REQUEST_MY_NOTICE_BOARD);
        url += "/"+userID;
        super.setURL(url);

        myNoticeBoardRequestCallback = cb;

    }

    @Override
    public void onResponseHandler(JSONObject response) {
        try {
            JSONObject mJsonArray = response.getJSONObject("data");
            JSONArray createdNoticeBoardArray = mJsonArray.getJSONArray("CreatedNoticeBoard");
            JSONArray subcribedNoticeBoardArray = null;
            try {
                subcribedNoticeBoardArray = mJsonArray.getJSONArray("SuscribedNoticeBoard");
            }catch (JSONException e) {
                e.printStackTrace();
            }

            int size = createdNoticeBoardArray.length();

            for (int i = 0;i<size; i++) {

                try {
                    JSONObject noticeJsonObject = createdNoticeBoardArray.getJSONObject(i);
                    JSONObject lastMsgObject = null;

                    try{
                        lastMsgObject = noticeJsonObject.getJSONObject("lastMessage");
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String noticeBoardId = noticeJsonObject.getString("id");
                    String noticeBoardName = noticeJsonObject.getString("name");
                    String noticeBoardAdminId = noticeJsonObject.getString("adminId");
                    JSONArray latlngJsonArray = null;
                    LatLng latLng = null;
                    try {
                        latlngJsonArray = new JSONArray(noticeJsonObject.getString("longlat"));
                        latLng = new LatLng(Double.valueOf(latlngJsonArray.getString(0)), Double.valueOf(latlngJsonArray.getString(1)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String msgID = null;
                    String msgText = null;
                    String timeStamp = null;




                    NoticeBoard mNoticeBoard = new NoticeBoard(noticeBoardAdminId,noticeBoardName);
                    List<NoticeBoardMessage> message = new ArrayList<>();


                    mNoticeBoard.setId(noticeBoardId);
                    mNoticeBoard.setLocation(latLng);

                    if (lastMsgObject != null) {
                        msgID = lastMsgObject.getString("id");
                        msgText = lastMsgObject.getString("text");
                        timeStamp = lastMsgObject.getString("timestamp");
                        message.add(new NoticeBoardMessage(msgID,msgText,timeStamp));
                    }

                    mNoticeBoard.setMessagesList(message);


                    myNoticeBoardList.add(mNoticeBoard);



                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            if (subcribedNoticeBoardArray != null) {
                int sNoticeSize = subcribedNoticeBoardArray.length();
                for (int i =0; i<sNoticeSize; i++) {


                    JSONObject noticeJsonObject = subcribedNoticeBoardArray.getJSONObject(i);
                    JSONObject lastMsgObject = null;
                    try {
                        lastMsgObject = noticeJsonObject.getJSONObject("lastMessage");
                    }catch (JSONException e){
                        e.printStackTrace();
                    }


                    String noticeBoardId = noticeJsonObject.getString("id");
                    String noticeBoardName = noticeJsonObject.getString("name");
                    String noticeBoardAdminId = noticeJsonObject.getString("adminId");
                    JSONArray latlngJsonArray = null;
                    LatLng latLng = null;
                    try {
                        latlngJsonArray = new JSONArray(noticeJsonObject.getString("longlat"));
                        latLng = new LatLng(Double.valueOf(latlngJsonArray.getString(0)), Double.valueOf(latlngJsonArray.getString(1)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String msgID = null;
                    String msgText = null;
                    String timeStamp = null;
                    if (lastMsgObject != null) {
                        msgID = lastMsgObject.getString("id");
                        msgText = lastMsgObject.getString("text");
                        timeStamp = lastMsgObject.getString("timestamp");
                    }


                    NoticeBoard mNoticeBoard = new NoticeBoard(noticeBoardAdminId,noticeBoardName);
                    List<NoticeBoardMessage> message = new ArrayList<>();
                    message.add(new NoticeBoardMessage(msgID,msgText,timeStamp));

                    mNoticeBoard.setId(noticeBoardId);
                    mNoticeBoard.setLocation(latLng);
                    mNoticeBoard.setMessagesList(message);


                    subscribedNoticeBoardList.add(mNoticeBoard);

                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        myNoticeBoardRequestCallback.MyNoticeBoardResponse(ResponseCode.COMMON_RES_SUCCESS,myNoticeBoardList,subscribedNoticeBoardList);

    }

    @Override
    public void onErrorHandler(VolleyError error) {
        String errorMsg = VolleyErrorHelper.getMessage(error, mContext);
        Log.v("onErrorHandler","error is" + error);

        CommonRequest.ResponseCode resCode;

        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
            resCode = ResponseCode.COMMON_RES_CONNECTION_TIMEOUT;
            myNoticeBoardRequestCallback.MyNoticeBoardResponse (resCode, myNoticeBoardList, subscribedNoticeBoardList);
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

       myNoticeBoardRequestCallback.MyNoticeBoardResponse(resCode,myNoticeBoardList,subscribedNoticeBoardList);
    }

    public interface MyNoticeBoardRequestCallback {
        void MyNoticeBoardResponse(CommonRequest.ResponseCode responseCode, List<NoticeBoard> myNoticeBoards, List<NoticeBoard> subscribedNoticeBoardList);
    }


}

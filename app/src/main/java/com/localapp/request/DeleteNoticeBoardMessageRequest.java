package com.localapp.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.localapp.data.NoticeBoardMessage;
import com.localapp.request.helper.VolleyErrorHelper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.localapp.request.CommonRequest.ResponseCode.COMMON_RES_CONNECTION_TIMEOUT;
import static com.localapp.request.CommonRequest.ResponseCode.COMMON_RES_FAILED_TO_CONNECT;
import static com.localapp.request.CommonRequest.ResponseCode.COMMON_RES_INTERNAL_ERROR;
import static com.localapp.request.CommonRequest.ResponseCode.COMMON_RES_SERVER_ERROR_WITH_MESSAGE;

/**
 * Created by 4 way on 08-04-2017.
 */

public class DeleteNoticeBoardMessageRequest extends CommonRequest{
    private Context mContext;
    private NoticeBoardMessage mNoticeBoardMessage;
    private Map<String,String> mParams;
    private DeleteNoticeBoardMessageResponseCallback deleteNoticeBoardMessageResponseCallback;

    public DeleteNoticeBoardMessageRequest(Context mContext, NoticeBoardMessage mNoticeBoardMessage,String uToken, DeleteNoticeBoardMessageResponseCallback cb) {
        super(mContext, RequestType.COMMON_REQUEST_DELETE_NOTICE_BOARD_MSG, CommonRequestMethod.COMMON_REQUEST_METHOD_POST, null);

        this.mContext = mContext;
        this.mNoticeBoardMessage = mNoticeBoardMessage;
        this.deleteNoticeBoardMessageResponseCallback = cb;

        mParams = new HashMap<>();
        mParams.put("messageId",mNoticeBoardMessage.getId());
        mParams.put("token",uToken);

        super.setParams(mParams);


    }

    @Override
    public void onResponseHandler(JSONObject response) {
        deleteNoticeBoardMessageResponseCallback.deleteNoticeBoardMessageResponse(ResponseCode.COMMON_RES_SUCCESS);
    }

    @Override
    public void onErrorHandler(VolleyError error) {

        String errorMsg = VolleyErrorHelper.getMessage(error, mContext);
        Log.v("onErrorHandler","error is" + error);

        CommonRequest.ResponseCode resCode;

        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
            resCode = COMMON_RES_CONNECTION_TIMEOUT;
            deleteNoticeBoardMessageResponseCallback.deleteNoticeBoardMessageResponse(resCode);
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
//            mRequestData.setmErrorMessage(errorMsg);
        }

        deleteNoticeBoardMessageResponseCallback.deleteNoticeBoardMessageResponse(resCode);
    }

    public interface DeleteNoticeBoardMessageResponseCallback {
        void deleteNoticeBoardMessageResponse(CommonRequest.ResponseCode responseCode);
    }


}

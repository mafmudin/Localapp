package com.localapp.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;
import com.localapp.R;
import com.localapp.appcontroller.AppController;
import com.localapp.audio.ViewProxy;
import com.localapp.background.ConnectivityReceiver;
import com.localapp.camera.Camera2Activity;
import com.localapp.compressor.Compressor;
import com.localapp.fcm.FcmNotificationRequest;
import com.localapp.models.GetFeedRequestData;
import com.localapp.models.Message;
import com.localapp.models.NotificationData;
import com.localapp.models.ReplyMessage;
import com.localapp.network.DeleteMessageRequest;
import com.localapp.network.EmergencyMsgAcceptRequest;
import com.localapp.network.GetFeedRequest;
import com.localapp.network.PicUrlRequest;
import com.localapp.network.helper.CommonRequest;
import com.localapp.preferences.AppPreferences;
import com.localapp.preferences.SessionManager;
import com.localapp.ui.activities.HomeActivity;
import com.localapp.ui.activities.VideoPlay;
import com.localapp.ui.adapters.EmergencyListAdapter;
import com.localapp.ui.adapters.EmojiGridAdapter;
import com.localapp.ui.adapters.ThreadAdapter;
import com.localapp.utils.AlertDialogHelper;
import com.localapp.utils.Constants;
import com.localapp.utils.NetworkUtil;
import com.localapp.utils.Utility;
import com.squareup.picasso.Picasso;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

import static com.localapp.network.helper.CommonRequest.ResponseCode.COMMON_RES_SUCCESS;
import static com.localapp.ui.adapters.ThreadAdapter.getEmojiResourceIdByMsgType;
import static com.localapp.ui.fragments.FeedFragment.MediaType.MEDIA_AUDIO;
import static com.localapp.ui.fragments.FeedFragment.MediaType.MEDIA_IMAGE;
import static com.localapp.ui.fragments.FeedFragment.MediaType.MEDIA_TEXT;
import static com.localapp.ui.fragments.FeedFragment.MediaType.MEDIA_VIDEO;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment implements GetFeedRequest.GetFeedRequestCallback,PicUrlRequest.PicUrlResponseCallback,
        EmergencyMsgAcceptRequest.EmergencyMsgAcceptResponseCallback, ConnectivityReceiver.ConnectivityReceiverListener,AlertDialogHelper.AlertDialogListener,DeleteMessageRequest.DeleteMessageResponseCallback {

    private final String TAG = FeedFragment.class.getSimpleName();

    private static final String sAddress = "tcp://13.56.50.98:1883";
//    private final String sAddress = "tcp://192.172.2.178:1883";//localhost
//    private final String sAddress = "tcp://192.172.3.23:2883";
    private static final String mTopic = "localapp";
    private static final String mTopicAcceptMsg = "accept";
//    private static final String mTopic = "vijay";

    private SessionManager sessionManager;
    final static String[] CAMERA_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
    final static String[] AUDIO_PERMISSIONS = {Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_AUDIO_CODE = 300;
    private static final int REQUEST_CAMERA_CODE = 201;

    private MQTT mqtt = null;
    private ProgressDialog progressDialog = null;
    private FutureConnection connection = null;
    private Map<String, String> mParams;

    AlertDialogHelper alertDialogHelper;

    //Recyclerview objects
    public static ActionMode mActionMode;
    Menu context_menu;


    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    boolean isMultiSelect = false;
    private ThreadAdapter adapter;
    private GridView emojiGridView;

    private ListView emergencyMessageListView;
    private EmergencyListAdapter emergencyListAdapter;
    private List<Message> emergencyMessageList;

    private View typeMessageAreaPreventClickView;


    //******************************** tool tips *******************//

    private RelativeLayout overlayRL,overlaySmsMode;
    private LinearLayout overlayVoiceLL, overlayCamMediaLL;
    private TextView textHelp;


    EmojiconEditText chatText;
    ImageView sendImageViewBtn, camShoutImgBtn,emoticImgBtn;
    public static int selectedMessageTypeInt = 0;
    public static int selectedEmojiResourceID = R.drawable.emoji_staright;

    private ReplyMessage replyMessage;

    @Bind(R.id.replay_layout)
    protected LinearLayout replyLayout;

    @Bind(R.id.close_reply)
    protected ImageButton replayClose;

    @Bind(R.id.textViewNameOld)
    protected TextView oldMsgName;

    @Bind(R.id.textViewMsgOld)
    protected EmojiconTextView oldTextMessageView;


    @Override
    public void EmergencyMsgAcceptResponse(CommonRequest.ResponseCode responseCode) {
        if (responseCode == COMMON_RES_SUCCESS) {
//            toast("accepted em");
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) request();
    }

    public enum MediaType {
        MEDIA_TEXT(0),
        MEDIA_IMAGE(1),
        MEDIA_VIDEO(2),
        MEDIA_AUDIO(3);

        private final int number;

        MediaType(int number) {
            this.number = number;
        }

         MediaType() {
            this.number = ordinal();
        }

        public int getNumber() {
            return number;
        }
    }

    //ArrayList of messages to store the thread messages
    private ArrayList<Message> messages;
    ArrayList<Message> multiselect_list = new ArrayList<>();


    LinearLayout linearLayoutMsgArea;

    /************** Valuables for Audio ****************/
    private TextView recordTimeText;
    private View recordPanel;
    private View slideText;
    private float startedDraggingX = -1;
    private float distCanMove = dp(80);
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private Timer timer;


    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        ButterKnife.bind(this,view);

        sessionManager = new SessionManager(getContext());

        alertDialogHelper = new AlertDialogHelper(getActivity(),this);

        linearLayoutMsgArea = (LinearLayout) view.findViewById(R.id.linear_layout_msg_area);
        typeMessageAreaPreventClickView = (View) view.findViewById(R.id.type_message_area_prevent_click_View);
        typeMessageAreaPreventClickView.setOnClickListener(typeMessageSurfaceClickListener);
        initializationOfAudioObjects(view);
        //Initializing recyclerView
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),R.color.colorAccent));
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        emojiGridView = (GridView) view.findViewById(R.id.shout_emiji);
        chatText = (EmojiconEditText) view.findViewById(R.id.chat_text);
        camShoutImgBtn = (ImageView) view.findViewById(R.id.btn_cam_shout);
        camShoutImgBtn.setOnClickListener(cameraClickListener);
        sendImageViewBtn = (ImageView) view.findViewById(R.id.btn_send_speak);
        emoticImgBtn = (ImageView) view.findViewById(R.id.btn_emoticon);

//        recyclerView.addOnItemTouchListener(recyclerTouchListener);
//        recyclerView.setHasFixedSize(true);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setItemViewCacheSize(50);
        final LinearLayoutManager  layoutManager = new LinearLayoutManager(AppController.getAppContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        emergencyMessageListView = (ListView) view.findViewById(R.id.emergency_ListView);
        emergencyMessageList = new ArrayList<>();
        emergencyListAdapter = new EmergencyListAdapter(getActivity(),emergencyMessageList);
        emergencyMessageListView.setAdapter(emergencyListAdapter);

        emergencyMessageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.getId() == R.id.accept_btn) {
                    Message message = emergencyMessageList.get(position);
                    emergencyMessageList.remove(position);

                    sendNotification(message.getFcmToken());
                    EmergencyMsgAcceptRequest msgAcceptRequest = new EmergencyMsgAcceptRequest(getContext(),message.getMsgIdOnlyForFrontEnd(), "1",FeedFragment.this);
                    if (connection.isConnected()) {
                        connection.publish(mTopicAcceptMsg, message.getMsgIdOnlyForFrontEnd().getBytes(), QoS.AT_LEAST_ONCE, false);
                        msgAcceptRequest.executeRequest();
                    }else {
                        connectMqtt();
                        connection.publish(mTopicAcceptMsg, message.getMsgIdOnlyForFrontEnd().getBytes(), QoS.AT_LEAST_ONCE, false);
                        msgAcceptRequest.executeRequest();
                    }
                    if (emergencyMessageList.size() == 0) {
                        emergencyMessageListView.setVisibility(View.GONE);
                    }
                    emergencyListAdapter.notifyDataSetChanged();
                }
            }
        });

        emoticImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        recyclerView.setLayoutManager(layoutManager);
//        sendImageViewBtn.setOnClickListener(sendVoiceClickListener);
        sendImageViewBtn.setOnTouchListener(audioSendOnTouchListener);
        chatText.addTextChangedListener(textWatcher);
        chatText.setEmojiconSize(80);
        // Inflate the layout for this fragment

        //Initializing message arraylist
                messages = new ArrayList<>();
        adapter = new ThreadAdapter(getActivity(),messages,multiselect_list, HomeActivity.mUserId,recyclerViewListener);//hardcoded token
        recyclerView.setAdapter(adapter);


        EmojiGridAdapter adapter1  =  new EmojiGridAdapter(getContext(),android.R.layout.simple_gallery_item, Constants.emoji_name);
        emojiGridView.setAdapter(adapter1);
        emojiGridView.setOnItemClickListener(selectMsgTypeEmojiListener);



        EmojIconActions  emojIcon=new EmojIconActions(getActivity(),view,chatText,emoticImgBtn);
        emojIcon.setIconsIds(R.drawable.ic_keyboard,R.drawable.ic_smily);
        emojIcon.ShowEmojIcon();



        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                request();
            }
        });

//        connectMqtt();

        //calcultae distance
//        String dis = calcDistance(new LatLng(28.550123, 77.326640),new LatLng(28.550189, 77.353430),"m",true);
//        toast(dis);

        if (!AppPreferences.getInstance(AppController.getAppContext()).isLaunchedBroadcastToolTip()) {
            toolTips(view);
        }
        if (!AppPreferences.getInstance(AppController.getAppContext()).isLaunchedSmsModeToolTip()) {
            smsModeToolTip(view);
        }


        //close reply message layout
        replayClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyLayout.setVisibility(View.GONE);
                replyMessage = null;
            }
        });

        return view;
    }


    // mqtt callback used for Future
    <T> Callback<T> onui(final Callback<T> original) {
        return new Callback<T>() {
            public void onSuccess(final T value) {
                getActivity().runOnUiThread(new Runnable(){
                    public void run() {
                        original.onSuccess(value);
                    }
                });
            }
            public void onFailure(final Throwable error) {

                try {
                    getActivity().runOnUiThread(new Runnable(){

                        public void run() {
                            original.onFailure(error);
                        }
                    });
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            }
        };
    }

    private void connectMqtt() {
        mqtt = new MQTT();


        mqtt.setClientId(HomeActivity.mLoginToken);
        try {
            mqtt.setHost(sAddress);
            Log.d(TAG, "Address set: " + sAddress);
        }
        catch(URISyntaxException urise) {
            Log.e(TAG, "URISyntaxException connecting to " + sAddress + " - " + urise);
        }


        connection = mqtt.futureConnection();
        connection.connect().then(onui(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                Log.v(TAG,"Mqtt Connected");
                subscribe();//subscribed
            }

            @Override
            public void onFailure(Throwable value) {
                Log.e(TAG, "Exception connecting to " + sAddress + " - " + value);
            }
        }));



    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        connection.disconnect();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (connection==null ||!connection.isConnected()) {
            connectMqtt();
        }
    }


    private void sendNotification(String fcmToken){
        if (ProfileFragment.myProfile != null) {
            NotificationData data = new NotificationData(fcmToken);
            data.setName(ProfileFragment.myProfile.getuName());
            data.setEmail(ProfileFragment.myProfile.getuEmail());
            data.setMobile(ProfileFragment.myProfile.getuMobile());
            data.setImg_url(ProfileFragment.myProfile.getuPictureURL());
            data.setLatLng(HomeActivity.mLastKnownLocation);
            data.setProfession(ProfileFragment.myProfile.getProfession());
            data.setProfile(ProfileFragment.myProfile);
            FcmNotificationRequest request = new FcmNotificationRequest(getContext(), data);
            request.executeRequest();
        }
    }


    private void subscribe() {
        Topic[] topics = {new Topic(mTopic, QoS.AT_LEAST_ONCE),new Topic(mTopicAcceptMsg,QoS.AT_LEAST_ONCE)};
        connection.subscribe(topics).then(onui(new Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] value) {
                connection.receive().then(onui(new Callback<org.fusesource.mqtt.client.Message>() {
                    @Override
                    public void onSuccess(org.fusesource.mqtt.client.Message message) {
                        String receivedMessageTopic = message.getTopic();
                        byte[] payload = message.getPayload();
                        String messagePayLoad = new String(payload);
                        message.ack();
                        JSONObject jsonObject = null;
                        Message messageData = new Message();
                        try {
                            jsonObject = new JSONObject(messagePayLoad);
                            messageData.setmUserID(jsonObject.getString("userId"));
                            try {
                                messageData.setId(jsonObject.getString("messageId"));
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            try {
                                messageData.setName(jsonObject.getString("userName"));
                            }catch (JSONException je) {
                                je.printStackTrace();
                            }
                            messageData.setMsgIdOnlyForFrontEnd(jsonObject.getString("emergencyId"));
                            messageData.setPicUrl(jsonObject.getString("picUrl"));
                            messageData.setMediaURL(jsonObject.getString("mediaUrl"));
                            messageData.setMediaType(MediaType.values()[Integer.parseInt(jsonObject.getString("mediaType"))]);
                            messageData.setToken(jsonObject.getString("token"));
                            messageData.setFcmToken(jsonObject.getString("fcmToken"));
                            messageData.setmText(jsonObject.getString("msg"));
                            messageData.setTimeStamp(jsonObject.getString("timestamp"));
                            messageData.setMessageType(getMessageType(jsonObject.getInt("messageType")));
                            JSONArray latlngJsonArray = new JSONArray(jsonObject.getString("longlat"));
                            messageData.setmLatLng(new LatLng(Double.valueOf(latlngJsonArray.getString(0)),Double.valueOf(latlngJsonArray.getString(1))));

                            //for reply message
                            try {
                                messageData.setReplyMessageId(jsonObject.getString("replyId"));
                                JSONObject repyMessageObject = jsonObject.getJSONObject("replyMessage");
                                ReplyMessage replyMessage = new ReplyMessage();
                                replyMessage.setId(repyMessageObject.getString("id"));
                                replyMessage.setName(repyMessageObject.getString("name"));
                                replyMessage.setTextMessage(repyMessageObject.getString("textMessage"));
                                messageData.setReplyMessage(replyMessage);
                            }catch (JSONException je) {
                                je.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (receivedMessageTopic.equals(mTopic)) {
                            if (HomeActivity.mUserId == null || !HomeActivity.mUserId.equals(messageData.getmUserID()) &&
                                    isMessageForMe(messageData.getMessageType(), messageData.getmLatLng())) {
                                messages.add(messageData);
                                adapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
//                                smoothScrollToBottom();
                                if (messageData.getMessageType() == MessageType.EMERGENCY) {
                                    emergencyMessageListView.setVisibility(View.VISIBLE);
                                    emergencyMessageList.add(messageData);
                                    emergencyListAdapter.notifyDataSetChanged();
                                    emergencyMessageListView.scrollTo(emergencyListAdapter.getCount()-2,emergencyListAdapter.getCount() -1);
                                }

                                if (messageData.getMessageType() == MessageType.WHISPER){
                                    whisperMsg(messageData);
                                }
                            }
                        }else {
                            for (Message message1:emergencyMessageList){
                                if (message1.getMsgIdOnlyForFrontEnd().equals(messagePayLoad)){
                                    int index = emergencyMessageList.indexOf(message1);
                                    emergencyMessageList.remove(index);
                                    emergencyListAdapter.notifyDataSetChanged();
                                    if (emergencyMessageList.size() == 0) {
                                        emergencyMessageListView.setVisibility(View.GONE);
                                    }
                                    break;
                                }
                            }
//                            toast("accept");
                        }
                        subscribe();//must subscribe on received every msg
                    }

                    @Override
                    public void onFailure(Throwable value) {
                        Log.e(TAG, "Exception receiving message: " + value);
                    }
                }));
            }

            @Override
            public void onFailure(Throwable value) {
                Log.e(TAG, "Exception subscribe: " + value);
            }
        }));
    }

    private void toast(String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }


    void request() {
        if (NetworkUtil.isConnected() && HomeActivity.mLastKnownLocation !=null ) {
//            LatLng latLng = new LatLng(28.545544, 77.331020);
            GetFeedRequest feedRequest = new GetFeedRequest(getContext(), HomeActivity.mLastKnownLocation, this);
            feedRequest.executeRequest();
            swipeRefreshLayout.setRefreshing(true);
        }else {
            swipeRefreshLayout.setRefreshing(false);
            new CountDownTimerTask(5000,5000).start();
        }

        if (HomeActivity.mUserId != null && !HomeActivity.mUserId.equals("")){
            typeMessageAreaPreventClickView.setVisibility(View.GONE);
        }else {
            typeMessageAreaPreventClickView.setVisibility(View.VISIBLE);
        }

    }


    View.OnClickListener typeMessageSurfaceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (HomeActivity.mUserId == null || HomeActivity.mUserId.equals("")) {
                Toast.makeText(getContext(), getText(R.string.message_login_first), Toast.LENGTH_SHORT).show();
            }else {
                typeMessageAreaPreventClickView.setVisibility(View.GONE);
                Toast.makeText(getContext(), getText(R.string.message_click_again), Toast.LENGTH_SHORT).show();
            }
        }
    };



    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (mActionMode != null) {
                mActionMode.finish();
            }
            request();
        }
    };


    View.OnClickListener sendTextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String text = chatText.getText().toString().trim();
            if (!text.matches("") && Utility.isLocationAvailable(getContext())) {

                Message messageData = new Message();
//                messageData.setToken("58c93b21f81fde4c11fe02e1");
                messageData.setToken(HomeActivity.mLoginToken);
                if (HomeActivity.mUserId != null) {
                    messageData.setmUserID(HomeActivity.mUserId);
                    messageData.setId(HomeActivity.mUserId + System.currentTimeMillis());
                } else {
                    messageData.setmUserID("");
                }

                if (HomeActivity.mUserName != null){
                    messageData.setName(HomeActivity.mUserName);
                }

                if (sessionManager.getFcmToken() != null) {
                    messageData.setFcmToken(sessionManager.getFcmToken());
                }else {
                    messageData.setFcmToken(FirebaseInstanceId.getInstance().getToken());
                }

                if (HomeActivity.mPicUrl !=null) {
                    messageData.setPicUrl(HomeActivity.mPicUrl);
                }
                messageData.setMessageType(getMessageType(selectedMessageTypeInt));

                if (messageData.getMessageType() == MessageType.MUMBLE) {
                    text = mumbleMessage(text);
                }

                if (messageData.getMessageType() == MessageType.EMERGENCY) {
                    messageData.setMsgIdOnlyForFrontEnd(nextSessionId());
                } else {
                    messageData.setMsgIdOnlyForFrontEnd("");
                }
                //reply message
                if (replyMessage != null) {
                    messageData.setReplyMessageId(replyMessage.getId());
                    messageData.setReplyMessage(replyMessage);
                }

                messageData.setTimeStamp(String.valueOf(System.currentTimeMillis()));
                messageData.setmText(text);
                messageData.setMediaType(MediaType.MEDIA_TEXT);
                messageData.setmLatLng(HomeActivity.mLastKnownLocation);

                messages.add(messageData);
                adapter.notifyDataSetChanged();
                chatText.setText("");
                smoothScrollToBottom();

                if (messageData.getMessageType() == MessageType.WHISPER){//remove after 2 min if WHISPER Message
                    whisperMsg(messageData);
                }


                Map<String, String> replyMessageMap = new HashMap<>();


                mParams =  new HashMap<>();


                if (messageData.getReplyMessage() != null) {
                    replyMessageMap.put("id", messageData.getReplyMessage().getId());
                    replyMessageMap.put("name", messageData.getReplyMessage().getName());
                    replyMessageMap.put("textMessage", messageData.getReplyMessage().getTextMessage());

                    replayClose.performClick();

                }

                mParams.put("token",messageData.getToken());
                mParams.put("replyId",messageData.getReplyMessageId());

                mParams.put("fcmToken",messageData.getFcmToken());
                mParams.put("userId",messageData.getmUserID());
                mParams.put("messageId",messageData.getId());
                mParams.put("userName", messageData.getName());
                mParams.put("picUrl",messageData.getPicUrl());
                mParams.put("mediaUrl","");
                mParams.put("mediaType",""+messageData.getMediaType().getNumber());
                mParams.put("emergencyId",messageData.getMsgIdOnlyForFrontEnd());
                mParams.put("msg",messageData.getmText());
                mParams.put("timestamp",messageData.getTimeStamp());
//                mParams.put("img",bitmap);
                mParams.put("messageType",""+selectedMessageTypeInt);
                String[] latlng = {""+messageData.getmLatLng().latitude,""+messageData.getmLatLng().longitude};
                mParams.put("longlat",Arrays.toString(latlng));
                JSONObject jsonObject = new JSONObject(mParams);
                JSONObject jsonReplayMessage = new JSONObject(replyMessageMap);


                try {
                    jsonObject.put("replyMessage",jsonReplayMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (connection.isConnected()) {
                    connection.publish(mTopic, jsonObject.toString().getBytes(), QoS.AT_LEAST_ONCE, false);
                }else {
                    connectMqtt();
                    connection.publish(mTopic, jsonObject.toString().getBytes(), QoS.AT_LEAST_ONCE, false);
                }

                initMsgTypeEmoji(); //set default message type straight

            }

        }
    };


    private void sendMedia(MediaType mediaType,String mediaUrl, int to ) {
        Message messageData = new Message();
//                messageData.setToken("58c93b21f81fde4c11fe02e1");
        messageData.setToken(HomeActivity.mLoginToken);
        if (HomeActivity.mUserId != null) {
            messageData.setmUserID(HomeActivity.mUserId);
            messageData.setId(HomeActivity.mUserId + System.currentTimeMillis());
        } else {
            messageData.setmUserID("");
        }

        if (HomeActivity.mUserName != null){
            messageData.setName(HomeActivity.mUserName);
        }

        if (HomeActivity.mPicUrl !=null) {
            messageData.setPicUrl(HomeActivity.mPicUrl);
        }

        messageData.setMediaURL(mediaUrl);

        messageData.setMessageType(getMessageType(selectedMessageTypeInt));


        if (messageData.getMessageType() == MessageType.EMERGENCY) {
            messageData.setMsgIdOnlyForFrontEnd(nextSessionId());
        } else {
            messageData.setMsgIdOnlyForFrontEnd("");
        }

        messageData.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        messageData.setMediaType(mediaType);
        messageData.setmLatLng(HomeActivity.mLastKnownLocation);



        if (to == 1) {
            mParams =  new HashMap<>();
            mParams.put("token",messageData.getToken());
            mParams.put("userId",messageData.getmUserID());
            mParams.put("messageId",messageData.getId());
            mParams.put("picUrl",messageData.getPicUrl());
            mParams.put("userName", messageData.getName());
            mParams.put("emergencyId","");
            mParams.put("mediaUrl",messageData.getMsgIdOnlyForFrontEnd());
            mParams.put("msg","");
            mParams.put("mediaUrl",messageData.getMediaURL());
            mParams.put("mediaType",""+messageData.getMediaType().getNumber());
            mParams.put("timestamp",messageData.getTimeStamp());
//                mParams.put("img",bitmap);
            mParams.put("messageType",""+selectedMessageTypeInt);
            String[] latlng = {""+messageData.getmLatLng().latitude,""+messageData.getmLatLng().longitude};
            mParams.put("longlat",Arrays.toString(latlng));
            JSONObject jsonObject = new JSONObject(mParams);
            if (connection.isConnected()) {
                connection.publish(mTopic, jsonObject.toString().getBytes(), QoS.AT_LEAST_ONCE, false);
            }else {
                connectMqtt();
                connection.publish(mTopic, jsonObject.toString().getBytes(), QoS.AT_LEAST_ONCE, false);
            }
        }else {
            messages.add(messageData);
            adapter.notifyDataSetChanged();
            smoothScrollToBottom();

        }
    }

    AdapterView.OnItemClickListener selectMsgTypeEmojiListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedMessageTypeInt = position;
            selectedEmojiResourceID = Constants.emojiResourceID[position];
            camShoutImgBtn.setImageResource(selectedEmojiResourceID);
            emojiGridView.setVisibility(View.GONE);
            Utility.showSoftKeyboard(chatText);
        }
    };

    void initMsgTypeEmoji() {
        selectedMessageTypeInt = 0;
        selectedEmojiResourceID = Constants.emojiResourceID[0];
        camShoutImgBtn.setImageResource(R.drawable.ic_camera);
    }



    View.OnClickListener dropDownClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (emojiGridView.getVisibility() == View.VISIBLE) {
                emojiGridView.setVisibility(View.GONE);
                Utility.showSoftKeyboard(chatText);
            }else {
                Utility.hideSoftKeyboard(getActivity());
                emojiGridView.setVisibility(View.VISIBLE);
            }
        }
    };

    View.OnClickListener cameraClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isCameraPermissionGrated()){
                openCamera();
            }else {
                requestPermissions(CAMERA_PERMISSIONS,REQUEST_CAMERA_CODE);
            }

        }
    };


    void openCamera(){
        Intent intent = new Intent(getContext(),Camera2Activity.class);
        intent.putExtra("requestCode", CAMERA_REQUEST);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    boolean isCameraPermissionGrated(){
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(),Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(),Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    boolean isAudioPermissionGranted() {
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(),Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED);
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(chatText.getText())) {
//                sendImageViewBtn.setOnClickListener(sendVoiceClickListener);
                sendImageViewBtn.setOnTouchListener(audioSendOnTouchListener);
                sendImageViewBtn.setImageResource(R.drawable.ic_speak);
                camShoutImgBtn.setImageResource(R.drawable.ic_camera);
                camShoutImgBtn.setOnClickListener(cameraClickListener);
                emojiGridView.setVisibility(View.GONE);
            }else {
                sendImageViewBtn.setOnClickListener(sendTextClickListener);
                sendImageViewBtn.setOnTouchListener(null);
                sendImageViewBtn.setImageResource(R.drawable.ic_send);
                camShoutImgBtn.setImageResource(selectedEmojiResourceID);
                camShoutImgBtn.setOnClickListener(dropDownClickListener);

                if (smsTipAvailable) {
                    overlaySmsMode.setVisibility(View.VISIBLE);
                    tipCount = 22;
                }
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void smoothScrollToBottom() {
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() > 1)
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
    }

    private void scrollToBottom() {
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() > 1)
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }


    private void scrollToPosition(int position) {
        adapter.notifyDataSetChanged();
        if (position < adapter.getItemCount())
            recyclerView.scrollToPosition(position);
    }

    private void setBroadcastHistory(GetFeedRequestData simpleMsgData, GetFeedRequestData emergencyMsgData) {

        for(Message msg : simpleMsgData.getMessageList()) {
            if (isMessageForMe(msg.getMessageType(),msg.getmLatLng())) {
                messages.add(msg);

                if (msg.getMessageType() == MessageType.WHISPER){
                    whisperMsg(msg);
                }
            }
        }

        for (Message msg : emergencyMsgData.getMessageList()) {
            if (isMessageForMe(msg.getMessageType(), msg.getmLatLng())) {
                emergencyMessageListView.setVisibility(View.VISIBLE);
                emergencyMessageList.add(msg);
            }
        }

        adapter.notifyDataSetChanged();
        emergencyListAdapter.notifyDataSetChanged();
        smoothScrollToBottom();


    }


    @Override
    public void GetFeedResponse(CommonRequest.ResponseCode responseCode, GetFeedRequestData data, GetFeedRequestData emergencyData) {
        switch (responseCode) {
            case COMMON_RES_SUCCESS:
                if (messages.size()>0) {
                    messages.clear();
                }

                if (emergencyMessageList.size() > 0){
                    emergencyMessageList.clear();
                }


                setBroadcastHistory(data, emergencyData);

                /*messages.addAll(data.getMessageList());
                emergencyMessageList.addAll(emergencyData.getMessageList());
                adapter.notifyDataSetChanged();
                emergencyListAdapter.notifyDataSetChanged();
                if (emergencyMessageList.size() > 0) {
                    emergencyMessageListView.setVisibility(View.VISIBLE);
                }*/
                break;
            case COMMON_RES_CONNECTION_TIMEOUT:
                break;
            case COMMON_RES_FAILED_TO_CONNECT:
                break;
            case COMMON_RES_INTERNAL_ERROR:
                break;
            case COMMON_RES_SERVER_ERROR_WITH_MESSAGE:
                break;
        }

        swipeRefreshLayout.setRefreshing(false);

    }


    public enum MessageType {
        STRAIGHT,
        SHOUT,
        WHISPER,
        GOSSIP,
        MURMUR,
        MUMBLE,
        EMERGENCY
    }

    public static MessageType getMessageType(int type) {
        switch (type) {
            case 0: return MessageType.STRAIGHT;
            case 1: return MessageType.SHOUT;
            case 2: return MessageType.WHISPER;
            case 3: return MessageType.GOSSIP;
            case 4: return MessageType.MURMUR;
            case 5: return MessageType.MUMBLE;
            case 6: return MessageType.EMERGENCY;
        }
        return MessageType.STRAIGHT;
    }

    private boolean isMessageForMe(MessageType messageType, LatLng latLng) {
        if (latLng == null) return false;

        double distance;

        try {
            distance = Double.valueOf(Utility.calcDistance(HomeActivity.mLastKnownLocation,latLng,"km",false));
        }catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }

        switch (messageType) {
            case STRAIGHT:
                if (distance <= 2)
                return true;
                break;
            case SHOUT:
                if (distance <= 5)
                    return true;
                break;
            case WHISPER:
                //TODO: will remove in 2 min
                if (distance <= 2)
                    return true;
                break;
            case GOSSIP:
                if (distance <= 4)
                    return true;
                break;
            case MURMUR:
                if (distance <= 1)
                    return true;
                break;
            case MUMBLE:
                //TODO: rearrange string
                if (distance <= 2)
                    return true;
                break;
            case EMERGENCY:
                if (distance <= 10)
                    return true;
                break;
        }
        return false;
    }

    /**
     *
     * @param msgText
     * @return
     */
    public String mumbleMessage(String msgText) {

        String mumbledStr = "";
        StringTokenizer st = new StringTokenizer(msgText," ");
        List<String> stringList = new ArrayList<>();
        while (st.hasMoreTokens()) {
            stringList.add(st.nextToken());
        }
        Collections.shuffle(stringList);


        for (int i=0;i<stringList.size();i++) {
            mumbledStr += stringList.get(i)+ " ";
        }

        if (stringList.size()>1 && mumbledStr.trim().equals(msgText)) {
            Collections.shuffle(stringList);
            mumbledStr = "";
            for (int i=0;i<stringList.size();i++) {
                mumbledStr += stringList.get(i)+ " ";
            }
        }

        return mumbledStr.trim();
    }


    public void whisperMsg(Message message){
        new CountDownWhisper(message,60000*2,1000).start();

    }

    /**
     * Random generate string for emergency msg id only for front end
     * @return
     */
    public String nextSessionId() {
       SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }




    /*********** calculate distance by GeoDataSource.com**********/


    /*class EmergencyListAdapter extends BaseAdapter{
        private Activity context;
        private List<Message> emergencyMessageList;
        private LayoutInflater inflater = null;

        public EmergencyListAdapter(Activity context, List<Message> emergencyMessageList) {
            this.context = context;
            this.emergencyMessageList = emergencyMessageList;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return emergencyMessageList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View vi=convertView;
            if(convertView==null)
                vi = inflater.inflate(R.layout.emergency_message_list, null);
            CircularImageView proPicImageView = (CircularImageView)vi.findViewById(R.id.msg_pic);
            EmojiconTextView msgTextView = (EmojiconTextView)vi.findViewById(R.id.textViewMsg);
            Button acceptButton = (Button) vi.findViewById(R.id.accept_btn);
            ImageView messageTypeImageView = (ImageView) vi.findViewById(R.id.msg_emoji) ;

            Message message = emergencyMessageList.get(position);

//            proPicImageView.setImageUrl(message.getMediaURL(), VolleySingleton.getInstance(context).getImageLoader());
            Picasso.with(context).load(message.getPicUrl()).into(proPicImageView);
            msgTextView.setText(message.getmText());
            if (message.getMessageType() != null) {
                messageTypeImageView.setImageResource(getEmojiResourceIdByMsgType(message.getMessageType()));
            }

            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });


            return vi;
        }
    }*/

    public static final int CAMERA_REQUEST = 55;
    public static final int VIDEO_REQUEST = 56;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == CAMERA_REQUEST) {
            Uri resultData = Uri.parse(data.getStringExtra("result"));
            sendMedia(MEDIA_IMAGE,resultData.toString(),0);

            File compressFile = new File(resultData.getPath());
            int file_size = Integer.parseInt(String.valueOf(compressFile.length()/1024));

            if (file_size > 80) {
                compressFile = Compressor.getDefault(getActivity()).compressToFile(compressFile);
            }


            PicUrlRequest picUrlRequest = new PicUrlRequest(getContext(),compressFile,MEDIA_IMAGE,this);
            picUrlRequest.executeRequest();
        }else if (resultCode == VIDEO_REQUEST) {
            Uri resultData = Uri.parse(data.getStringExtra("result"));
            sendMedia(MEDIA_VIDEO,resultData.toString(),0);

            PicUrlRequest picUrlRequest = new PicUrlRequest(getContext(),new File(resultData.getPath()),MEDIA_VIDEO,this);
            picUrlRequest.executeRequest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CAMERA_CODE:
                if (grantResults.length > 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                openCamera();
            }else {
                Toast.makeText(getContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
            }

            break;
            case REQUEST_AUDIO_CODE:
                if (!(grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                    Toast.makeText(getContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
            }
            break;

        }
    }

    @Override
    public void PicUrlResponse(CommonRequest.ResponseCode responseCode, String picUrl,MediaType mediaType) {

        if (responseCode == COMMON_RES_SUCCESS) {
            switch (mediaType) {
                case MEDIA_IMAGE:sendMedia(MEDIA_IMAGE,picUrl,1);return;
                case MEDIA_VIDEO:sendMedia(MediaType.MEDIA_VIDEO,picUrl,1);return;
                case MEDIA_AUDIO:sendMedia(MediaType.MEDIA_AUDIO,picUrl,1);
            }

        }

    }


    ThreadAdapter.RecyclerViewListener recyclerViewListener = new ThreadAdapter.RecyclerViewListener() {
        @Override
        public void onClick(View view, int position) {
            Message message = messages.get(position);
            if (isMultiSelect) {
                multi_select(position);
                /*if (message.getmUserID().equals(HomeActivity.mUserId)) {
                    multi_select(position);
                }else {
                    Toast.makeText(getContext(), "Please select your message", Toast.LENGTH_SHORT).show();
                }*/

            }else {
                if (view.getTag() != null && view.getTag().equals("vdo")){

                    Intent intent=new Intent(getActivity(), VideoPlay.class);
                    intent.putExtra("url",message.getMediaURL());
                    startActivity(intent);

                }

                //scroll to original message
                if (message.getReplyMessageId() != null && !message.getReplyMessageId().equals("null")) {
                    for (Message message1:messages) {
                        if (message1.getId().equals(message.getReplyMessageId())) {
                            scrollToPosition(messages.indexOf(message1));
                            break;
                        }
                    }

                }
            }
        }

        @Override
        public void onLongClick(View view, int position) {
            if (!isMultiSelect) {
                multiselect_list = new ArrayList<Message>();
                isMultiSelect = true;

                if (mActionMode == null && getActivity() != null) {
//                    mActionMode = HomeActivity.homeActivityActionBar.startActionMode(mActionModeCallback);
                    try {
                        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                    }catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }

            multi_select(position);
        }
    };




    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_select, menu);
            context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    alertDialogHelper.showAlertDialog("",getString(R.string.message_delete),getString(R.string.btn_delete),getString(R.string.btn_cancel),1,false);
                    return true;
                case R.id.action_replay:
                    if (replyMessage != null) {
                        if (replyMessage.getMediaType() == MEDIA_TEXT) {
                            replyLayout.setVisibility(View.VISIBLE);
                            oldMsgName.setText(replyMessage.getName());
                            oldTextMessageView.setText(replyMessage.getTextMessage());
                        }else {
                            Toast.makeText(getContext(), "Please select a text message for reply", Toast.LENGTH_SHORT).show();
                        }

                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                    }
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiselect_list = new ArrayList<Message>();
            refreshAdapter();
        }
    };

    @Override
    public void onPositiveClick(int from) {
        if(from==1) {
            if (multiselect_list.size() > 0) {
                deleteMessageRequest(multiselect_list);
            }
        }
    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }

    public void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(messages.get(position))) {
                multiselect_list.remove(messages.get(position));


                if (multiselect_list.size() == 1) {
                    Message message = multiselect_list.get(0);
                    replyMessage = new ReplyMessage(message.getId(), message.getName(), message.getmText());
                    replyMessage.setMediaType(message.getMediaType());
                }

                if (multiselect_list.size() > 1) {
                    context_menu.getItem(0).setVisible(false);
                }else {
                    context_menu.getItem(0).setVisible(true);
                }

                if (multiselect_list.size() == 0) {
                    mActionMode.finish();
                    refreshAdapter();
                    return;
                }

            } else {


               /* for (Message message : multiselect_list) {
                    if (!message.getmUserID().equals(HomeActivity.mUserId)) {
                        Toast.makeText(getContext(), "Please select your message", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }*/

                if (multiselect_list.size() == 1 &&  !multiselect_list.get(0).getmUserID().equals(HomeActivity.mUserId)) {
                    Toast.makeText(getContext(), "Please select only one message for reply", Toast.LENGTH_SHORT).show();
                    return;
                }

               if (multiselect_list.size() > 0 && !messages.get(position).getmUserID().equals(HomeActivity.mUserId)) {
                   Toast.makeText(getContext(), "Please select your message for delete", Toast.LENGTH_SHORT).show();
                   return;
               }

                multiselect_list.add(messages.get(position));
                if (multiselect_list.size() == 1) {
                    Message message = messages.get(position);
                    replyMessage = new ReplyMessage(message.getId(), message.getName(), message.getmText());
                    replyMessage.setMediaType(message.getMediaType());
                }

                if (multiselect_list.size() > 1) {
                    context_menu.getItem(0).setVisible(false);
                }else {
                    context_menu.getItem(0).setVisible(true);
                }
            }

            if (multiselect_list.size() == 1) {
                if (multiselect_list.get(0).getmUserID().equals(HomeActivity.mUserId)) {
                    context_menu.getItem(1).setVisible(true);
                }else {
                    context_menu.getItem(1).setVisible(false);
                }
            }


            if (multiselect_list.size() > 0) {
                mActionMode.setTitle(multiselect_list.size() + " selected");
            }
            else
                mActionMode.setTitle("");

            refreshAdapter();


            /*if (multiselect_list.size() > 1) {

                context_menu.getItem(0).setVisible(false);
            }else {
                context_menu.getItem(0).setVisible(true);
                if (multiselect_list.get(0).getId().equals(HomeActivity.mUserId)) {
                    context_menu.getItem(1).setVisible(true);
                }else {
                    context_menu.getItem(1).setVisible(false);
                }
            }*/

        }
    }

    public void refreshAdapter() {
        adapter.selected_messageList = multiselect_list;
        adapter.messages = messages;
        adapter.notifyDataSetChanged();
    }



    /*****************============= FOR Audio =======================***************/

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mAudioFileName = null;
    private static String mAudioPath = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;
    private boolean isCanceled = false;
    ImageView recImage;


    boolean isStartRecording = false;
    public void initializationOfAudioObjects(View view) {

        // Record to the external cache directory for visibility
//        mAudioPath = getActivity().getExternalCacheDir().getAbsolutePath();
        mAudioPath = Environment
                .getExternalStorageDirectory() + "/Localapp";
        File folder = null;
        String state = Environment.getExternalStorageState();
        if (state.contains(Environment.MEDIA_MOUNTED)) {
            folder = new File(Environment
                    .getExternalStorageDirectory() + "/Localapp");
        } else {
            folder = new File(Environment
                    .getExternalStorageDirectory() + "/Localapp");
        }

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }


        recordPanel = view.findViewById(R.id.record_panel);
        recordTimeText = (TextView) view.findViewById(R.id.recording_time_text);
        recImage = (ImageView) view.findViewById(R.id.rec_img);
        slideText = view.findViewById(R.id.slideText);
        TextView textView = (TextView) view.findViewById(R.id.slideToCancelTextView);
        textView.setText(R.string.message_slide_to_cancel);
    }

    View.OnTouchListener audioSendOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                sendImageViewBtn.setImageResource(R.drawable.ic_speak_pressed);
                recImage.setVisibility(View.VISIBLE);
                recordPanel.setVisibility(View.VISIBLE);
                linearLayoutMsgArea.setVisibility(View.INVISIBLE);

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                        .getLayoutParams();
                params.leftMargin = dp(30);
                slideText.setLayoutParams(params);
                ViewProxy.setAlpha(slideText, 1);
                startedDraggingX = -1;

                if (isAudioPermissionGranted()){
                    startRecording();
                    isStartRecording = true;
                    isCanceled = false;
                }else {
                    requestPermissions(AUDIO_PERMISSIONS, REQUEST_AUDIO_CODE);
                }


                sendImageViewBtn.getParent()
                        .requestDisallowInterceptTouchEvent(true);


            }else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                    || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                sendImageViewBtn.setImageResource(R.drawable.ic_speak);

                startedDraggingX = -1;

                if (isStartRecording) {
                     stopRecording();

                    isStartRecording = false;

                }

                recordPanel.setVisibility(View.INVISIBLE);
                linearLayoutMsgArea.setVisibility(View.VISIBLE);

            }else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                float x = motionEvent.getX();
                if (x < -distCanMove-180) {
                    if (isStartRecording) {
                        sendImageViewBtn.setImageResource(R.drawable.ic_speak);
                        isCanceled = true;
                         stopRecording();
                        isStartRecording = false;
                    }
                    recordPanel.setVisibility(View.INVISIBLE);
                    linearLayoutMsgArea.setVisibility(View.VISIBLE);
                }
                x = x + ViewProxy.getX(sendImageViewBtn);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                        .getLayoutParams();

                if (startedDraggingX != -1) {
                    float dist = (x - startedDraggingX);
                    params.leftMargin = dp(30) + (int) dist;
                    slideText.setLayoutParams(params);
                    float alpha = 1.3f + dist / distCanMove;
                    if (alpha > 1) {
                        alpha = 1;
                    } else if (alpha < 0) {
                        alpha = 0;
                    }
                    ViewProxy.setAlpha(slideText, alpha);
                }

                if (x <= ViewProxy.getX(slideText) + slideText.getWidth()
                        + dp(30)) {
                    if (startedDraggingX == -1) {
                        startedDraggingX = x;
                        distCanMove = (recordPanel.getMeasuredWidth()
                                - slideText.getMeasuredWidth() - dp(20)) / 2.0f;
                        if (distCanMove <= 0) {
                            distCanMove = dp(200);
                        } else if (distCanMove > dp(200)) {
                            distCanMove = dp(80);
                        }
                    }
                }

                if (params.leftMargin > dp(30)) {
                    params.leftMargin = dp(30);
                    slideText.setLayoutParams(params);
                    ViewProxy.setAlpha(slideText, 1);
                    startedDraggingX = -1;
                }

                v.onTouchEvent(motionEvent);

            }
            return true;
        }
    };


    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mAudioFileName = mAudioPath + "/audio"+System.currentTimeMillis()+".3gp";
        mRecorder.setOutputFile(mAudioFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
        startRecord();
    }

    private void stopRecording() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            if (!isCanceled && !recordTimeText.getText().toString().equals("00:00")) {
                Uri resultData = Uri.parse(mAudioFileName);
                sendMedia(MEDIA_AUDIO, mAudioFileName, 0);
//                toast(resultData.toString());

                PicUrlRequest picUrlRequest = new PicUrlRequest(getContext(), new File(resultData.getPath()), MEDIA_AUDIO, this);
                picUrlRequest.executeRequest();
            }
        }catch (Exception e){
            e.printStackTrace();
            mRecorder.release();
            mRecorder = null;
        }
        stopRecord();


    }



    private void startRecord() {
        // TODO Auto-generated method stub
        startTime = SystemClock.uptimeMillis();
        timer = new Timer();
        MyTimerTask myTimerTask = new MyTimerTask();
        timer.schedule(myTimerTask, 1000, 1000);
        vibrate();
    }

    private void stopRecord() {
        // TODO Auto-generated method stub
        if (timer != null) {
            timer.cancel();
        }
        if (recordTimeText.getText().toString().equals("00:00")) {
            vibrate();
            return;
        }
        recordTimeText.setText("00:00");
        vibrate();
    }

    public static int dp(float value) {
        return (int) Math.ceil(1 * value);
    }

    private void vibrate() {
        // TODO Auto-generated method stub
        try {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            @SuppressLint("DefaultLocale") final String hms = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(updatedTime)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                            .toHours(updatedTime)),
                    TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(updatedTime)));
            final long lastsec = TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                    .toMinutes(updatedTime));
            System.out.println(lastsec + " hms " + hms);
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (recordTimeText != null) {
                            recordTimeText.setText(hms);

                            if (lastsec !=0 && lastsec%2==0) {
                                recImage.setVisibility(View.VISIBLE);
                            }else {
                                recImage.setVisibility(View.INVISIBLE);
                            }

                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppController.getInstance().addConnectivityListener(this);
        View view = getView();
        if (view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(onKeyListener);
        }

    }

    View.OnKeyListener onKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction()!= KeyEvent.ACTION_DOWN ) {
                HomeActivity.mViewPager.setCurrentItem(0);
                return true;
            }
            return false;
        }
    };

    public class CountDownWhisper extends CountDownTimer{
        private Message message;
        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CountDownWhisper(Message message,long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.message = message;
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if (messages.contains(message)){
                messages.remove(message);
                adapter.notifyDataSetChanged();
            }
        }
    }


    private class CountDownTimerTask extends CountDownTimer {


        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CountDownTimerTask(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.d("CountDownTimerTask",": "+millisUntilFinished / 1000);
        }

        @Override
        public void onFinish() {
            if (AppController.isActivityVisible()) {
                request();
            }else {
                new CountDownTimerTask(5000,5000).start();
            }

        }
    }

    private int tipCount = 0;
    private boolean smsTipAvailable = false;
    private void toolTips (View view) {
        overlayRL = (RelativeLayout) view.findViewById(R.id.rlOverlay);
        overlayVoiceLL = (LinearLayout) view.findViewById(R.id.rlVoice);
        overlayCamMediaLL = (LinearLayout) view.findViewById(R.id.rlCamMedia);
        textHelp = (TextView) view.findViewById(R.id.textHelp);

        overlayRL.setVisibility(View.VISIBLE);

        textHelp.setOnClickListener(toolTipClickListener);
        overlayRL.setOnClickListener(toolTipClickListener);
    }

    private void smsModeToolTip(View view) {
        smsTipAvailable = true;
        overlaySmsMode = (RelativeLayout) view.findViewById(R.id.rlMsgMode);
        overlaySmsMode.setOnClickListener(toolTipClickListener);
    }
    private View.OnClickListener toolTipClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (tipCount) {
                case 0:
                    overlayVoiceLL.setVisibility(View.GONE);
                    overlayCamMediaLL.setVisibility(View.VISIBLE);
                    textHelp.setText(R.string.btn_got_it);
                    tipCount++;
                    break;

                case 22:
                    overlaySmsMode.setVisibility(View.GONE);

                    if (emojiGridView.getVisibility() == View.VISIBLE) {
                        emojiGridView.setVisibility(View.GONE);
                    }else {
                        emojiGridView.setVisibility(View.VISIBLE);
                    }

                    smsTipAvailable = false;
                    AppPreferences.getInstance(AppController.getAppContext()).smsToolTipLaunched();
                    tipCount = 0;
                    break;

                default:
                    overlayRL.setVisibility(View.GONE);
                    AppPreferences.getInstance(AppController.getAppContext()).broadcastToolTipLaunched();
            }
        }
    };


    private void deleteMessageRequest (ArrayList<Message> messageList) {
        DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(getContext(),messageList,this);
        deleteMessageRequest.executeRequest();
    }

    @Override
    public void onDeleteMessageResponse(CommonRequest.ResponseCode responseCode, String data) {
        if (responseCode == COMMON_RES_SUCCESS) {
            if (mActionMode != null) {
                mActionMode.finish();
            }
            Toast toast = Toast.makeText(getContext(), R.string.message_deleted, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();


            request();
        }else {
            Toast.makeText(getContext(), R.string.error_something_went_wrong_try_again, Toast.LENGTH_SHORT).show();
        }
    }
}

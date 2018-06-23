package kr.maden.watson_iot.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.Chat;
import kr.maden.watson_iot.object.ChatManual;
import kr.maden.watson_iot.object.ChatMsg;
import kr.maden.watson_iot.object.ChatUser;
import kr.maden.watson_iot.object.Message;
import kr.maden.watson_iot.object.MsgObj;
import kr.maden.watson_iot.object.User;
import kr.maden.watson_iot.object.worker.WorkMeta;
import kr.maden.watson_iot.object.worker.Worker;
import kr.maden.watson_iot.ui.MainActivity;
import kr.maden.watson_iot.utils.mqtt.BusanMqttCallback;
import kr.maden.watson_iot.utils.mqtt.BusanMqttConnect;
import kr.maden.watson_iot.utils.sql.ChatListHelper;
import kr.maden.watson_iot.utils.sql.model.ChatListModel;
import kr.maden.watson_iot.utils.trans.BusanChatApiClient;
import retrofit2.Call;
import retrofit2.Response;


public class ChatFragment extends Fragment implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener,
        MessageInput.InputListener,
        MessageInput.AttachmentsListener {
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static ChatFragment chatFragment;
    private MessagesList messagesList;
    private MessageInput input;
    private ImageLoader imageLoader;
    private MessagesListAdapter<Message> messagesAdapter;

    private ChatListHelper mDbHelper;

    private String[] projection = {
            ChatListModel.ChatListEntry._ID,
            ChatListModel.ChatListEntry.COLUMN_NAME_TO,
            ChatListModel.ChatListEntry.COLUMN_NAME_FROM,
            ChatListModel.ChatListEntry.COLUMN_NAME_MSG,
            ChatListModel.ChatListEntry.COLUMN_NAME_CREATED
    };

//    private String subscribeUrl = "iot-2/type/Android/id/application/cmd/chat/fmt/json";

    private static BusanMqttConnect busanMqttConnect;
    private static MqttAndroidClient mqttAndroidClient;
    private static boolean manualMode = false;
    public static final int BOT_VOICE = 111;

    public ChatFragment() {
    }

    public static ChatFragment getInstance() {
        if (chatFragment == null)
            chatFragment = new ChatFragment();
        return chatFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        messagesList = (MessagesList) view.findViewById(R.id.messagesList);
        input = (MessageInput) view.findViewById(R.id.input);
        busanMqttConnect = BusanMqttConnect.getInstance(getContext());

        mDbHelper = new ChatListHelper(getContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String sortOrder =
                ChatListModel.ChatListEntry.COLUMN_NAME_CREATED + " DESC";

        Cursor c = db.query(
                ChatListModel.ChatListEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<Message> messageArrayList = new ArrayList<>();

        c.moveToFirst();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Log.d(TAG, "::" + c.getLong(4));
            messageArrayList.add(new Message(c.getString(0), new User(c.getString(2), c.getString(2), "https://media-exp2.licdn.com/mpr/mpr/shrink_200_200/AAIA_wDGAAAAAQAAAAAAAAzLAAAAJGQ1ZGFiYzgwLTRjOWMtNDBlYS05ODQyLTZkMjFkYTMzNDhjNw.png", true), c.getString(3), new Date(c.getLong(4))));

        }

        input.setAttachmentsListener(new MessageInput.AttachmentsListener() {
            @Override
            public void onAddAttachments() {
                voiceStart();

            }
        });
//        GetChatMessage getChatMessage = new GetChatMessage();
//        getChatMessage.execute();


        ///Subscribe
/*        mqttAndroidClient = busanMqttConnect.getMqttAndroidClient();

        try {
            // mqttAndroidClient.subscribe(BusanMqttCallback.CHAT_SUBSCRIBE, 0, null, new IMqttActionListener() {
            mqttAndroidClient.subscribe("iot-2/cmd/chat/fmt/json", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "subscribe onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to subscribe");
                    Log.e(TAG, "Subscribe Error", exception);
                }
            });

        } catch (MqttException e) {
            Log.e(TAG, "Subscribe Error", e);
        }*/

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(getContext()).load(url).into(imageView);
            }
        };

        messagesAdapter = new MessagesListAdapter<>("application", imageLoader);
        messagesAdapter.enableSelectionMode(this);
        messagesAdapter.setLoadMoreListener(this);
        messagesAdapter.registerViewClickListener(R.id.messageUserAvatar,
                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
                    @Override
                    public void onMessageViewClick(View view, Message message) {
                        Log.d(TAG, message.getUser().getName() + " avatar click");
                    }
                });
        messagesAdapter.addToEnd(messageArrayList, false);

        messagesList.setAdapter(messagesAdapter);

        input.setInputListener(this);

        registerReceiver();
//        if (!MainActivity.startBot) {
        MainActivity.startBot = true;
        botSubmit("안녕하세요, 김호성님. 저는 00조선소의 안전을 책임지고 있는 왓슨 어시스턴트입니다. ");
        botSubmit("무엇을 도와드릴까요?");
//        }
        return view;
    }

    public void voiceStart() {
        try {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        1010);
                return;
            }
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getContext().getPackageName());
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "무엇을 도와드릴까요?");
            i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
            i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500);

            getActivity().startActivityForResult(i, BOT_VOICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(BusanMqttCallback.CHAT_DATA);
        Receiver receiver = new Receiver();
        getContext().registerReceiver(receiver, filter);
        Log.d(TAG, "Executed the registerReceiver");
    }

    public class Receiver extends BroadcastReceiver {
        public Receiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            String message = intent.getStringExtra("message");
            if (BusanMqttCallback.CHAT_DATA.equals(action)) {
                Gson gson = new Gson();
                MsgObj msgObj = gson.fromJson(message, MsgObj.class);
                String msg = (String) ((Map<String, Object>) msgObj.getData().get("message")).get("msg");
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(projection[1], "its");
                values.put(projection[2], "to");
                values.put(projection[3], msg);
                values.put(projection[4], new Date().getTime());

                long newRowId = db.insert(ChatListModel.ChatListEntry.TABLE_NAME, null, values);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("채팅")
                                .setContentInfo("safetymanager")
                                .setVisibility(Notification.VISIBILITY_PUBLIC)
                                .setAutoCancel(true)
                                .setVibrate(new long[]{1000, 1000, 1000})
                                .setLights(Color.BLUE, 3000, 3000)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setContentText(msg);

                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(12, mBuilder.build());

                messagesAdapter.addToStart(new Message(String.valueOf(newRowId), new User("Manager", "Manager", "https://media-exp2.licdn.com/mpr/mpr/shrink_200_200/AAIA_wDGAAAAAQAAAAAAAAzLAAAAJGQ1ZGFiYzgwLTRjOWMtNDBlYS05ODQyLTZkMjFkYTMzNDhjNw.png", true), msg), true);
            }
        }
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.d(TAG, "onSelectionChagned::page::" + page + "::totalItemsCount::" + totalItemsCount);
    }

    @Override
    public void onSelectionChanged(int count) {
        Log.d(TAG, "onSelectionChagned::count::" + count);
    }

    @Override
    public void onAddAttachments() {

    }

    @Override

    public void onDestroy() {
        super.onDestroy();
    }

    public void botSubmit(CharSequence input) {
        botSubmit(input, 300);
    }

    public void botSubmit(final CharSequence input, long delay) {
        ChatUser chatUserTo = new ChatUser("application", "application", "Android");
        final ChatUser chatUserFrom = new ChatUser("safetymanager", "safetymanager", "manager");
        ChatMsg chatMsg = new ChatMsg(chatUserFrom, chatUserTo, input.toString());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(projection[1], chatUserTo.getId());
        values.put(projection[2], chatUserFrom.getId());
        values.put(projection[3], input.toString());
        values.put(projection[4], new Date().getTime());
        final long newRowId = db.insert(ChatListModel.ChatListEntry.TABLE_NAME, null, values);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messagesAdapter.addToStart(new Message(String.valueOf(newRowId),
                        new User(chatUserFrom.getId(), chatUserFrom.getName(), "https://media-exp2.licdn.com/mpr/mpr/shrink_200_200/AAIA_wDGAAAAAQAAAAAAAAzLAAAAJGQ1ZGFiYzgwLTRjOWMtNDBlYS05ODQyLTZkMjFkYTMzNDhjNw.png", true), input.toString()), true);
            }
        }, delay);

    }

    @Override
    public boolean onSubmit(CharSequence input) {
        try {
            boolean isFromVoice = false;
            if (input.toString().startsWith("!Voice:")) {
                isFromVoice = true;
                input = input.toString().substring("!Voice:".length());
            }
            ChatUser chatUserFrom = new ChatUser("application", "application", "Android");
            ChatUser chatUserTo = new ChatUser("safetymanager", "safetymanager", "manager");
            ChatMsg chatMsg = new ChatMsg(chatUserFrom, chatUserTo, input.toString());

            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(projection[1], chatUserTo.getId());
            values.put(projection[2], chatUserFrom.getId());
            values.put(projection[3], input.toString());
            values.put(projection[4], new Date().getTime());

            long newRowId = db.insert(ChatListModel.ChatListEntry.TABLE_NAME, null, values);

            String uuid = GetDevicesUUID(getContext());
            String msg = "{\"from\":\"" + uuid + "\",\"to\":\"server\",\"msg\":\"" + input.toString() + "\"}";

            //boolean isSuccess = busanMqttConnect.publicMessageWithDevice("iot-2/cmd/chat/fmt/json", msg);
            //Log.d(TAG, "sent::" + isSuccess + "::" + msg);
            ArrayList<String> part = new ArrayList<>();
            part.add("application");
            part.add("safetymanager");
            Chat chat = new Chat(part, chatMsg, new Date().getTime());
            SendChatMessage sendChatMessage = new SendChatMessage();
//        sendChatMessage.execute(chat);

            messagesAdapter.addToStart(new Message(String.valueOf(newRowId),
                    new User(chatUserFrom.getId(), chatUserFrom.getName(), "https://media-exp2.licdn.com/mpr/mpr/shrink_200_200/AAIA_wDGAAAAAQAAAAAAAAzLAAAAJGQ1ZGFiYzgwLTRjOWMtNDBlYS05ODQyLTZkMjFkYTMzNDhjNw.png", true), input.toString()), true);

            String inp = input.toString();
            if (!manualMode && (inp.contains("안전 매뉴얼") || inp.contains("안전매뉴얼"))) {
                botSubmit("어떤 작업이신가요?");
                manualMode = true;
                if (isFromVoice)
                    voiceStart();
                return true;
            } else if (!manualMode) {
                botSubmit("다시한번 말씀해주시겠어요?");
                if (isFromVoice)
                    voiceStart();
            }
            if (manualMode) {
                manualMode = false;
                botSubmit("네 알려드리겠습니다.");
                new ChatBotTask().execute(input.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return true;
    }

    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }

    private String GetDevicesUUID(Context mContext) {
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

    private class SendChatMessage extends AsyncTask<Chat, Void, Chat> {
        @Override
        protected Chat doInBackground(Chat... chats) {
            BusanChatApiClient busan_api = BusanChatApiClient.retrofit.create(BusanChatApiClient.class);
            String gson = new Gson().toJson(chats[0]);
            Call<Chat> call = busan_api.postChatMessage(chats[0]);
            Log.d(TAG, gson);

            try {
                Log.d(TAG, "sent chat::" + chats[0]);
                Response<Chat> response = call.execute();
                Log.d(TAG, response.toString());
                Log.d(TAG, response.raw().toString());
                Log.d(TAG, String.valueOf(response.code()));
                return response.body();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Chat chat) {
            super.onPostExecute(chat);

            // 메시지 전송 성공 여부를 판단하기
            Log.d(TAG, "send chat::" + chat);

        }

    }

    private class GetChatMessage extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            BusanChatApiClient busan_api = BusanChatApiClient.retrofit.create(BusanChatApiClient.class);
            WorkMeta workMeta = new WorkMeta(33.857317, 129.22226, "worker", true, "족장", 150, 1505, 1.5, 0);
            Worker worker = new Worker("application", "test", "Android", "regular", workMeta, true, "YYYYMMDDHHMMSS");

            try {
                busan_api.postWorker(worker).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ChatBotTask extends AsyncTask<String, ChatManual, ChatManual> {

        @Override
        protected ChatManual doInBackground(String... strings) {
            if (strings.length == 0) return null;
            ChatManual chatManual = null;
            BusanChatApiClient busan_api = BusanChatApiClient.retrofit.create(BusanChatApiClient.class);
            try {
                Response<ChatManual> response = busan_api.getManual(strings[0]).execute();
                chatManual = response.body();

                return chatManual;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ChatManual chatManual) {
            if (chatManual == null) {
                botSubmit("매뉴얼을 가져오는데 문제가 발생하였습니다");
                return;
            }
            ArrayList<String> list = new ArrayList<>();
            try {
                list.add(chatManual.getResult().getName() + " 위험 요소입니다.");
                String s = "";
                for (String a : chatManual.getResult().getDanger()) {
                    if (!s.isEmpty()) s = s + "\n";
                    s += "●" + a;
                }
                list.add(s);
                list.add("안전을 위한 방법을 안내해드리겠습니다.");
                String ss = "";
                for (String a : chatManual.getResult().getSafety()) {
                    if (!ss.isEmpty()) ss = ss + '\n';
                    ss = "●" + a + "\n";
                }
                list.add(ss);
                try {
                    Set<String> set = ((LinkedTreeMap<String, Object>) chatManual.getResult().getAttachments()).keySet();
                    String is = "";
                    for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
                        is = it.next();
                        Log.d(TAG, "whycantudfausdf " + is);
                    }
                    String imgurl = "http://wsa-worker-safety.mybluemix.net/v1/api/work/manual/img/" + chatManual.getResult().getId() + "/" + is;
                    Log.d(TAG, imgurl);
                    list.add(imgurl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (String b : list) {
                    botSubmit(b);
                }


            } catch (Exception e) {
                botSubmit("매뉴얼을 가져오는데 문제가 발생하였습니다");
                return;
            }
            super.onPostExecute(chatManual);
        }
    }
}


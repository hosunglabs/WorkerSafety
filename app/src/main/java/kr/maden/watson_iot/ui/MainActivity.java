package kr.maden.watson_iot.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.AssistantsObj;
import kr.maden.watson_iot.object.TimeLineMessage;
import kr.maden.watson_iot.object.TodaysJob;
import kr.maden.watson_iot.object.worker.WorkMeta;
import kr.maden.watson_iot.object.worker.Worker;
import kr.maden.watson_iot.service.SensorTagService;
import kr.maden.watson_iot.ui.fragment.AssistantFragment;
import kr.maden.watson_iot.ui.fragment.ChatFragment;
import kr.maden.watson_iot.ui.fragment.ChatListFragment;
import kr.maden.watson_iot.ui.fragment.SensorTagFragment;
import kr.maden.watson_iot.ui.fragment.SettingsFragment;
import kr.maden.watson_iot.utils.mqtt.BusanMqttConnect;
import kr.maden.watson_iot.utils.trans.BusanChatApiClient;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private FragmentManager fragmentManager;
    private Fragment fragment;
    private boolean mKillFlag = false;
    private Handler mKillHandler;

    private SensorTagService mBoundService;
    private View finishWork;
    private Timer timer;
    public static TodaysJob todaysJob = null;
    public static long startTime = 15 * 1000 * 60 * 60;
    public static Worker worker = null;
    public static boolean workEnded = false;
    public static boolean startBot = false;
    public static boolean workEndNotified = false;
    public static boolean DEBUGGNIG = false;
    private Handler handler;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatListFragment.ADD_NEW_WORKER) {
            if (resultCode == RESULT_OK) {
                Worker worker = (Worker) data.getSerializableExtra("worker");
                if (worker != null) {
                    ChatListFragment chatListFragment = ChatListFragment.getInstance();
                    chatListFragment.addNewWorker(worker);
                }
            }
        } else if (requestCode == ChatFragment.BOT_VOICE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.size() > 0) {
//                        Toast.makeText(MainActivity.this, "result" + result.get(0), Toast.LENGTH_SHORT).show();
                        ChatFragment c = ChatFragment.getInstance();
                        c.onSubmit("!Voice:" + result.get(0));
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1010: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ChatFragment c = ChatFragment.getInstance();
                    c.voiceStart();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(" ");
        finishWork = findViewById(R.id.main_finish_work);
        finishWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        handler = new Handler();

//        Log.d(TAG, "onCreate: " + todaysJob.getResults().getWorklist().size());

        BusanMqttConnect busanMqttConnect = BusanMqttConnect.getInstance(this);
        if (!busanMqttConnect.isConnected())
            busanMqttConnect.connect();

        mKillHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    mKillFlag = false;
                }
            }
        };

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_frame_content, SensorTagFragment.getInstance()).commit();

        Intent intent = getIntent();
        String hosung = intent.getStringExtra("hosung");
        if (hosung != null)
            transaction.replace(R.id.main_frame_content, AssistantFragment.getInstance()).commit();
        Log.d(TAG, "Executed the transaction");


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d(TAG, String.valueOf(item.getItemId()));
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        fragment = SensorTagFragment.getInstance();
                        break;
                    case R.id.navigation_dashboard:
                        fragment = ChatFragment.getInstance();
//                        fragment = ChatListFragment.getInstance();
                        break;
                    case R.id.navigation_Assistant:
                        fragment = AssistantFragment.getInstance();
                        break;
                    case R.id.navigation_notifications:
                        fragment = SettingsFragment.getInstance();
                        break;
                }
              /*  if (item.getItemId() == R.id.navigation_home) {
                    if (finishWork.getVisibility() == View.GONE) {
                        finishWork.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (finishWork.getVisibility() == View.VISIBLE) {
                        finishWork.setVisibility(View.GONE);
                    }
                }
*/

                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_frame_content, fragment).commit();
                return true;
            }
        });

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                SensorTagFragment.time += 1000;
                SensorTagFragment.getInstance().updateTime(MainActivity.this);
                if ((!workEnded && (SensorTagFragment.time - startTime) >= (1000 * 60 * 5)) || SensorTagFragment.endingTime != -1 && SensorTagFragment.time >= SensorTagFragment.endingTime) {
                    if (workEndNotified) return;
                    workEndNotified = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            workEndNotified = false;
                        }
                    }, 1000 * 60 * 5);
                    SensorTagFragment.endingTime = -1;

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(MainActivity.this)
                                    .setSmallIcon(R.drawable.notification_icon)
                                    .setContentTitle("업무시간이 종료되었습니다")
                                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                                    .setAutoCancel(true)
                                    .setVibrate(new long[]{1000, 1000, 1000})
                                    .setLights(Color.RED, 3000, 3000)
                                    .setPriority(Notification.PRIORITY_MAX)
                                    .setContentText("오늘도 안전하게 업무를 마치셨습니까?");

                    PendingIntent resultPendingIntent = PendingIntent.getActivity(MainActivity.this, 0,
                            new Intent(MainActivity.this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(123, mBuilder.build());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SensorTagFragment.getInstance().addTimelineMessage(getLayoutInflater(), new TimeLineMessage(TimeLineMessage.MODE_MESSAGE, R.drawable.img_newinfo, "업무시간이 종료되었습니다", "오늘도 안전하게 업무를 마치셨습니까?"));
                            AssistantFragment.getInstance().addAssistantList(new AssistantsObj(AssistantFragment.ASSISTANT_MODE_DONE, new Date().getTime(), "업무시간이 종료되었습니다.\n오늘도 안전하게 업무를 마치셨습니까?"));
                        }
                    });

                }
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);

        WorkerAsActiveTask asActiveTask = new WorkerAsActiveTask();
        asActiveTask.execute();

    }

    public void setFactoryMode() {
        byte[] value = new byte[16];
        value[0] = (byte) 0x12;
        value[15] = (byte) 0x12;
        SensorTagService.writeValue(value);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("종료 알림")
                .setMessage("업무를 최종적으로 마치시겠습니까?")
                .setPositiveButton("예,종료합니다", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
                        setFactoryMode();
                        finishAndRemoveTask();
//                        finishAffinity();
                    }
                })
                .setNegativeButton("아니요!업무중입니다", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "종료하지 않습니다", Toast.LENGTH_SHORT).show();
                    }
                })
                .create().show();
    }

    protected void finishApp() {
        BusanMqttConnect busanMqttConnect = BusanMqttConnect.getInstance(this);
        busanMqttConnect.discoonect();
        finish();

    }

    public static void disableShiftMode(BottomNavigationView view) {

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        if (worker != null) {
            Log.d("workerdelete", "bb");

            WorkerAsNotActiveTask workerAsNotActiveTask = new WorkerAsNotActiveTask();
            workerAsNotActiveTask.execute();
//            WorkerDeleteTask workerDeleteTask = new WorkerDeleteTask();
//            workerDeleteTask.execute();
        }

    }

    private class WorkerAsNotActiveTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            BusanChatApiClient busan_api = BusanChatApiClient.retrofit.create(BusanChatApiClient.class);
            if (worker != null) {
                worker.setActive(false);
            }
            //  Log.d("workerr", PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("pf_watson_device_id","application"));
            try {
                busan_api.updateWorker(worker).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class WorkerAsActiveTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            BusanChatApiClient busan_api = BusanChatApiClient.retrofit.create(BusanChatApiClient.class);
            WorkMeta workMeta = new WorkMeta(33.857317, 129.22226, "worker", true, "족장", 150, 1505, 1.5, 0);
            worker = new Worker("Android", "application", "Android", "regular", workMeta, true, "YYYYMMDDHHMMSS");
//            Log.d("workerr", PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("pf_watson_device_id","application"));
            try {
                busan_api.postWorker(worker).execute();
                busan_api.updateWorker(worker).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class WorkerDeleteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            BusanChatApiClient busan_api = BusanChatApiClient.retrofit.create(BusanChatApiClient.class);
            try {
                busan_api.updateWorker(worker).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}

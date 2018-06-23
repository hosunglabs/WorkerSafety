package kr.maden.watson_iot.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.AssistantsObj;
import kr.maden.watson_iot.object.CurrentJob;
import kr.maden.watson_iot.object.ExtendedWeather;
import kr.maden.watson_iot.object.TimeLineMessage;
import kr.maden.watson_iot.object.TodaysJob;
import kr.maden.watson_iot.object.Weather;
import kr.maden.watson_iot.service.SensorTagService;
import kr.maden.watson_iot.ui.MainActivity;
import kr.maden.watson_iot.utils.comm.CommUtil;
import kr.maden.watson_iot.utils.mqtt.BusanMqttConnect;
import kr.maden.watson_iot.utils.sql.AssistantListHelper;
import kr.maden.watson_iot.utils.sql.model.AssistantListModel;
import kr.maden.watson_iot.utils.trans.WeatherApiClient;

import static kr.maden.watson_iot.service.SensorTagService.writeValue;

public class SensorTagFragment extends Fragment implements OnMapReadyCallback {
    private static String TAG = "SensorTagFragment";

    private int mInterval = 5000;
    private Handler mHandler;

    private boolean isEnabled = false;
    private static View view;
    private GoogleMap mapView;
    private ScrollableMapFragment gMapFragment;
    private Button quitButton;
    private AssistantListHelper mDbHelper;

    private TextView heartrate;
    private TextView cal;
    private TextView km;
    private TextView step;
    private TextView activityTimes;
    private TextView Degree;
    private ImageView weathers;

    private BusanMqttConnect busanMqttConnect;

    private String[] projection = {
            AssistantListModel.AssistantListEntry._ID,
            AssistantListModel.AssistantListEntry.COLUMN_NAME_TYPE,
            AssistantListModel.AssistantListEntry.COLUMN_NAME_SHORT_MSG,
            AssistantListModel.AssistantListEntry.COLUMN_NAME_DESCRIPTION
    };

    //새로운 날씨 이미지는 w_ 로 시작하는 파일들입니다  예: w_thunderstorm.png
    public static int icons[] = {
            R.drawable.icon0, R.drawable.icon1, R.drawable.icon2, R.drawable.icon3,
            R.drawable.icon4, R.drawable.icon5, R.drawable.icon6, R.drawable.icon7,
            R.drawable.icon8, R.drawable.icon9, R.drawable.icon10, R.drawable.icon11,
            R.drawable.icon12, R.drawable.icon13, R.drawable.icon14, R.drawable.icon15,
            R.drawable.icon16, R.drawable.icon17, R.drawable.icon18, R.drawable.icon19,
            R.drawable.icon20, R.drawable.icon21, R.drawable.icon22, R.drawable.icon23,
            R.drawable.icon24, R.drawable.icon25, R.drawable.icon26, R.drawable.icon27,
            R.drawable.icon28, R.drawable.icon29, R.drawable.icon30, R.drawable.icon31,
            R.drawable.icon32,
            R.drawable.icon33,
            R.drawable.icon34,
            R.drawable.icon35,
            R.drawable.icon36
            , R.drawable.icon37, R.drawable.icon38, R.drawable.icon39,
            R.drawable.icon40, R.drawable.icon41, R.drawable.icon42, R.drawable.icon43,
            R.drawable.icon44, R.drawable.icon45, R.drawable.icon46, R.drawable.icon47
    };

    private static SensorTagFragment sensorTagFragment;
    private static Chronometer timeElapsed;
    private static PowerManager.WakeLock sCpuWakeLock;
    private NestedScrollView nestedScrollView;
    private LinearLayout timelineLayout;
    private static final List<TimeLineMessage> timeLineMessages = new ArrayList<>();
    public static final List<CurrentJob> currentJobList = new ArrayList<>();
    public static boolean workEnded = false;

    static {
        //시간별 작업 더미 데이터
        try {
            // [,)
            String times[] = {"00:00", "03:00", "03:00", "06:00", "06:00", "10:00", "10:00", "15:00", "15:00", "23:00"};
            String names[] = {"선박 왼쪽 페인팅 작업", "선박 오른쪽 페인팅 작업", "선체 왼쪽 외부 도장 작업", "선체 오른쪽 외부 도장 작업", "선체 전면 안정성 검사"};
            for (int i = 0; i < times.length / 2; i++) {
                Date date1 = new SimpleDateFormat("HH:mm").parse(times[i * 2]);
                Date date2 = new SimpleDateFormat("HH:mm").parse(times[i * 2 + 1]);
                currentJobList.add(new CurrentJob(names[i], date1.getTime(), date2.getTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Timer timer;
    public static long time = 15 * 1000 * 60 * 60;
    public static long startTime = time;
    //    public static long endingTime = 15 * 1000 * 60 * 60 + 1000 * 20;
    public static long endingTime = 15 * 1000 * 60 * 60 + 30 * 1000 * 60;

    private TextView activityTime;
    private TextView currentJobTime;
    private TextView currentJobName;
    private TextView weatherInfo;
    private ImageView weatherIcon;
    private boolean toFastOK = false;
    private boolean showHeartRate = false;
    private Handler handler;


    public static SensorTagFragment getInstance() {
        if (sensorTagFragment == null)
            sensorTagFragment = new SensorTagFragment();
        return sensorTagFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (isEnabled)
                    writeValue(b018PlusRealHeart());
            } finally {

                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        registerReceiver();
        mDbHelper = new AssistantListHelper(getContext());

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }

        try {
            view = inflater.inflate(R.layout.fragment_sensor_tag, container, false);
        } catch (InflateException e) {
        }

        heartrate = (TextView) view.findViewById(R.id.heartrate);
        km = (TextView) view.findViewById(R.id.km);
        cal = (TextView) view.findViewById(R.id.cal);
        step = (TextView) view.findViewById(R.id.steps);
        weathers = (ImageView) view.findViewById(R.id.weather);
        activityTimes = (TextView) view.findViewById(R.id.activity_time);
        Degree = (TextView) view.findViewById(R.id.degree);
        activityTime = (TextView) view.findViewById(R.id.activity_time);
        currentJobTime = (TextView) view.findViewById(R.id.currentjob_time);
        currentJobName = (TextView) view.findViewById(R.id.currentjob_name);
        nestedScrollView = (NestedScrollView) view.findViewById(R.id.nestedscrollview);
        quitButton = (Button) view.findViewById(R.id.quit_work);
        weatherInfo = (TextView) view.findViewById(R.id.weather_info);
        weatherIcon = (ImageView) view.findViewById(R.id.weather_icon);

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BusanMqttConnect busanMqttConnect = BusanMqttConnect.getInstance(getContext());
                if (busanMqttConnect.isConnected())
                    busanMqttConnect.discoonect();
                getActivity().finish();
                setFactoryMode();
            }
        });
        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showHeartRate = true;
            }
        }, 1000 * 30);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Date date1 = new Date();
                try {
                    date1 = new SimpleDateFormat("HH:mm").parse(new SimpleDateFormat("HH:mm").format(date1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long curhr = time;
                TodaysJob.ResultsBean.WorklistBean worklistBean = null;
                long cnt = 0;
                try {
                    if (1000 * 60 * 3 <= (curhr - startTime) && (curhr - startTime) < (1000 * 60 * 10)) {
                        if (MainActivity.todaysJob.getResults().getWorklist().size() > 1) {
                            worklistBean = MainActivity.todaysJob.getResults().getWorklist().get(1);
                        }
                    } else {
                        for (int i = 0; i < MainActivity.todaysJob.getResults().getWorklist().size(); i++) {
                            if (cnt <= curhr) {
                                worklistBean = MainActivity.todaysJob.getResults().getWorklist().get(i);
                                break;
                            }
                            cnt += MainActivity.todaysJob.getResults().getWorklist().get(i).getHour() * 1000 * 60 * 60;
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (worklistBean != null) {
                    final TodaysJob.ResultsBean.WorklistBean finalWorklistBean = worklistBean;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentJobName.setText(finalWorklistBean.getDetail());
                            currentJobTime.setText("hour : " + String.valueOf(finalWorklistBean.getHour()));
                        }
                    });
                }

               /* for (final CurrentJob c : currentJobList) {
                    if (c.getStartTime() <= date1.getTime() && date1.getTime() < c.getEndTime()) {
                        final String ss = new SimpleDateFormat("HH:mm").format(new Date(c.getStartTime())) + " - "
                                + new SimpleDateFormat("HH:mm").format(new Date(c.getEndTime()));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentJobName.setText(c.getJobName());
                                currentJobTime.setText(ss);
                            }
                        });
                        break;
                    }
                }*/
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);

        ImageView imageView = (ImageView) view.findViewById(R.id.sensor_tag_stop);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusanMqttConnect busanMqttConnect = BusanMqttConnect.getInstance(getContext());
                if (busanMqttConnect.isConnected())
                    busanMqttConnect.discoonect();
                getActivity().onBackPressed();
//                getActivity().finish();
//                setFactoryMode();
            }
        });

        timelineLayout = (LinearLayout) view.findViewById(R.id.timeline_adding_view);
        updateTimeLineView(inflater);

        AssistantFragment fragment = AssistantFragment.getInstance();
        //TODO 심장 박동 빠를시 값

        Log.d(TAG, "tracking::1");


        if (timeElapsed == null) {
            timeElapsed = (Chronometer) view.findViewById(R.id.chronometer);
            timeElapsed.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer cArg) {
                    long time = SystemClock.elapsedRealtime() - cArg.getBase();
                    int h = (int) (time / 3600000);
                    int m = (int) (time - h * 3600000) / 60000;
                    int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                    String hh = h < 10 ? "0" + h : h + "";
                    String mm = m < 10 ? "0" + m : m + "";
                    String ss = s < 10 ? "0" + s : s + "";
                    if (m == 1 && (s >= 0 && s <= 3)) {
                        Log.d(TAG, "start");

                        writeValue(enableHeart(true));
                        isEnabled = true;

                    }
                    if (m == 1.5 && (s >= 0 && s <= 3)) {
                        Log.d(TAG, "stop");

                        writeValue(enableHeart(false));
                        isEnabled = false;
                    }
                    isEnabled = true;

                    if (!isEnabled == true) {
                        byte[] value = new byte[16];
                        value[0] = (byte) 0x48;
                        value[15] = (byte) 0x48;
                        writeValue(value);
                    }

                    cArg.setText(hh + ":" + mm + ":" + ss);

                }


            });
            timeElapsed.setBase(SystemClock.elapsedRealtime());
            timeElapsed.start();

        }


        gMapFragment = (ScrollableMapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        gMapFragment.setListener(new ScrollableMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                nestedScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });
        gMapFragment.getMapAsync(this);


        try {
            getWeather getWeather1 = new getWeather();
            getWeather1.execute();

            getExtendedWeather getExtendedWeather1 = new getExtendedWeather();
            getExtendedWeather1.execute();
        } catch (Exception ex) {
            Log.e(TAG, "hi", ex);
        }

        Log.d(TAG, "tracking::3");

        busanMqttConnect = BusanMqttConnect.getInstance(getContext());

        mHandler = new Handler();
        startRepeatingTask();
        Log.d(TAG, "tracking::4");

        byte[] value = new byte[16];
        value[0] = (byte) 0x48;
        value[15] = (byte) 0x48;

        writeValue(value);
        Log.d(TAG, "tracking::returned");
        return view;
    }

    public byte[] enableHeart(boolean isEnable) {
        byte[] value = new byte[16];

        value[0] = 0x19;
        if (isEnable) {
            value[1] = 1;
        }
        value[15] = getCrcValue(value);
        return value;
    }

    public void startRealHeartRate() {
        SensorTagService.writeValue(enableHeart(true));
    }

    public void startRealTimeMeterMode() {
        byte[] value = new byte[16];
        value[0] = (byte) 0x09;
        value[15] = (byte) 0x09;
        SensorTagService.writeValue(value);
    }

    public void setFactoryMode() {
        byte[] value = new byte[16];
        value[0] = (byte) 0x12;
        value[15] = (byte) 0x12;
        SensorTagService.writeValue(value);
    }


    public static byte getCrcValue(byte[] value) {
        byte crc = 0;
        for (int i = 0; i < 15; i++) {
            crc += value[i];
        }
        return (byte) (crc & 0xff);
    }

    public static byte[] b018PlusRealHeart() {
        byte[] value = new byte[16];
        value[0] = 0x2c;
        value[1] = 1;
        value[15] = getCrcValue(value);
        return value;
    }


    public byte[] getBaseHeartValue() {
        byte[] value = new byte[16];
        value[0] = 0x6d;
        value[15] = getCrcValue(value);
        return value;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        final FragmentManager fragManager = this.getFragmentManager();
        final Fragment fragment = fragManager.findFragmentById(R.id.map);
        if (fragment != null) {
            fragManager.beginTransaction().remove(fragment).commit();
        }
    }


    private void registerReceiver() {
        IntentFilter temperatureFilter = new IntentFilter(SensorTagService.ACTION_JSTYLE_DATA_AVAILABLE);
        GattReceiver receiver = new GattReceiver();
        getContext().registerReceiver(receiver, temperatureFilter);
        Log.d(TAG, "Executed the registerReceiver");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        stopRepeatingTask();
    }

    public class GattReceiver extends BroadcastReceiver {
        public GattReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, action);
            String str = new String();
            str = "com.jstyle.ble.service.ACTION_DATA_AVAILABLE";
            try {
                if (SensorTagService.ACTION_JSTYLE_DATA_AVAILABLE.equals(action)) {
                    resloveData(intent.getByteArrayExtra("result"));
                } else if (str.equals(action)) {
                    resloveData(intent.getByteArrayExtra("result"));
                } else if (SensorTagService.ACTION_GATT_CONNECTED.equals(action)) {
                    resloveData(intent.getByteArrayExtra("result"));
                }
            } catch (Exception e) {
                Log.e(TAG, "onReceive", e);
            }
        }
    }

    private void resloveData(byte[] value) {
        if (value == null)
            return;

        Log.d(TAG, "result::resolveData::" + CommUtil.byte2Hex(value));

        if (value[0] == (byte) 0x48) {

            int steps = getValue(value[1]) + getHighValue(value[2])
                    + getLowValue(value[3]);
            int cal = getValue(value[7]) + getHighValue(value[8])
                    + getLowValue(value[9]);
            int km = getValue(value[10]) + getHighValue(value[11])
                    + getLowValue(value[12]);
            int activityTime = getHighValue(value[13]) + getLowValue(value[14]);


            Log.d(TAG, "steps::" + steps);
            Log.d(TAG, "km::" + km);
            Log.d(TAG, "cal::" + cal);
            Log.d(TAG, "activityTime::" + activityTime);

            if (busanMqttConnect.isConnected()) {
                busanMqttConnect.publicMessage("iot-2/evt/data/fmt/json", "{ \"steps\":" + steps + ", \"km\":" + km + ", \"cal\": " + cal + ", \"activityTime\":" + activityTime + "}");
            }

            this.step.setText(Integer.toString(steps) + "");
            this.cal.setText(Integer.toString(cal) + "");
            this.km.setText(Integer.toString(km) + "");
            this.activityTimes.setText(Integer.toString(activityTime) + "");

        } else if (value[0] == (byte) 0x2c) {

            if (!showHeartRate) {
                this.heartrate.setText("측정중");
                return;
            }

            this.heartrate.setText(Integer.toString(getLowValue(value[1])) + " BPM");

            int heart = getLowValue(value[1]);
            if (busanMqttConnect.isConnected()) {
                busanMqttConnect.publicMessage("iot-2/evt/data/fmt/json", "{ \"heart\":" + heart + "}");
            }


            if (heart <= 50 || heart >= 150) {
                if (toFastOK)
                    return;

                toFastOK = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toFastOK = false;
                    }
                }, 1000 * 60);


                AssistantFragment fragment = AssistantFragment.getInstance();
                //TODO 심장 박동 빠를시 값


                AssistantsObj assistantsObj = new AssistantsObj(AssistantFragment.ASSISTANT_MODE_HEARTRATE, new Date().getTime(), heart);
                fragment.addAssistantList(assistantsObj);
                SensorTagFragment.getInstance().addTimelineMessage(getActivity().getLayoutInflater(), new TimeLineMessage(TimeLineMessage.MODE_HEARTRATE, R.drawable.ic_error_outline_black_24dp, "심장박동수가 너무 높습니다", heart + " BPM"));

                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(projection[1], "현재 심장 박동수 : ");
                values.put(projection[2], "심장 박동에 이상이 있습니다!");
                values.put(projection[3], String.valueOf(heart) + " 심박수");

                long newRowId = db.insert(AssistantListModel.AssistantListEntry.TABLE_NAME, null, values);
                Log.d(TAG, newRowId + "ㄹㅇㄹㅁㄴ");

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("경고!!")
                                .setContentInfo("safetymanager")
                                .setVisibility(Notification.VISIBILITY_PUBLIC)
                                .setAutoCancel(true)
                                .setVibrate(new long[]{1000, 1000, 1000})
                                .setLights(Color.RED, 3000, 3000)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setContentText("현재 심장 박동 수에 이상이 있습니다.");


                PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(), 0,
                        new Intent(getContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(123, mBuilder.build());

/*
                Intent resultIntent = new Intent(getContext(), MainActivity.class);
                resultIntent.putExtra("hosung", "kim");


                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());

                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                mBuilder.addAction(android.R.drawable.star_on, "확인", resultPendingIntent);
                mBuilder.addAction(android.R.drawable.star_off, "취소", resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(123, mBuilder.build());*/
                if (sCpuWakeLock != null) {
                    return;
                }
                PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                sCpuWakeLock = pm.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.ON_AFTER_RELEASE, "hi");

                sCpuWakeLock.acquire();


                if (sCpuWakeLock != null) {
                    sCpuWakeLock.release();
                    sCpuWakeLock = null;
                }

            }

        } else if (value[0] == (byte) 0x69) {
            this.heartrate.setText(Integer.toString(getLowValue(value[1])) + " BPM");
        }
    }


    public int getValue(byte b) {
        return (b & 0xff) * 256 * 256;
    }

    public int getLowValue(byte b) {
        return b & 0xff;
    }

    public int getHighValue(byte b) {
        return (b & 0xff) * 256;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        Log.d(TAG, "Executed google Maps");

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mapView.setMyLocationEnabled(true);

            LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            Criteria mCriteria = new Criteria();
            String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));
            Location mLocation = manager.getLastKnownLocation(bestProvider);
            if (mLocation != null) {
                Log.e("TAG", "GPS is on");
                final double currentLatitude = mLocation.getLatitude();
                final double currentLongitude = mLocation.getLongitude();
                LatLng loc1 = new LatLng(currentLatitude, currentLongitude);
                mapView.addMarker(new MarkerOptions().position(loc1).title("Your Current Location"));
                mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 15));
                mapView.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
            }


        }
    }

    public void addViewToTimeLine(LayoutInflater inflater, TimeLineMessage timeLineMessage) {
        View view = inflater.inflate(R.layout.sensor_tag_integrated_item, null);
        ((TextView) view.findViewById(R.id.timeline_title)).setText(timeLineMessage.getTitle());
        ((TextView) view.findViewById(R.id.timeline_content)).setText(timeLineMessage.getContent());
        ((ImageView) view.findViewById(R.id.timeline_icon)).setImageDrawable(getResources().getDrawable(timeLineMessage.getImageId()));
        if (timelineLayout != null)
            timelineLayout.addView(view);
    }

    //여기를 통해 타임라인을 추가할수 있습니다.
    public void addTimelineMessage(LayoutInflater layoutInflater, TimeLineMessage timeLineMessage) {
        for (int i = 0; i < timeLineMessages.size(); i++) {
            if (timeLineMessages.get(i).getMode() == timeLineMessage.getMode()) {
                timeLineMessages.remove(i);
                i--;
            }
        }
        timeLineMessages.add(timeLineMessage);
        if (layoutInflater != null && isAdded())
            updateTimeLineView(layoutInflater);
    }


    private void updateTimeLineView(LayoutInflater inflater) {
        if (timelineLayout != null) {
            timelineLayout.removeAllViews();
            for (TimeLineMessage timeLineMessage : timeLineMessages) {
                addViewToTimeLine(inflater, timeLineMessage);
            }
        }
    }


    public void updateTime(Activity activity) {
        Date date = new Date(time);
        final String s = new SimpleDateFormat("HH:mm:ss").format(date);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activityTime != null)
                    activityTime.setText(s);
            }
        });
    }

    private class getWeather extends AsyncTask<Void, Void, Weather> {
        @Override
        protected Weather doInBackground(Void... voids) {
            //TODO 위치 가변으로
            WeatherApiClient busanApiClient = WeatherApiClient.retrofit.create(WeatherApiClient.class);
            try {
                double lat = 36, lng = 127;
                try {
                    LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                    Criteria mCriteria = new Criteria();
                    String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));
                    @SuppressLint("MissingPermission") Location mLocation = manager.getLastKnownLocation(bestProvider);
                    lat = mLocation.getLatitude();
                    lng = mLocation.getLongitude();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Weather weather = busanApiClient.getWeather(String.valueOf(lat), String.valueOf(lng)).execute().body();
                return weather;
            } catch (IOException e) {
                Log.e(TAG, "testDataTrans", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            try {
                weatherIcon.setImageResource(icons[weather.getIcon_code()]);
                weathers.setImageResource(icons[weather.getIcon_code()]);
            } catch (Exception ex) {
            }
        }
    }

    private class getExtendedWeather extends AsyncTask<Void, Void, ExtendedWeather> {
        @Override
        protected ExtendedWeather doInBackground(Void... voids) {

            WeatherApiClient busanApiClient = WeatherApiClient.retrofit.create(WeatherApiClient.class);
            try {
                double lat = 36, lng = 127;
                try {
                    LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                    Criteria mCriteria = new Criteria();
                    String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));
                    @SuppressLint("MissingPermission") Location mLocation = manager.getLastKnownLocation(bestProvider);
                    lat = mLocation.getLatitude();
                    lng = mLocation.getLongitude();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                List<ExtendedWeather> weather = busanApiClient.getExtendedWeather(String.valueOf(lat), String.valueOf(lng)).execute().body();


                return weather.get(0);
            } catch (IOException e) {
                Log.e(TAG, "testDataTrans", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ExtendedWeather weather) {
            Degree.setText(Integer.toString(weather.getTemp()) + "'c");
            weatherInfo.setText(weather.getPhrase_32char());
        }
    }
}
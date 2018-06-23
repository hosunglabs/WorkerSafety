package kr.maden.watson_iot.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Objects;

import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.BluetoothDeviceCustom;
import kr.maden.watson_iot.service.SensorTagService;

public class DeviceSearchActivity extends AppCompatActivity {
    private static final String TAG = DeviceSearchActivity.class.getSimpleName();
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    private boolean mScanning;
    private boolean mScanningS;

    private Handler mHandler;

    private Button button;
    private Button button1;

    private static SensorTagService mBoundService;
    private boolean mIsConnected = false;
    private boolean mIsBound = false;
    public static final String BUNDLE_KEY_IS_CONNECTED = "connected";

    private ListView listView;
    private AVLoadingIndicatorView indicatorView;
    private ImageView connecting;
    private View success;
    private boolean started = false;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mLeDeviceListAdapter.addDevice(new BluetoothDeviceCustom(device, rssi));
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        listView = (ListView) findViewById(R.id.ble_lists);
        button = (Button) findViewById(R.id.scanorstop_btn);
        connecting = (ImageView) findViewById(R.id.search_connecting);
        success = findViewById(R.id.search_success);

        indicatorView = (AVLoadingIndicatorView) findViewById(R.id.avi);
        if (indicatorView.getVisibility() == View.VISIBLE)
            indicatorView.setVisibility(View.INVISIBLE);

        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter(getApplicationContext());

        listView.setAdapter(mLeDeviceListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDeviceCustom bluetoothDeviceCustom = (BluetoothDeviceCustom) adapterView.getAdapter().getItem(i);
                connecting.setVisibility(View.VISIBLE);
                if (mBoundService == null)
                    mBoundService = DeviceSearchActivity.getBountService();

                mBoundService.connectLeDevice(bluetoothDeviceCustom.getBluetoothDevice());

            }
        });

        mScanningS = false;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MainActivity.DEBUGGNIG) {
                    Intent mIntent = new Intent(getApplicationContext(), TodayJobActivity.class);
                    startActivity(mIntent);
                    return;
                }


                if (mScanningS) {
                    button.setText("내 디바이스 찾기");
                    if (indicatorView.getVisibility() == View.VISIBLE)
                        indicatorView.setVisibility(View.INVISIBLE);
                    if (connecting.getVisibility() != View.INVISIBLE) {
                        connecting.setVisibility(View.INVISIBLE);
                    }
                    scanLeDevice(false);
                    mScanningS = false;
                } else {
                    button.setText("중지");
                    indicatorView.smoothToShow();
                    if (indicatorView.getVisibility() == View.INVISIBLE)
                        indicatorView.setVisibility(View.VISIBLE);
                    scanLeDevice(true);
                    mScanningS = true;
                }
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

        if (mIsBound) {
            doUnbindService();
            mIsBound = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        doBindService();
        registerReceiver();
    }

    public class GattReceiver extends BroadcastReceiver {
        public GattReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (connecting.getVisibility() == View.VISIBLE) {
                connecting.setVisibility(View.INVISIBLE);
            }
            if (SensorTagService.ACTION_GATT_CONNECTED.equals(action)) {
                mIsConnected = true;
                if (success.getVisibility() == View.INVISIBLE)
                    success.setVisibility(View.VISIBLE);
//                Toast.makeText(getApplicationContext(), "정상적으로 BLE에 연결했습니다.", Toast.LENGTH_SHORT).show();
            } else if (SensorTagService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mIsConnected = false;
//                Toast.makeText(context, "연결 실패", Toast.LENGTH_SHORT).show();
                if (indicatorView.getVisibility() == View.VISIBLE)
                    indicatorView.setVisibility(View.INVISIBLE);
            } else if (SensorTagService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (!started) {
                    started = true;
                    Intent mIntent = new Intent(getApplicationContext(), TodayJobActivity.class);
//                Intent mIntent = new Intent(getApplicationContext(), TodayisActivity.class);
//                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(mIntent);
                    finish();
                }
            }
        }
    }


    public static SensorTagService getBountService() {
        return mBoundService;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            SensorTagService.LocalBinder binder = (SensorTagService.LocalBinder) service;
            mBoundService = binder.getService();
            Log.d(TAG, mBoundService.toString());
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            mIsBound = false;
        }
    };

    void doBindService() {
        Intent intent = new Intent(getApplicationContext(), SensorTagService.class);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Executed the doBindService");
    }

    void doUnbindService() {
        if (mIsBound) {
            getApplicationContext().unbindService(mConnection);
            mIsBound = false;
            Log.d(TAG, "Executed the doUnbindService");
        }
    }

    //private

    private void registerReceiver() {
        IntentFilter temperatureFilter = new IntentFilter(SensorTagService.ACTION_GATT_CONNECTED);
        temperatureFilter.addAction(SensorTagService.ACTION_GATT_DISCONNECTED);
        temperatureFilter.addAction(SensorTagService.ACTION_GATT_SERVICES_DISCOVERED);
        GattReceiver receiver = new GattReceiver();
        getApplicationContext().registerReceiver(receiver, temperatureFilter);
        Log.d(TAG, "Executed the registerReceiver");

    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDeviceCustom> mLeDevices;
        private LayoutInflater inflater;

        public LeDeviceListAdapter(Context context) {
            super();
            mLeDevices = new ArrayList<>();
            this.inflater = LayoutInflater.from(context);
        }

        public void setOrder() {
            for (int i = 0; i < mLeDevices.size(); i++) {
                if (Objects.equals(mLeDevices.get(i).getBluetoothDevice().getAddress(), "D0:6B:26:5C:E4:74")) {
                    BluetoothDeviceCustom bluetoothDeviceCustom = mLeDevices.get(i);
                    mLeDevices.remove(bluetoothDeviceCustom);
                    mLeDevices.add(0, bluetoothDeviceCustom);
                }
            }
        }

        public void addDevice(BluetoothDeviceCustom device) {
            boolean isUnique = true;

            for (BluetoothDeviceCustom bluetoothDeviceCustom : mLeDevices) {
                if (bluetoothDeviceCustom.getBluetoothDevice().equals(device.getBluetoothDevice())) {
                    isUnique = false;
                }
            }

            if (isUnique) {
                Log.d(TAG, "addDevice: " + device.getBluetoothDevice().getAddress());
                if (device.getBluetoothDevice() != null && (device.getBluetoothDevice().getAddress().equals("D0:6B:26:5C:E4:75") ||
                        device.getBluetoothDevice().getAddress().equals("CE:3A:EC:0A:84:29"))) {
                    mLeDevices.add(device);
                }
            }
//            setOrder();
        }

        public BluetoothDeviceCustom getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = inflater.inflate(R.layout.fragment_ble_list_item, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.rssi = (ImageView) view.findViewById(R.id.device_rssi);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDeviceCustom deviceCustom = mLeDevices.get(i);
            BluetoothDevice device = deviceCustom.getBluetoothDevice();
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            if (deviceCustom.getRssi() != -1000) {
                final int rssiPercent = (int) (100.0f * (127.0f + deviceCustom.getRssi()) / (127.0f + 20.0f));
                viewHolder.rssi.setImageLevel(rssiPercent);
                viewHolder.rssi.setVisibility(View.VISIBLE);
            } else {
                viewHolder.rssi.setVisibility(View.GONE);
            }

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        ImageView rssi;
    }
}

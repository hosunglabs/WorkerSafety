package kr.maden.watson_iot.ui.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import kr.maden.watson_iot.R;
import kr.maden.watson_iot.object.BluetoothDeviceCustom;

import java.util.ArrayList;

import static com.google.android.gms.internal.zzagz.runOnUiThread;

public class BleDialogFragment extends DialogFragment {
    private static final String TAG = BleDialogFragment.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private OnLeConnectionRequestHandler mCallback;
    private boolean mScanning;
    private Handler mHandler;
    private Button scan_btn;
    private ListView scan_list;
    private static final long SCAN_PERIOD = 10000;

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


    public interface OnLeConnectionRequestHandler {
        void onLeDeviceConnectionRequest(BluetoothDevice device);
    }



    public BleDialogFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnLeConnectionRequestHandler) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLeConnectionRequestHandler.");
        }
    }

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ble_dialog, container);
        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter(getContext());

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        scan_list = (ListView) view.findViewById(R.id.ble_list_view);
        scan_list.setAdapter(mLeDeviceListAdapter);
        scan_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice bluetoothDevice = ((BluetoothDeviceCustom) mLeDeviceListAdapter.getItem(i)).getBluetoothDevice();

                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());
                mCallback.onLeDeviceConnectionRequest(device);
                dismiss();
            }
        });



        getDialog().setTitle(getString(R.string.dialog_title_configuration));

        scan_btn = (Button) view.findViewById(R.id.action_cancel);
        scan_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//
                if(mScanning){
                    mLeDeviceListAdapter.clear();
                    scan_btn.setText(R.string.scanner_action_scan);
                    scanLeDevice(false);
                } else {
                    scan_btn.setText(R.string.scanner_action_stop);
                    scanLeDevice(true);
                }

            }
        });
        return view;
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDeviceCustom> mLeDevices;
        private LayoutInflater inflater;

        public LeDeviceListAdapter(Context context) {
            super();
            mLeDevices = new ArrayList<BluetoothDeviceCustom>();
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addDevice(BluetoothDeviceCustom device) {
            boolean isUnique = true;

            for(BluetoothDeviceCustom bluetoothDeviceCustom : mLeDevices){
                if(bluetoothDeviceCustom.getBluetoothDevice().equals(device.getBluetoothDevice())){
                    isUnique = false;
                }
            }

            if(isUnique) {
                mLeDevices.add(device);
            }
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
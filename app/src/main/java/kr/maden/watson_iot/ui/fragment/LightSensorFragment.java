package kr.maden.watson_iot.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import kr.maden.watson_iot.R;
import kr.maden.watson_iot.service.SensorTagService;
import kr.maden.watson_iot.utils.mqtt.BusanMqttConnect;

public class LightSensorFragment extends SectionFragment {
    private TextView mLightValue;
    private LinearLayout mLightDetails;
    private LightSensorFragment.LightReceiver mReceiver;
    private static int sentMessageCount = 0;
    private static LightSensorFragment temperatureSensorFragment;

    public static LightSensorFragment getInstance(Bundle args) {
        if(temperatureSensorFragment==null)
            temperatureSensorFragment = new LightSensorFragment();

        temperatureSensorFragment.setArguments(args);
        return temperatureSensorFragment;
    }

    public class LightReceiver extends BroadcastReceiver {
        public LightReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("Test", action);
            if (SensorTagService.ACTION_GATT_CONNECTED.equals(action)) {
                setConnected(true);
            } else if (SensorTagService.ACTION_GATT_DISCONNECTED.equals(action)) {
                setConnected(false);
            } else if (SensorTagService.ACTION_LIGHT_DATA_AVAILABLE.equals(action)) {
                setConnected(true);
                double light = intent.getDoubleExtra(SensorTagService.DATA_LIGHT,0);
                mLightValue.setText(Double.toString(light));
                BusanMqttConnect busanMqttConnect = BusanMqttConnect.getInstance(getContext());
                busanMqttConnect.publicMessageCustom("light", "{\"light\":" + Double.toString(light) + "}");
                Log.d("test", "ds::" + light);

            }
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_light_sensor;
    }

    @Override
    protected void onCreateViewHook(View rootView) {
        mLightValue = (TextView) rootView.findViewById(R.id.light_details_ambient);
        mLightDetails = (LinearLayout) rootView.findViewById(R.id.light_details);
    }

    @Override
    protected LinearLayout getSectionLayout() {
        return mLightDetails;
    }

    @Override
    protected void registerSectionReceiver(IntentFilter filter) {
        filter.addAction(SensorTagService.ACTION_LIGHT_DATA_AVAILABLE);
        mReceiver = new LightSensorFragment.LightReceiver();
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    protected void unregisterSectionReceiver() {
        getActivity().unregisterReceiver(mReceiver);
    }
}

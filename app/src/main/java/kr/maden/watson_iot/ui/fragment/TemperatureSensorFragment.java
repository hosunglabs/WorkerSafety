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

public class TemperatureSensorFragment extends SectionFragment {
    private static TemperatureSensorFragment temperatureSensorFragment;
    private TextView mAmbientValueTextView;
    private LinearLayout mTemperatureDetails;
    private TemperatureSensorFragment.TemperatureReceiver mReceiver;

    public static TemperatureSensorFragment getInstance(Bundle args) {
        if(temperatureSensorFragment==null)
            temperatureSensorFragment = new TemperatureSensorFragment();
        temperatureSensorFragment.setArguments(args);
        return temperatureSensorFragment;
    }

    public class TemperatureReceiver extends BroadcastReceiver {
        public TemperatureReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SensorTagService.ACTION_GATT_CONNECTED.equals(action)) {
                setConnected(true);
            } else if (SensorTagService.ACTION_GATT_DISCONNECTED.equals(action)) {
                setConnected(false);
            } else if (SensorTagService.ACTION_IR_TEMPERATURE_DATA_AVAILABLE.equals(action)) {
                setConnected(true);
                double ambientTemperature = intent.getDoubleExtra(SensorTagService.DATA_IR_TEMPERATURE_AMBIENT, 0);

                mAmbientValueTextView.setText(Double.toString(ambientTemperature));

                BusanMqttConnect busanMqttConnect = BusanMqttConnect.getInstance(getContext());
                busanMqttConnect.publicMessageCustom("temp", "{\"temp\":" + Double.toString(ambientTemperature) + "}");
                Log.d("test", "ds::" + ambientTemperature);

            }
        }
    }

    ;

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_temperature_sensor;
    }

    @Override
    protected void onCreateViewHook(View rootView) {

        mAmbientValueTextView = (TextView) rootView.findViewById(R.id.temperature_details_ambient_value);
        mTemperatureDetails = (LinearLayout) rootView.findViewById(R.id.temperature_details);
    }

    @Override
    protected LinearLayout getSectionLayout() {
        return mTemperatureDetails;
    }

    @Override
    protected void registerSectionReceiver(IntentFilter filter) {
        filter.addAction(SensorTagService.ACTION_IR_TEMPERATURE_DATA_AVAILABLE);
        mReceiver = new TemperatureSensorFragment.TemperatureReceiver();
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    protected void unregisterSectionReceiver() {
        getActivity().unregisterReceiver(mReceiver);
    }
}

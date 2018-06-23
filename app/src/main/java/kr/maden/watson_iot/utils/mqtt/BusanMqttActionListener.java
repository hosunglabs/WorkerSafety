package kr.maden.watson_iot.utils.mqtt;

import android.util.Log;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class BusanMqttActionListener implements IMqttActionListener {
    private static String TAG = BusanMqttActionListener.class.getSimpleName();

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.d(TAG, "Failed to connect to: ");
        Log.e(TAG, "Failed to connect to: ", exception);

    }
}

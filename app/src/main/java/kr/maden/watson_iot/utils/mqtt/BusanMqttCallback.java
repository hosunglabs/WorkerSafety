package kr.maden.watson_iot.utils.mqtt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class BusanMqttCallback implements MqttCallbackExtended {
    private static final String TAG = BusanMqttCallback.class.getSimpleName();
    private static Context context;
    public static final String CHAT_DATA = "kr.maden.watson_iot.ie.CHAT_DATA";
    public static final String CHAT_SUBSCRIBE = "iot-2/cmd/chat/fmt/json";

    public BusanMqttCallback(Context context) {
        this.context = context;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

        if (reconnect) {
            Log.d(TAG, "Reconnected to : " + serverURI);
        } else {
            Log.d(TAG, "Connected to : " + serverURI);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "The Connection was lost.");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        Log.d(TAG, topic + "::Incoming message::" + new String(message.getPayload()));
        switch (topic) {
            case CHAT_SUBSCRIBE:
                Intent intent = new Intent(CHAT_DATA);
                intent.putExtra("message", new String(message.getPayload()));
                Log.d(TAG, "why!!");
                context.sendBroadcast(intent);
                break;
            default:
                Log.d(TAG, "알 수 없는 메시지를 받음");
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


}

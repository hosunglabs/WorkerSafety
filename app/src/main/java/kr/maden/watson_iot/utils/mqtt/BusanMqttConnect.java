package kr.maden.watson_iot.utils.mqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.net.SocketFactory;

import kr.maden.watson_iot.object.MqttAccount;
import kr.maden.watson_iot.utils.Constants;

public class BusanMqttConnect {
    private static String TAG = BusanMqttConnect.class.getSimpleName();
    private static BusanMqttConnect busanMqttConnect;
    private static MqttAndroidClient mqttAndroidClient;
    private static boolean isConnected = false;
    private static Context context;
    private static MqttAccount mqttAccount;

    public BusanMqttConnect(Context context) {
        this.context = context;
    }

    public static BusanMqttConnect getInstance(Context context) {
        if (busanMqttConnect == null)
            busanMqttConnect = new BusanMqttConnect(context);

        return busanMqttConnect;
    }

    private MqttAccount getMqttAccount() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (mqttAccount == null && context != null) {
            mqttAccount = new MqttAccount(prefs.getString("pf_watson_org", ""),
                    prefs.getString("pf_watson_device_type", ""),
                    prefs.getString("pf_watson_device_id", ""),
                    prefs.getString("pf_watson_auth_token", ""),
                    Constants.IOT_DEVICE_USERNAME,
                    prefs.getBoolean("pf_watson_use_ssl", false));
        }

        return mqttAccount;
    }

    public MqttAndroidClient getMqttAndroidClient() {
        return mqttAndroidClient;
    }

    public boolean discoonect() {
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            return false;
        }
        return true;
    }

    public boolean isConnected() {
        try {

            return mqttAndroidClient.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean connect() {
        if (mqttAndroidClient == null || mqttAndroidClient.isConnected() == false) {
            if(mqttAndroidClient!=null){
                Log.d(TAG, "IsConnected::"+mqttAndroidClient.isConnected());
            }
            String clientId, serverURL;
            SocketFactory factory = null;

            MqttAccount mqttAccount = getMqttAccount();

            try {
                clientId = "d:" + mqttAccount.getOrganization() + ":" + mqttAccount.getDeviceType() + ":" + mqttAccount.getDeviceId();
                serverURL = "tcp://" + mqttAccount.getOrganization() + Constants.IOT_ORGANIZATION_TCP;



                mqttAndroidClient = new MqttAndroidClient(context, serverURL, clientId);
                mqttAndroidClient.setCallback(new BusanMqttCallback(context));

                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setAutomaticReconnect(false);
                mqttConnectOptions.setCleanSession(false);
                mqttConnectOptions.setUserName(Constants.IOT_DEVICE_USERNAME);
                mqttConnectOptions.setPassword(mqttAccount.getUserToken().toCharArray());
                mqttConnectOptions.setSocketFactory(factory);
                mqttConnectOptions.setCleanSession(true);
                mqttAndroidClient.connect(mqttConnectOptions, null, new BusanMqttActionListener());

            } catch (MqttException e) {
                Log.e(TAG, "mqtt", e);
            } catch (Exception e) {
                Log.e(TAG, "Exception", e);
            }
        } else {
            Log.d(TAG, "Already connected to the server");
        }

        return true;
    }

    public boolean publicMessage(String topic, String rawMessage) {
        try {
            Log.d(TAG, topic + ":" + rawMessage);
            MqttMessage message = new MqttMessage(rawMessage.getBytes());
            IMqttDeliveryToken iMqttDeliveryToken = mqttAndroidClient.publish(topic, message);
            return iMqttDeliveryToken.isComplete();
        } catch (MqttException e) {
            Log.e(TAG, "mqtt", e);
        }
        return false;
    }

    public boolean publicMessageCustom(String topic, String rawMessage) {
        return publicMessage(getEventTopic(topic, "json"), rawMessage);
    }

    public boolean publicMessageWithDevice(String topic, String rawMessage){
        return publicMessage(topic, rawMessage);
    }


    private static String getEventTopic(String event, String format) {
        return "iot-2/evt/" + event + "/fmt/" + format;
    }

    private void addToHistory(String mainText) {
        Log.d(TAG, mainText);
        Toast.makeText(context, mainText, Toast.LENGTH_SHORT).show();
    }
}
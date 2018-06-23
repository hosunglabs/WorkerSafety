package kr.maden.watson_iot.service;

import android.app.Service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import kr.maden.watson_iot.utils.comm.CommUtil;
import kr.maden.watson_iot.utils.mqtt.BusanMqttConnect;

public class SensorTagService extends Service {

    private static final String TAG = SensorTagService.class.getSimpleName();


    private static BusanMqttConnect busanMqttConnect;

    public static final byte[] SERVICE_ENABLED = {0x01};
    public static final byte[] SERVICE_DISABLED = {0x00};

    public static final byte[] SERVICE_GYROSCOPE_ALL = {0x07};

    private static BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_IR_TEMPERATURE_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_IR_TEMPERATURE_DATA_AVAILABLE";
    public final static String ACTION_SIMPLE_KEYS_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_SIMPLE_KEYS_DATA_AVAILABLE";
    public final static String ACTION_LIGHT_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_IR_LIGHT_DATA_AVAILABLE";
    public final static String ACTION_JSTYLE_DATA_AVAILABLE =
            "kr.maden.watson_iot.ble.service.ACTION_JSTYLE_DATA_AVAILABLE";


    private static BluetoothGattCharacteristic dataGattCharacteristic;
    public final static String EXTRA_DATA = "com.jstyle.ble.service.EXTRA_DATA";


    public final static String DATA_IR_TEMPERATURE_AMBIENT = "com.example.bluetooth.le.DATA_IR_TEMPERATURE_AMBIENT";
    public final static String DATA_IR_TEMPERATURE_TARGET = "com.example.bluetooth.le.DATA_IR_TEMPERATURE_TARGET";
    public final static String DATA_LIGHT = "com.example.bluetooth.le.DATA_LIGHT";

    public final static UUID UUID_SERVICE_GAP = UUID.fromString("F0001800-0451-4000-B000-000000000000");
    public final static UUID UUID_SERVICE_GATT = UUID.fromString("F0001801-0451-4000-B000-000000000000");
    public final static UUID UUID_SERVICE_DEVICE_INFORMATION = UUID.fromString("F000180A-0451-4000-B000-000000000000");
    public final static UUID UUID_SERVICE_IR_TEMPERATURE = UUID.fromString("F000AA00-0451-4000-B000-000000000000");
    public final static UUID UUID_SERVICE_SIMPLE_KEYS = UUID.fromString("F000FFE0-0451-4000-B000-000000000000");
    public final static UUID UUID_SERVICE_TEST = UUID.fromString("F000AA60-0451-4000-B000-000000000000");


    public final static UUID UUID_JSTYLE_CHANGE = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_JSTYLE_NOTY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_JSTYLE_DATA = UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_JSTYLE_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");


    public final static UUID UUID_DESCRIPTOR_NOTIFICATION_CFG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public final static UUID UUID_CHAR_IR_TEMPERATURE_CONF = UUID.fromString("F000AA02-0451-4000-B000-000000000000");
    public final static UUID UUID_CHAR_IR_TEMPERATURE_DATA = UUID.fromString("F000AA01-0451-4000-B000-000000000000");

    public final static UUID UUID_CHAR_SIMPLE_KEYS_CONF = UUID.fromString("F000FFE2-0451-4000-B000-000000000000");
    public final static UUID UUID_CHAR_SIMPLE_KEYS_DATA = UUID.fromString("F000FFE1-0451-4000-B000-000000000000");
    public final static UUID LUX_SERV = UUID.fromString("f000aa70-0451-4000-b000-000000000000");
    public final static UUID LUX_DATA = UUID.fromString("f000aa71-0451-4000-b000-000000000000");
    public final static UUID LUX_CON = UUID.fromString("f000aa72-0451-4000-b000-000000000000"); // 0: disable, 1: enable
    public final static UUID LUX_PERI = UUID.fromString("f000aa73-0451-4000-b000-000000000000");

    ArrayBlockingQueue<BluetoothGattService> mServiceToSubscribe = new ArrayBlockingQueue<BluetoothGattService>(12);

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public SensorTagService getService() {
            return SensorTagService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    private void processService() {
        if (!mServiceToSubscribe.isEmpty()) {
            BluetoothGattService service = null;
            Log.d(TAG, "starting process service");
            try {
                service = mServiceToSubscribe.take();
                UUID uuid = service.getUuid();

                dataGattCharacteristic = service.getCharacteristic(UUID_JSTYLE_DATA);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                enableNotificationForService(true, mBluetoothGatt, service.getCharacteristic(UUID_JSTYLE_CHANGE));
            } catch (InterruptedException e) {
                Log.e(TAG, "process", e);
            }
        }
    }
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        List<BluetoothGattService> services = gatt.getServices();
                        for (BluetoothGattService service : services) {
                            UUID uuid = service.getUuid();
                            try {
                                if(uuid.equals(UUID_JSTYLE_SERVICE)){
                                    mServiceToSubscribe.put(service);
                                    Log.d(TAG, "Registered "+UUID_JSTYLE_SERVICE);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                        processService();
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
                    UUID characteristicUUID = characteristic.getUuid();
                    UUID serviceCharToEnable = null;
                    byte[] configValue = null;
                    if (characteristicUUID.equals(UUID_CHAR_IR_TEMPERATURE_DATA)) {
                        serviceCharToEnable = UUID_CHAR_IR_TEMPERATURE_CONF;
                    }
                    if (characteristicUUID.equals(LUX_DATA)) {
                        serviceCharToEnable = LUX_CON;
                    }
                    if (serviceCharToEnable != null) {
                        enableService(true, gatt, characteristic.getService(), serviceCharToEnable, configValue);
                    }
                    super.onDescriptorWrite(gatt, descriptor, status);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    boolean serviceEnabled = false;


                    if(BluetoothGatt.GATT_SUCCESS == status) {
                        Log.d(TAG, CommUtil.byte2Hex(characteristic.getValue()));
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    UUID characteristicUUID = characteristic.getUuid();
                    busanMqttConnect = BusanMqttConnect.getInstance(null);
                    Intent intent = new Intent(ACTION_JSTYLE_DATA_AVAILABLE);
                    Log.d(TAG, "notify" + CommUtil.byte2Hex(characteristic.getValue()));
                    intent.putExtra("result", characteristic.getValue());
                    resloveData(characteristic.getValue());
                    sendBroadcast(intent);
                }
            };

    private static Integer shortSignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, offset + 1);

        return (upperByte << 8) + lowerByte;
    }

    private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {
        Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1);

        return (upperByte << 8) + lowerByte;
    }


    private double extractAmbientTemperature(BluetoothGattCharacteristic c) {
        int offset = 2;
        return shortUnsignedAtOffset(c, offset) / 128.0;
    }

    private double extractTargetTemperature(BluetoothGattCharacteristic c, double ambient) {
        Integer twoByteValue = shortSignedAtOffset(c, 0);

        double Vobj2 = twoByteValue.doubleValue();
        Vobj2 *= 0.00000015625;

        double Tdie = ambient + 273.15;

        double S0 = 5.593E-14;
        double a1 = 1.75E-3;
        double a2 = -1.678E-5;
        double b0 = -2.94E-5;
        double b1 = -5.7E-7;
        double b2 = 4.63E-9;
        double c2 = 13.4;
        double Tref = 298.15;
        double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * Math.pow((Tdie - Tref), 2));
        double Vos = b0 + b1 * (Tdie - Tref) + b2 * Math.pow((Tdie - Tref), 2);
        double fObj = (Vobj2 - Vos) + c2 * Math.pow((Vobj2 - Vos), 2);
        double tObj = Math.pow(Math.pow(Tdie, 4) + (fObj / S), .25);

        return tObj - 273.15;
    }
    public double convert(BluetoothGattCharacteristic value) {
        int mantissa;
        int exponent;
        Integer sfloat= shortUnsignedAtOffset(value, 0);
        mantissa = sfloat & 0x0FFF;
        exponent = (sfloat >> 12) & 0xFF;
        double output;
        double magnitude = Math.pow(2.0f, exponent);
        output = (mantissa * magnitude);
        return output / 100.0f;
    }

    /**
     * Enables/disables service.
     *
     * @param enable
     * @param gatt
     * @param service
     * @param confCharacteristicUUID
     */
    private void enableService(boolean enable, BluetoothGatt gatt, BluetoothGattService service, UUID confCharacteristicUUID, byte[] configValue) {
        BluetoothGattCharacteristic confCharacteristic = service.getCharacteristic(confCharacteristicUUID);

        if (configValue == null) {
            configValue = enable ? SERVICE_ENABLED : SERVICE_DISABLED;
        }

        confCharacteristic.setValue(configValue);
        mBluetoothGatt.writeCharacteristic(confCharacteristic);
    }

    /**
     * Enables/disables notifications for a service.
     *
     * @param enable
     * @param gatt
     * @param dataCharacteristic
     */
    private void enableNotificationForService(boolean enable, BluetoothGatt gatt, BluetoothGattCharacteristic dataCharacteristic) {
        gatt.setCharacteristicNotification(dataCharacteristic, enable);
        BluetoothGattDescriptor descriptor = dataCharacteristic.getDescriptor(UUID_DESCRIPTOR_NOTIFICATION_CFG);

        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);

    }

    public static void writeValue(byte[] value) {
        if (dataGattCharacteristic == null || mBluetoothGatt == null)
            return;
         dataGattCharacteristic.setValue(value);
         mBluetoothGatt.writeCharacteristic(dataGattCharacteristic);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void connectLeDevice(BluetoothDevice device) {
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    }

    private void resloveData(byte[] value) {
        if (value == null)
            return;

        Log.d(TAG, "result::resolveData::"+ CommUtil.byte2Hex(value));

        if (value[0] == (byte) 0x48) {

            int steps = getValue(value[1]) + getHighValue(value[2])
                    + getLowValue(value[3]);
            int cal = getValue(value[7]) + getHighValue(value[8])
                    + getLowValue(value[9]);
            int km = getValue(value[10]) + getHighValue(value[11])
                    + getLowValue(value[12]);
            int activityTime = getHighValue(value[13]) + getLowValue(value[14]);

            Log.d(TAG, "steps::"+steps);
            Log.d(TAG, "km::"+km);
            Log.d(TAG, "cal::"+cal);
            Log.d(TAG, "activityTime::"+activityTime);

        }else if (value[0] == (byte) 0x69) {
            Log.d(TAG, "Heart : " + getLowValue(value[1]));

        } else if (value[0] == (byte) 0x6a) {

            Log.d(TAG, "Heart : " + getLowValue(value[1]));
        } else if (value[0] == (byte) 0x2c) {

            Log.d(TAG, "Heart : " + getLowValue(value[1]));
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

}
package kr.maden.watson_iot.object;

import android.bluetooth.BluetoothDevice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BluetoothDeviceCustom {
    private BluetoothDevice bluetoothDevice;
    private int rssi;
}

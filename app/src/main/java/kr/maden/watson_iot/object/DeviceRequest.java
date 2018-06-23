package kr.maden.watson_iot.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRequest {
    private String deviceId;
    private String deviceType;
}

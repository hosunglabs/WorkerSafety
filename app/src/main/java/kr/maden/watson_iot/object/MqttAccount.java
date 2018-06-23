package kr.maden.watson_iot.object;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqttAccount {
    private String organization;
    private String deviceType;
    private String deviceId;
    private String userToken;
    private String userName;
    private boolean useSSL;
}

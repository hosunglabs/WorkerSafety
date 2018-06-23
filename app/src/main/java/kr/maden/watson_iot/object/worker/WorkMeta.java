package kr.maden.watson_iot.object.worker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkMeta {
    private double lat;
    private double lag;
    private String device_type;
    private boolean location;
    private String work;
    private long hrm;
    private long step;
    private double distance;
    private long state;

}
/*
      "lat": 37.525205,
      "lng": 126.925367,
      "device_type": "Android",
      "location": true,
      "work": "족장",
      "hrm": 150,
      "step": 1500,
      "distance": 1.5,
      "state": 0
 */
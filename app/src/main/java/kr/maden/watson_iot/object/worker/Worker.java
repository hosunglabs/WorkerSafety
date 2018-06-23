package kr.maden.watson_iot.object.worker;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Worker implements Serializable {
    private String id;
    private String name;
    private String type;
    private String work_type;
    private WorkMeta activity;
    private Boolean active;
    private String updatetime;
}

/*
{
  "active": true,
  "id": "sample",
  "name": "sample",
  "type": "Android",
  "work_type": "regular",
  "updatetime": "YYYYMMDDHHMMSS",
  "activity": {
    "lat": 33.857317,
    "lng": 129.22226,
    "device_type": "Android",
    "location": true,
    "work": "족장",
    "hrm": 150,
    "step": 1500,
    "distance": 1.5,
    "state": 0
  }
}
    "id": "sample",
    "name": "장기숭",
    "type": "Android",
    "work_type": "regular",
    "active": true,
    "updatetime": "YYYYMMDDHHMMSS",
    "activity": {
      "lat": 37.525205,
      "lng": 126.925367,
      "device_type": "Android",
      "location": true,
      "work": "족장",
      "hrm": 150,
      "step": 1500,
      "distance": 1.5,
      "state": 0
    }
 */
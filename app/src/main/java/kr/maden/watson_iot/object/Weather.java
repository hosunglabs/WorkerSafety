package kr.maden.watson_iot.object;

import lombok.Data;

@Data
public class Weather {
    private String dow;
    private int icon_code;
    private String desc;
    private String wind;
    private String humidity;
    private String sunrise;
    private String sunset;
    private String moonrise;
    private String moonset;
}
package kr.maden.watson_iot.object;

import lombok.Data;

@Data
public class ExtendedWeather {
    private int temp;
    private int icon_code;
    private String phrase_32char;
}


//"class": "fod_short_range_hourly",
//        "expire_time_gmt": 1504850518,
//        "fcst_valid": 1504850400,
//        "fcst_valid_local": "2017-09-08T15:00:00+0900",
//        "num": 1,
//        "day_ind": "D",
//        "temp": 27,
//        "dewpt": 18,
//        "hi": 29,
//        "wc": 27,
//        "feels_like": 29,
//        "icon_extd": 3000,
//        "wxman": "wx1100",
//        "icon_code": 30,
//        "dow": "금요일",
//        "phrase_12char": "",
//        "phrase_22char": "",
//        "phrase_32char": "구름 조금",
//        "subphrase_pt1": "",
//        "subphrase_pt2": "",
//        "subphrase_pt3": "",
//        "pop": 0,
//        "precip_type": "rain",
//        "qpf": 0,
//        "snow_qpf": 0,
//        "rh": 57,
//        "wspd": 9,
//        "wdir": 294,
//        "wdir_cardinal": "서북서",
//        "gust": null,
//        "clds": 35,
//        "vis": 16,
//        "mslp": 1010.72,
//        "uv_index_raw": 4.92,
//        "uv_index": 5,
//        "uv_warning": 0,
//        "uv_desc": "보통",
//        "golf_index": 9,
//        "golf_category": "우수",
//        "severity": 1
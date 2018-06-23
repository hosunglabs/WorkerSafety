package kr.maden.watson_iot.object;

import lombok.Data;

import java.util.Map;

@Data
public class MsgObj {
    private String cmd;
    private Map<String, Object> data;
}

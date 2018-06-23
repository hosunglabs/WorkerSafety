package kr.maden.watson_iot.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    private ArrayList<String> participant;
    private ChatMsg message;
    private long timestamp;
}

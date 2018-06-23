package kr.maden.watson_iot.object;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
public class TimeLineMessage {
    public static final int MODE_MESSAGE = 1;
    public static final int MODE_HEARTRATE = 2;
    public static final int MODE_WEATHER = 3;

    private int mode;
    private int imageId;
    private String title;
    private String content;

    public TimeLineMessage(int mode, int imageId, String title, String content) {
        this.mode = mode;
        this.imageId = imageId;
        this.title = title;
        this.content = content;
    }
}

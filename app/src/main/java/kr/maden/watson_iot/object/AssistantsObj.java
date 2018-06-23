package kr.maden.watson_iot.object;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantsObj implements Serializable {
    private String content;

    private int assistMode;
    private long timeStamp;
    //심박수
    private int heartrate;
    //날씨
    private int weatherDegree;
    private String weatherContent;
    private int weather1;
    private int weather2;
    private int time2;
    private int weather3;
    private int time3;
    private int weather4;
    private int time4;

    //종료
    private String doneContent;
    //경고
    private String warningTitle;
    private String warningContent;


    public AssistantsObj(int assistMode) {
        this.assistMode = assistMode;
    }

    public AssistantsObj(int assistMode, long timeStamp, int heartrate) {
        this.assistMode = assistMode;
        this.timeStamp = timeStamp;
        this.heartrate = heartrate;
    }

    public AssistantsObj(int assistMode, long timeStamp, int weatherDegree, String weatherContent, int weather1, int weather2, int time2, int weather3, int time3, int weather4, int time4) {
        this.assistMode = assistMode;
        this.timeStamp = timeStamp;
        this.weatherDegree = weatherDegree;
        this.weatherContent = weatherContent;
        this.weather1 = weather1;
        this.weather2 = weather2;
        this.time2 = time2;
        this.weather3 = weather3;
        this.time3 = time3;
        this.weather4 = weather4;
        this.time4 = time4;
    }

    public AssistantsObj(int assistMode, long timeStamp, String doneContent) {
        this.assistMode = assistMode;
        this.timeStamp = timeStamp;
        this.doneContent = doneContent;
    }

    public AssistantsObj(int assistMode, long timeStamp, String warningTitle, String warningContent) {
        this.assistMode = assistMode;
        this.timeStamp = timeStamp;
        this.warningTitle = warningTitle;
        this.warningContent = warningContent;
    }
}

package kr.maden.watson_iot.object;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CurrentJob {
    private String jobName;
    private Long startTime;
    private Long endTime;
}

package kr.maden.watson_iot.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCheck {
    private String content;
    private boolean checked;
}

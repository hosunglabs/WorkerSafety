package kr.maden.watson_iot.object;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccidentExample implements Serializable {
    private String imageURL;
    private String date;
    private String content;
    private boolean checked = false;
    private String title;

}

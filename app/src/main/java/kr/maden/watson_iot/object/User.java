package kr.maden.watson_iot.object;

import com.stfalcon.chatkit.commons.models.IUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements IUser  {
    private String id;
    private String name;
    private String avatar;
    private boolean online;
}
package kr.maden.watson_iot.object;

import com.stfalcon.chatkit.commons.models.IUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUser implements IUser {
    private String id;
    private String name;
    private String type;

    @Override
    public String getAvatar() {
        return null;
    }
}

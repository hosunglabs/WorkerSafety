package kr.maden.watson_iot.object;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Message implements IMessage,
        MessageContentType.Image,
        MessageContentType {

    private String id;
    private String text;
    private Date createdAt;
    private User user;
    private Image image;
    private Voice voice;

    public Message(String id, User user, String text) {
        this(id, user, text, new Date());
    }

    public Message(String id, User user, String text, Date createdAt) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }


    @Override
    public String getImageUrl() {
        return text.startsWith("http") ? text : null;
    }


    public String getStatus() {
        return "Sent";
    }

    public static class Image {

        private String url;

        public Image(String url) {
            this.url = url;
        }
    }

    public static class Voice {

        private String url;
        private int duration;

        public Voice(String url, int duration) {
            this.url = url;
            this.duration = duration;
        }

        public String getUrl() {
            return url;
        }

        public int getDuration() {
            return duration;
        }
    }
}
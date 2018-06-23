package kr.maden.watson_iot.utils.comm;

public class CommUtil {

    public static String byte2Hex(byte[] data) {

        if (data != null && data.length > 0) {
            StringBuilder sb = new StringBuilder(data.length);
            for (byte tmp : data) {
                sb.append(String.format("%02X ", tmp));
            }
            return sb.toString();
        }
        return "no data";
    }
}

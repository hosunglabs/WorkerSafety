package kr.maden.watson_iot.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    /**
     * user : test_user
     * msg : Urgent Situation
     * msg_type : 0
     * alert_type : worker or safetymanager or supervisor
     * work_type : something
     * meta : {"lat":33.857317,"lag":129.22226,"device_type":"worker","device_id":"workerdevice UUID"}
     * active : true
     */

    private String user;
    private String msg;
    private int msg_type;
    private String alert_type;
    private String work_type;
    private MetaBean meta;
    private boolean active;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(int msg_type) {
        this.msg_type = msg_type;
    }

    public String getAlert_type() {
        return alert_type;
    }

    public void setAlert_type(String alert_type) {
        this.alert_type = alert_type;
    }

    public String getWork_type() {
        return work_type;
    }

    public void setWork_type(String work_type) {
        this.work_type = work_type;
    }

    public MetaBean getMeta() {
        return meta;
    }

    public void setMeta(MetaBean meta) {
        this.meta = meta;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaBean {
        /**
         * lat : 33.857317
         * lag : 129.22226
         * device_type : worker
         * device_id : workerdevice UUID
         */

        private double lat;
        private double lag;
        private String device_type;
        private String device_id;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLag() {
            return lag;
        }

        public void setLag(double lag) {
            this.lag = lag;
        }

        public String getDevice_type() {
            return device_type;
        }

        public void setDevice_type(String device_type) {
            this.device_type = device_type;
        }

        public String getDevice_id() {
            return device_id;
        }

        public void setDevice_id(String device_id) {
            this.device_id = device_id;
        }
    }
}

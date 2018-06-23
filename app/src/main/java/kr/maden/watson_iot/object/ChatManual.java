package kr.maden.watson_iot.object;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.List;

public class ChatManual {

    /**
     * status : success
     * result : {"danger":["옥내 외 작업장 통행 중 제품, 부자재, 조도 불량  등에 의한 전도 재해 위험이 있습니다."],"safety":["옥내, 외 작업장 바닥의 상태와 정돈 상태를 확인합니다.","옥내, 외 작업장의 근로자가 넘어지거나 미끄러지는 위험이 없도록 안전하고 청결한 상태를 유지하고 제품, 자재, 부재 등이 넘어지지 않도록 지지등의 안전조치를 합니다.","작업장 정리 정돈은 모든 생산 활동에 있어 꼭 필요한 안전조치 사항이며,품질과 생산성 향상에도 큰 영향을 주므로 근로자 스스로 작업장을 정리정돈하고 이를 습관화 하도록 합니다."],"attachments":{"clean.png":{"content_type":"image/png","revpos":2,"digest":"md5-v37iTzoUyH1xAlcu6a4O1w==","length":511257,"stub":true}},"id":"재료하역 매뉴얼","name":"재료하역"}
     */

    private String status;
    private String rawJSON;
    private ResultBean result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public String getRawJSON() {
        return rawJSON;
    }

    public void setRawJSON(String rawJSON) {
        this.rawJSON = rawJSON;
    }

    public static class ResultBean {
        /**
         * danger : ["옥내 외 작업장 통행 중 제품, 부자재, 조도 불량  등에 의한 전도 재해 위험이 있습니다."]
         * safety : ["옥내, 외 작업장 바닥의 상태와 정돈 상태를 확인합니다.","옥내, 외 작업장의 근로자가 넘어지거나 미끄러지는 위험이 없도록 안전하고 청결한 상태를 유지하고 제품, 자재, 부재 등이 넘어지지 않도록 지지등의 안전조치를 합니다.","작업장 정리 정돈은 모든 생산 활동에 있어 꼭 필요한 안전조치 사항이며,품질과 생산성 향상에도 큰 영향을 주므로 근로자 스스로 작업장을 정리정돈하고 이를 습관화 하도록 합니다."]
         * attachments : {"clean.png":{"content_type":"image/png","revpos":2,"digest":"md5-v37iTzoUyH1xAlcu6a4O1w==","length":511257,"stub":true}}
         * id : 재료하역 매뉴얼
         * name : 재료하역
         */

        private String id;
        private String name;
        private List<String> danger;
        private List<String> safety;
        private Object attachments;

        public Object getAttachments() {
            return attachments;
        }

        public void setAttachments(Object attachments) {
            this.attachments = attachments;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getDanger() {
            return danger;
        }

        public void setDanger(List<String> danger) {
            this.danger = danger;
        }

        public List<String> getSafety() {
            return safety;
        }

        public void setSafety(List<String> safety) {
            this.safety = safety;
        }

    }
}

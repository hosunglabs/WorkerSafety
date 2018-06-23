package kr.maden.watson_iot.object;

import java.util.List;



public class TodaysJob {


    /**
     * status : success
     * results : {"_id":"kisjang_20171210","_rev":"2-3d03d2ec2fb348fbd385c9fbcefb74a6","id":"kisjang","date":"20171210","worklist":[{"work":"강재하역","sub":"하역, 이송, 적재, 반출","detail":"000에서 강재 하역 작업","equipment":["크레인"],"location":{"name":"somewhere","lat":126,"lng":36},"hour":4},{"work":"도장","sub":"도장","detail":"000에서 도장 작업","equipment":[""],"location":{"name":"somewhere","lat":126,"lng":36},"hour":3}],"risk":["협착","추락"],"name":"장기숭"}
     */

    private ResultsBean results;


    public ResultsBean getResults() {
        return results;
    }

    public void setResults(ResultsBean results) {
        this.results = results;
    }

    public static class ResultsBean {
        /**
         * _id : kisjang_20171210
         * _rev : 2-3d03d2ec2fb348fbd385c9fbcefb74a6
         * id : kisjang
         * date : 20171210
         * worklist : [{"work":"강재하역","sub":"하역, 이송, 적재, 반출","detail":"000에서 강재 하역 작업","equipment":["크레인"],"location":{"name":"somewhere","lat":126,"lng":36},"hour":4},{"work":"도장","sub":"도장","detail":"000에서 도장 작업","equipment":[""],"location":{"name":"somewhere","lat":126,"lng":36},"hour":3}]
         * risk : ["협착","추락"]
         * name : 장기숭
         */

        private String _id;
        private String _rev;
        private String id;
        private String date;
        private String name;
        private List<WorklistBean> worklist;
        private List<String> risk;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String get_rev() {
            return _rev;
        }

        public void set_rev(String _rev) {
            this._rev = _rev;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<WorklistBean> getWorklist() {
            return worklist;
        }

        public void setWorklist(List<WorklistBean> worklist) {
            this.worklist = worklist;
        }

        public List<String> getRisk() {
            return risk;
        }

        public void setRisk(List<String> risk) {
            this.risk = risk;
        }

        public static class WorklistBean {
            /**
             * work : 강재하역
             * sub : 하역, 이송, 적재, 반출
             * detail : 000에서 강재 하역 작업
             * equipment : ["크레인"]
             * location : {"name":"somewhere","lat":126,"lng":36}
             * hour : 4
             */

            private String work;
            private String sub;
            private String detail;
            private LocationBean location;
            private int hour;
            private List<String> equipment;
            private boolean checked=false;

            public boolean isChecked() {
                return checked;
            }

            public void setChecked(boolean checked) {
                this.checked = checked;
            }

            public String getWork() {
                return work;
            }

            public void setWork(String work) {
                this.work = work;
            }

            public String getSub() {
                return sub;
            }

            public void setSub(String sub) {
                this.sub = sub;
            }

            public String getDetail() {
                return detail;
            }

            public void setDetail(String detail) {
                this.detail = detail;
            }

            public LocationBean getLocation() {
                return location;
            }

            public void setLocation(LocationBean location) {
                this.location = location;
            }

            public int getHour() {
                return hour;
            }

            public void setHour(int hour) {
                this.hour = hour;
            }

            public List<String> getEquipment() {
                return equipment;
            }

            public void setEquipment(List<String> equipment) {
                this.equipment = equipment;
            }

            public static class LocationBean {
                /**
                 * name : somewhere
                 * lat : 126
                 * lng : 36
                 */

                private String name;
                private int lat;
                private int lng;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public int getLat() {
                    return lat;
                }

                public void setLat(int lat) {
                    this.lat = lat;
                }

                public int getLng() {
                    return lng;
                }

                public void setLng(int lng) {
                    this.lng = lng;
                }
            }
        }
    }
}

package kr.maden.watson_iot.utils.trans;

import kr.maden.watson_iot.object.Alert;
import kr.maden.watson_iot.object.ContainerRes;
import kr.maden.watson_iot.object.DeviceRequest;
import kr.maden.watson_iot.object.Safety;
import kr.maden.watson_iot.object.Solve;
import kr.maden.watson_iot.object.TodaysJob;
import kr.maden.watson_iot.object.WorkTypeRsl;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface BusanApiClient {


    @PUT("/api/v1/device")
    Call<Object> putNewDevice(@Body DeviceRequest deviceRequest);

    @DELETE("/api/v1/device")
    Call<Object> deleteDevice();

    @GET("/v1/api/info/worktype")
    Call<WorkTypeRsl> getWorkType();

    @GET("/api/v1/safety")
    Call<ContainerRes> getSafetyData();

    @GET("/api/v1/convtrigger/{type}/{id}")
    Call<Object> getMqttTrigger(
            @Path("type") String type,
            @Path("id") String id
    );

    @GET("/v1/api/work/list/kisjang/20171210")
    Call<TodaysJob> getTodaysJob();

    @GET("/v1/api/work/safety/kisjang/20171210")
    Call<Safety> getSafety();

    @GET("/v1/api/work/safety/type/%EB%8F%84%EC%9E%A5")
    Call<Safety> getDojang();

    @POST("/v1/api/alert")
    Call<Alert> postAlert(@Body Alert alert);

    @POST("/v1/api/alert/solve")
    Call<Void> postSolve(@Body Solve solve);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://wsa-worker-safety.mybluemix.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}

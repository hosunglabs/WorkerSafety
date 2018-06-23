package kr.maden.watson_iot.utils.trans;

import java.util.List;

import kr.maden.watson_iot.object.Chat;
import kr.maden.watson_iot.object.ChatManual;
import kr.maden.watson_iot.object.worker.Worker;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface BusanChatApiClient {

    @POST("/v1/api/message")
    Call<Chat> postChatMessage(@Body Chat body);

    @PUT("/v1/api/worker")
    Call<Worker> putWorker(@Body Worker worker);

    @POST("/v1/api/worker")
    Call<String> postWorker(@Body Worker worker);

    @PUT("/v1/api/worker")
    Call<Void> updateWorker(@Body Worker worker);

    @POST("/v1/api/worker/active")
    Call<Worker> postWorkerActive(@Body Worker worker);

    @GET("/v1/api/worker/active")
    Call<List<Worker>> getWorkerActive();

    @GET("/v1/api/work/manual/{id}")
    Call<ChatManual> getManual(@Path("id") String id);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://wsa-worker-safety.mybluemix.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

}

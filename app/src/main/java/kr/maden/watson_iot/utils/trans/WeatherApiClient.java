package kr.maden.watson_iot.utils.trans;

import java.util.List;

import kr.maden.watson_iot.object.ExtendedWeather;
import kr.maden.watson_iot.object.Weather;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WeatherApiClient {

    @GET("/v1/api/weather/current/{lat}/{lon}")
    Call<Weather> getWeather(
            @Path("lat") String lat,
            @Path("lon") String lon
    );

    @GET("/v1/api/weather/location/{lat}/{lon}")
    Call<List<ExtendedWeather>> getExtendedWeather(
            @Path("lat") String lat,
            @Path("lon") String lon
    );


    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://wsa-worker-safety.mybluemix.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}

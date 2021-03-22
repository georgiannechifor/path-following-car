package com.example.pathfollowingcar.server;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Api  {
    String API_URL = "https://605489c4d4d9dc001726d659.mockapi.io/api/";
    String drawingEndpoint = "drawing";
    String mapsEndpoint = "maps";

    @Headers({
            "Content-type: application/json"
    })
    @POST("drawing")
    Call<DrawingDTO> postDrawing(@Body JSONObject drawingData);


    @Headers({
            "Content-type: application/json"
    })
    @POST("maps")
    Call<DrawingDTO> postMaps(@Body JSONObject drawingData);
}

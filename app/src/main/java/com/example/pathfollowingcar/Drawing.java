package com.example.pathfollowingcar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pathfollowingcar.server.Api;
import com.example.pathfollowingcar.server.DrawingDTO;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Drawing extends AppCompatActivity {
    private PaintView paintView;
    private Retrofit retrofit;
    private Api api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        PlacesClient placesClient = Places.createClient(this);

        setContentView(R.layout.activity_drawing);
        paintView = (PaintView) findViewById(R.id.paintView);
        SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        paintView.addSharedPreferences(shPref);


        Button drawingSend = findViewById(R.id.drawingSendButton);

        String option = shPref.getString("PREF_LIST", "Medium");
        setTitle("Drawing");


        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(Api.class);

        drawingSend.setOnClickListener(v -> {
            if (paintView.pointsValidated.size() > 0) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("points", paintView.stringList);
                    Call<DrawingDTO> postCall = api.postDrawing(obj);

                    postCall.enqueue(new Callback<DrawingDTO>() {
                        @Override
                        public void onResponse(Call<DrawingDTO> call, Response<DrawingDTO> response) {
                            Toast.makeText(getApplicationContext(), response.code() + " " + response.message(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<DrawingDTO> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.draw:
                return true;
            case R.id.control:
                intent = new Intent(getApplicationContext(), Controls.class);
                startActivity(intent);
                return true;
            case R.id.voice:
                intent = new Intent(getApplicationContext(), Voice.class);
                startActivity(intent);
                return true;
            case R.id.maps:
                intent = new Intent(getApplicationContext(), Maps.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
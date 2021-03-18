package com.example.pathfollowingcar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

public class Drawing extends AppCompatActivity {
    private PaintView paintView;
    private static final String MAPS_API_KEY = "AIzaSyDFOWUE0bzjxroPECBP3E1j2jd4yNbkWyY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Places.initialize(getApplicationContext(), MAPS_API_KEY);
        PlacesClient placesClient = Places.createClient(this);
        setContentView(R.layout.activity_drawing);
        paintView = (PaintView) findViewById(R.id.paintView);
        SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        paintView.addSharedPreferences(shPref);

        String option = shPref.getString("PREF_LIST", "Medium");
        setTitle("Drawing");
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
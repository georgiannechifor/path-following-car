package com.example.pathfollowingcar;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

public class Maps extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST = 1337;
    private static final int CURRENT_PLACE_AUTOCOMPLETE_REQUEST_CODE = 53;
    private static final int DESTINATION_PLACE_AUTOCOMPLETE_REQUEST_CODE = 63;
    private Marker startMarker;
    private Marker finishMarker;
    private int searchType = 0;
    private Polyline route;
    private List<LatLng> path;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ArrayList<Point> pointsFromRoute;

    //CLOUD AMQP
    private ConnectionFactory factory = new ConnectionFactory();
    private Channel channel;
    private static final String QUEUE_NAME = "PATH_FOLLOWING_CAR";


    private void setupConnectionFactory() {
        String URI = "amqps://cnuluevh:My-bNh599eJWkjPj_rPZz55nk75AEmts@cow.rmq2.cloudamqp.com/cnuluevh";
        boolean connectionMade = false;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    factory.setAutomaticRecoveryEnabled(false);
                    factory.setUri(URI);

                    Connection connection = factory.newConnection();
                    channel = connection.createChannel();

                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

                } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | TimeoutException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        t.start();
    }

    private void sendMessage(JsonElement object) {
        String message = object.toString();
        try {
            channel.basicPublish(message, QUEUE_NAME, null, message.getBytes());

            Toast.makeText(this, "Data send to server", Toast.LENGTH_LONG).show();
            Log.e("MESSAGE TO QUEUE", message + " " + Arrays.toString(message.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!isPermissionGiven()) {
            givePermission();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setupConnectionFactory();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        MaterialButton setStartPoint = findViewById(R.id.setStartPoint);
        MaterialButton setFinishPoint = findViewById(R.id.setFinishPoint);
        MaterialCardView locationCardView = findViewById(R.id.locationCardView);
        MaterialCardView sendCardView = findViewById(R.id.sendCardView);

        Button getRoute = findViewById(R.id.getRoute);

        sendCardView.setVisibility(View.GONE);

        getRoute.setOnClickListener(v -> {
            drawRouteFromLocations();

            sendCardView.setAlpha(0f);
            sendCardView.setVisibility(View.VISIBLE);

            sendCardView.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null);

            locationCardView.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            locationCardView.setVisibility(View.GONE);
                        }
                    });

        });

        setStartPoint.setOnClickListener(v -> {
            searchType = 1;
            givePermission();
        });

        setFinishPoint.setOnClickListener(v -> {
            searchType = 2;
            givePermission();
        });

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            sendCardView.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            sendCardView.setVisibility(View.GONE);
                        }
                    });


            locationCardView.setAlpha(0f);
            locationCardView.setVisibility(View.VISIBLE);

            locationCardView.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(null);
        });


        Button sendButton = findViewById(R.id.sendData);
        sendButton.setOnClickListener(v -> {
            JsonElement object = new Gson().toJsonTree(pointsFromRoute);
            sendMessage(object);
        });
    }

    private boolean isPermissionGiven() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void givePermission() {
        System.out.println(Arrays.toString(LOCATION_PERMISSIONS));
        requestPermissions(LOCATION_PERMISSIONS, INITIAL_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length > 0) {
            if (searchType == 0) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
                if (!Places.isInitialized()) {
                    Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
                }

                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);

                if (searchType == 1) {
                    startActivityForResult(intent, CURRENT_PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } else {
                    startActivityForResult(intent, DESTINATION_PLACE_AUTOCOMPLETE_REQUEST_CODE);
                }

            }
        } else {
            Toast.makeText(this, "Permission required for showing location", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setupGoogleMapScreenSettings(googleMap);

        mMap = googleMap;
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            getCurrentLocation();
        } else {
            requestPermissions(LOCATION_PERMISSIONS, INITIAL_REQUEST);
        }
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 100);
        locationRequest.setFastestInterval(2000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest request = builder.build();

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(request);
        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult();
                if (!response.getLocationSettingsStates().isLocationPresent()) {
                    getLastLocation();
                }
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location mLastLocation = task.getResult();
                    setStartLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), "");
                } else {
                    Toast.makeText(this, "No current location found", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            requestPermissions(LOCATION_PERMISSIONS, INITIAL_REQUEST);
        }
    }

    private void setStartLocation(Double lat, Double lng, String addr) {
        String address = "Current Address";
        if (addr.isEmpty()) {
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(lat, lng, 1);
                if (!addresses.isEmpty()) {
                    address = addresses.get(0).getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            address = addr;
        }

        if (mMap != null) {
            if (startMarker != null) {
                startMarker.remove();
            }

            startMarker = mMap.addMarker(
                    new MarkerOptions().position(new LatLng(lat, lng))
                            .title("Start Location")
                            .snippet(address)
            );

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(17f).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            AppCompatTextView fromLocationTxt = findViewById(R.id.fromLocationTxt);
            fromLocationTxt.setText(String.format("From: %s", address));
        }

        if (route != null) route.remove();
    }

    private void setFinishLocation(Double lat, Double lng, String addr) {
        String address = "Destination Address";
        if (addr.isEmpty()) {
            Geocoder gcd = new Geocoder(this, Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(lat, lng, 1);
                if (!addresses.isEmpty()) {
                    address = addresses.get(0).getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            address = addr;
        }

        if (mMap != null) {
            if (finishMarker != null) {
                finishMarker.remove();
            }
            finishMarker = mMap.addMarker(
                    new MarkerOptions().position(new LatLng(lat, lng))
                            .title("Finish Location")
                            .snippet(address)
            );

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(17f).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            AppCompatTextView toLocationTxt = findViewById(R.id.toLocationTxt);
            toLocationTxt.setText(String.format("To: %s", address));
        }

        if (route != null) route.remove();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CURRENT_PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            switch (resultCode) {
                case AutocompleteActivity.RESULT_OK: {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    LatLng latLng = place.getLatLng();
                    setStartLocation(latLng.latitude, latLng.longitude, place.getName());
                    break;
                }
                case AutocompleteActivity.RESULT_ERROR: {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_LONG).show();
                    break;
                }
                case AutocompleteActivity.RESULT_CANCELED: {
                    Toast.makeText(this, "Set current place canceled", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        } else if (requestCode == DESTINATION_PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            switch (resultCode) {
                case AutocompleteActivity.RESULT_OK: {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    LatLng latLng = place.getLatLng();
                    setFinishLocation(latLng.latitude, latLng.longitude, place.getName());
                    break;
                }
                case AutocompleteActivity.RESULT_ERROR: {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    break;
                }
                case AutocompleteActivity.RESULT_CANCELED: {
                    Toast.makeText(this, "Set destination place canceled", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

    private void setupGoogleMapScreenSettings(GoogleMap mMap) {
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
    }

    private void drawRouteFromLocations() {
        if (startMarker == null || finishMarker == null) {
            Toast.makeText(this, "Choose two locations", Toast.LENGTH_SHORT).show();
        } else {
            path = new ArrayList();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(startMarker.getPosition());
            builder.include(finishMarker.getPosition());

            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(BuildConfig.MAPS_API_KEY)
                    .build();

            String origin = startMarker.getPosition().latitude + "," + startMarker.getPosition().longitude;
            String destination = finishMarker.getPosition().latitude + "," + finishMarker.getPosition().longitude;
            DirectionsApiRequest req = DirectionsApi.getDirections(context, origin, destination);

            try {
                DirectionsResult res = req.await();

                if (res.routes != null && res.routes.length > 0) {
                    DirectionsRoute route = res.routes[0];

                    if (route.legs != null) {
                        for (int i = 0; i < route.legs.length; i++) {
                            DirectionsLeg leg = route.legs[i];
                            if (leg.steps != null) {
                                for (int j = 0; i < leg.steps.length; j++) {
                                    DirectionsStep step = leg.steps[j];
                                    if (step.steps != null && step.steps.length > 0) {
                                        for (int k = 0; k < step.steps.length; k++) {
                                            DirectionsStep step1 = step.steps[k];
                                            EncodedPolyline points1 = step1.polyline;

                                            if (points1 != null) {
                                                //Decode polyline and add points to list of route coordinates
                                                List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                                for (com.google.maps.model.LatLng coord1 : coords1) {
                                                    path.add(new LatLng(coord1.lat, coord1.lng));
                                                }
                                            }
                                        }
                                    } else {
                                        EncodedPolyline points = step.polyline;
                                        if (points != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords = points.decodePath();
                                            for (com.google.maps.model.LatLng coord : coords) {
                                                path.add(new LatLng(coord.lat, coord.lng));
                                                builder.include(new LatLng(coord.lat, coord.lng));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Log.e("ERROR", ex.getLocalizedMessage());
            }

            if (path.size() > 0) {
                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(25);
                route = mMap.addPolyline(opts);

                pointsFromRoute = new ArrayList<>();

                for (LatLng p : path) {
                    Point temp = mMap.getProjection().toScreenLocation(p);
                    pointsFromRoute.add(temp);
                }
                Log.e("SCREEN ROUTE", Arrays.toString(pointsFromRoute.toArray()));
            }

            mMap.getUiSettings().setZoomControlsEnabled(true);
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            mMap.animateCamera(cu);
        }

    }
}


package com.example.pathfollowingcar;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Maps extends AppCompatActivity implements OnMapReadyCallback {

    private Marker startMarker;
    private Marker finishMarker;
    private Marker currentMarker;
    private Polyline route;
    private GoogleMap mMap;
    private List<LatLng> path;
    private ArrayList<MapPoint> stepPointsInRoute = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private int searchType = 0;
    private static final int INITIAL_REQUEST = 1337;
    private static final int CURRENT_PLACE_AUTOCOMPLETE_REQUEST_CODE = 53;
    private static final int DESTINATION_PLACE_AUTOCOMPLETE_REQUEST_CODE = 63;
    private static final int REQUEST_PERMISSION_PHONE_STATE = 1;
    private static final String[] LOCATION_PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};

    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                Toast.makeText(this, "Permission Needed", Toast.LENGTH_SHORT).show();
            } else {
                requestPermission(Manifest.permission.READ_PHONE_STATE, REQUEST_PERMISSION_PHONE_STATE);
            }
        }
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!isPermissionGiven()) {
            givePermission();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        showPhoneStatePermission();

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
            if (drawRouteFromLocations()) {
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
            }

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
            System.out.println(getStringList());
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length > 0) {
            if (searchType == 0) {
                // TO DO CURRENT LOCATION
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            setupGoogleMapScreenSettings(googleMap);
            mMap = googleMap;

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPS && !isNetwork) {
                    Toast.makeText(getApplicationContext(), "Current location not found", Toast.LENGTH_SHORT).show();
                } else {
                    if (isGPS) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, listener);

                        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null) {
                            currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude())));
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(17f).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
                }
            }

        } else {
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, 1);
        }
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
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);

        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);

        mUiSettings.setZoomControlsEnabled(true); // DISABLE WHEN ROUTE IS GIVEN
        mUiSettings.setScrollGesturesEnabled(true); // DISABLE WHEN ROUTE IS GIVEN
        mUiSettings.setZoomGesturesEnabled(true); // DISABLE WHEN ROUTE IS GIVEN
        mUiSettings.setTiltGesturesEnabled(true); // DISABLE WHEN ROUTE IS GIVEN
        mUiSettings.setRotateGesturesEnabled(true); // DISABLE WHEN ROUTE IS GIVEN
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

    private boolean drawRouteFromLocations() {
        if (startMarker == null || finishMarker == null) {
            Toast.makeText(this, "Choose two locations", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            currentMarker.remove();
            mMap.getUiSettings().setZoomControlsEnabled(false); // DISABLE WHEN ROUTE IS GIVEN
            mMap.getUiSettings().setScrollGesturesEnabled(false); // DISABLE WHEN ROUTE IS GIVEN
            mMap.getUiSettings().setZoomGesturesEnabled(false); // DISABLE WHEN ROUTE IS GIVEN
            mMap.getUiSettings().setTiltGesturesEnabled(false); // DISABLE WHEN ROUTE IS GIVEN
            mMap.getUiSettings().setRotateGesturesEnabled(false); // DISABLE WHEN ROUTE IS GIVEN
            mMap.getUiSettings().setZoomControlsEnabled(false); // DISABLE WHEN ROUTE IS GIVEN

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

                                    MapPoint mapPoint = new MapPoint(step.startLocation, step.endLocation, step.distance.inMeters);
                                    stepPointsInRoute.add(mapPoint);

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
                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(15);
                route = mMap.addPolyline(opts);
            }

            mMap.getUiSettings().setZoomControlsEnabled(true);
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 75);
            mMap.animateCamera(cu);

            return true;
        }
    }

    private boolean isPermissionGiven() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void givePermission() {
        requestPermissions(LOCATION_PERMISSIONS, INITIAL_REQUEST);
    }

    private ArrayList<String> getStringList() {
        ArrayList<String> stringList = new ArrayList<>();

        for (int i = 0; i < stepPointsInRoute.size(); i++) {
            MapPoint point = stepPointsInRoute.get(i);
            com.google.maps.model.LatLng firstPoint = point.getStartPoint();
            com.google.maps.model.LatLng nextPoint = point.getEndPoint();
            com.google.maps.model.LatLng lastPoint;

            double xTwo = nextPoint.lat;
            double xOne = firstPoint.lat;
            double xLast;

            double yTwo = nextPoint.lng;
            double yOne = firstPoint.lng;
            double yLast;

            double dx1 = xTwo - xOne;
            double dy1 = yTwo - yOne;

            double vectorLengthA = Math.sqrt(Math.pow(dx1, 2) + Math.pow(dy1, 2));
            vectorLengthA = point.getDistance();

            String tmpStr = "goForward " + (int) vectorLengthA + "*";

            stringList.add(tmpStr);

            if (i < stepPointsInRoute.size() - 1) {
                firstPoint = stepPointsInRoute.get(i + 1).getStartPoint();
                nextPoint = stepPointsInRoute.get(i + 1).getEndPoint();
                lastPoint = stepPointsInRoute.get(i).getEndPoint();


                xTwo = nextPoint.lat;
                xOne = firstPoint.lat;
                xLast = lastPoint.lat;

                yTwo = nextPoint.lng;
                yOne = firstPoint.lng;
                yLast = lastPoint.lng;

                double rotation;

                double angleOne = (Math.atan2((yOne - yLast), (xOne - xLast)));
                double angleTwo = (Math.atan2((yTwo - yOne), (xTwo - xOne)));


                rotation = (angleTwo - angleOne);
                if (rotation > (Math.PI)) rotation = rotation - (2 * (Math.PI));


                if (rotation > 0) {
                    rotation = Math.toDegrees(rotation);
                    tmpStr = "rotateClockwise " + rotation + "*";
                } else {
                    rotation = -Math.toDegrees(rotation);
                    tmpStr = "rotateCounterClockwise " + rotation + "*";
                }
                stringList.add(tmpStr);
            }
        }

        return stringList;
    }
}

class MapPoint {
    private com.google.maps.model.LatLng startPoint;
    private com.google.maps.model.LatLng endPoint;
    private long distance;

    public MapPoint(com.google.maps.model.LatLng startPoint, com.google.maps.model.LatLng endPoint, long distance) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.distance = distance;
    }

    public com.google.maps.model.LatLng getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(com.google.maps.model.LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public com.google.maps.model.LatLng getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(com.google.maps.model.LatLng endPoint) {
        this.endPoint = endPoint;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "MapPoint{" +
                "startPoint=" + startPoint +
                ", endPoint=" + endPoint +
                ", distance=" + distance +
                '}';
    }
}


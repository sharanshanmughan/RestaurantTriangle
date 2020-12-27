package com.auggen.restuaranttriangle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.GeoPolyline;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapPolygon;
import com.here.android.mpa.mapping.MapPolyline;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends FragmentActivity {
    private Map map = null;
    int j =-1;
    private Button findButton,shortestButton,findAreaButton;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private AndroidXMapFragment mapFragment = null;
    Double[] latitude = new Double[10];
    Double[] longitude = new Double[10];
    Double[] distanceList = new Double[10];
    Double[] driveDistanceList = new Double[10];
    Double senderLatitude = 8.8932;
    Double senderLongitude =76.6141;
    Double side1 = 0.0;
    Double side2 = 0.0;
    Double side3 = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

    }

    private void initialize() {

        mapFragment = (AndroidXMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        findButton= findViewById(R.id.findRestaurant);

        shortestButton=findViewById(R.id.shortestFarthestDistance);
        shortestButton.setEnabled(false);

        findAreaButton = findViewById(R.id.findArea);
        findAreaButton.setEnabled(false);


        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps");

        if (!success){
            Toast.makeText(getApplicationContext(),"Unable to set cache",Toast.LENGTH_SHORT).show();
        }
        else{
            mapFragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(Error error) {
                    if (error== Error.NONE){
                        map = mapFragment.getMap();
                        requestPermissions();
                        findButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                getRestaurantApi();
                            }
                        });

                        shortestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                findDistance();
                            }
                        });

                        findAreaButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                findArea();

                            }
                        });
                    }

                }
            });
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
        }else{

            try {
                Image img =new Image();
                img.setImageResource(R.drawable.home);
                MapMarker customMarker = new MapMarker(new GeoCoordinate(senderLatitude,senderLongitude, 0.0), img);
                map.addMapObject(customMarker);
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.setCenter(new GeoCoordinate(senderLatitude,senderLongitude),Map.Animation.NONE);
            double level = map.getMinZoomLevel() + map.getMaxZoomLevel()/1.6;
            map.setZoomLevel(level);
        }
    }

    private void getRestaurantApi(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://developers.zomato.com/api/v2.1/search?&lat=8.8932&lon=76.6141";
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response

                        if(response!=null){
                            try {
                                String lat;
                                for (int i=0;i<10;i++){
                                    latitude[i] =Double.parseDouble(
                                            response.getJSONArray("restaurants")
                                                    .getJSONObject(i).getJSONObject("restaurant").getJSONObject("location").getString("latitude")
                                    );
                                    longitude[i] =Double.parseDouble(
                                            response.getJSONArray("restaurants")
                                                    .getJSONObject(i).getJSONObject("restaurant").getJSONObject("location").getString("longitude")
                                    );
                                }
                                List<MapMarker> mapMarkers = new ArrayList<>();
                                for (int i = 0; i<10;i++){
                                    MapMarker marker = new MapMarker();
                                    marker.setCoordinate(new GeoCoordinate(latitude[i],longitude[i]));
                                    mapMarkers.add(marker);
                                }


                                for (int i = 0; i<10;i++) {
                                    map.addMapObject(mapMarkers.get(i));
                                }

                                shortestButton.setEnabled(true);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                java.util.Map<String, String> params = new HashMap<String, String>();
                params.put("user-key", "b67cd0255799ac696171930750c2381a");
                params.put("Accept", "application/json");

                return params;
            }
        };
        queue.add(postRequest);
    }

    private void findDistance() {
        for (int i=0;i<10;i++){

            double theta = senderLongitude - longitude[i];
            double dist = Math.sin(deg2rad(senderLatitude))
                    * Math.sin(deg2rad(latitude[i]))
                    + Math.cos(deg2rad(senderLatitude))
                    * Math.cos(deg2rad(latitude[i]))
                    * Math.cos(deg2rad(theta));
            double dist1 = Math.acos(dist);
            double dist2 = rad2deg(dist1);
            double dist3 = dist2 * 60 * 1.1515;
            distanceList[i] = dist3;

        }

        Double minimumDistance = distanceList[0];
        Double maximumDistance = distanceList[0];
        int smallestPos=0,largestPos=0;
        for (int i=0; i<10; i++){
            Log.d("total"," "+distanceList[i]);
            if (distanceList[i]<minimumDistance){
                minimumDistance = distanceList[i];
                smallestPos=i;
            }
            if (distanceList[i]>maximumDistance){
                maximumDistance=distanceList[i];
                largestPos=i;
            }
        }

        try {
            Image shortestLabel =new Image();
            shortestLabel.setImageResource(R.drawable.shortest);
            MapMarker shortestLabelMarker = new MapMarker(new GeoCoordinate(latitude[smallestPos],longitude[smallestPos], 0.0), shortestLabel);

            map.addMapObject(shortestLabelMarker);

            Image farthestLabel = new Image();
            farthestLabel.setImageResource(R.drawable.farthest);
            MapMarker farthestLabelMarker = new MapMarker(new GeoCoordinate(latitude[largestPos],longitude[largestPos], 0.0), farthestLabel);
            map.addMapObject(farthestLabelMarker);

        } catch (IOException e) {
            e.printStackTrace();
        }
        List<GeoCoordinate> points = new ArrayList<GeoCoordinate>();
        points.add(new GeoCoordinate(senderLatitude,senderLongitude));
        points.add(new GeoCoordinate(latitude[smallestPos],longitude[smallestPos]));
        points.add(new GeoCoordinate(latitude[largestPos],longitude[largestPos]));
        points.add(new GeoCoordinate(senderLatitude,senderLongitude));
        GeoPolygon polygon = new GeoPolygon(points);
        MapPolygon mapPolygon = new MapPolygon(polygon);
        GeoPolyline polyline = new GeoPolyline(points);
        MapPolyline mapPolyline = new MapPolyline(polyline);
        map.addMapObject(mapPolyline);

        double theta = longitude[smallestPos] - longitude[largestPos];
        double dist = Math.sin(deg2rad(latitude[smallestPos]))
                * Math.sin(deg2rad(latitude[largestPos]))
                + Math.cos(deg2rad(latitude[smallestPos]))
                * Math.cos(deg2rad(latitude[largestPos]))
                * Math.cos(deg2rad(theta));
        double dist1 = Math.acos(dist);
        double dist2 = rad2deg(dist1);
        double dist3 = dist2 * 60 * 1.1515;

        side1 = maximumDistance/0.62137;
        side2 = dist3/0.62137;
        side3 = minimumDistance/ 0.62137;
        findAreaButton.setEnabled(true);
        findDrivingDistance(smallestPos,largestPos);

    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void findDrivingDistance(int smallestPos, int largestPos){
        Double[] latitudes = {latitude[smallestPos],latitude[largestPos]};
        Double[] longitudes ={longitude[smallestPos],longitude[largestPos]};
        final Double[] distance = new Double[3];
        //create route plan and way points

        RoutePlan routePlan = new RoutePlan();
        List<RouteWaypoint> routeWayPointListEnd = new ArrayList<>();

        for (int i=0; i<2;i++){
            RouteWaypoint end = new RouteWaypoint(new GeoCoordinate(latitudes[i],longitudes[i]));
            routeWayPointListEnd.add(end);
        }

        RouteWaypoint start = new RouteWaypoint(new GeoCoordinate(senderLatitude,senderLongitude));

        for (int i=0;i<2;i++){

            routePlan.addWaypoint(start);
            routePlan.addWaypoint(routeWayPointListEnd.get(i));

            //set route options
            RouteOptions routeOptions = new RouteOptions();
            routeOptions.setRouteType(RouteOptions.Type.FASTEST);
            routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);

            //calculate route
            CoreRouter router = new CoreRouter();
            router.calculateRoute(routePlan, new Router.Listener<List<RouteResult>, RoutingError>() {
                @Override
                public void onProgress(int i) {
                    Log.d("progress","progress");
                }

                @Override
                public void onCalculateRouteFinished(@Nullable List<RouteResult> routeResults, @NonNull RoutingError routingError) {
                    if (routingError==RoutingError.NONE && routeResults!=null && !routeResults.isEmpty()){
                        double length = routeResults.get(0).getRoute().getLength();
                        MapRoute mapRoute = new MapRoute(routeResults.get(0).getRoute());
                        map.addMapObject(mapRoute);
                        Double distance = length/1000;
                        findDrivingDistanceList(distance);
//                        Log.d("rrrr"," "+routeResults.get(1).getRoute());

                    }
                }
            });
        }


        routePlan.addWaypoint(routeWayPointListEnd.get(0));
        routePlan.addWaypoint(routeWayPointListEnd.get(1));

        //set route options
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);

        //calculate route
        CoreRouter router = new CoreRouter();
        router.calculateRoute(routePlan, new Router.Listener<List<RouteResult>, RoutingError>() {
            @Override
            public void onProgress(int i) {
                Log.d("progress","progress");
            }

            @Override
            public void onCalculateRouteFinished(@Nullable List<RouteResult> routeResults, @NonNull RoutingError routingError) {
                if (routingError==RoutingError.NONE && routeResults!=null && !routeResults.isEmpty()){
                    double length = routeResults.get(0).getRoute().getLength();
                    MapRoute mapRoute = new MapRoute(routeResults.get(0).getRoute());
                    map.addMapObject(mapRoute);
                    Double distance = length/1000;
                    findDrivingDistanceList(distance);
                }
            }
        });

    }

    private void findDrivingDistanceList(Double driveDistance) {

            j++;
            driveDistanceList[j]= driveDistance;


    }

    private void findArea() {

        double perimeter = side1+side2+side3;
        double halfperimeter = perimeter/2;
        double Area = Math.sqrt(halfperimeter*(halfperimeter-side1)*(halfperimeter-side2)*(halfperimeter-side3));
        double speed = 5.0;
        double time = Math.round(perimeter/speed) ;
        double dist1 = driveDistanceList[0];
        double dist2 = driveDistanceList[1];
        double dist3 = driveDistanceList[2];
        double perimeter2 = dist1 + dist2 + dist3;

        double time2 = Math.round(perimeter2/speed) ;


        Intent intent = new Intent(MainActivity.this,ResultViewActivity.class);
        intent.putExtra("Area1", Double.toString(Area));
        intent.putExtra("Time1", Double.toString(time));
        intent.putExtra("Time2", Double.toString(time2));
        startActivity(intent);


    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length>0){
            if (grantResults[0] ==  PackageManager.PERMISSION_GRANTED){
                getCurrentPosition();
            }else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void getCurrentPosition() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest,new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(MainActivity.this)
                        .removeLocationUpdates(this);
                if (locationRequest !=null && locationResult.getLocations().size()>0){
                    int latestLocationIndex = locationResult.getLocations().size()-1;
                    double lat = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    double lng = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                }
            }
        }, Looper.getMainLooper());
    }


}

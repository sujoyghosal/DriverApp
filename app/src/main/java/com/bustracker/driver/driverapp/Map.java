package com.bustracker.driver.driverapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class Map extends Activity {

    private Context context = this;
    private String group = "";
    private GoogleMap map;
    private static LatLng l =  new LatLng(22,88);
    private static int counter = 0;
    PolylineOptions busTrail;
    private static Marker busMarker;
    private static LatLngBounds bounds;
    private static LatLng busPos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Bundle b = getIntent().getExtras();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        busTrail = new PolylineOptions();
        Log.d("#######", "Loading All Bus Stops on Map  :-0");
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        if(map==null){
            Log.e("####","Could not get GoogleMap object!!");
            return;
        }

        if(RoutesUtils.routeStopsArray!=null && RoutesUtils.routeStopsArray.size()>0) {
            drawAllBusStops();
            loadCurrentBusPosition();
        }

//           map.moveCamera(CameraUpdateFactory.newLatLngZoom(l, 100));
//           map.setBuildingsEnabled(true);
//           map.setMyLocationEnabled(true);
//           map.setTrafficEnabled(true);
//           map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);

           map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
               @Override
               public boolean onMarkerClick(Marker marker) {
                   Log.d("####", "This is from MarkerClick Handler....");
                   if (RoutesUtils.routeName != null && marker.getTitle() != null) {
                       group = RoutesUtils.routeName.trim().toUpperCase().replace(" ", "-")
                               + "-" + marker.getTitle().trim().toUpperCase().replace(" ", "-");
                       displayDialog(context, "Action", "Do you want to send arrival notification to passengers for stop " + marker.getTitle() + "?");
                   }
                   marker.showInfoWindow();
                   return true;
               }
           });
        trackBusesOnARoute();

    }

    private void drawAllBusStops() {
        PolylineOptions po = new PolylineOptions();
        {
            for (int i = 0; i < RoutesUtils.routeStopsArray.size(); i++) {
                RoutesUtils.Coordinates c = RoutesUtils.routeStopsArray.get(i).getCoordinates();
                if(c.getLatitude()==null || !RoutesUtils.isDouble(c.getLatitude()))
                    continue;
                l = new LatLng(Double.valueOf(c.getLatitude()), Double.valueOf(c.getLongitude()));

                Marker m = map.addMarker(new MarkerOptions()
                        .position(l)
                        .title(RoutesUtils.routeStopsArray.get(i).getStopName())
                        .snippet(RoutesUtils.routeStopsArray.get(i).getDistance())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop_clipped_rev_1)));

                m.showInfoWindow();
                po.add(l);
            }

            po.color(Color.RED);
            po.width(5);
            po.geodesic(true);
            po.describeContents();
            map.addPolyline(po);
        }
    }
    private void loadCurrentBusPosition() {
        ++counter;
//       l = new LatLng(Double.valueOf(RoutesUtils.getCurrentDeviceLatitude()),
//               counter*0.008*Double.valueOf(RoutesUtils.getCurrentDeviceLongitude()));
/*         l = new LatLng(l.latitude, l.longitude);
        Double dist = RoutesUtils.getDistanceFromLatLonInKm(Double.valueOf(RoutesUtils.getCurrentDeviceLatitude()),
                Double.valueOf(RoutesUtils.getCurrentDeviceLongitude()),l.latitude,l.longitude);
        DecimalFormat myFormatter = new DecimalFormat("###.##");
        Log.d("####", "Distance=" + myFormatter.format(dist)); */
        int s = RoutesUtils.routeStopsArray.size();
        final LatLng nextStop = new LatLng(Double.valueOf(RoutesUtils.routeStopsArray.get(s-2).getCoordinates().getLatitude()),
                Double.valueOf(RoutesUtils.routeStopsArray.get(s - 2).getCoordinates().getLongitude()));


        busPos = l;
        if(s>2)
            busPos = new LatLng(l.latitude,l.longitude);
//            busPos = new LatLng(l.latitude - counter*0.01*(l.latitude -nextStop.latitude),
//                    l.longitude - counter*0.01*(l.longitude -nextStop.longitude));
        try {
            bounds = new LatLngBounds(l, nextStop);
        }catch (IllegalArgumentException e){
            bounds = new LatLngBounds(nextStop, l);
        }
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 10));


/*        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(nextStop)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/

        busTrail.add(busPos);
        busTrail.color(Color.GREEN);
        busTrail.width(6);
        busTrail.geodesic(true);
        busTrail.describeContents();
        map.addPolyline(busTrail);
        if(busMarker!=null)
            busMarker.remove();
        busMarker = map.addMarker(new MarkerOptions()
                .position(busPos)
                .title(RoutesUtils.routeName)
                .snippet(busPos.latitude + "," + busPos.longitude)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_small_clipped_rev_2)));
        busMarker.showInfoWindow();
//        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
        map.setOnCameraChangeListener(null);
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {
                // Move camera.
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
                // Remove listener to prevent position reset on camera move.
                map.setOnCameraChangeListener(null);
            }
        });
    }

    public void displayDialog(Context ctx,String title, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setMessage(msg)
                .setTitle(title)
                .setIcon(R.drawable.bus_stop_clipped_rev_1)
                .setInverseBackgroundForced(true)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String urls[] = new String[2];

                        urls[0] = "http://sujoyghosal-test.apigee.net/busroutenocache/sendpushtogroup?grouppath=" + group
                                + "&notifier=" + RoutesUtils.NOTIFIER + "&message=" + Uri.encode("Arriving shortly at stop: " + group);
                        new RouteListActivity.CallSendPushAPI().execute(urls);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void trackBusesOnARoute(){
            new GetCurrentBusLocations().execute("");
    }
    private class GetCurrentBusLocations extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected String doInBackground(String... urls) {
            String response = null;

            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                Log.e("Error!!!!", e.toString());

                return null;
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result){

            loadCurrentBusPosition();
            trackBusesOnARoute();
        }

    }

    @Override
    public void onPause(){
        MainActivity.stopAlarm();
        Log.d("####", "Cancelled Location Push to Server.");
        finish();
        super.onPause();
    }

    @Override
    public void onStop(){
        MainActivity.stopAlarm();
        Log.d("####", "Cancelled Location Push to Server.");
        finish();
        super.onStop();
    }
    @Override
    public void onBackPressed(){
        MainActivity.stopAlarm();
        Log.d("####", "Cancelled Location Push to Server.");
        super.onBackPressed();
        finish();
    }
}
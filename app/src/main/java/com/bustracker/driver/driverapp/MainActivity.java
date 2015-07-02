package com.bustracker.driver.driverapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;



public class MainActivity extends Activity {

    private Context context = this;
    private  static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         if (!RoutesUtils.isNetConnected(context)) {
            RoutesUtils.displayDialog(context, "Error Connecting", "It appears you are not connected to the internet, this app requires an internet connection to work properly.");
        }
        RoutesUtils.loadDeviceLocation(context);
        RoutesUtils.deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        progressDialog = ProgressDialog.show(context, "Status", "Getting Routes..please wait", true, true);
        if(RoutesUtils.allRoutesArray==null || RoutesUtils.allRoutesArray.isEmpty()) {
                new GetAllRoutes().execute("");
        } else
            loadRouteNames();
    }

    public void loadRouteNames() {
        if(progressDialog!=null && progressDialog.isShowing())
            progressDialog.dismiss();
        Spinner spinnerRoutes = (Spinner) findViewById(R.id.spinnerRoutes);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, RoutesUtils.routeNames); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoutes.setAdapter(spinnerArrayAdapter);
        spinnerRoutes.setOnItemSelectedListener(new CustomSpinnerSelectionListener());
    }
    public void sendBusLocationUpdate(View v){
        RoutesUtils.locationFeed = true;
        RoutesUtils.displayDialog(context, "Status", "Started Location Updates for this Bus");
    }

    public void getStopsByRouteName(View view){
        RoutesUtils.routeStopsArray.clear();
        if(!RoutesUtils.allRoutesArray.isEmpty()){
            for(int i=0; i<RoutesUtils.allRoutesArray.size(); i++){
                RouteObject aObject = RoutesUtils.allRoutesArray.get(i);
                if(aObject.getRouteName().equalsIgnoreCase(RoutesUtils.routeName)) {
                    for(int k=0;k<aObject.getBusStopsArray().size();k++) {
                        RoutesUtils.routeStopsArray.add(aObject.getBusStopsArray().get(k));
                    }
                    break;
                }
            }
            startActivity(new Intent(context, RouteListActivity.class));
        }
    }
    public  static class SendCurrentBusLocation extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";


            try {
                URL routeurl = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) routeurl.openConnection();
                conn.connect();
                InputStream content = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                Log.e("Error!!!!", e.toString());
                return null;
            }

            Log.d("SendCurrentBusLocation:", response);
            return response;
        }

        @Override
        protected void onPostExecute(String result){

            if (result==null)
                return;
            Log.i("SendCurrentBusLocation", "Success. Result = " + result);
      }
    }
    public  class GetAllRoutes extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";

            String url = "http://sujoyghosal-test.apigee.net/busroute/allroutes";

            try {
                URL routeurl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) routeurl.openConnection();
                conn.connect();

                InputStream content = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                Log.e("Error!!!!", e.toString());
                return null;
            }

            Log.d("GetAllRoutes Call Response Received:", response);
            return response;
        }

        @Override
        protected void onPostExecute(String result){
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (result==null)
                return;
            Log.i("GetAllRoutes", "Success. Result = " + result);
            try {
                RoutesUtils.allRoutesArray.clear();
                JSONArray ar = new JSONArray(result);
                if(ar!=null)
                    Log.d("##Number of routes found: ", "" + ar.length());
                for(int i=0; i < ar.length(); i++){
                    JSONObject oe = ar.getJSONObject(i);
                    RouteObject routeObject = new RouteObject();
                    routeObject.setRouteName(oe.getString("name"));
//                    RouteListActivity.RouteStopsObject rsObject = new RouteListActivity.RouteStopsObject();

                    if(oe.has("description"))
                        routeObject.setRouteDesc(oe.getString("description"));
                    if(oe.has("uuid"))
                        routeObject.setUUID(oe.getString("uuid"));
                    if(oe.has("bus_locations")) {
                        JSONArray bus_locations = oe.getJSONArray("bus_locations");

                        if(bus_locations!=null && bus_locations.length()>0) {
                            for (int j = 0; j < bus_locations.length(); j++) {
                                JSONObject bl = bus_locations.getJSONObject(j);
                                String bus_id = "";
                                String lat;
                                String lng;
                                if(bl.has("busID"))
                                    bus_id = bl.getString("busID");
                                if(bl.getJSONObject("location")!=null && bl.getJSONObject("location").has("latitude")
                                        && bl.getJSONObject("location").has("longitude")) {
                                    lat = bl.getJSONObject("location").getString("bus_latitude");
                                    lng = bl.getJSONObject("location").getString("bus_longitude");
                                    routeObject.busLocationsArray.add(new RoutesUtils.Coordinates(bus_id, lat, lng));
                                }
                            }
                        }
                    }

                    if(oe.has("bus_stops")) {
                        JSONArray bus_stops = oe.getJSONArray("bus_stops");
                        if(bus_stops!=null && bus_stops.length()>0) {
                            for (int j = 0; j < bus_stops.length(); j++) {
                                JSONObject bs = bus_stops.getJSONObject(j);
                                RouteListActivity.RouteStopsObject busStop = new RouteListActivity.RouteStopsObject();
                                if(bs.has("stop_name"))
                                    busStop.setStopName(bs.getString("stop_name"));
                                if(bs.has("street"))
                                    busStop.setStreet(bs.getString("street"));
                                if(bs.has("address_line2"))
                                    busStop.setAddress2(bs.getString("address_line2"));
                                if(bs.has("city"))
                                    busStop.setCity(bs.getString("city"));
                                if(bs.has("state"))
                                    busStop.setState(bs.getString("state"));
                                if(bs.has("country"))
                                    busStop.setCountry(bs.getString("country"));
                                if(bs.has("postal_code"))
                                    busStop.setPC(bs.getString("postal_code"));

                                busStop.setAddress(busStop.getStreet() + "," + busStop.getCity());

                                if(bs.has("location")) {
                                    JSONObject location = bs.getJSONObject("location");
                                    if(location!=null && location.has("latitude") && location.has("longitude")) {
                                        busStop.setCoordinates(new RoutesUtils.Coordinates(location.getString("latitude"), location.getString("longitude"), ""));
                                        if (location.getString("latitude") != null && RoutesUtils.isDouble(location.getString("latitude"))
                                                && location.getString("longitude") != null && RoutesUtils.isDouble(location.getString("longitude"))) {
                                            try {
                                                Double dist = RoutesUtils.getDistanceFromLatLonInKm(Double.valueOf(RoutesUtils.getCurrentDeviceLatitude()),
                                                        Double.valueOf(RoutesUtils.getCurrentDeviceLongitude()),
                                                        Double.valueOf(location.getString("latitude")),
                                                        Double.valueOf(location.getString("longitude")));
                                                DecimalFormat myFormatter = new DecimalFormat("###.#");
                                                busStop.setDistance(myFormatter.format(dist));
                                            } catch (Exception e) {
                                                Log.d("####", "Could not set distance");
                                                routeObject.busStopsArray.add(busStop);
                                                continue;
                                            }
                                        }
                                    }

                                }
                                routeObject.busStopsArray.add(busStop);
//                                RoutesUtils.allStopsArray.add(busStop);
                            }
                        }
                    }

                    RoutesUtils.allRoutesArray.add(routeObject);
                }
                Log.d("####Spinner:", "Populating Route Names - All Stops Array Size Right Now is " + RoutesUtils.allRoutesArray.size());
                if(RoutesUtils.allRoutesArray!=null && RoutesUtils.allRoutesArray.size()>0) {
                    for (int i = 0; i < RoutesUtils.allRoutesArray.size(); i++) {
                        if (!RoutesUtils.routeNames.contains(RoutesUtils.allRoutesArray.get(i).getRouteName()))
                            RoutesUtils.routeNames.add(RoutesUtils.allRoutesArray.get(i).getRouteName());
                    }
                }
                loadRouteNames();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public void performShare(View v){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Have your bus driver install this app. It allows bus driver to send arrival notifications at bus stops and helps track the bus." +
                "https://www.dropbox.com/s/2z70phuyoz233cp/com.sujoy.checkinwipro.apk?dl=0";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}

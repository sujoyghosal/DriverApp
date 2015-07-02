package com.bustracker.driver.driverapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Map extends Activity {

    private Context context = this;
    private String group = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Bundle b = getIntent().getExtras();
        GoogleMap map;
        if(b!=null){
            int pos = b.getInt("POSITION");
            Log.d("########Got position as:", String.valueOf(pos));
            if(pos>=0){
                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                        .getMap();
                RoutesUtils.Coordinates c =  RoutesUtils.routeStopsArray.get(pos).getCoordinates();
                if(c.getLatitude()==null || !RoutesUtils.isDouble(c.getLatitude())){
                    Log.d("#####Null latitude in Map class", "Returning");
                    return;
                }

                Log.d("######In Mapper, locating coordinates:", c.getLatitude() + "," + c.getLongitude());
                LatLng l =  new LatLng(Double.valueOf(c.getLatitude()), Double.valueOf(c.getLongitude()));
                if(l==null)
                    l = new LatLng(22,88);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.getUiSettings().setZoomGesturesEnabled(true);
//                map.setTrafficEnabled(true);
                Marker m = map.addMarker(new MarkerOptions()
                        .position(l)
                        .title(RoutesUtils.routeStopsArray.get(pos).getName())
                        .snippet(RoutesUtils.routeStopsArray.get(pos).getDistance())
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.bus_stop)));
 //                     .fromResource(R.drawable.generic_business_71)));

                m.showInfoWindow();
                // Move the camera instantly to hamburg with a zoom of 15.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(l, 15));

                // Zoom in, animating the camera.
                map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                return;
            }
        }
        Log.d("#######", "Loading All Locations  :-0");
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        LatLng l =  new LatLng(22,88);
           PolylineOptions po = new PolylineOptions();
        if(RoutesUtils.routeStopsArray!=null && RoutesUtils.routeStopsArray.size()>0) {
            for (int i = 0; i < RoutesUtils.routeStopsArray.size(); i++) {
                RoutesUtils.Coordinates c = RoutesUtils.routeStopsArray.get(i).getCoordinates();
                if(c.getLatitude()==null || !RoutesUtils.isDouble(c.getLatitude()))
                    continue;
                l = new LatLng(Double.valueOf(c.getLatitude()), Double.valueOf(c.getLongitude()));

                Marker m = map.addMarker(new MarkerOptions()
                        .position(l)
                        .title(RoutesUtils.routeStopsArray.get(i).getStopName())
                        .snippet(RoutesUtils.routeStopsArray.get(i).getDistance())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop)));

                m.showInfoWindow();
                po.add(l);
            }
            po.color(Color.RED);
            po.width(5);
            po.geodesic(true);
            po.describeContents();
            map.addPolyline(po);
        }else {
            Marker m = map.addMarker(new MarkerOptions()
                    .position(l)
                    .title("Kolkata")
                    .snippet("West Bengal, India")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.thin_pin)));

            m.showInfoWindow();

        }
           if(map !=null) {
               map.moveCamera(CameraUpdateFactory.newLatLngZoom(l, 12));
               map.setBuildingsEnabled(true);
               map.setMyLocationEnabled(true);
               map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);

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

           }
    }
    public void displayDialog(Context ctx,String title, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setMessage(msg)
                .setTitle(title)
                .setIcon(R.drawable.bus_stop)
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

}
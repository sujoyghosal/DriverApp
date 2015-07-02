package com.bustracker.driver.driverapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;


public class
        RoutesUtils {
        public static ArrayList<RouteListActivity.RouteStopsObject> routeStopsArray = new ArrayList<>();
        private static String currentDeviceLatitude = null;
        private static String currentDeviceLongitude = null;
        public static int routeStopsRadius = 1;
        public static ArrayList<RouteObject> allRoutesArray = new ArrayList<>();

        public static GPSTracker gpsTracker = null;
    static final String NOTIFIER = "stopnotifier";
    public static String deviceID = "";
    public static ArrayList<Subscriptions> subscriptionsArray = new ArrayList<>();
    public static String routeName = "ROUTE1";
    public static boolean locationFeed = false;
    public static ArrayList routeNames = new ArrayList();


    public static boolean isNetConnected(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    public static String getCurrentDeviceLatitude() {
        return currentDeviceLatitude;
    }

    public static void setCurrentDeviceLatitude(String latitude) {
        currentDeviceLatitude = latitude;
    }

    public static String getCurrentDeviceLongitude() {
        return currentDeviceLongitude;
    }
    public  static void loadDeviceLocation(Context ctx){
        gpsTracker = new GPSTracker(ctx);
//        RoutesUtils.setCurrentDeviceLatitude("");
//        RoutesUtils.setCurrentDeviceLongitude("");
        gpsTracker.getLocation();
        if (gpsTracker.canGetLocation())
        {
            Log.d("Got GPS Tracekr Data. ", String.valueOf(gpsTracker.latitude) + "," + String.valueOf(gpsTracker.longitude));
            RoutesUtils.setCurrentDeviceLatitude(String.valueOf(gpsTracker.latitude));
            RoutesUtils.setCurrentDeviceLongitude(String.valueOf(gpsTracker.longitude));
        }
        else
        {
            gpsTracker.showSettingsAlert();
        }
    }

    public static void setCurrentDeviceLongitude(String longitude) {
        currentDeviceLongitude = longitude;
    }

    public static boolean isNullOrEmpty(String in) {
            if (in == null)
                return true;
            else if (in.trim().equalsIgnoreCase(""))
                return true;
            else if (in.trim().equalsIgnoreCase("null"))
                return true;
            else if (in.length() > 0)
                return false;
            return false;
        }

    static double getDistanceFromLatLonInKm(double lat1,double lon1,double lat2,double lon2) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    static double  deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    public static boolean isDouble(String s) {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

    public static void displayDialog(Context ctx,String title, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(msg)
                .setTitle(title)
                .setIcon(R.drawable.bus_stop)
                .setInverseBackgroundForced(true)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static class Coordinates {
            private String latitude;
            private String longitude;
            private String name;

            public Coordinates(String latitude, String longitude, String name) {
                this.latitude = latitude;
                this.longitude = longitude;
                this.name = name;

            }

            public Coordinates() {
            }

            public String getLatitude() {
                return latitude;
            }

            public void setLatitude(String latitude) {
                this.latitude = latitude;
            }

            public String getLongitude() {
                return longitude;
            }

            public void setLongitude(String longitude) {
                this.longitude = longitude;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        public static class Subscriptions {
                public Subscriptions(){

                }
                private String routeName;
                private String stopName;

                private String latitude;
                private String longitude;

                public String getRouteName() {
                    return routeName;
                }

                public void setRouteName(String routeName) {
                    this.routeName = routeName;
                }

                public String getStopName() {
                    return stopName;
                }

                public void setStopName(String stopName) {
                    this.stopName = stopName;
                }

                public String getLatitude() {
                    return latitude;
                }

                public void setLatitude(String latitude) {
                    this.latitude = latitude;
                }

                public String getLongitude() {
                    return longitude;
                }

                public void setLongitude(String longitude) {
                    this.longitude = longitude;
                }

        }

}

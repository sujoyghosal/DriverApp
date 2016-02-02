package com.bustracker.driver.driverapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Sujoy on 20-07-2015.
 */
public class LocationService extends IntentService {
    private String content = "";
    private Context context = this;
    public LocationService() {
        super("LocationUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("####LocationService:", "About to send current bus location");
        if(RoutesUtils.locationFeed) {
            String[] br = new String[2];
            br[0] = "http://sujoyghosal-test.apigee.net/busroute/sendcurrentbuslocation?id="
                    + RoutesUtils.deviceID + "&route=" + Uri.encode(RoutesUtils.routeName)
                    + "&latitude=" + RoutesUtils.getCurrentDeviceLatitude()
                    + "&longitude=" + RoutesUtils.getCurrentDeviceLongitude();

//            new SendCurrentBusLocation().execute(br);
            new SendCurrentBusLocation().execute(br);
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
                Log.d("####Sending Location Update With URL: ",routeurl.toString());
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
            Log.i("####Location Update: ", "Success!!!! Result = " + result);
        }
    }
    private void sendNotification(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        NotificationManager notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
//                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentTitle("Score Updates")
                .setSmallIcon(R.drawable.bus_stop_clipped_rev_1)
                .setWhen(System.currentTimeMillis())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(contentIntent)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationMgr.notify(0, notification);
    }
}

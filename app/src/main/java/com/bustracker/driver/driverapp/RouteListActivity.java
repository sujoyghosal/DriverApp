package com.bustracker.driver.driverapp;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class RouteListActivity extends ListActivity {

    private Context context = this;
    private String errorMsg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoutesUtils.subscriptionsArray.clear();
        if(RoutesUtils.routeStopsArray.isEmpty()){
            RoutesUtils.displayDialog(context, "Error", "Could Not Get Bus Stops List, going back to main screen.");
            startActivity(new Intent(context,MainActivity.class));
        }

        Log.d("#$#$#$#$", "Trying Stops List with count of bus stops =" + RoutesUtils.routeStopsArray.size() + "");

        RoutesAdapter customAdapter = new RoutesAdapter(this, RoutesUtils.routeStopsArray);
        ListView listView = getListView();
        LayoutInflater inflater=this.getLayoutInflater();
        View header=inflater.inflate(R.layout.response_header, null);
        TextView h = (TextView)header.findViewById(R.id.textViewHeader);
        h.setText( RoutesUtils.routeStopsArray.size() + " Stops For Route" );
        listView.addHeaderView(header);

        View footer = inflater.inflate(R.layout.response_footer, null);
        listView.addFooterView(footer);

        listView.setAdapter(customAdapter);
        listView.setTextFilterEnabled(true);
        customAdapter.notifyDataSetChanged();
    }
    public void returnHomeFromList(View v){
        Intent mainAct=new Intent(getApplicationContext(),MainActivity.class);
        startActivity(mainAct);
    }



    public void showInMap(String latitude, String longitude){
        Uri uri= Uri.parse("http://maps.google.com/maps?saddr=" + RoutesUtils.getCurrentDeviceLatitude()
                + "," + RoutesUtils.getCurrentDeviceLongitude()
                + "&daddr=" + latitude
                + "," + longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void createMapForAllStops(View v){
        Intent intent = new Intent(this, Map.class);
        intent.putExtra("POSITION", -1); //show all
        startActivity(intent);
    }
    public static class RouteStopsObject {
        private String name;
        private String address;
        private String prevStop;
        private String street;
        private String address2;
        private String city;
        private String state;
        private String PC;

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        private String postalCode;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        private String country;

        public String getRouteName() {
            return routeName;
        }

        private String routeName;

        public String getStopName() {
            return stopName;
        }

        public void setStopName(String stopName) {
            this.stopName = stopName;
        }

        private String stopName;

        public String getNextStop() {
            return nextStop;
        }

        public void setNextStop(String nextStop) {
            this.nextStop = nextStop;
        }

        public String getPrevStop() {
            return prevStop;
        }

        public void setPrevStop(String prevStop) {
            this.prevStop = prevStop;
        }

        private String nextStop;
        
        private RoutesUtils.Coordinates coordinates;

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        private String distance;


        public RouteStopsObject(){
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

       

        public RoutesUtils.Coordinates getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(RoutesUtils.Coordinates coordinates) {
            this.coordinates = coordinates;
        }

        public void setRouteName(String routeName) {
            this.routeName = routeName;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getStreet() {
            return street;
        }

        public void setAddress2(String address2) {
            this.address2 = address2;
        }

        public String getAddress2() {
            return address2;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCity() {
            return city;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }


        public void setPC(String PC) {
            this.PC = PC;
        }

        public String getPC() {
            return PC;
        }
    }
    public class RoutesAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<RouteStopsObject> objects;

        public RoutesAdapter(Context context, ArrayList<RouteStopsObject> objects) {
            inflater = LayoutInflater.from(context);
            this.objects = objects;
        }

        public int getCount() {
            return objects.size();
        }

        public RouteStopsObject getItem(int position) {
            return objects.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.activity_route_stops, null);
                holder.textViewName = (TextView) convertView.findViewById(R.id.textViewName);
                holder.textViewAddress = (TextView) convertView.findViewById(R.id.textViewAddress);
                holder.imgViewMap = (ImageView) convertView.findViewById(R.id.imageMap);
                holder.textViewDistance = (TextView) convertView.findViewById(R.id.tvDistance2);
                holder.checkBoxSubscribe = (CheckBox) convertView.findViewById(R.id.checkBoxSubscribe);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final CheckBox current =  holder.checkBoxSubscribe;
            final RoutesUtils.Subscriptions s = new RoutesUtils.Subscriptions();
            s.setRouteName(RoutesUtils.routeName);
            s.setStopName(objects.get(position).getStopName());
            s.setLatitude(objects.get(position).getCoordinates().getLatitude());
            s.setLongitude(objects.get(position).getCoordinates().getLongitude());

            current.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(current.isChecked()) {
                        RoutesUtils.subscriptionsArray.add(s);
                    }else {
                        RoutesUtils.subscriptionsArray.remove(s);
                    }
                }
            });



            holder.imgViewMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInMap(getItem(position).getCoordinates().getLatitude(),
                            getItem(position).getCoordinates().getLongitude());
                }
            });

            holder.textViewName.setText(objects.get(position).getStopName());
            holder.textViewAddress.setText(objects.get(position).address);
            holder.textViewDistance.setText(objects.get(position).distance + "km");
            return convertView;
        }

        private class ViewHolder {
            TextView textViewName;
            TextView textViewAddress;
            ImageView imgViewMap;
            TextView textViewDistance;
            CheckBox checkBoxSubscribe;
        }
    }

    public  void sendPushForSelection(View v){

            for(int i=0; i<RoutesUtils.subscriptionsArray.size();i++) {
                String route = RoutesUtils.subscriptionsArray.get(i).getRouteName();
                String stop = RoutesUtils.subscriptionsArray.get(i).getStopName();
                String group = route.trim().toUpperCase().replace(" ","-") + "-" + stop.trim().toUpperCase().replace(" ","-");
                String urls[] = new String[2];
                urls[0] = "http://sujoyghosal-test.apigee.net/busroutenocache/sendpushtogroup?grouppath=" + group
                    + "&notifier=" + RoutesUtils.NOTIFIER + "&message=" + Uri.encode("Arriving shortly at stop: " + group);
                    new CallSendPushAPI().execute(urls);
            }
            RoutesUtils.displayDialog(context, "Status", "Sent Push Request to Passengers");
    }

    public static class CallSendPushAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            String url = urls[0];
            System.out.println("Pushing: " + url);

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
                e.printStackTrace();
                return null;
            }
            Log.d("Push Response:", response);
            return response;
        }


        protected void onPostExecute(String result){
            if(result!=null && result.equalsIgnoreCase("Created")){
                Log.e("##Success!!!!", " Push");
            }else{
                Log.e("##oops!!!!", "Push not sent");
            }
        }
    }
  }

package com.bustracker.driver.driverapp;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;
    protected LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;
    double latitude;
    double longitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; //10 metters
    private static final long MIN_TIME_BW_UPDATES = 25000; // 10 sec

    public GPSTracker(Context context)
    {
        this.mContext = context;
        getLocation();
    }
    public Location getLocation()
    {
        try
        {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled)
            {
                // no network provider is enabled

            }
            else
            {
                this.canGetLocation = true;
                if (isNetworkEnabled)
                {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("Network", "Network");

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        updateGPSCoordinates();
                    }
                }
//if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled)
                {
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");

                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            updateGPSCoordinates();

                        }
                    }
                }
            }
        }catch (Exception e)
        {
            //e.printStackTrace();

            Log.e("Error : Location", "Impossible to connect to LocationManager", e);
        }

        return location;
    }

    public void updateGPSCoordinates()
    {
        if (location != null)
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

/**
 * Stop using GPS listener
 * Calling this function will stop using GPS in your app
 */
public void stopUsingGPS()
{
    if (locationManager != null)
    {
        locationManager.removeUpdates(GPSTracker.this);
    }
}

    /**
     * Function to get latitude
     */
    public double getLatitude()
    {
        if (location != null)
        {
            latitude = location.getLatitude();
        }

        return latitude;
    }
    /**
     * Function to get longitude
     */
    public double getLongitude()
    {
        if (location != null)
        {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     */
    public boolean canGetLocation()
    {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        //Setting Dialog Title
        alertDialog.setTitle(R.string.GPSAlertDialogTitle);

        //Setting Dialog Message
        alertDialog.setMessage(R.string.GPSAlertDialogMessage);

        //On Pressing Setting button
        alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        //On pressing cancel button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    /**
     * Get list of address by latitude and longitude
     * @return null or List<Address>
     */
    public List<Address> getGeocoderAddress(Context context)
    {
        if (location != null)
        {
            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
            try
            {
                return geocoder.getFromLocation(latitude, longitude, 1);
            }
            catch (IOException e)
            {
                //e.printStackTrace();
                Log.e("Error : Geocoder", "Impossible to connect to Geocoder", e);
            }
        }

        return null;
    }
    /**
     * Try to get AddressLine
     * @return null or addressLine
     */
    public String getAddressLine(Context context)
    {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0)
        {
            Address address = addresses.get(0);

            return address.getAddressLine(0);
        }
        else
        {
            return null;
        }
    }/**
     * Try to get Locality
     * @return null or locality
     */
    public String getLocality(Context context)
    {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0)
        {
            Address address = addresses.get(0);

            return address.getLocality();
        }
        else
        {
            return null;
        }
    }

/**
 * Try to get Postal Code
 * @return null or postalCode
 */
public String getPostalCode(Context context)
{
    List<Address> addresses = getGeocoderAddress(context);
    if (addresses != null && addresses.size() > 0)
    {
        Address address = addresses.get(0);

        return address.getPostalCode();
    }
    else
    {
        return null;
    }
}

/**
 * Try to get CountryName
 * @return null or postalCode
 */
public String getCountryName(Context context)
{
    List<Address> addresses = getGeocoderAddress(context);
    if (addresses != null && addresses.size() > 0)
    {
        Address address = addresses.get(0);

        return address.getCountryName();
    }
    else
    {
        return null;
    }
}

    @Override
    public void onLocationChanged(Location location)
    {
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        RoutesUtils.setCurrentDeviceLatitude(Double.toString(location.getLatitude()));
        RoutesUtils.setCurrentDeviceLongitude(Double.toString(location.getLongitude()));

//        Log.d("$$$$Location Update:", msg);

  }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
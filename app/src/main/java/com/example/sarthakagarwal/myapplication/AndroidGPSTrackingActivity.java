package com.example.sarthakagarwal.myapplication;
//import android.R;

/**
 * Created by sarthakagarwal on 18/12/2016.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.content.pm.PackageManager;
import android.Manifest;
import android.content.ContextWrapper;
import android.app.Activity;
import android.location.LocationListener;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import android.os.AsyncTask;
import android.net.Uri;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
//import java.util.logging.Handler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class AndroidGPSTrackingActivity extends Activity implements LocationListener {

    Button btnShowLocation;

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    private String downloadAudioPath;
    private String urlDownloadLinkFolder = "http://sarthakagarwal.co.uk/CheerOn/audioUploads/";
    private String urlDownloadLink = "";

    Timer timer;
    TimerTask timertask;

    final Handler handler = new android.os.Handler();

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 500 * 1 * 1; // half a second

    // Declaring a Location Manager
    protected LocationManager locationManager;

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public AndroidGPSTrackingActivity() {
        this.mContext = (Context) AndroidGPSTrackingActivity.this;
    }

    private static final int LOCATION_REQUEST = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("OnCreate", "Calling On Create");
        // Set the path where the fle should be downloaded to
        downloadAudioPath = getFilesDir().getAbsolutePath();
        Log.i("OnCreate","downloadAudioPath" + downloadAudioPath );

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        setContentView(R.layout.activity_main);

        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        Log.i("OnCreate", "Found Button Location");

        if (canAccessLocation()) {
            Log.i("getLocation", "ALready have perms");
        } else {
            // ActivityCompat.requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
        }

        getLocation();

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                Log.i("TAG1", "CALLING BUTTONG LOCATION");

                // check if GPS enabled
                if (canGetLocation()) {
                    getLocation();
                    double latitude = getLatitude();
                    double longitude = getLongitude();
                    // \n is for new line
                    // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    showSettingsAlert();
                }
            }
        });
        // Starting the timer
        startTimer();
    }

    public void startTimer(){
        Log.i("startTimer", "Calling Start Timer");

        timer = new Timer();

        // initialise the timers task
        ListenForNewAudioTimerTask();

        // Schedule the timer's task
        timer.schedule(timertask,3000,3000);
    }

    public void ListenForNewAudioTimerTask(){
        timertask = new TimerTask() {
            @Override
            public void run() {

                handler.post(new Runnable(){
                    public void run(){
                        Log.i("ListenForNewAudioTimerTask", "Performing task");
                        class GetAudioReqAsyncTask extends AsyncTask<String, Void, String> {
                            HttpURLConnection conn;
                            URL url = null;

                            @Override
                            protected String doInBackground(String... params) {
                                try {
                                    // Enter URL address where your php file resides
                                    url = new URL("http://sarthakagarwal.co.uk/CheerOn/getAudio.php");
                                } catch (MalformedURLException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    return "exception";
                                }
                                try {
                                    // Setup HttpURLConnection class to send and receive data from php and mysql
                                    conn = (HttpURLConnection) url.openConnection();
                                    conn.setReadTimeout(READ_TIMEOUT);
                                    conn.setConnectTimeout(CONNECTION_TIMEOUT);
                                    conn.setRequestMethod("GET");

                                    // setDoInput and setDoOutput method depict handling of both send and receive
                                    conn.setDoInput(true);
                                    conn.setDoOutput(true);

                                    // Append parameters to URL
                                    Uri.Builder builder = new Uri.Builder();
                                    String query = builder.build().getEncodedQuery();

                                    // Open connection for sending data
                                    OutputStream os = conn.getOutputStream();
                                    BufferedWriter writer = new BufferedWriter(
                                            new OutputStreamWriter(os, "UTF-8"));
                                    writer.flush();
                                    writer.close();
                                    os.close();
                                    conn.connect();
                                } catch (IOException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                    return "exception";
                                }

                                try {
                                    int response_code = conn.getResponseCode();
                                    // Check if successful connection made
                                    if (response_code == HttpURLConnection.HTTP_OK) {
                                        // Read data sent from server
                                        InputStream input = conn.getInputStream();
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                                        StringBuilder result = new StringBuilder();
                                        String line;
                                        while ((line = reader.readLine()) != null) {
                                            result.append(line);
                                        }
                                        JSONArray audios = new JSONArray();
                                        // Now download the audio files
                                        try {
                                            Log.i("JSON String",result.toString());
                                            audios = (JSONArray) new JSONTokener(result.toString()).nextValue();
                                            Log.i("initialzeTimerTask","JSON Length" + audios.length());
                                            for (int i =0 ; i < audios.length() ; i++) {
                                                JSONObject audio = audios.getJSONObject(i);
                                                String audioFileName = audio.getString("filename");
                                                urlDownloadLink = urlDownloadLinkFolder + audioFileName;
                                                MediaPlayer mediaPlayer = new MediaPlayer();
                                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                                mediaPlayer.setDataSource(urlDownloadLink);
                                                mediaPlayer.prepare(); // might take long! (for buffering, etc)
                                                mediaPlayer.start();
                                                // Previously I was going to download the file and play it,
                                                // but the above Media Player is much slicker.
                                                // Keeping the below code in case I want to use it again.
                                                //downloadAudioPath = downloadAudioPath + File.separator + audioFileName;
                                                //DownloadFile downloadAudioFile = new DownloadFile();
                                                //downloadAudioFile.execute(urlDownloadLink, downloadAudioPath);
                                            }

                                        } catch (JSONException e){
                                            e.printStackTrace();
                                            return "JSONException";
                                        }
                                        // Play downloaded file
                                        // Pass data to onPostExecute method
                                        return (result.toString());
                                    } else {
                                        return ("unsuccessful");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return "exception";
                                } finally {
                                    conn.disconnect();
                                }
                            }

                            @Override
                            protected void onPostExecute(String result) {
                                super.onPostExecute(result);
                                Log.i("onPostExecuteGetAudio",result);
                                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                            }
                        }

                        GetAudioReqAsyncTask getAudioReqAsyncTask = new GetAudioReqAsyncTask();
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
                        getAudioReqAsyncTask.execute();

                        //get the current timeStamp
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
                        final String strDate = simpleDateFormat.format(calendar.getTime());
                        //show the toast
                        int duration = Toast.LENGTH_SHORT;
                        //Toast toast = Toast.makeText(getApplicationContext(), strDate, duration);
                        //toast.show();
                    }
                });
            }
        };
    }

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

    public Location getLocation() {
        try {
            Log.i("getLocation", "getLocation");

            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.i("getLocation", "IsGPSEnabled" + isGPSEnabled);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {

                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.i("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            //Log.i("getLocation", "locagtionManager is not null");
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.i("GPSTracker GetLocation", "Just Set lat and longitude");
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.i("GPSTracker GetLocation", "Exception Caught");
            e.printStackTrace();
        }
        this.location = location;
        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(AndroidGPSTrackingActivity.this);
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        String msg = "New Latitude: " + getLatitude()
                + "New Longitude: " + getLongitude();
        Log.i("OnLocationChanged", "Location Changed!" + msg);
        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        //Date dt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        float alt = 0.0f;
        insertToDatabase((float) getLatitude(), (float) getLongitude(), alt, new Date());
    }



    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void insertToDatabase(float latitude, float longitude, float altitude, Date datetime) {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            HttpURLConnection conn;
            URL url = null;

            @Override
            protected String doInBackground(String... params) {
                try {
                    // Enter URL address where your php file resides
                    url = new URL("http://sarthakagarwal.co.uk/CheerOn/location.php");
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return "exception";
                }
                try {
                    // Setup HttpURLConnection class to send and receive data from php and mysql
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(READ_TIMEOUT);
                    conn.setConnectTimeout(CONNECTION_TIMEOUT);
                    conn.setRequestMethod("POST");

                    // setDoInput and setDoOutput method depict handling of both send and receive
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    // Append parameters to URL
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("latitude", params[0])
                            .appendQueryParameter("longitude", params[1])
                            .appendQueryParameter("altitude", params[2])
                            .appendQueryParameter("datetime", params[3]);
                    String query = builder.build().getEncodedQuery();

                    // Open connection for sending data
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    os.close();
                    conn.connect();

                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    return "exception";
                }

                try {

                    int response_code = conn.getResponseCode();

                    // Check if successful connection made
                    if (response_code == HttpURLConnection.HTTP_OK) {

                        // Read data sent from server
                        InputStream input = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        StringBuilder result = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        // Pass data to onPostExecute method
                        return (result.toString());
                    } else {

                        return ("unsuccessful");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "exception";
                } finally {
                    conn.disconnect();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Log.i("onPostExecute",result);
                //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
        sendPostReqAsyncTask.execute(Float.toString(latitude), Float.toString(longitude), Float.toString(altitude), df.format(datetime));
    }

    //@Override
    //public IBinder onBind(Intent arg0) {
    //    return null;
    //}

    private class DownloadFile extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... url) {
            int count;
            try {
                URL urls = new URL(url[0]);
                URLConnection connection = urls.openConnection();
                connection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(urls.openStream());
                File newfile = new File(url[1]);
                newfile.createNewFile(); // if file already exists will do nothing
                OutputStream output = new FileOutputStream(newfile,false);

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // publishProgress((int) (total * 100 / lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}


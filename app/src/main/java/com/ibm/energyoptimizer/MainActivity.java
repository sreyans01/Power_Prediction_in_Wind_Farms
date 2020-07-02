package com.ibm.energyoptimizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private LocationManager thislocationManager;
    private Location thislocation;
    private LocationListener thislistener;
    private Context context = MainActivity.this;
    private Double thislat,thislng;
    private static int minTime=500;
    private static int minDis=500;
    public static final int GET_LOCATION_REQUEST = 101;
    public ArrayList<String> timeslots;

    private String myAccessToken = "0";
    JSONObject exportedJSON;

    private TextView fetchResult,bestPowerTV1,bestPowerTV2,bestPowerTV3;
    private Button getDataBtn,refreshBtn;
    private EditText e_cust_addr;

    private List<Double> currPredictionList;
    private MediaPlayer mp;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        currPredictionList = new ArrayList<>();
        mp = MediaPlayer.create(context, R.raw.btnclick1);
        allfindviewbyids();
        HandleOnClicks();
        progressBar.setVisibility(View.VISIBLE);
        getData();
    }

    public void getData(){

        fetchResult.setText("Fetching Your Address");
        fetchResult.setTextColor(Color.parseColor("#F44336"));
        tracelocation();
        getAccessToken();
    }

    public void allfindviewbyids(){

        getDataBtn = findViewById(R.id.getDataBtn);
        refreshBtn = findViewById(R.id.refresh);
        e_cust_addr = findViewById(R.id.e_cust_address);
        fetchResult = findViewById(R.id.fetchResult);
        bestPowerTV1 = findViewById(R.id.bestPowerTV1);
        bestPowerTV2 = findViewById(R.id.bestPowerTV2);
        bestPowerTV3 = findViewById(R.id.bestPowerTV3);
        progressBar = findViewById(R.id.progressBar);


    }

    public void HandleOnClicks(){

        getDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(fetchResult.getText().toString().compareTo("Done")!=0){
                    Toast.makeText(context,"Please wait while we fetch the data",Toast.LENGTH_SHORT).show();
                    return;
                }

                ObjectAnimator animation2 = ObjectAnimator.ofFloat(getDataBtn, "translationX", 50f);
                animation2.setDuration(200);
                animation2.start();

                try {

                        mp.stop();
                        mp.release();

                }catch (Exception e){}
                Intent i = new Intent(context,FullPredictionActivity.class);
                i.putExtra("PredictionList", (Serializable) currPredictionList);
                if(timeslots.size() ==0) {
                    timeslots = generateTimeSlots(72, 60);
                }
                i.putStringArrayListExtra("TimeSlots",timeslots);
                startActivity(i);

            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    public void getCurrentLoc(){


    }
    public int getLocationPermission() {
        int req = 1;

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, GET_LOCATION_REQUEST);

        } else
            req = 0;
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            {
                Toast.makeText(context,"Location permission is required to analyse your weather data.",Toast.LENGTH_SHORT).show();
            }
        }
        return req;
    }


    public void tracelocation() {
        if(getLocationPermission()==1){

            {
                // Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                thislocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                }
                thislocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, thislistener);
                thislistener.onLocationChanged(thislocation);

            }
            return;
        }

        thislocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.

                return;
            }
        }

        if (!thislocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !thislocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //All thislocation services are disabled
            try {
                thislocation = thislocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            } catch (Exception e) {
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                startActivity(myIntent);
                Toast.makeText(context, "Please enable your location from settings", Toast.LENGTH_LONG).show();
                return;
            }

        } else {
            /*
            if (thislocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                thislocation = thislocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            } else if (thislocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                thislocation = thislocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            else {
                try {
                    thislocation = thislocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                } catch (Exception e) {
                }
            }
             */

            Criteria criteria = new Criteria();
            String bestProvider = thislocationManager.getBestProvider(criteria, true);
            //thislocation = thislocationManager.getLastKnownLocation(bestProvider);

        }

        thislistener = new LocationListener() {
            @Override
            public void onLocationChanged(Location thislocation) {

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(context, Locale.getDefault());

                if (thislocation != null) {
                    thislat = thislocation.getLatitude();
                    thislng = thislocation.getLongitude();

                    String address;
                    try {
                        address = getAddressFromLocation(thislat, thislng);
                        /*
                        tuitionaddress = address;
                        tutlatlng.setEnabled(true);
                        tut_location_snackbar.dismiss();

                         */

                    }catch (Exception e){
                    }
                    //tut_location_snackbar.dismiss();

                }


                try {
                    //Log.e("latitude", "inside latitude--" + thislat);
                    addresses = geocoder.getFromLocation(thislat, thislng, 1);


                    if (addresses != null && addresses.size() > 0) {
                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName();

                        //Log.i("Address", knownName);

                        //e_address.setText(address);
                        //Toast.makeText(context,"Get Address : "+address,Toast.LENGTH_LONG).show();
                        if(address!=null) {
                            bestPowerTV3.setText("Address : " + address);
                            fetchResult.setTextColor(Color.MAGENTA);
                            fetchResult.setText("Fetching weather data from server");
                        }


                        getMyWeather(thislat, thislng, new AssembleWeatherInfo() {
                            @Override
                            public void OnAssemblingWeatherInfo(List<JSONObject> jsonObjectList) {

                                List<JSONObject> finaljsonObjects = new ArrayList<>();

                                for(int i=1;i<jsonObjectList.size()-1;i++){

                                    if(finaljsonObjects.size()>=72)
                                        break;
                                    try {
                                        JSONObject object0 = jsonObjectList.get(i);
                                        Log.i("WeatherData",object0.getString("deg"));
                                        Double speed0 = Double.parseDouble(object0.getString("speed"));
                                        Double deg0 = Double.parseDouble(object0.getString("deg"));


                                        JSONObject object3 = jsonObjectList.get(i+1);
                                        Double speed3 = Double.parseDouble(object3.getString("speed"));
                                        Double deg3 = Double.parseDouble(object3.getString("deg"));

                                        Double m1 = (double)(speed3-speed0)/3;
                                        Double m2 = (double)(deg3-deg0)/3;
                                        Double c1 = speed0;
                                        Double c2 = deg0;

                                        Double speedy1 = m1*1 + c1;  // First interpolated value
                                        Double speedy2 = m1*2 + c1;  //Second Interpolated value

                                        Double degy1 = m2*1 + c2;
                                        Double degy2 = m2*2 + c2;

                                        JSONObject interpolatedObj1 = new JSONObject();
                                        interpolatedObj1.put("speed",speedy1);
                                        interpolatedObj1.put("deg",degy1);
                                        JSONObject interpolatedObj2 = new JSONObject();
                                        interpolatedObj2.put("speed",speedy2);
                                        interpolatedObj2.put("deg",degy2);

                                        finaljsonObjects.add(object0);
                                        finaljsonObjects.add(interpolatedObj1);
                                        finaljsonObjects.add(interpolatedObj2);
                                        finaljsonObjects.add(object3);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    catch (Exception e1){}
                                }

                                if(finaljsonObjects.size() >= 72){

                                    JSONObject obj1 = new JSONObject();
                                    List<String> fields = new ArrayList<>();
                                    //fields.add(doubleQuotedString("speed"));
                                    //fields.add(doubleQuotedString("dog"));
                                    fields.add("speed");
                                    fields.add("deg");
                                    try {
                                        obj1.put("fields",fields);
                                        Log.i("FinalL",obj1.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    //JSONObject obj2 = new JSONObject();
                                    List<List<String>> values = new ArrayList<>();
                                    try {
                                        for (int i = 0; i < finaljsonObjects.size(); i++) {

                                            JSONObject curr = finaljsonObjects.get(i);


                                            List<String> list2 = new ArrayList<>();
                                            double speedfactor,degreefactor;
                                            speedfactor = (double)1/25;
                                            degreefactor = (double)1/360;
                                            Double speed,deg;
                                            speed = Double.parseDouble(curr.getString("speed"))*speedfactor;
                                            deg = Double.parseDouble(curr.getString("deg"))*degreefactor;
                                            list2.add(String.valueOf(speed));
                                            list2.add(String.valueOf(deg));
                                            //list2.add(curr.getString("speed"));
                                            //list2.add(curr.getString("deg"));

                                            values.add(list2);

                                        }
                                        obj1.put("values", values);
                                        exportedJSON = obj1;
                                        if(myAccessToken.length()>1){
                                            // It means we have succesfully retrieved our access token
                                            getFinalPrediction();
                                        }else{
                                            //noinspection deprecation
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(myAccessToken.length()>1){
                                                        // It means we have succesfully retrieved our access token
                                                        getFinalPrediction();
                                                    }else
                                                        Toast.makeText(context,"Network connectivity issue",Toast.LENGTH_SHORT).show();

                                                }
                                            },500);
                                        }
                                        Log.i("fofofo",exportedJSON.toString());

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }
                        });


                        thislocationManager.removeUpdates(thislistener);
                        //locationTxt.setText(address + " " + city + " " + country);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                }
                if (thislocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    thislocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            minTime,
                            minDis,
                            thislistener);
                else
                    thislocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            minTime,
                            minDis,
                            thislistener);
            }

            @Override
            public void onProviderDisabled(String provider) {

                if(thislat == null) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    Toast.makeText(context, "Please enable your location from settings.", Toast.LENGTH_SHORT).show();
                }

            }
        };

        if(thislocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            thislocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDis,
                    thislistener);
        else
            thislocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDis,
                    thislistener);
    }

    public String getAddressFromLocation(double thislat,double thislng){

        Geocoder geocoder;
        List<Address> addresses = null;
        String address="";
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(thislat, thislng, 1);


            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                //tuitionaddress = address;
                //Toast.makeText(context,"Get Address Function : "+address,Toast.LENGTH_LONG).show();
                //e_address.setText(address);

                //Log.i("Address", knownName);

                //locationTxt.setText(address + " " + city + " " + country);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return address;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == GET_LOCATION_REQUEST &&  (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            tracelocation();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public interface AssembleWeatherInfo{
        void OnAssemblingWeatherInfo(List<JSONObject> jsonObjectList);
    }

    public void getMyWeather(Double lat, Double lng, final AssembleWeatherInfo callback){

        String apiKey="132607bc82173a65fe0830e01a1674f3";
        String fetchUrl;

        fetchUrl = "https://api.openweathermap.org/data/2.5/forecast?lat="+lat+"&lon="+lng+"&appid="+apiKey;
        //e.g -  "https://api.openweathermap.org/data/2.5/forecast?lat=23&lon=139&appid=132607bc82173a65fe0830e01a1674f3"

        GetWeatherData task = new GetWeatherData(new GetWeatherCallback() {
            @Override
            public void OnGettingWeatherData(JSONArray jsonArray) {
                List<JSONObject> objects = new ArrayList<>();
                int pos = 0;
                for(int i=0; i<jsonArray.length();i++){

                    try {
                        JSONObject jsonPart = jsonArray.getJSONObject(i).getJSONObject("wind");
                        objects.add(jsonPart);
                        try{
                            bestPowerTV2.setText("Wind speed : "+jsonPart.getString("speed")+"m/s"+"   Wind degrees : "+
                                    jsonPart.getString("deg"));

                            fetchResult.setText("Generating Wind Farm Power Prediction");
                            fetchResult.setTextColor(Color.BLUE);
                        }catch (Exception e){}
                        Log.i("WeatherInfoOfDay",jsonPart.getString("speed"));
                        pos++;
                        if(pos == jsonArray.length()){
                            callback.OnAssemblingWeatherInfo(objects);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(pos<jsonArray.length()){
                    Toast.makeText(context,"Some error occured in network.Please restart the app.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        task.execute(fetchUrl);
        timeslots = generateTimeSlots(72,60);



        //GetServerResponse serverResponse = new GetServerResponse(context,0);
        //serverResponse.onPreExecute();
        //serverResponse.doInBackground();


    }

    public void getAccessToken(){
        GetAccessToken getAccessToken = new GetAccessToken(new AccessTokenApi() {
            @Override
            public void OnGettingAccessToken(String token) {
                myAccessToken = token;
            }
        });
        getAccessToken.execute("https://iam.cloud.ibm.com/identity/token");

    }
    public void getFinalPrediction(){

        Log.i("GetFinalPrediction","true");
        RequestPowerOutput requestPowerOutput = new RequestPowerOutput(context, new PredictionResultApi() {
            @Override
            public void OnGettingPredictionResult(List<Double> predictionList) {

                currPredictionList = new ArrayList<>(predictionList);
                progressBar.setVisibility(View.GONE);
                fetchResult.setText("Done");
                fetchResult.setTextColor(Color.GREEN);

                try {
                    if (mp.isPlaying()) {
                        mp.stop();
                        mp.release();
                        mp = MediaPlayer.create(context, R.raw.btnclick1);
                    } mp.start();
                } catch(Exception e) { e.printStackTrace(); }
                ObjectAnimator animation2 = ObjectAnimator.ofFloat(getDataBtn, "translationX", 300f,-200f,-100f,-100f,50f);
                animation2.setDuration(500);
                animation2.start();

                Double value = predictionList.get(0);
                for(int i=0;i<predictionList.size();i++){
                    value = value>predictionList.get(i)?value:predictionList.get(i);

                }

                int index = predictionList.indexOf(value);
                bestPowerTV1.setText("From "+timeslots.get(index)+" get "+String.valueOf((int)(value*3600))+"KW"+" max power");


            }
        });
                String baseUrl = "https://eu-gb.ml.cloud.ibm.com/v3/wml_instances/de8b904f-69b1-4926-bd62-5c5c65893cf8/deployments/c76bf77f-f7e7-49a5-ad13-83f62b8cdd2d/online";
        Log.i("zozozo",exportedJSON.toString());
        requestPowerOutput.execute(baseUrl,myAccessToken,exportedJSON.toString());

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(bestPowerTV3.getText().toString().compareTo("")==0)
            getData();
    }

    public String doubleQuotedString(String string){
        if (TextUtils.isEmpty(string))
            return "";

        final int lastPos = string.length() - 1;
        if (lastPos < 0 || (string.charAt(0) == '"' && string.charAt(lastPos) == '"'))
            return string;


        return "\"" + string + "\"";
    }

    public void getCurrentDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss aa");
        String datetime = dateformat.format(c.getTime());
        Log.i("CURR_DATE",datetime);
    }
    public ArrayList<String> generateTimeSlots(Integer n,Integer timediff){
        ArrayList<String> timeslots = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        String currentTime = new SimpleDateFormat("dd-MM-yyyy hh:mm aa", Locale.getDefault()).format(new Date());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");
        Date d = null;
        try {
            d = df.parse(currentTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);

            for(int i =0;i<n;i++) {
                String startTime = df.format(cal.getTime());
                cal.add(Calendar.MINUTE, timediff);
                String endTime = df.format(cal.getTime());

                String slot = startTime.substring(11) +"-"+endTime.substring(11);
                timeslots.add(slot);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timeslots;
    }

}
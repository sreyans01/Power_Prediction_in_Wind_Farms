package com.ibm.energyoptimizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

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
import java.util.Map;

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

    private TextView fetchResult,bestPowerTV1,bestPowerTV3,bestTimeTV1;
    private TextView windspeedTV1,windspeedTV2,windspeedTV3,weatherTV;
    private ImageView weatherIcon;
    private Button getDataBtn;

    private List<Double> currPredictionList;
    private MediaPlayer mp;

    private ProgressBar progressBar;

    private SharedPreferences addressPref;
    private TextView changeAddress;

    private List<Double> powerList;

    private String funcAccessToken;

    private String maxPowerData = "none";
    private SharedPreferences maxPowDurPref;
    private TextView infoTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        mTitle.setText("Energy Optimizer");

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        

        addressPref = getSharedPreferences("Loc Details",MODE_PRIVATE);
        maxPowDurPref = getSharedPreferences("Max Power Duration",MODE_PRIVATE);

        currPredictionList = new ArrayList<>();
        mp = MediaPlayer.create(context, R.raw.btnclick1);
        allfindviewbyids();
        HandleOnClicks();
        progressBar.setVisibility(View.VISIBLE);

        setHourBtn(1);

        if(addressPref.getBoolean("Has Address",false)==false){

            openAddressDialog();
        }else {
            getData2();
        }


    }

    public void getData(){

        fetchResult.setText("Fetching Your Address");
        fetchResult.setTextColor(Color.parseColor("#F44336"));
        tracelocation();
        getAccessToken(1);
        getFunctionAccessToken();
    }

    public void getData2(){

        getAccessToken(2);

        String address = addressPref.getString("Address","Please add your address");
        bestPowerTV3.setText("Address : " + address);
        fetchResult.setTextColor(Color.MAGENTA);
        fetchResult.setText("Fetching weather data from server");
        getFunctionAccessToken();

    }
    public void allfindviewbyids(){

        getDataBtn = findViewById(R.id.getDataBtn);
        fetchResult = findViewById(R.id.fetchResult);
        bestPowerTV1 = findViewById(R.id.bestPowerTV1);
        bestTimeTV1 = findViewById(R.id.bestTimeTV1);

        bestPowerTV3 = findViewById(R.id.bestPowerTV3);
        progressBar = findViewById(R.id.progressBar);

        windspeedTV1 = findViewById(R.id.windspeedTV1);
        windspeedTV2 = findViewById(R.id.windspeedTV2);
        windspeedTV3 = findViewById(R.id.windspeedTV3);
        weatherTV = findViewById(R.id.weatherTV);
        weatherIcon = findViewById(R.id.weatherIcon);
        changeAddress = findViewById(R.id.changeAddress);

        infoTV = findViewById(R.id.infoTV);


    }

    public void HandleOnClicks(){
        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);
        Button btn3 = findViewById(R.id.btn3);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setHourBtn(1);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setHourBtn(2);
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setHourBtn(3);
            }
        });

        infoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(fetchResult.getText().toString().compareTo("Done")!=0 || fetchResult.getText().toString().compareTo("Refreshing Data")==0){
                    Toast.makeText(context,"Please wait while we fetch the data",Toast.LENGTH_SHORT).show();
                    return;
                }

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                bottomSheetDialog.setContentView(R.layout.dialog_currtimeslot);
                TextView tapTV = bottomSheetDialog.findViewById(R.id.tapTV);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                bottomSheetDialog.setCancelable(true);

                TextView textView = bottomSheetDialog.findViewById(R.id.text);
                String s = maxPowDurPref.getString("MaxPowerDur","24");
                textView.setText("The current max power output data is shown according to the next "+s+" hours.To increase or decrease the duration tap below.");

                LinearLayout layout = bottomSheetDialog.findViewById(R.id.layout);
                layout.setVisibility(View.GONE);

                tapTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        layout.setVisibility(View.VISIBLE);
                    }
                });
                EditText editText = bottomSheetDialog.findViewById(R.id.editText);
                Button submitBtn = bottomSheetDialog.findViewById(R.id.submitBtn);

                submitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(TextUtils.isEmpty(editText.getText())){
                            Toast.makeText(context,"Press back to dismiss",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int hours = Integer.parseInt(editText.getText().toString());
                        if(hours<5 || hours>72){
                            Toast.makeText(context,"Enter a value between 5 and 72",Toast.LENGTH_SHORT).show();

                        }else {
                            maxPowDurPref.edit().putString("MaxPowerDur", String.valueOf(hours)).commit();
                            progressBar.setVisibility(View.VISIBLE);
                            fetchResult.setText("Refreshing Data");
                            fetchResult.setTextColor(ContextCompat.getColor(context, R.color.blue1));
                            getHourlyPrediction();
                            bottomSheetDialog.dismiss();
                        }
                    }
                });
                bottomSheetDialog.show();

            }
        });

        changeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddressDialog();
            }
        });
        getDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(fetchResult.getText().toString().compareTo("Done")!=0 || fetchResult.getText().toString().compareTo("Refreshing Data")==0){
                    Toast.makeText(context,"Please wait while we fetch the data",Toast.LENGTH_SHORT).show();
                    return;
                }

                ObjectAnimator animation2 = ObjectAnimator.ofFloat(getDataBtn, "translationX", 50f,-50f);
                animation2.setDuration(200);
                //animation2.start();

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
    }
    public void getCurrentLoc(){


    }
    public Location getLocation2FromAddress(String strAddress){
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        Location p1 = new Location(LocationManager.GPS_PROVIDER);

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1.setLongitude(location.getLongitude());
            p1.setLatitude(location.getLatitude());
        }
        catch (IOException ex){

        }
        return p1;
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

                            addressPref.edit().putString("Lat",String.valueOf(thislat)).commit();
                            addressPref.edit().putString("Lng",String.valueOf(thislng)).commit();
                            addressPref.edit().putString("Address",address).commit();
                            addressPref.edit().putBoolean("Has Address",true).commit();
                        }

                        getWeather(thislat,thislng);


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
                try {

                    JSONObject jsonPart_main = jsonArray.getJSONObject(0).getJSONObject("main");
                    Log.i("KKKKKK",jsonArray.getJSONObject(0).getString("weather").toString());
                    String s = jsonArray.getJSONObject(0).getString("weather").toString();
                    s = s.replace("[","");
                    JSONObject jsonPart_weather = new JSONObject(s);

                    Double temp = Double.parseDouble(jsonPart_main.getString("temp"));
                    temp = temp - 273.15;
                    weatherTV.setText(String.valueOf((temp.intValue())) + "°C");
                    String weatherInfo = jsonPart_weather.getString("description");
                    windspeedTV3.setText("Description : "+weatherInfo);
                    setIcon(weatherInfo);
                }catch (Exception e){
                    Toast.makeText(context,"haha",Toast.LENGTH_SHORT).show();
                }
                int pos = 0;
                for(int i=0; i<jsonArray.length();i++){

                    try {

                        JSONObject jsonPart = jsonArray.getJSONObject(i).getJSONObject("wind");
                        objects.add(jsonPart);
                        try{
                            windspeedTV1.setText("Wind speed : "+jsonPart.getString("speed")+"m/s");
                            windspeedTV2.setText("Wind degrees : "+ jsonPart.getString("deg")+"°");

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

    public void getFunctionAccessToken(){

        GetFunctionAccessToken accessToken = new GetFunctionAccessToken(new AccessTokenApi() {
            @Override
            public void OnGettingAccessToken(String token) {

                funcAccessToken = token;

            }
        });
        accessToken.execute("https://iam.bluemix.net/oidc/token");
    }
    public void getAccessToken(int id){
        GetAccessToken getAccessToken = new GetAccessToken(new AccessTokenApi() {
            @Override
            public void OnGettingAccessToken(String token) {
                myAccessToken = token;
                if(id==2){
                    Double lat = Double.valueOf(addressPref.getString("Lat","0"));
                    Double lng = Double.valueOf(addressPref.getString("Lng","0"));
                    getWeather(lat,lng);
                }
            }
        });
        getAccessToken.execute("https://iam.cloud.ibm.com/identity/token");

    }
    public void getFinalPrediction(){

        Log.i("GetFinalPrediction","true");
        RequestPowerOutput requestPowerOutput = new RequestPowerOutput(context, new PredictionResultApi() {
            @Override
            public void OnGettingPredictionResult(List<Double> predictionList) {

                powerList = new ArrayList<>(predictionList);
                getHourlyPrediction();
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
                ObjectAnimator animation2 = ObjectAnimator.ofFloat(getDataBtn, "translationX", 300f,-200f,-100f,-100f,0f);
                animation2.setDuration(500);
                animation2.start();


                Double value = predictionList.get(0);
                for(int i=0;i<predictionList.size();i++){
                    value = value>predictionList.get(i)?value:predictionList.get(i);

                }

                int index = predictionList.indexOf(value);
                bestPowerTV1.setText(String.valueOf((int)(value*3600))+"KW");
                bestTimeTV1.setText(timeslots.get(predictionList.indexOf(value)));


            }
        });
                String baseUrl = "https://eu-gb.ml.cloud.ibm.com/v3/wml_instances/e8b2e1c3-9068-458f-92ff-7b1b413da3af/deployments/92157151-024d-476c-9919-84cbffcea543/online";
        Log.i("zozozo",exportedJSON.toString());
        requestPowerOutput.execute(baseUrl,myAccessToken,exportedJSON.toString());

    }

    @Override
    protected void onResume() {
        super.onResume();
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

    public void setHourBtn(int n){
        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);
        Button btn3 = findViewById(R.id.btn3);
        if(n==3){

            if(fetchResult.getText().toString().compareTo("Done")!=0){
                Toast.makeText(context,"Please wait till the data is fetched.",Toast.LENGTH_SHORT).show();
                return;
            }

            btn3.setBackgroundColor(ContextCompat.getColor(context,R.color.blue1));
            btn3.setTextColor(ContextCompat.getColor(context,R.color.white));

            btn2.setBackgroundResource(android.R.drawable.btn_default);
            btn2.setTextColor(ContextCompat.getColor(context,R.color.black));
            btn1.setTextColor(ContextCompat.getColor(context,R.color.black));
            btn1.setBackgroundResource(android.R.drawable.btn_default);

            if(maxPowerData.compareTo("none")!=0){

                    Log.i("cococo", maxPowerData);
                    setStartEndTime(n);


                return;
            }

            Double value = powerList.get(0)+powerList.get(1)+powerList.get(2);
            for(int i=1;i<powerList.size()-2;i++){
                Double pow = powerList.get(i)+powerList.get(i+1)+powerList.get(i+2);
                value = value>pow?value:pow;
            }

            List<Double> threeList = new ArrayList<>();
            for(int i=0;i<powerList.size()-2;i++){
                Double pow = powerList.get(i)+powerList.get(i+1)+powerList.get(i+2);
                threeList.add(pow);
            }
            String startTime[] = timeslots.get(threeList.indexOf(value)).split("-");
            String endTime[]   = timeslots.get(threeList.indexOf(value)+2).split("-");
            bestPowerTV1.setText(String.valueOf((int)((value*3600)/n))+"KW");
            //Log.i("POPOPO",String.valueOf(startindex)+"   "+String.valueOf(endindex)+"   "+startTime[0]+"to"+endTime[1]+String.valueOf(value));
            bestTimeTV1.setText(startTime[0]+"-"+endTime[1]);


        }else if(n==2){


            if(fetchResult.getText().toString().compareTo("Done")!=0){
                Toast.makeText(context,"Please wait till the data is fetched.",Toast.LENGTH_SHORT).show();
                return;
            }

            btn2.setBackgroundColor(ContextCompat.getColor(context,R.color.blue1));
            btn2.setTextColor(ContextCompat.getColor(context,R.color.white));

            btn3.setBackgroundResource(android.R.drawable.btn_default);
            btn1.setBackgroundResource(android.R.drawable.btn_default);
            btn3.setTextColor(ContextCompat.getColor(context,R.color.black));
            btn1.setTextColor(ContextCompat.getColor(context,R.color.black));
            if(maxPowerData.compareTo("none")!=0){

                    Log.i("cococo", maxPowerData);
                    setStartEndTime(n);


                return;
            }

            Double value = powerList.get(0)+powerList.get(1);
            for(int i=0;i<powerList.size()-1;i++){
                Double pow = powerList.get(i)+powerList.get(i+1);
                value = value>pow?value:pow;

            }

            List<Double> twoList = new ArrayList<>();
            for(int i=0;i<powerList.size()-1;i++){
                Double pow = powerList.get(i)+powerList.get(i+1);
                twoList.add(pow);
            }
            String startTime[] = timeslots.get(twoList.indexOf(value)).split("-");
            String endTime[]   = timeslots.get(twoList.indexOf(value)+1).split("-");
            bestPowerTV1.setText(String.valueOf((int)((value*3600)/n))+"KW");
            bestTimeTV1.setText(startTime[0]+"-"+endTime[1]);
        }else{



            btn1.setBackgroundColor(ContextCompat.getColor(context,R.color.blue1));
            btn1.setTextColor(ContextCompat.getColor(context,R.color.white));

            btn2.setBackgroundResource(android.R.drawable.btn_default);
            btn3.setBackgroundResource(android.R.drawable.btn_default);
            btn2.setTextColor(ContextCompat.getColor(context,R.color.black));
            btn3.setTextColor(ContextCompat.getColor(context,R.color.black));


            if(fetchResult.getText().toString().compareTo("Done")!=0){
                Toast.makeText(context,"Please wait till the data is fetched.",Toast.LENGTH_SHORT).show();
                return;
            }
            if(maxPowerData.compareTo("none")!=0){

                    Log.i("cococo", maxPowerData);
                    setStartEndTime(n);


                return;
            }
            Double value = powerList.get(0);
            for(int i=0;i<powerList.size();i++){
                Double pow = powerList.get(i);
                value = value>pow?value:pow;

            }
            bestPowerTV1.setText(String.valueOf((int)((value*3600)/n))+"KW");
            bestTimeTV1.setText(timeslots.get(powerList.indexOf(value)));
        }

    }

    public void setStartEndTime(int n){
        try {
            String split[] = maxPowerData.split(" ");
            String mins  = split[(n*2)-1];
            String power = split[(n*2) - 2];

            Log.i("hihihi",mins+"     "+power);
            Double value = Double.parseDouble(power);
            String startTime, endTime;
            bestPowerTV1.setText(String.valueOf((value.intValue())) + "KW");

            String currentTime = new SimpleDateFormat("dd-MM-yyyy hh:mm aa", Locale.getDefault()).format(new Date());

            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");
            Date d = null;
            d = df.parse(currentTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            int min = Integer.parseInt(mins);
            cal.add(Calendar.MINUTE, min);
            startTime = df.format(cal.getTime());
            cal.add(Calendar.MINUTE, 60*n);
            endTime = df.format(cal.getTime());
            String startTime1 = startTime.substring(11);
            String endTime1   = endTime.substring(11);
            Log.i("cicici",startTime1+"    "+endTime1);
            bestTimeTV1.setText(startTime1 + "-" + endTime1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setIcon(String weatherInfo){


        if(weatherInfo.contains("clouds")){

            if(weatherInfo.compareToIgnoreCase("few clouds")==0){
                weatherIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.wicon_cloudwithsun));
            }else if(weatherInfo.compareToIgnoreCase("broken clouds")==0 || weatherInfo.contains("scattered clouds")){
                weatherIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.wicon_cloudwithsun2));
            }else if(weatherInfo.contains("thunderstorm") || weatherInfo.contains("overcast")){
                weatherIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.wicon_cloudwiththunderstorm));
            }else if(weatherInfo.contains("rain")){
                weatherIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.wicon_cloudswithrain2));
            }
            else {
                weatherIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.wicon_cloudwithsun3));
            }

        }else if(weatherInfo.equalsIgnoreCase("clear sky")){

            weatherIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.wicon_sunny));
        }else{

            weatherIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.wicon_cloudwithsun));
        }
    }

    public void openAddressDialog(){

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.dialog_getaddress);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        bottomSheetDialog.setCancelable(true);

        Button writeBtn,trackBtn,submitBtn;

        writeBtn = bottomSheetDialog.findViewById(R.id.writeBtn);
        trackBtn = bottomSheetDialog.findViewById(R.id.trackBtn);
        submitBtn = bottomSheetDialog.findViewById(R.id.submit);

        EditText editText = bottomSheetDialog.findViewById(R.id.e_address);
        LinearLayout layout = bottomSheetDialog.findViewById(R.id.addressLayout);

        if(addressPref.getBoolean("Has Address",false)){
            editText.setText(addressPref.getString("Address","Please type your address"));
        }

        trackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
                bottomSheetDialog.dismiss();
            }
        });

        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.setVisibility(View.VISIBLE);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Location location = getLocation2FromAddress(editText.getText().toString());
                    if(location == null){
                        Toast.makeText(context,"The address you entered isn't valid.",Toast.LENGTH_SHORT).show();

                        return;
                    }
                    addressPref.edit().putString("Lat",String.valueOf(location.getLatitude())).commit();
                    addressPref.edit().putString("Lng",String.valueOf(location.getLongitude())).commit();
                    addressPref.edit().putString("Address",editText.getText().toString()).commit();
                    addressPref.edit().putBoolean("Has Address",true).commit();
                    Toast.makeText(context,"Address Updated Successfully",Toast.LENGTH_SHORT).show();
                    getData2();
                    bottomSheetDialog.dismiss();

                }catch (Exception e){
                    Toast.makeText(context,"The address you entered may not be valid. Use track location button instead or try again.",Toast.LENGTH_SHORT).show();
                }

            }
        });

        bottomSheetDialog.show();
    }

    public void getWeather(Double thislat,Double thislng){


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

    }


    public void getHourlyPrediction(){

        Log.i("hohoho",myAccessToken);
        Log.i("hohohg",funcAccessToken);



        RequestTimeSlotPowerOutput timeSlotPowerOutput = new RequestTimeSlotPowerOutput(context, new MaxPredictionApi() {
            @Override
            public void OnGettingMaxPowerOut(String data) {

                progressBar.setVisibility(View.GONE);
                fetchResult.setText("Done");
                fetchResult.setTextColor(Color.GREEN);
                if(data.compareTo("none")==0){
                    maxPowerData = data;
                    return;
                }
                try{
                    JSONObject jsonObject = new JSONObject(data);
                    maxPowerData = jsonObject.getString("Result");
                    setHourBtn(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        try {
            String baseUrl = "https://eu-gb.ml.cloud.ibm.com/v3/wml_instances/89def234-5ebd-4907-a37b-e59d988f9286/deployments/86b6f00d-1a2c-4afa-b400-0ca3b1aa8b76/online";
            List<Integer> data = new ArrayList<>();
            int hours = Integer.parseInt(maxPowDurPref.getString("MaxPowerDur","24"));

            for(int i=0;i<hours;i++){
                 Integer integer = (int)(powerList.get(i)*3600);
                 data.add(integer);
            }


            JSONObject jsonObject = new JSONObject();
            jsonObject.put("values",data.toString());
            Log.i("KKKKKK",jsonObject.toString());




            timeSlotPowerOutput.execute(baseUrl,funcAccessToken,jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
package com.ibm.energyoptimizer;

import android.os.AsyncTask;
import android.util.Log;

import com.ibm.energyoptimizer.PojoClasses.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("ALL")
public class GetWeatherData extends AsyncTask<String,Void,String> {
    public JSONArray jsonArray;
    public GetWeatherCallback getWeatherCallback;

    public GetWeatherData(GetWeatherCallback getWeatherCallback){
        this.getWeatherCallback = getWeatherCallback;
    }

    @Override
    protected String doInBackground(String... urls) {

        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection)url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();
            while(data != -1){
                char current = (char)data;
                result += current;
                data = reader.read();

            }
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(result==null)
            return;
        try {
            JSONObject jsonObject = new JSONObject(result);
            String weatherInfo = jsonObject.getString("list");

            jsonArray = new JSONArray(weatherInfo);

            getWeatherCallback.OnGettingWeatherData(jsonArray);
            for(int i=0; i<jsonArray.length();i++){

                JSONObject jsonPart = jsonArray.getJSONObject(i);
            }
            Log.i("Weather content",weatherInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }


            Log.i("Website content",result);
    }
}

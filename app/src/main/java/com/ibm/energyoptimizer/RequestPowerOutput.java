package com.ibm.energyoptimizer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL")
public class RequestPowerOutput extends AsyncTask<String,Void,String> {
    public JSONArray jsonArray;
    public PredictionResultApi predictionResultApi;
    private List<Double> predictedList;

    public RequestPowerOutput(Context context, PredictionResultApi predictionResultApi) {
        this.predictionResultApi = predictionResultApi;
    }


    @Override
    protected String doInBackground(String... urls) {

        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("POST");

            JSONObject object = new JSONObject();
            object.put("Content-Type","application/json");
            object.put("Authorization","Bearer "+urls[1]);
            object.put("ML-Instance-ID","de8b904f-69b1-4926-bd62-5c5c65893cf8");
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setRequestProperty("Authorization","Bearer "+urls[1]);
            urlConnection.setRequestProperty("ML-Instance-ID","de8b904f-69b1-4926-bd62-5c5c65893cf8");
            Log.i("kokok",object.toString());
            urlConnection.setDefaultUseCaches(false);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            /*
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("grant_type","urn:ibm:params:oauth:grant-type:apikey");
            jsonObject.put("apikey","pm21W0cT04V5q0HDQmfL8_xI0zn-PwzELPkO1a6Ua3ln");

             */

            String dataToBePushed = urls[2];
            String newdata = dataToBePushed.replace(":\"",":");
            newdata = newdata.replace("speed","\"speed\"");
            newdata = newdata.replace("deg","\"deg\"");
            newdata = newdata.replace("]\"","]");
            Log.i("fofofo",newdata);


            OutputStream os = urlConnection.getOutputStream();
            os.write(newdata.getBytes());
            os.flush();
            os.close();

            InputStream in = (urlConnection.getInputStream());
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
        } catch (JSONException e) {
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
            String weatherInfo = jsonObject.getString("values");

            weatherInfo = weatherInfo.replace("[","");
            weatherInfo = weatherInfo.replace("]","");
            Log.i("Weather content5",weatherInfo);


            String split[] = weatherInfo.split(",");
            Double prediction;
            predictedList = new ArrayList();
            for(int i = 0;i<split.length;i=i+3){
                prediction = Double.parseDouble(split[i]);
                predictedList.add(prediction);
            }
           predictionResultApi.OnGettingPredictionResult(predictedList);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.i("Website content3",result);
    }
}

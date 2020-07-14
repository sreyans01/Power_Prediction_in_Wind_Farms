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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class RequestTimeSlotPowerOutput extends AsyncTask<String,Void,String> {
    public JSONArray jsonArray;
    public PredictionResultApi predictionResultApi;
    private MaxPredictionApi maxPredictionApi;
    private List<Double> predictedList;

    public RequestTimeSlotPowerOutput(Context context, MaxPredictionApi maxPredictionApi) {
        this.maxPredictionApi = maxPredictionApi;
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


            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setRequestProperty("Authorization","Bearer "+urls[1]);
            urlConnection.setRequestProperty("ML-Instance-ID","89def234-5ebd-4907-a37b-e59d988f9286");
            Log.i("kokok",urls[1]);
            urlConnection.setDefaultUseCaches(false);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            /*
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("grant_type","urn:ibm:params:oauth:grant-type:apikey");
            jsonObject.put("apikey","pm21W0cT04V5q0HDQmfL8_xI0zn-PwzELPkO1a6Ua3ln");

             */

            String dataToBePushed = urls[2];
            String newdata = dataToBePushed.replace("\"[","[\"");
            newdata = newdata.replace("]\"","\"]");
            newdata = newdata.replace(","," ");
            newdata = newdata.replace("  "," ");

            //newdata = "{\"values\": [\"1 2 3 4 5 6 7 8 9 1 2 3 4 5 6 7 8 9 1 2 3 4 5 5\"]}";
            //newdata = "{\"values\":[\"76  79  82  85  85  105  118  111  111  139  172  192  192  275  401  571  571  548  526  505  505  464  409  358\"]}";


            Log.i("cococo",newdata);


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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);



        if(result==null) {
            maxPredictionApi.OnGettingMaxPowerOut("none");
            return;
        }
        try {

         maxPredictionApi.OnGettingMaxPowerOut(result);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}

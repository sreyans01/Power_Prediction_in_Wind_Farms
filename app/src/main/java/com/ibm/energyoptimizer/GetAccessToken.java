package com.ibm.energyoptimizer;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ibm.energyoptimizer.PojoClasses.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

@SuppressWarnings("ALL")
public class GetAccessToken extends AsyncTask<String,Void,String> {
    public JSONArray jsonArray;
    public AccessTokenApi accessTokenApi;


    public GetAccessToken(AccessTokenApi accessTokenApi) {
        this.accessTokenApi = accessTokenApi;
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

            urlConnection.setRequestProperty("Content-type","application/x-www-form-urlencoded");
            urlConnection.setDefaultUseCaches(false);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            /*
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("grant_type","urn:ibm:params:oauth:grant-type:apikey");
            jsonObject.put("apikey","pm21W0cT04V5q0HDQmfL8_xI0zn-PwzELPkO1a6Ua3ln");

             */

            String dataToBePushed = "grant_type=urn:ibm:params:oauth:grant-type:apikey&apikey=pm21W0cT04V5q0HDQmfL8_xI0zn-PwzELPkO1a6Ua3ln";


            OutputStream os = urlConnection.getOutputStream();
            os.write(dataToBePushed.getBytes());
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
            String tokenInfo = jsonObject.getString("access_token");

            accessTokenApi.OnGettingAccessToken(tokenInfo);
            Log.i("Access Token",tokenInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.i("Website content",result);
    }
}

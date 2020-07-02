package com.ibm.energyoptimizer;

import org.json.JSONArray;

public interface GetWeatherCallback {
    void OnGettingWeatherData(JSONArray jsonArray);
}

# Power_Prediction_in_Wind_Farms
A prdeiction model with an end user android application to detect the Power Grid for maximum Power Unit Prediction in Wind Farms.
The app tracks the exact location coordinates of the user and fetches the weayher data accordingly. Using the interpolation techniques, we improve the weather data errors(if any).
In the next step, the app sends a request to the NODE_RED server and gets the access token from the server.
Once, we receive the access token, we finally send another request to the API to get our final prediction data.
In the end, we plot a graph of Power VS Timings and display it to the user, and also give the user a complete 72 hour forecast of the Wind-Farm-Power-Prediction.

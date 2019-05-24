package com.example.myapplication;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.listener.UpdateListener;
import com.example.myapplication.service.LocationService;


import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements View.OnClickListener, UpdateListener {

    double latitude, longitude;
    TextView latTW, longTW, provTW;
    Button startBTN, stopBTN;
    private ProgressDialog dialog;
    public static final String log = "LOG: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latTW = (TextView) findViewById(R.id.lat_tw);
        longTW = (TextView) findViewById(R.id.long_tw);
        provTW = (TextView) findViewById(R.id.providerTW);
        startBTN = (Button) findViewById(R.id.start_btn);
        startBTN.setOnClickListener(this);
        stopBTN = (Button) findViewById(R.id.stop_btn);
        stopBTN.setOnClickListener(this);
        Config.context = this;
        startService(new Intent(this, LocationService.class));
        setCoordOnTW();
        CurrentLocation currentLocation = new CurrentLocation();
        currentLocation.addListener(this);





    }

    private void setCoordOnTW () {
        if (CurrentLocation.getCurrentLocation()!=null) {
            provTW.setText(getString(R.string.providerTW) + " " + CurrentLocation.getProvider());
            latTW.setText(getString(R.string.latitude) + " " + CurrentLocation.getCurrentLocation().getLatitude());
            longTW.setText(getString(R.string.longitude) + " " + CurrentLocation.getCurrentLocation().getLongitude());
        }
    }

    @Override
    public void onClick(View view) {
        //new RequestTask().execute("http://grope.io/testapp/savecoord.php");
        switch (view.getId()){
            case R.id.start_btn:
                startService(new Intent(this, LocationService.class));
                break;
            case R.id.stop_btn:
                stopService(new Intent(this, LocationService.class));
                break;
        }
    }

    @Override
    public void onPositionChanged() {
        setCoordOnTW();
    }

    @Override
    public void onProviderChanged() {
        setCoordOnTW();
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("lat", Double.toString(latitude));
                jsonObject.put("long", Double.toString(longitude));

                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost postMethod = new HttpPost(params[0]);

                StringEntity se = new StringEntity(jsonObject.toString());
                postMethod.setEntity(se);
                postMethod.setHeader("Accept", "application/json");
                postMethod.setHeader("Content-type", "application/json");
                ResponseHandler responseHandler = new BasicResponseHandler();
                httpclient.execute(postMethod, responseHandler);
            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Sending...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
            super.onPreExecute();
        }
    }
}

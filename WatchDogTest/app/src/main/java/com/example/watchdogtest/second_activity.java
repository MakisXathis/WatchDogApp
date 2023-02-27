package com.example.watchdogtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class second_activity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    String publicIP;
    TextView pressureValue;
    TextView movementValue;
    TextView accelerometerValue;
    TextView systemStatusValue;
    TextView alarmStatusValue;
    Button doorLockSensorButton;
    Button systemStatusButton;
    Button deactivateAlarmButton;
    ImageButton refreshButton;

    String retrieveHTMLAsString(String htmlpage) throws MalformedURLException {

        FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                URL url = new URL(htmlpage);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                // Return the HTML code as a string
                return stringBuilder.toString();
            }
        });

        executor.execute(futureTask);
        String htmlCode="";
        try {
            htmlCode = futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return htmlCode;
    }

    private void makeRequest(String htmlpage){
        FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                URL url = new URL(htmlpage);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                // Return the HTML code as a string
                return stringBuilder.toString();
            }
        });

        executor.execute(futureTask);
        String htmlCode="";
        try {
            htmlCode = futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    HashMap<String, String> sensorValues(Document document){
        HashMap<String, String> kitchenCensorValues = new HashMap<String, String>();
        Element table = document.select("table").get(0);
        Element rows = table.select("tr").get(0);
        Elements columnNames = rows.select("th");
        List<String> columns = new ArrayList<String>();

        for (int i = 0; i < columnNames.size() ; i++){
            columns.add(columnNames.get(i).toString().replace("<th>","").replace("</th>",""));
        }

        rows = table.select("tr").get(1);
        Elements columnValues = rows.select("th");
        List<String> values = new ArrayList<String>();

        for (int i = 0; i < columnValues.size() ; i++){
            values.add(columnValues.get(i).toString().replace("<th>","").replace("</th>",""));
        }

        for (int i = 0; i < values.size() ; i++){
            if (values.get(i).contains("Lock Door")){
                kitchenCensorValues.put(columns.get(i), "Lock Door");
            } else if (values.get(i).contains("Unlock Door")){
                kitchenCensorValues.put(columns.get(i), "Unlock Door");
            }else {
                kitchenCensorValues.put(columns.get(i), values.get(i));
            }
        }

        return kitchenCensorValues;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Boolean activated_system = false;
        Boolean statusOk = false;
        Boolean active_alarm = false;



        // create the get Intent object
        Intent intent = getIntent();
        // receive the value by getStringExtra() method and
        // key must be same which is send by first activity
        publicIP = intent.getStringExtra("Public IP");
        // display the string into textView

        System.out.println("URL is: " +publicIP);
        String content = null;
        try {
            content = retrieveHTMLAsString(publicIP);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HashMap<String, String> kitchenCensorValues = new HashMap<String, String>();
        Document document = Jsoup.parse(content);
        kitchenCensorValues = sensorValues(document);

        for (String key : kitchenCensorValues.keySet()) {
            System.out.println(key);
            System.out.println(kitchenCensorValues.get(key));
        }

        Elements hrefs = document.select("a");

        for (Element currentHref : hrefs) {
            if (currentHref.toString().contains("/D")) {
                activated_system = true;
            } else if (currentHref.toString().contains("/A")) {
                activated_system = false;
            }
            if (currentHref.toString().contains("/H")) {
                active_alarm = true;
            }
        }

        Elements h1 = document.select("h1");

        if (h1.toString().contains("background-color:green;")) {
            statusOk = true;
        } else if (h1.toString().contains("background-color:red;")) {
            statusOk = false;
        }

        System.out.println("Active Alarm:"+active_alarm);
        System.out.println("Deactivated System:"+activated_system);
        System.out.println("Status Ok:"+statusOk);

        pressureValue = findViewById(R.id.pressureSensorValue);
        movementValue = findViewById(R.id.movementSensorValue);
        accelerometerValue = findViewById(R.id.accelerometerSensorValue);
        systemStatusValue = findViewById(R.id.statusValue);
        alarmStatusValue = findViewById(R.id.alarmStatusValue);
        systemStatusButton = findViewById(R.id.systemActivationButton);
        deactivateAlarmButton = findViewById(R.id.deactivateAlarmButton);
        doorLockSensorButton = findViewById(R.id.doorLockSensorButton);
        refreshButton = findViewById(R.id.refreshButton);

        pressureValue.setText(kitchenCensorValues.get("Air Pressure"));
        if (kitchenCensorValues.get("Air Pressure").equals("OK"))
            pressureValue.setTextColor(Color.parseColor("#15BD17"));
        else
            pressureValue.setTextColor(Color.parseColor("#FF0000"));
        movementValue.setText(kitchenCensorValues.get("Motion Sensor"));
        if (kitchenCensorValues.get("Motion Sensor").equals("OK"))
            movementValue.setTextColor(Color.parseColor("#15BD17"));
        else
            movementValue.setTextColor(Color.parseColor("#FF0000"));
        accelerometerValue.setText(kitchenCensorValues.get("Door Handle"));
        if (kitchenCensorValues.get("Door Handle").equals("OK"))
            accelerometerValue.setTextColor(Color.parseColor("#15BD17"));
        else
            accelerometerValue.setTextColor(Color.parseColor("#FF0000"));

        if(activated_system){
            systemStatusValue.setText("Activated");
            systemStatusValue.setTextColor(Color.parseColor("#15BD17"));
            systemStatusButton.setText("Deactivate System");
        }else{
            systemStatusValue.setText("Deactivated");
            systemStatusValue.setTextColor(Color.parseColor("#FF0000"));
            systemStatusButton.setText("Activate System");
        }

        if(statusOk){
            alarmStatusValue.setText("OK");
            alarmStatusValue.setTextColor(Color.parseColor("#15BD17"));
            deactivateAlarmButton.setVisibility(View.GONE);
        }else{
            alarmStatusValue.setText("IN ALARM");
            alarmStatusValue.setTextColor(Color.parseColor("#FF0000"));
            deactivateAlarmButton.setVisibility(View.VISIBLE);
        }

        if (kitchenCensorValues.get("Lock Status").equals("Lock Door"))
            doorLockSensorButton.setText(kitchenCensorValues.get("Lock Status"));
        else
            doorLockSensorButton.setText(kitchenCensorValues.get("Lock Status"));

        refreshButton.setOnClickListener(v -> {
            finish();
            startActivity(getIntent());
        });


        Boolean finalActivated_system = activated_system;
        systemStatusButton.setOnClickListener(v -> {
            String requestURL = "";
            if (finalActivated_system){
                requestURL = intent.getStringExtra("Public IP")+"/D";
            }else
                requestURL = intent.getStringExtra("Public IP")+"/A";
            makeRequest(requestURL);

            System.out.println("Made System Request");

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    refreshButton.performClick();
                }
            }, 1500); // 5 seconds
        });

        deactivateAlarmButton.setOnClickListener(v -> {

            String requestURL = intent.getStringExtra("Public IP")+"/H";
            makeRequest(requestURL);

            System.out.println("Made Alarm Deactivate Request");

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    refreshButton.performClick();
                }
            }, 1500); // 1.5 seconds
        });

        doorLockSensorButton.setOnClickListener(v -> {
            String requestURL = "";
            if(doorLockSensorButton.getText().toString() == "LOCK DOOR")
                requestURL = intent.getStringExtra("Public IP")+"/kitchen/lock";
            else
                requestURL = intent.getStringExtra("Public IP")+"/kitchen/unlock";
            makeRequest(requestURL);

            System.out.println("Made Door Lock/Unlock Request");

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    refreshButton.performClick();
                }
            }, 1500); // 1.5 seconds
        });

    }
}
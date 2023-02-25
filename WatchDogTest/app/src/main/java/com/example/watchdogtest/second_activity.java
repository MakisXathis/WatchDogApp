package com.example.watchdogtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

        Boolean deactivated_system = false;
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
                deactivated_system = false;
            } else if (currentHref.toString().contains("/A")) {
                deactivated_system = true;
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

        System.out.println(active_alarm);
        System.out.println(deactivated_system);
        System.out.println(statusOk);

        pressureValue = findViewById(R.id.pressureSensorValue);
        movementValue = findViewById(R.id.movementSensorValue);
        accelerometerValue = findViewById(R.id.accelerometerSensorValue);

        pressureValue.setText(kitchenCensorValues.get("Air Pressure"));
        movementValue.setText(kitchenCensorValues.get("Motion Sensor"));
        accelerometerValue.setText(kitchenCensorValues.get("Door Handle"));
    }
}
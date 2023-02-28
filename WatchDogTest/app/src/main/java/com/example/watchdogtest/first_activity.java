package com.example.watchdogtest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class first_activity extends AppCompatActivity {

    // define the variable
    Button signInButton;
    EditText email;
    EditText password;
    private Executor executor = Executors.newSingleThreadExecutor();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here
                Intent intent = new Intent();
                startActivity(intent);
                finish();
                return true;
            case R.id.exit:
                finishAndRemoveTask();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_activity);


        signInButton = findViewById(R.id.signInButton);
        email = findViewById(R.id.EmailAddress);
        password = findViewById(R.id.password);

        // add the OnClickListener in sender button after clicked this button following Instruction will run
        signInButton.setOnClickListener(v -> {

            if (!email.getText().toString().equals("kostas.kabourdiris@gmail.com")){
                Snackbar mySnackbar = Snackbar.make(v, "Wrong Username or Password. Please try again",Snackbar.LENGTH_LONG);
                mySnackbar.show();
                email.setText("");
                password.setText("");
                return;
            }else if (true) {
                System.out.println("Password: " + password);
                try {
                    if (retrieveHTMLAsString(password.getText().toString()) == "") {
                        Snackbar mySnackbar = Snackbar.make(v, "Could not load data from the Security System. Please try again later.", Snackbar.LENGTH_LONG);
                        mySnackbar.show();
                        return;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            // get the value which input by user in EditText and convert it to string
            String str = password.getText().toString();
            // Create the Intent object of this class Context() to Second_activity class
            Intent intent = new Intent(getApplicationContext(), second_activity.class);
            // now by putExtra method put the value in key, value pair key is
            // message_key by this key we will receive the value, and put the string
            intent.putExtra("Public IP", str);
            // start the Intent
            startActivity(intent);
        });
    }
}

package com.example.lokerku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.renderscript.Sampler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    // Declare Items
    private final Handler handler = new Handler();
    private TextView statusNumber;
    private TextView lockerNumber;
    private TextView currentDate;
    private TextView statusText;
    private Button button;
    private Chronometer stopwatch;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    // Declare SharedPreferences
    private SharedPreferences lastClick;
    private SharedPreferences currentLockerNumber;
    private SharedPreferences.Editor editor;
    private SharedPreferences.Editor editor2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Last Click With SharedPreferences
        lastClick = getSharedPreferences("status", Context.MODE_PRIVATE);
        editor = lastClick.edit();

        // Get Current Locker Number With SharedPreferences
        currentLockerNumber = getSharedPreferences("LockerNum", Context.MODE_PRIVATE);
        editor2 = currentLockerNumber.edit();

        final boolean prevStatus = lastClick.getBoolean("buttonStatus", false);
        final long prevTime = lastClick.getLong("time", 0);

        final String noLoker;

        // FindViewById
        statusNumber = findViewById(R.id.statusNumber);
        lockerNumber = findViewById(R.id.lockerNumber);
        currentDate = findViewById(R.id.CurrentDate);
        statusText = findViewById(R.id.statusText);
        button = findViewById(R.id.button);
        stopwatch = findViewById(R.id.stopwatch);

        // If noLoker Intent Is Not Null, Then Save With SharedPreferences
        if (getIntent().getStringExtra("noLoker") != null) {

            // Get Locker Number With Intent From RequestActivity
            noLoker = getIntent().getStringExtra("noLoker");
            editor2.putString("LockerNumber", noLoker);
            editor2.apply();

            // Set Locker Number Text
            lockerNumber.setText(noLoker);
        }

        // If noLoker Is Null, Get From SharedPreferences
        else {
            noLoker = currentLockerNumber.getString("LockerNumber", "");

            // Set Locker Number Text
            lockerNumber.setText(noLoker);
        }

        System.out.println("noLoker Main: " + noLoker);



        // Set Current Date
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd-MM-YYYY");
        String date = dateFormat.format(currentTime);
        currentDate.setText(date);

        // Set Last Click
        if (prevStatus == true) {
            statusText.setText("CLOSED");
            statusText.setTextColor(Color.parseColor("#FF0303"));
            button.setText("UNLOCK");

            // Keep Updating The Time
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopwatch.setBase(prevTime);
                    stopwatch.start();
                    handler.postDelayed(this, 1000);
                }
            }, 1000);
        }
        else {
            statusText.setText("OPEN");
            statusText.setTextColor(Color.parseColor("#2AFF00"));
            button.setText("LOCK");
        }

        // Set Status Value
        database.child("statusValue").setValue(0);

        // Get data from Firebase and set statusNumber
        database.child("statusValue").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue().toString().equals("1")) {
                    statusNumber.setText("1");
                }
                else if (snapshot.getValue().toString().equals("0")) {
                    statusNumber.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Lock/Unlock Button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Status Changes
                String statusChanges = statusText.getText().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                // Status Changes "OPEN"
                if (statusChanges.equals("OPEN")) {
                    // Alert Dialog
                    builder.setPositiveButton("Lock", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // Set Status to CLOSED
                            statusText.setText("CLOSED");
                            statusText.setTextColor(Color.parseColor("#FFE91E63"));
                            button.setText("UNLOCK");

                            // Set Status In Firebase To 1
                            database.child("Loker").child("loker_" + noLoker).child("status").setValue("1");

                            // Start the stopwatch
                            stopwatch.setBase(SystemClock.elapsedRealtime());
                            stopwatch.start();

                            // Save the status True (CLOSED) & keep the Stopwatch running
                            editor.putBoolean("buttonStatus", true);
                            editor.putLong("time", SystemClock.elapsedRealtime());
                            editor.apply();

                            // Set Status Value on Firebase to 1 (OPEN)
                            database.child("statusValue").setValue(1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MainActivity.this, "Locked", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Failed to Lock", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).setTitle(Html.fromHtml("<font color = '#ffffff'>"+"<b>Apakah anda yakin ingin mengunci locker Anda ?</b>"+"</font>")).
                            setMessage(Html.fromHtml("<font color = '#ffffff'>"+"Pastikan Anda sudah memasukan barang Anda dan Menutup locker dengan benar !!!"+"</font>"));

                    AlertDialog dialog= builder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_button_blue);
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.orangeMyLocker));
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.orangeMyLocker));
                            dialog.setIcon(getResources().getDrawable(R.drawable.logo_mylocker));
                        }
                    });
                    dialog.show();
                }

                // Status Changes "CLOSED"
                else if (statusChanges.equals("CLOSED")){
                    // Alert Dialog
                    builder.setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // Set Status to OPEN
                            statusText.setText("OPEN");
                            statusText.setTextColor(Color.parseColor("#43A047"));
                            button.setText("LOCK");

                            // Set Availability In Firebase to 1
                            database.child("Loker").child("loker_" + noLoker).child("availability").setValue("1");

                            // Set Status In Firebase to 0
                            database.child("Loker").child("loker_" + noLoker).child("status").setValue("0");

                            // Stop the Stopwatch
                            stopwatch.stop();
                            handler.removeCallbacksAndMessages(null);

                            // Save the status to False (OPEN)
                            editor.putBoolean("buttonStatus", false);
                            editor.apply();

                            // Set The Login Status To True And The Request Status To False In Firebase
                            database.child("login").setValue(true);
                            database.child("request").setValue(false);

                            Intent intent = new Intent(MainActivity.this, RequestActivity.class);
                            intent.putExtra("loker", noLoker);
                            startActivity(intent);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).setTitle(Html.fromHtml("<font color = '#ffffff'>"+"<b>Apakah anda yakin ingin membuka locker Anda ?</b>"+"</font>"));


                    AlertDialog dialog= builder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_button_blue);
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.orangeMyLocker));
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.orangeMyLocker));
                            dialog.setIcon(getResources().getDrawable(R.drawable.logo_mylocker));
                        }
                    });
                    dialog.show();
                }
            }
        });
    }
}

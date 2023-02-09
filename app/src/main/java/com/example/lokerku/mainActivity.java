package com.example.lokerku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class mainActivity extends AppCompatActivity {
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
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Last Click With SharedPreferences
        lastClick = getSharedPreferences("status", Context.MODE_PRIVATE);
        editor = lastClick.edit();

        final boolean prevStatus = lastClick.getBoolean("buttonStatus", false);
        final long prevTime = lastClick.getLong("time", 0);

        // FindViewById
        statusNumber = findViewById(R.id.statusNumber);
        lockerNumber = findViewById(R.id.lockerNumber);
        currentDate = findViewById(R.id.CurrentDate);
        statusText = findViewById(R.id.statusText);
        button = findViewById(R.id.button);
        stopwatch = findViewById(R.id.stopwatch);

        // Randomize Locker Number
        Random rand = new Random();
        int randomNum = rand.nextInt((2- 1) + 1) + 1;
        lockerNumber.setText(String.valueOf(randomNum));

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

                if (statusChanges.equals("OPEN")) {

                    // Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity.this);
                    builder.setPositiveButton("Lock", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // Set Status to CLOSED
                            statusText.setText("CLOSED");
                            statusText.setTextColor(Color.parseColor("#FFE91E63"));
                            button.setText("UNLOCK");

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
                                    Toast.makeText(mainActivity.this, "Locked", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(mainActivity.this, "Failed to Lock", Toast.LENGTH_SHORT).show();
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
                else {

                    // Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity.this);
                    builder.setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // Set Status to OPEN
                            statusText.setText("OPEN");
                            statusText.setTextColor(Color.parseColor("#43A047"));
                            button.setText("LOCK");

                            // Stop the Stopwatch
                            stopwatch.stop();
                            handler.removeCallbacksAndMessages(null);

                            // Save the status to False (OPEN)
                            editor.putBoolean("buttonStatus", false);
                            editor.apply();

                            // Set Status Value on Firebase to 0 (CLOSED)
                            database.child("statusValue").setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    navigateUpTo(new Intent(mainActivity.this, RequestActivity.class));
//                                    finish();
//                                    Intent intent = new Intent(mainActivity.this, RequestActivity.class);
//                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(mainActivity.this, "Failed to Unlock", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

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

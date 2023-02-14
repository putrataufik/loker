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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

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
    private SharedPreferences.Editor editor;
    private int [] arrRand = new int [2];

    private String noLoker = "";
    int randomIndex = 0;

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

        Intent intent = getIntent();
        arrRand = intent.getIntArrayExtra("arrRand");

        System.out.println("arrRand: " + Arrays.toString(arrRand));

        UI();

        // Set Current Date
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd-MM-YYYY");
        String date = dateFormat.format(currentTime);
        currentDate.setText(date);

        // Set Current Date And Time To Database
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
        Date currentTimes = Calendar.getInstance(timeZone).getTime();
        SimpleDateFormat dateFormats = new SimpleDateFormat("EEEE, dd-MM-YYYY HH:mm:ss");
        dateFormat.setTimeZone(timeZone);
        String dateForDatabase = dateFormats.format(currentTimes);


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

                // Status Changes "OPEN"
                if (statusChanges.equals("OPEN")) {
                    // Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setPositiveButton("Lock", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // Set Status to CLOSED
                            statusText.setText("CLOSED");
                            statusText.setTextColor(Color.parseColor("#FFE91E63"));
                            database.child("Loker").child("loker_"+(randomIndex+1)).child("status").setValue(1);
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
                else {
                    // Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setPositiveButton("Unlock", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // Set Status to OPEN
                            statusText.setText("OPEN");
                            statusText.setTextColor(Color.parseColor("#43A047"));
                            database.child("Loker").child("loker_"+(randomIndex+1)).child("status").setValue(0);

                            button.setText("LOCK");

                            // Stop the Stopwatch
                            stopwatch.stop();
                            handler.removeCallbacksAndMessages(null);

                            // Save the status to False (OPEN)
                            editor.putBoolean("buttonStatus", false);
                            editor.apply();

                            // Set The Login Status To True And The Request Status To False In Firebase
                            database.child("login").setValue(true);
                            database.child("request").setValue(false);

                            // Set Status Value on Firebase to 0 (CLOSED)
                            database.child("statusValue").setValue(0).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.child("Loker").child("loker_"+(randomIndex+1)).child("availability").setValue(0);

                                    database.child("History").push().setValue(new ModelHistory("-NN7dCDgMf1NWOMuM3I_",dateForDatabase, (randomIndex+1)));

                                    navigateUpTo(new Intent(getBaseContext(), RequestActivity.class));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Failed to Unlock", Toast.LENGTH_SHORT).show();
                                }
                            });
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

    private void UI(){
        database.child("Loker").child("loker_1").child("availability").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int availability_1 = Integer.valueOf(snapshot.getValue().toString());
                arrRand [0] = availability_1;
                lockerNumber.setText(noLoker);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.child("Loker").child("loker_2").child("availability").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int availability_2 = Integer.valueOf(snapshot.getValue().toString());
                arrRand [1] = availability_2;
                lockerNumber.setText(noLoker);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(arrRand[0] == 0 || arrRand[1] == 0) {
            Random rand = new Random();
            randomIndex = rand.nextInt(2);

            while (arrRand[randomIndex] == 1) {
                randomIndex = rand.nextInt(2);
            }

            database.child("no_loker").setValue(randomIndex + 1);

            if (randomIndex+1 == 1) {
                noLoker = "1";

                database.child("Loker").child("loker_1").child("availability").setValue(1);
                database.child("no_loker").setValue(1);

                System.out.println("Locker yang dipilih: 1");
            }

            else if (randomIndex+1 == 2) {
                noLoker = "2";

                database.child("Loker").child("loker_2").child("availability").setValue(1);
                database.child("no_loker").setValue(2);

                System.out.println("Locker yang dipilih: 2");
            }
        }

        else if(arrRand[0] == 0 || arrRand[1] == 1) {
            if (randomIndex+1 == 1) {
                noLoker = "1";

                database.child("Loker").child("loker_1").child("availability").setValue(1);
                database.child("no_loker").setValue(1);

                System.out.println("Locker yang dipilih: 1");
            }
        }

        else if(arrRand[0] == 1 || arrRand[1] == 0){
            if (randomIndex+1 == 2){
                noLoker = "2";

                database.child("Loker").child("loker_2").child("availability").setValue(1);
                database.child("no_loker").setValue(2);

                System.out.println("Locker yang dipilih: 2");
            }
        }
    }
}

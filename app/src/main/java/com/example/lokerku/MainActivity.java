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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    // Declare Widgets
    private final Handler handler = new Handler();
    private TextView lockerNumber;
    private TextView currentDate;
    private TextView statusText;
    private Button button;
    private Chronometer stopwatch;

    // Declare DatabaseReference Firebase
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    // Declare SharedPreferences
    private SharedPreferences lastClick;
    private SharedPreferences.Editor editor;

    // Declare Primitive
    private int [] arrRand = new int [2];
    private String noLoker = "";
    private int randomIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Last Click With SharedPreferences
        lastClick = getSharedPreferences("status", Context.MODE_PRIVATE);
        editor = lastClick.edit();

        // Get The Previous Status And Stopwatch With SharedPreferences
        final boolean prevStatus = lastClick.getBoolean("buttonStatus", false);
        final long prevTime = lastClick.getLong("time", 0);

        // FindViewById
        lockerNumber = findViewById(R.id.lockerNumber);
        currentDate = findViewById(R.id.CurrentDate);
        statusText = findViewById(R.id.statusText);
        button = findViewById(R.id.button);
        stopwatch = findViewById(R.id.stopwatch);

        // Get The Locker Availability In An Array With Singleton
        if (UserDataSingleton.getInstance().getArrRand() != null) {
            arrRand = UserDataSingleton.getInstance().getArrRand();
        }

        // Method To Random The Locker Number And Set The Locker Number Text
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
        dateFormats.setTimeZone(timeZone);
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
                    // Set The Stopwatch To The Previous Time
                    stopwatch.setBase(prevTime);
                    stopwatch.start();
                    handler.postDelayed(this, 1000);
                }
            }, 1000);
        }
        else {
            // Set The Status Text To "OPEN" And Set The Button Text To "LOCK"
            statusText.setText("OPEN");
            statusText.setTextColor(Color.parseColor("#2AFF00"));
            button.setText("LOCK");
        }

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
                            button.setText("UNLOCK");

                            // Start the stopwatch
                            stopwatch.setBase(SystemClock.elapsedRealtime());
                            stopwatch.start();

                            // Save the status True (CLOSED) & keep the Stopwatch running
                            editor.putBoolean("buttonStatus", true);
                            editor.putLong("time", SystemClock.elapsedRealtime());
                            editor.apply();

                            // Set Status In Firebase To 1 (Closed)
                            database.child("Loker").child("loker_"+(randomIndex+1)).child("status").setValue(1);

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
                            button.setText("LOCK");

                            // Stop the Stopwatch
                            stopwatch.stop();
                            handler.removeCallbacksAndMessages(null);

                            // Save the status to False (OPEN)
                            editor.putBoolean("buttonStatus", false);
                            editor.apply();

                            // Get Singleton
                            if (UserDataSingleton.getInstance().getUsername() != null) {
                                String username = UserDataSingleton.getInstance().getUsername();
                                editor.putString("username", username);
                                editor.apply();

                                // Set The Login Status In Firebase To True
                                database.child("user_data").child(username).child("login").setValue(true);

                                // Set The Request Status In Firebase To False
                                database.child("user_data").child(username).child("request").setValue(false);

                                // Set The Locker Availability In Firebase To 0 (Open)
                                database.child("Loker").child("loker_"+(randomIndex+1)).child("availability").setValue(0);

                                // Set The Status In Firebase To 0 (Open)
                                database.child("Loker").child("loker_"+(randomIndex+1)).child("status").setValue(0);

                                // Back To Request Page
                                Intent intent = new Intent(MainActivity.this, RequestActivity.class);
                                startActivity(intent);
                            }
                            else {
                                String username = lastClick.getString("username", "");

                                // Set The Login Status In Firebase To True
                                database.child("user_data").child(username).child("login").setValue(true);

                                // Set The Request Status In Firebase To False
                                database.child("user_data").child(username).child("request").setValue(false);

                                // Set The Locker Availability In Firebase To 0 (Open)
                                database.child("Loker").child("loker_"+(randomIndex+1)).child("availability").setValue(0);

                                // Set The Status In Firebase To 0 (Open)
                                database.child("Loker").child("loker_"+(randomIndex+1)).child("status").setValue(0);

                                // Back To Request Page
                                Intent intent = new Intent(MainActivity.this, RequestActivity.class);
                                startActivity(intent);
                            }


                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).setTitle(Html.fromHtml("<font color = '#ffffff'>"+"<b>Apakah anda yakin ingin membuka locker Anda ?</b>"+"</font>"));

                    // Show The Alert Dialog
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

    // Method To Random The Locker Number And Set The Locker Number Text
    private void UI(){
        database.child("Loker").child("loker_1").child("availability").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int availability_1 = Integer.valueOf(snapshot.getValue().toString());
                arrRand [0] = availability_1;
                lockerNumber.setText(noLoker);

                if (UserDataSingleton.getInstance().getUsername() != null) {
                    String username = UserDataSingleton.getInstance().getUsername();

                    database.child("user_data").child(username).child("request").setValue(true);
                }
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
                database.child("Loker").child("loker_1").child("status").setValue(0);

                database.child("no_loker").setValue(1);

                System.out.println("Locker yang dipilih: 1");
            }

            else if (randomIndex+1 == 2) {
                noLoker = "2";

                database.child("Loker").child("loker_2").child("availability").setValue(1);
                database.child("Loker").child("loker_2").child("status").setValue(0);

                database.child("no_loker").setValue(2);

                System.out.println("Locker yang dipilih: 2");
            }
        }

        else if(arrRand[0] == 0 || arrRand[1] == 1) {
            if (randomIndex+1 == 1) {
                noLoker = "1";

                database.child("Loker").child("loker_1").child("availability").setValue(1);
                database.child("Loker").child("loker_1").child("status").setValue(0);

                database.child("no_loker").setValue(1);

                System.out.println("Locker yang dipilih: 1");
            }
        }

        else if(arrRand[0] == 1 || arrRand[1] == 0){
            if (randomIndex+1 == 2){
                noLoker = "2";

                database.child("Loker").child("loker_2").child("availability").setValue(1);
                database.child("Loker").child("loker_2").child("status").setValue(0);

                database.child("no_loker").setValue(2);

                System.out.println("Locker yang dipilih: 2");
            }
        }
    }
}

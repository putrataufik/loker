package com.example.lokerku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class RequestActivity extends AppCompatActivity {

    // Declare SharedPreferences
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    // Declare Array For Locker Availability
    private int[] arrRand = new int [2];

    // Declare Final String
    private String name;
    private String username;

    // Declare DatabaseReference Firebase
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Define SharedPreferences
        preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        editor = preferences.edit();


        if (UserDataSingleton.getInstance().getUsername() == null) {
            username = preferences.getString("username", "p");

            // Get request Boolean from Firebase
            database.child("user_data").child(username).child("request").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    String request = task.getResult().getValue().toString();

                    if (request.equalsIgnoreCase("true")) {
                        startActivity(new Intent(RequestActivity.this, MainActivity.class));
                    }
                }
            });
        }
//        else {
//            username = UserDataSingleton.getInstance().getUsername();
//
//            // Get request Boolean from Firebase
//            database.child("user_data").child(username).child("request").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DataSnapshot> task) {
//                    String request = task.getResult().getValue().toString();
//
//                    if (request.equalsIgnoreCase("true")) {
//                        startActivity(new Intent(RequestActivity.this, MainActivity.class));
//                    }
//                }
//            });
//        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);



        // Declare Widgets
        TextView userName = findViewById(R.id.userName);
        Button requestButton = findViewById(R.id.requestButton);

        // Get user_name From Singleton Or SharedPreferences
        if (UserDataSingleton.getInstance().getName() != null) {
            name = UserDataSingleton.getInstance().getName();
        }
        else {
            name = preferences.getString("name","");
        }

        // Save name to SharedPreferences
        editor.putString("name", name);
        editor.apply();

        // Set user_name
        userName.setText("Hi, " + name);

        // Request Button
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
                builder.setPositiveButton("Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (UserDataSingleton.getInstance().getUsername() != null) {
                            username = UserDataSingleton.getInstance().getUsername();

                            // Save The Previous Username With SharedPreferences
                            editor.putString("username", username);
                            editor.apply();
                        }
                        else {
                            username = preferences.getString("username", "");
                        }

                        // Get The loker_1 Availability From Firebase
                        database.child("Loker").child("loker_1").child("availability").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                String ava = task.getResult().getValue().toString();
                                arrRand[0] = Integer.parseInt(ava);
                            }
                        });

                        // Get The loker_2 Availability From Firebase
                        database.child("Loker").child("loker_2").child("availability").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                String ava = task.getResult().getValue().toString();
                                arrRand[1] = Integer.parseInt(ava);

                                System.out.println("arrRand: " + Arrays.toString(arrRand));

                                if (arrRand [0] == 1 && arrRand[1] == 1) {
                                    builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).setNegativeButton("", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).setTitle(Html.fromHtml("<font color = '#ffffff'>" + "<b>Locker Availability</b>" + "</font>")).
                                            setMessage(Html.fromHtml("<font color = '#ffffff'>" + "Sorry, No Locker Available Right Now!" + "</font>"));

                                    // Show The Alert Dialog
                                    AlertDialog dialog = builder.create();
                                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialogInterface) {
                                            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_button_blue);
                                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.orangeMyLocker));
                                            dialog.setIcon(getResources().getDrawable(R.drawable.logo_mylocker));
                                        }
                                    });
                                    dialog.show();
                                }
                                else {
                                    // Set arrRand For Locker Availability With Singleton
                                    UserDataSingleton.getInstance().setArrRand(arrRand);

                                    // Set The Request Status In Firebase To True
                                    database.child("user_data").child(username).child("request").setValue(true);

                                    Intent intent = new Intent(RequestActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setTitle(Html.fromHtml("<font color = '#ffffff'>" + "<b>KONFIRMASI</b>" + "</font>")).
                setMessage(Html.fromHtml("<font color = '#ffffff'>" + "Apakah Anda Yakin Ingin Request?" + "</font>"));

                // Show The Alert Dialog
                AlertDialog dialog = builder.create();
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
        });
    }
}
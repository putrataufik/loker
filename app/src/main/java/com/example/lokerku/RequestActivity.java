package com.example.lokerku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RequestActivity extends AppCompatActivity {


    private SharedPreferences preferences;
   private SharedPreferences.Editor editor;

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        editor = preferences.edit();

        final boolean isRequested = preferences.getBoolean("request",false);

        boolean request = UserDataSingleton.getInstance().isRequested();

        if (UserDataSingleton.getInstance().isRequested()) {
            editor.putBoolean("request", request);
            editor.apply();
        }



//        if(isRequested){
//            Intent intent = new Intent(RequestActivity.this, MainActivity.class);
//            startActivity(intent);
//        }

        database.child("request").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String request = snapshot.getValue().toString();

                if (request.equals("true")){
                    Intent intent = new Intent(RequestActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        editor = preferences.edit();

        final String name;

        // Declare
        TextView userName = findViewById(R.id.userName);
        Button requestButton = findViewById(R.id.requestButton);

        // Get Intent Extra
        if (UserDataSingleton.getInstance().getName() != null){
        name = UserDataSingleton.getInstance().getName();
        }
        else {
            name = preferences.getString("name","");
        }

        editor.putString("name",name);
        editor.apply();

        // Set User Name
        userName.setText("Hi, " + name);


        // Get data from Firebase and set statusNumber
        database.child("User_Data");

        // Request Button
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
                builder.setPositiveButton("Request", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                editor.putBoolean("request",true);
                                editor.apply();

                                database.child("request").setValue(true);
                                database.child("login").setValue(false);

                                Intent intent = new Intent(RequestActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setTitle(Html.fromHtml("<font color = '#ffffff'>" + "<b>KONFIRMASI</b>" + "</font>")).
                        setMessage(Html.fromHtml("<font color = '#ffffff'>" + "Apakah Anda Yakin Ingin Request?" + "</font>"));


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
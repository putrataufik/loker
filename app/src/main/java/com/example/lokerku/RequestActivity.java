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

        // Get request Boolean from Firebase
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

        // Define SharedPreferences
        preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        editor = preferences.edit();

        // Declare user_name
        final String name;

        // Declare
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
                        // // Set The Request Status In Firebase To True
                        database.child("request").setValue(true);

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
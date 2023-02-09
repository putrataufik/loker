package com.example.lokerku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.DialogInterface;
import android.content.Intent;
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

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Declare
        TextView userName = findViewById(R.id.userName);
        Button requestButton = findViewById(R.id.requestButton);

        // Get Intent Extra
        String name = getIntent().getStringExtra("name");

        // Set User Name
        userName.setText("Hi, " + name);


        // Get data from Firebase and set statusNumber
//        database.child("User_Data").


        // Request Button
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
                builder.setPositiveButton("Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(RequestActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setTitle(Html.fromHtml("<font color = '#ffffff'>"+"<b>Confirmation</b>"+"</font>")).
                        setMessage(Html.fromHtml("<font color = '#ffffff'>"+"Do You Want to Request a Locker?"+"</font>"));


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
        });


    }
}
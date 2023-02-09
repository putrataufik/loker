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
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Declare
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        // Login Button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get Data From Firebase
                database.child("user_data").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // Get email & password text
                        String getEmail = email.getText().toString();
                        String getPassword = password.getText().toString();

                        try {
                            // Hash the Password
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            byte[] hash = digest.digest(getPassword.getBytes("UTF-8"));
                            StringBuilder hashedPassword = new StringBuilder();

                            for (byte b : hash) {
                                String hex = Integer.toHexString(0xff & b);
                                if (hex.length() == 1)
                                    hashedPassword.append('0');
                                hashedPassword.append(hex);
                            }

                            // Get Data from Firebase
                            for (DataSnapshot item : snapshot.getChildren()) {
                                ModelRegister modelRegister = item.getValue(ModelRegister.class);

                                if (getEmail.isEmpty()) {
                                    email.setError("Masukkan Email!");
                                }
                                else if (getPassword.isEmpty()) {
                                    password.setError("Masukkan Password");
                                }

                                else if (getEmail.equals(modelRegister.getEmail()) && hashedPassword.toString().equals(modelRegister.getPassword())) {
                                    String name = modelRegister.getName();

                                    Intent intent = new Intent(LoginActivity.this, RequestActivity.class);

                                    intent.putExtra("name", name);
                                    startActivity(intent);
                                }
                            }
                        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                            System.err.println("Error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        // Register Button for Switch to Register Page
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


    }
    //alert dialog from leaving application using back navigator
    @Override
    public void onBackPressed(){
        // Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setPositiveButton("Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setTitle(Html.fromHtml("<font color = '#ffffff'>"+"<b>Exit App</b>"+"</font>")).
                setMessage(Html.fromHtml("<font color = '#ffffff'>"+"Do You Want To Exit The Application?"+"</font>"));


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
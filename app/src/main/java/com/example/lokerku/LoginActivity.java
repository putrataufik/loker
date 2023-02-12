package com.example.lokerku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        // Get login Boolean From Firebase
        database.child("login").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String login = snapshot.getValue().toString();

                if (login.equals("true")) {
                    Intent intent = new Intent(LoginActivity.this, RequestActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Declare
        EditText email = findViewById(R.id.userNameLogin);
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
                                    UserDataSingleton.getInstance().setName(name);

                                    // Set The Login Status In Firebase To True
                                    database.child("login").setValue(true);

                                    Intent intent = new Intent(LoginActivity.this, RequestActivity.class);
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
}

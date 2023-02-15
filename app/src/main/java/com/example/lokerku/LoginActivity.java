package com.example.lokerku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    // Declare DatabaseReference Firebase
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    // Declare SharedPreferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Get Last Click With SharedPreferences
        sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Declare Final String
        final String username;

        if (UserDataSingleton.getInstance().getUsername() == null) {
            // Get The Previous Username With SharedPreferences
            username = sharedPreferences.getString("username", "p");

            // Get login Boolean From Firebase
            database.child("user_data").child(username).child("login").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    String login = task.getResult().getValue().toString();

                    if (login.equals("true")) {
                        Intent intent = new Intent(LoginActivity.this, RequestActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Declare Widgets
        EditText usernameLogin = findViewById(R.id.userNameLogin);
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
                        String getUsername = usernameLogin.getText().toString();
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

                                if (getUsername.isEmpty()) {
                                    usernameLogin.setError("Masukkan Username!");
                                }
                                else if (getPassword.isEmpty()) {
                                    password.setError("Masukkan Password");
                                }
                                else if (getUsername.equals(modelRegister.getUsername()) && hashedPassword.toString().equals(modelRegister.getPassword())) {
                                    String name = modelRegister.getName();
                                    String username = modelRegister.getUsername();

                                    // Set Name And Username To Singleton
                                    UserDataSingleton.getInstance().setName(name);
                                    UserDataSingleton.getInstance().setUsername(username);

                                    // Save The Username With SharedPreferences
                                    editor.putString("username", username);
                                    editor.putBoolean("login", true);
                                    editor.apply();

                                    // Set The Login Status In Firebase To True
                                    database.child("user_data").child(username).child("login").setValue(true);

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



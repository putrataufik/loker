package com.example.lokerku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Declare
        EditText name = findViewById(R.id.nameRegister);
        EditText email = findViewById(R.id.emailRegister);
        EditText password = findViewById(R.id.passwordRegister);
        EditText passwordConfirm = findViewById(R.id.passwordConfirmationRegister);
        Button registerAccButton = findViewById(R.id.registerAccButton);

        registerAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password.getText().toString().equals(passwordConfirm.getText().toString())) {

                    // Get Name, Email and Password Text
                    String getName = name.getText().toString();
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

                        // Send data to Firebase
                        database.child("user_data").push().setValue(new ModelRegister(getName, getEmail, hashedPassword.toString()));

                    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                        System.err.println("Error: " + e.getMessage());
                    }

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Password Wrong", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
package com.example.lokerku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity {

    // Declare DatabaseReference Firebase
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Theme to Light Mode
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Declare Widgets
        EditText name = findViewById(R.id.nameRegister);
        EditText username = findViewById(R.id.usernameRegister);
        EditText password = findViewById(R.id.passwordRegister);
        EditText passwordConfirm = findViewById(R.id.passwordConfirmationRegister);
        Button registerAccButton = findViewById(R.id.registerAccButton);

        // RegisterAccButton
        registerAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Name, Email, Password and Password Confirm Text
                String getName = name.getText().toString();
                String getUsername = username.getText().toString();
                String getPassword = password.getText().toString();
                String getPasswordConfirm = passwordConfirm.getText().toString();

                if (getName.isEmpty()) {
                    name.setError("Name Is Empty");
                }
                else if (getUsername.isEmpty()) {
                    username.setError("Username Is Empty");
                }
                else if (getPassword.isEmpty()) {
                    password.setError("Password Is Empty");
                }
                else if (getPasswordConfirm.isEmpty()) {
                    passwordConfirm.setError("Password Confirm Is Empty");
                }

                else if (password.getText().toString().equals(passwordConfirm.getText().toString())) {
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
                        database.child("user_data").child(getUsername).setValue(new ModelRegister(getName, getUsername, hashedPassword.toString()));

                    } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                        System.err.println("Error: " + e.getMessage());
                    }

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            }
        });
    }
}
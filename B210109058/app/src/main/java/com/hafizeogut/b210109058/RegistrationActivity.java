package com.hafizeogut.b210109058;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    EditText registerName, registerSurname, registerPhone, registerEmail, registerPassword;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registerName = findViewById(R.id.name);
        registerSurname = findViewById(R.id.surname);
        registerPhone = findViewById(R.id.phone_number);
        registerEmail = findViewById(R.id.email);
        registerPassword = findViewById(R.id.password);

        registerPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
    }

    public void register(View view) {
        String userName = registerName.getText().toString();
        String userSurname = registerSurname.getText().toString();
        String userPhone = registerPhone.getText().toString();
        String userEmail = registerEmail.getText().toString();
        String userPassword = registerPassword.getText().toString();


        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "Enter your first name!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userSurname)) {
            Toast.makeText(this, "Enter your last name!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userPhone)) {
            Toast.makeText(this, "Enter your phone number!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Enter your email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userPassword)) {
            Toast.makeText(this, "Enter your password!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userPassword.length() < 6) {
            Toast.makeText(this, "Password is too short, enter at least 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String newId = null;
                        if (task.getResult() != null && task.getResult().getUser() != null) {
                            newId = task.getResult().getUser().getUid(); // getUid()
                        }
                        String defaultRole = "user";

                        if (newId != null) {
                            userClass userClas = new userClass(newId, userName, userSurname, userPhone, userEmail, userPassword, defaultRole);


                            reference.child(newId).setValue(userClas)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegistrationActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RegistrationActivity.this, "Save Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Error: Unable to retrieve user ID", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        String errorMessage = "Registration failed.";
                        if (task.getException() != null) {
                            errorMessage = "Registration failed: " + task.getException().getMessage();
                        }
                        Toast.makeText(RegistrationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void login(View view) {
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void home(View view) {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
